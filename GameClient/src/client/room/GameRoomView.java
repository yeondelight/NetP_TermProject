package client.room;

import java.awt.Container;

import javax.swing.JFrame;

import data.GameRoom;

public class GameRoomView extends JFrame{
	
	private WaitingView parent;
	private GameRoom room;
	
	private Container contentPane;
	
	public GameRoomView(WaitingView parent, GameRoom room) {
		this.parent = parent;
		this.room = room;
		
		// 기본 설정
		setTitle("Network MAZE Game - " + "[#" + room.getKey() + "] "+room.getName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = getContentPane();
		contentPane.setLayout(null);

		// Frame 크기 설정
		setSize(900, 500);
		setResizable(false);
		setVisible(true);
				
		// focus 지정 - Mouse Listener를 받을 수 있게 함
		contentPane.setFocusable(true);
		contentPane.requestFocus();
	}
}
