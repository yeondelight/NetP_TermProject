package client.wait;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RoomListPanel extends JPanel{

	private HashMap<Integer, String> roomInfo;		// 방의 ID, 이름을 저장한 HashMap
	private HashMap<Integer, RoomView> roomViews;	// 해당 방에 해당하는 RoomView를 저장한 HashMap
	
	public RoomListPanel(HashMap<Integer, String> roomInfo) {
		this.roomInfo = roomInfo;
		
		// room에 따라 JLabel, JButton 생성
		roomViews = new HashMap<Integer, RoomView>();
		Set<Integer> roomInfoKeys = roomInfo.keySet();
		Iterator<Integer> roomInfoIt = roomInfoKeys.iterator();
		while(roomInfoIt.hasNext()) {
			int key = roomInfoIt.next();
			String name = roomInfo.get(key);
			RoomView roomView = new RoomView(key, name);
			roomViews.put(key, roomView);
			
			add(roomView.name);
			add(roomView.enter);
		}
		
		setPreferredSize(new Dimension(680, roomInfo.size() * 60));
	}
	
	public boolean addRoom(int key, String name) {
		try {
			// Room 객체 생성한 뒤 hashMap (roomInfo, rooms)에 넣는다.
			Room room = new Room(key, name);
			RoomView roomView = new RoomView(key, name) ;
			roomInfo.put(key, name);
			roomViews.put(key, roomView);
			this.add(roomView.name);
			this.add(roomView.enter);
			
			setPreferredSize(new Dimension(680, roomInfo.size() * 60));
			revalidate();
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean delRoom(Room room) {
		try {
			// Room 객체와 hashmap의 정보들을 제거한다.
			int key = room.getKey();
			roomInfo.remove(key);
			
			RoomView roomView = roomViews.get(key);
			this.remove(roomView.name);
			this.remove(roomView.enter);
			
			setPreferredSize(new Dimension(680, roomInfo.size() * 60));
			revalidate();
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public class RoomView{
		
		private int key;
		private JLabel name = new JLabel();
		private JButton enter = new JButton(" ENTER ");
		
		// 누를 수 있는 버튼과 그렇지 않은 버튼 구분
		private Color btnEnable = new Color(180, 210, 255);
		private Color btnDisable = new Color(200, 200, 200);
		
		public RoomView(int key, String roomName) {
			this.key = key;
			name.setText(" [" + key + "] " + roomName);
			name.setOpaque(true);
			name.setBackground(Color.WHITE);
			name.setPreferredSize(new Dimension(560, 50));
			name.setFont(new Font("맑은 고딕", Font.BOLD, 20));
			
			// 추후 인원수도 추가할 예정
			
			enter.setOpaque(true);
			enter.setBackground(btnEnable);
			enter.setPreferredSize(new Dimension(100, 50));
			enter.setFont(new Font("Arial", Font.BOLD + Font.ITALIC, 15));
			enter.addMouseListener(new EnterMouseListener(key));
			
			// 방의 상태를 파악해 Enable 상태를 변경하는 코드
			
		} // End of RoomView()
		
		public class EnterMouseListener extends MouseAdapter{
			private int key;
			public EnterMouseListener(int key) {
				this.key = key;
			}
			public void onMouseClicked(MouseEvent e) {
				// key에 해당하는 room을 찾고, 해당 room이 존재하며 AVAIL 상태면 입장한다.
			}
		} // End of class MouseListener
	} // End of class RoomView
} // End of class RoomListPanel
