package client.room;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import data.ChatMsg;
import data.GameMap;
import data.GameRoom;

public class GameRoomView extends JFrame{
	
	// 이하 userStatus 처리를 위한 변수
	private static final String ONLINE = "ONLINE";
	private static final String SLEEP = "SLEEP";
	private static final String READYON = "READYON";
	private static final String READYOFF = "READYOFF";
	private static final String GAME = "GAME";
	
	private static final String C_UPDROOM = "302";
	private static final String C_STRGAME = "304";
	
	private WaitingView parent;
	private GameRoom room;

	private Container contentPane;
	private JLabel roomTitle;
	
	private JTextField txtInput;
	private JTextArea textArea;
	private JButton btnSend;
	private JButton startBtn;
	
	private String myName;
	
	private Vector<String> userList = new Vector<String>();
	private Vector<String> btnStatus = new Vector<String>();
	private Vector<JButton> readyBtn = new Vector<JButton>();
	
	private boolean isPressed = false;
	private boolean isStarted = false;
	
	private Color btnEnable = new Color(180, 210, 255);
	private Color btnDisable = new Color(200, 200, 200);
	
	// 게임 시작 이후 map 표시
	private MapPanel mapPanel;

	public GameRoomView(WaitingView parent, GameRoom room) {
		this.parent = parent;
		this.room = room;
		myName = parent.getMyName();
		
		// 기본 설정
		setTitle("Network MAZE Game - " + "[#" + room.getKey() + "] "+room.getName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = getContentPane();
		contentPane.setLayout(null);
		
		roomTitle = new JLabel(" [#" + room.getKey() + "] "+room.getName());
		roomTitle.setOpaque(true);
		roomTitle.setBounds(10, 10, 200, 25);
		roomTitle.setBackground(btnDisable);
		roomTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		contentPane.add(roomTitle);
		
		startBtn = new JButton("START");
		startBtn.setOpaque(true);
		startBtn.setBackground(btnDisable);
		startBtn.setFont(new Font("맑은 고딕", Font.BOLD + Font.ITALIC, 12));
		startBtn.setBounds(10, 45, 200, 30);
		startBtn.setEnabled(false);
		contentPane.add(startBtn);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 75, 200, 345);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림체", Font.PLAIN, 12));
		scrollPane.setViewportView(textArea);

		txtInput = new JTextField();
		txtInput.setBounds(10, 420, 130, 30);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		btnSend = new JButton("SEND");
		btnSend.setOpaque(true);
		btnSend.setBackground(btnEnable);
		btnSend.setFont(new Font("맑은 고딕", Font.BOLD + Font.ITALIC, 12));
		btnSend.setBounds(140, 420, 70, 30);
		contentPane.add(btnSend);
		
		// ReadyBtn 설정
		userList = room.getUserList();
		for (int i = 0; i < userList.size(); i++) {
			String userName = userList.get(i).toString();
			JButton ready = new JButton(userName);
			ready.setOpaque(true);
			ready.setBackground(btnDisable);
			ready.setFont(new Font("맑은 고딕", Font.BOLD + Font.ITALIC, 20));
			ready.setBounds(220 + 165*i, 50, 160, 400);
			if(!userName.equals(myName))
				ready.setEnabled(false);
			ready.addActionListener(new ReadyActionListener(room.getKey()));
			contentPane.add(ready);
			readyBtn.add(ready);
		}
		
		// Event Listener 설정
		TextSendAction action = new TextSendAction();
		btnSend.addActionListener(action);
		txtInput.addActionListener(action);
		startBtn.addActionListener(new StartActionListener(room.getKey()));

		// Frame 크기 설정
		setSize(900, 500);
		setResizable(false);
		setVisible(true);
				
		// focus 지정 - Mouse Listener를 받을 수 있게 함
		contentPane.setFocusable(true);
		contentPane.requestFocus();
	}
	
	// Server로부터 Room의 변경사항을 받은 경우, revalidate(), repaint()로 업데이트
	public void clear() {
		for (int i = 0; i < readyBtn.size(); i++) {
			contentPane.remove(readyBtn.get(i));
		}
		readyBtn.clear();
		userList.clear();
		btnStatus.clear();
		contentPane.revalidate();
		contentPane.repaint();
	}
	
	// 새로운 User 추가
	public void addUser(String name, String status) {
		JButton ready = new JButton(name);
		ready.setOpaque(true);
		if(status.equals(READYON))
			ready.setBackground(btnEnable);
		else
			ready.setBackground(btnDisable);
		ready.setFont(new Font("맑은 고딕", Font.BOLD + Font.ITALIC, 20));
		ready.setBounds(220 + 165*readyBtn.size(), 50, 160, 400);
		
		if(!name.equals(myName))
			ready.setEnabled(false);
		else
			ready.addActionListener(new ReadyActionListener(room.getKey()));

		// startBtn 활성화 체크
		int i = 0;
		for (i = 0; i < btnStatus.size(); i++) {
			if (!btnStatus.get(i).equals(READYON))
				break;
		}
		if (i==btnStatus.size() && status.equals(READYON)) {
			startBtn.setEnabled(true);
			startBtn.setBackground(new Color(240, 200, 200));
		}
		else {
			startBtn.setEnabled(false);
			startBtn.setBackground(btnDisable);
		}
			
		contentPane.add(ready);
		userList.add(name);
		readyBtn.add(ready);
		btnStatus.add(status);
		
		contentPane.repaint();
		contentPane.revalidate();
	}
	
	// 화면에 출력 - Chatting
	public void AppendText(String msg) {
		textArea.append(msg + "\n");
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		//textArea.setCaretPosition(len);
		//textArea.replaceSelection(msg + "\n");
	}
	
	// 게임 시작
	// GameView를 새롭게 그리고 update요청
	public void startGame(GameMap gameMap) {
		isStarted = true;
		for (int i = 0; i < readyBtn.size(); i++) {
			contentPane.remove(readyBtn.get(i));
		}
		startBtn.setEnabled(false);
		
		// Map 그리기
		mapPanel = new MapPanel(gameMap, myName);
		mapPanel.setBounds(220, 00, 460, 460);
		contentPane.add(mapPanel);
		System.out.println("CLIENT "+myName+" GAME STARTED");

		contentPane.revalidate();
		contentPane.repaint(); 
		
		mapPanel.requestFocus();
	}
	
	// keyboard enter key 치면 서버로 전송
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				msg = txtInput.getText();
				parent.SendMessage(room.getKey(), msg);
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				if (mapPanel != null)
					mapPanel.requestFocus();
				else
					txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
			}
		}
	}
	
	// ReadyBtn을 위한 EventListener - 버튼을 누르면 ready 상태를 바꾸고 server에 update 요청
	class ReadyActionListener implements ActionListener{
		private int key;
		public ReadyActionListener(int key) {
			this.key = key;
		}
		public void actionPerformed(ActionEvent e) {
			isPressed = !isPressed;
			parent.SendObject(new ChatMsg(myName, C_UPDROOM, key+" "+isPressed));
		}
	} // End of class ReadyActionListener
	
	// StartBtn을 위한 EventListener - 버튼을 누르면 ready 상태를 체크하고 Game start
	class StartActionListener implements ActionListener{
		private int key;
		public StartActionListener(int key) {
			this.key = key;
		}
		public void actionPerformed(ActionEvent e) {
			parent.SendObject(new ChatMsg(myName, C_STRGAME, key+""));
		}
	} // End of class ReadyActionListener
}
