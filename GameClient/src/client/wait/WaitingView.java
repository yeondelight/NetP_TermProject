package client.wait;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class WaitingView extends JFrame{
	
	private Container contentPane;
	private JScrollPane scrollPane;
	private RoomListPanel roomListPanel;
	private MakeRoomDialog makeDialog;

	private HashMap<Integer, String> rooms;
	
	private JButton makeRoom;
	private String btnText = "<HTML><body><center>MAKE<br>NEW ROOM</center></body></HTML>";
	
	private ImageIcon img = new ImageIcon("res/howToPlay.jpg");
	private JLabel imgLabel = new JLabel(img);
	
	// JFrame 생성 : Swing Frame
	public WaitingView() {
		
		// 기본 설정
		setTitle("Network MAZE Game - Waiting Room");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = getContentPane();
		contentPane.setLayout(null);

		// 방 리스트를 표시하는 RoomListPanel 붙이기
		rooms = new HashMap<Integer, String>();
		roomListPanel = new RoomListPanel(rooms);
		scrollPane
			= new JScrollPane(roomListPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setLocation(0, 3);
		scrollPane.setSize(new Dimension(700, 500));
		contentPane.add(scrollPane);
		
		// Dialog 만들기
		makeDialog = new MakeRoomDialog(this, "Make New Room");
				
		// makeRoombtn 붙이기
		makeRoom = new JButton(btnText);
		makeRoom.setOpaque(true);
		makeRoom.setBackground(new Color(220, 250, 200));
		makeRoom.setFont(new Font("Arial", Font.BOLD + Font.ITALIC, 15));
		makeRoom.setLocation(705, 8);
		makeRoom.setSize(new Dimension(170, 70));
		makeRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int key = makeDialog.getKey();
				makeDialog.setVisible(true);

				// make btn을 눌러 return 한 경우
				String name = makeDialog.getInput();
				if (name == null)	return;
				roomListPanel.addRoom(key, name);
			}
		});
		contentPane.add(makeRoom);
		
		// 사용 설명서 붙이기
		imgLabel.setLocation(705, 85);
		imgLabel.setSize(new Dimension(170, 360));
		contentPane.add(imgLabel, BorderLayout.EAST);

		// Frame 크기 설정
		setSize(900, 500);
		setResizable(false);
		setVisible(true);
		
		// focus 지정 - Mouse Listener를 받을 수 있게 함
		contentPane.setFocusable(true);
		contentPane.requestFocus();
	}
}
