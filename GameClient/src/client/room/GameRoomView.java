package client.room;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
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
	private static final String C_EXITROOM = "308";
	
	private WaitingView parent;
	private GameRoomView gameRoomView;
	private GameRoom room;

	private Container contentPane;
	private JLabel roomTitle;
	
	private JTextField txtInput;
	private JTextArea textArea;
	private JButton btnSend;
	private JButton startBtn;
	private JButton exitBtn;
	
	private String myName;
	
	private Vector<String> userList = new Vector<String>();
	private Vector<String> btnStatus = new Vector<String>();
	private Vector<JButton> readyBtn = new Vector<JButton>();
	
	private boolean isPressed = false;
	private boolean isStarted = false;
	
	private Color btnEnable = new Color(180, 210, 255);
	private Color btnDisable = new Color(200, 200, 200);
	private Color defaultColor = new Color(238, 238, 238);
	
	// 게임 시작 이후
	private MapPanel mapPanel;
	private JLabel timerLabel;
	private Vector<JLabel> scoreInfo = new Vector<JLabel>();		// 1st, 2nd ...
	private Vector<UserScore> scores = new Vector<UserScore>();		// score 정보 및 JLabel 관리
	
	private ImageIcon rank1 = new ImageIcon("res/ranks/1.png");
	private ImageIcon rank2 = new ImageIcon("res/ranks/2.png");
	private ImageIcon rank3 = new ImageIcon("res/ranks/3.png");
	private ImageIcon rank4 = new ImageIcon("res/ranks/4.png");
	private ImageIcon[] ranks = {rank1, rank2, rank3, rank4};
	
	// 게임 종료 후
	private Vector<JLabel> rankUsers;
	private Vector<JLabel> rankScore;
	private JLabel rankBoard;
	private JLabel gameOver;
	private JButton replay;
	private JButton goHome;

	public GameRoomView(WaitingView parent, GameRoom room) {
		this.parent = parent;
		this.room = room;
		gameRoomView = this;
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
		
		exitBtn = new JButton(new ImageIcon("res/exit.png"));
		exitBtn.setFocusPainted(false);
		exitBtn.setBorderPainted(false);
		exitBtn.setContentAreaFilled(false);
		exitBtn.setBounds(760, 10, 115, 30);
		contentPane.add(exitBtn);
		
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
		exitBtn.addActionListener(new ExitActionListener(room.getKey()));

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
		startBtn.setBackground(btnDisable);
		
		contentPane.remove(exitBtn);
		
		System.out.println("CLIENT "+myName+" GAME STARTED");
		
		// Map 그리기
		mapPanel = new MapPanel(parent, gameMap, room.getKey(), myName);
		mapPanel.setBounds(220, 00, 460, 460);
		contentPane.add(mapPanel);
		
		// TimerLabel 그리기
		timerLabel = new JLabel(" 남은 시간 : "+99+" sec");		// 그냥 임의로 넣어둔 99. 이게 나타나면 오류라는 뜻.
		timerLabel.setOpaque(true);
		timerLabel.setBackground(new Color(240, 200, 200));
		timerLabel.setBounds(690, 10, 185, 40);
		timerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		contentPane.add(timerLabel);
		
		// ScoreLabel 그리기
		for(int i = 0; i < userList.size(); i++) {
			String userName = userList.get(i);
			UserScore userScore = new UserScore(userName);
			
			// Rank 붙이기
			JLabel rank = new JLabel(ranks[i]);
			rank.setOpaque(true);
			rank.setBounds(690, 60 + 40*i, 70, 40);
			if(userName.equals(myName))	rank.setBackground(Color.WHITE);
			else						rank.setBackground(defaultColor);
			contentPane.add(rank);
			
			// userScore 붙이기
			JLabel info = new JLabel(" " + userName);
			info.setOpaque(true);
			info.setBounds(760, 60 + 40*i, 60, 40);
			info.setFont(new Font("맑은 고딕", Font.BOLD, 15));
			if(userName.equals(myName))	info.setBackground(Color.WHITE);
			else						info.setBackground(defaultColor);
			userScore.setNameLabel(info);
			contentPane.add(info);
			
			userScore.setScore(0);
			JLabel score = new JLabel(" "+userScore.getScore());
			score.setOpaque(true);
			score.setBounds(820, 60 + 40*i, 55, 40);
			score.setFont(new Font("맑은 고딕", Font.BOLD, 15));
			if(userName.equals(myName))	score.setBackground(Color.WHITE);
			else						score.setBackground(defaultColor);
			userScore.setScoreLabel(score);
			contentPane.add(score);
			
			scoreInfo.add(rank);
			scores.add(userScore);
		}

		contentPane.revalidate();
		contentPane.repaint(); 
		
		mapPanel.requestFocus();
	}
	
	// Server로부터 남은 시간을 받아 update
	public void updateTimer(int timeout) {
		timerLabel.setText(" 남은 시간 : " + timeout + " 초 ");
		// 남은 시간이 촉박할수록 배경색이 true RED에 가까워진다.
		if(timeout<=10)
			timerLabel.setBackground(new Color(240, 200-(10-timeout)*15, 200-(10-timeout)*15));
		
		if(timeout==0) {
			// 모든 keyListener 해제
			isStarted = false;
			mapPanel.deleteKeyListener();
			
			// 3초 대기 후에 결과화면 호출
			new Thread() {
				public void run() {
					try {
						Thread.sleep(3000);
						endGame();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		
		contentPane.revalidate();
		contentPane.repaint(); 
		
		mapPanel.requestFocus();
	}
	
	// Server로부터 점수를 받아 update
	public void updateScore(String userName, String cal, int score) {
		
		// 해당 user의 점수 update
		UserScore userScore = null;
		for (int i = 0; i < scores.size(); i++) {
			userScore = scores.get(i);
			if(userScore.getUserName().equals(userName))
				break;
		}
		
		int tempScore = userScore.getScore();
		
		switch(cal) {
		case "+":
			tempScore += score;
			break;
		case "-":
			tempScore -= score;
			break;
		}
		
		userScore.setScore(tempScore);
		
		// Label update	with sorting
		String tempName;
		JLabel tempNameLabel;
		JLabel tempScoreLabel;
		JLabel scoreInfoLabel;
		Collections.sort(scores, new UserScoreComparator());
		for (int i = 0; i < scores.size(); i++) {
			scoreInfoLabel = scoreInfo.get(i);
			userScore = scores.get(i);
			tempName = userScore.getUserName();
			
			tempNameLabel = userScore.getNameLabel();
			tempNameLabel.setBounds(760, 60 + 40*i, 60, 40);
			if(tempName.equals(myName)) {
				tempNameLabel.setBackground(Color.WHITE);
				scoreInfoLabel.setBackground(Color.WHITE);
			}
			else {
				tempNameLabel.setBackground(defaultColor);
				scoreInfoLabel.setBackground(defaultColor);
			}

			tempScoreLabel = userScore.getScoreLabel();
			tempScoreLabel.setBounds(820, 60 + 40*i, 55, 40);
			if (tempName.equals(userName))	tempScoreLabel.setText(" "+tempScore);	// 점수 변경 처리
			if(tempName.equals(myName))	tempScoreLabel.setBackground(Color.WHITE);
			else						tempScoreLabel.setBackground(defaultColor);
		}
		
		contentPane.revalidate();
		contentPane.repaint(); 
				
		mapPanel.requestFocus();
	}
	
	// 게임 종료
	public void endGame() {
		// 다 지우고
		contentPane.remove(mapPanel);
		contentPane.remove(timerLabel);

		for (int i = 0; i < scores.size(); i++) {
			UserScore userScore = scores.get(i);
			JLabel scoreInfoLabel = scoreInfo.get(i);
			JLabel tempNameLabel = userScore.getNameLabel();
			JLabel tempScoreLabel = userScore.getScoreLabel();
			
			contentPane.remove(scoreInfoLabel);
			contentPane.remove(tempNameLabel);
			contentPane.remove(tempScoreLabel);
		}
		
		gameOver = new JLabel(new ImageIcon("res/gameResult/gameover.png"));
		gameOver.setSize(255, 70);
		gameOver.setLocation(415, 30);
		gameOver.setHorizontalAlignment(JLabel.CENTER);
		contentPane.add(gameOver);
		
		// 1~3위 계산
		rankUsers = new Vector<JLabel>(3);
		rankScore = new Vector<JLabel>(3);
		Collections.sort(scores, new UserScoreComparator());
		int max = (scores.size() > 3) ? 3 : scores.size();
		for (int i = 0; i < max; i++) {
			UserScore userScore = scores.get(i);
			JLabel tempRankName = new JLabel(userScore.getUserName());
			JLabel tempRankScore = new JLabel(userScore.getScore()+" pts");
			rankUsers.add(tempRankName);
			rankScore.add(tempRankScore);
		}
		
		// GAMEOVER 그리기
		
		
		// 1위 위치 맞추기
		rankUsers.get(0).setSize(210, 30);
		rankUsers.get(0).setLocation(440, 125);
		rankUsers.get(0).setHorizontalAlignment(JLabel.CENTER);
		rankUsers.get(0).setFont(new Font("맑은 고딕", Font.BOLD, 25));
		contentPane.add(rankUsers.get(0));
		
		rankScore.get(0).setSize(210, 45);
		rankScore.get(0).setLocation(440, 160);
		rankScore.get(0).setHorizontalAlignment(JLabel.CENTER);
		rankScore.get(0).setFont(new Font("맑은 고딕", Font.BOLD + Font.ITALIC, 40));
		contentPane.add(rankScore.get(0));
		
		switch(max) {
		case 3:		// 3위 위치 맞추기
			rankUsers.get(2).setSize(210, 30);
			rankUsers.get(2).setLocation(645, 220);
			rankUsers.get(2).setHorizontalAlignment(JLabel.CENTER);
			rankUsers.get(2).setFont(new Font("맑은 고딕", Font.BOLD, 25));
			contentPane.add(rankUsers.get(2));
			
			rankScore.get(2).setSize(210, 45);
			rankScore.get(2).setLocation(645, 255);
			rankScore.get(2).setHorizontalAlignment(JLabel.CENTER);
			rankScore.get(2).setFont(new Font("맑은 고딕", Font.BOLD + Font.ITALIC, 40));
			contentPane.add(rankScore.get(2));
		case 2:		// 2위 위치 맞추기
			rankUsers.get(1).setSize(210, 30);
			rankUsers.get(1).setLocation(230, 200);
			rankUsers.get(1).setHorizontalAlignment(JLabel.CENTER);
			rankUsers.get(1).setFont(new Font("맑은 고딕", Font.BOLD, 25));
			contentPane.add(rankUsers.get(1));
			
			rankScore.get(1).setSize(210, 45);
			rankScore.get(1).setLocation(230, 235);
			rankScore.get(1).setHorizontalAlignment(JLabel.CENTER);
			rankScore.get(1).setFont(new Font("맑은 고딕", Font.BOLD + Font.ITALIC, 40));
			contentPane.add(rankScore.get(1));
			break;
		}

		// 시상식 화면
		rankBoard = new JLabel(new ImageIcon("res/gameResult/rankboard.png"));
		rankBoard.setOpaque(true);
		rankBoard.setSize(620, 200);
		rankBoard.setLocation(235, 190);
		contentPane.add(rankBoard);
		
		// 이 방에 남을건지 버튼
		replay = new JButton(new ImageIcon("res/gameResult/replay.png"));
		replay.setFocusPainted(false);
		replay.setBorderPainted(false);
		replay.setContentAreaFilled(false);
		replay.setBounds(395, 405, 110, 60);
		//replay.addActionListener();
		contentPane.add(replay);
		
		// 아님 홈으로 가던가
		goHome = new JButton(new ImageIcon("res/gameResult/home.png"));
		goHome.setFocusPainted(false);
		goHome.setBorderPainted(false);
		goHome.setContentAreaFilled(false);
		goHome.setBounds(605, 393, 110, 60);
		goHome.addActionListener(new ExitActionListener(room.getKey()));
		contentPane.add(goHome);
		
		contentPane.revalidate();
		contentPane.repaint(); 
	}
	
	// Server로부터 받은 이벤트 전달하기
	public void doKeyEvent(String userName, int keyCode) {
		mapPanel.doKeyEvent(userName, keyCode);
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
	
	// StartBtn을 위한 EventListener - 버튼을 누르면 ready 상태를 체크하고 Game start
	class ExitActionListener implements ActionListener{
		private int key;
		public ExitActionListener(int key) {
			this.key = key;
		}
		public void actionPerformed(ActionEvent e) {
			gameRoomView.setVisible(false);
			parent.getWaitingView().setVisible(true);	
			parent.SendObject(new ChatMsg(myName, C_EXITROOM, key+""));
		}
	} // End of class ReadyActionListener
}