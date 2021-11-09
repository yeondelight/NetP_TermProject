package client.room;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import data.ChatMsg;
import data.GameMap;
import data.GameMsg;
import data.GameRoom;
import data.RoomMsg;

public class WaitingView extends JFrame{
	
	// 이하 Protocol msg
	private static final String C_LOGIN = "100";		// 새로운 client 접속
	private static final String C_ACKLIST = "101";		// C->S 101을 정상적으로 수신
	private static final String C_MAKEROOM = "200";		// 새로운 방 생성
	private static final String C_ENTROOM = "201";		// 해당 방에 입장 
	private static final String C_CHATMSG = "301";		// C->S GameRoom 내 일반 채팅 메세지
	private static final String C_UPDROOM = "302";		// C->S GameRoom 업데이트좀
	private static final String C_ACKROOM = "303";		// C->S 320 ACK
	private static final String C_STRGAME = "304";		// C->S 게임 시작할래
	private static final String C_UPDGAME = "305";		// Client -> Server 움직임 알림
	private static final String C_UPDSCORE = "307";		// Client -> Server 점수 변경 알림
	
	private static final String S_REQLIST = "110";		// S->C 생성되어 있는 room 개수 전송
	private static final String S_SENLIST = "120";		// S->C 각 room의 key, name 전송
	private static final String S_UPDLIST = "210";		// room 목록 update
	private static final String S_ENTROOM = "220";		// S->C room 입장 허가
	private static final String S_CHATMSG = "310";		// S->C GameRoom 내 방송
	private static final String S_UPDROOM = "320";		// GameRoom 정보 update : user 수 반환
	private static final String S_USRLIST = "330";		// userList update
	private static final String S_STRGAME = "340";		// S->C 그래 시작해
	private static final String S_UPDGAME = "350";		// 각 게임방의 Client로 게임 정보 변경 일괄 전송 (Event)
	private static final String S_UPDTIME = "360";		// 각 게임방의 Client로 게임 정보 변경 일괄 전송 (Timer)
	private static final String S_UPDSCORE = "370";		// 각 게임방의 Client로 게임 정보 변경 일괄 전송 (Score)
	
	private static final int BUF_LEN = 128; //  Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	private String userName;
	
	private Container contentPane;
	private JScrollPane scrollPane;
	private RoomListPanel roomListPanel;
	private MakeRoomDialog makeDialog;

	private GameRoom gameRoom;
	private GameRoomView gameRoomView;		// 추후 User가 입장할 GameRoom
	private HashMap<Integer, String> rooms;
	private int roomNum = 0;
	
	private JButton makeRoom;
	private String btnText = "<HTML><body><center>MAKE<br>NEW ROOM</center></body></HTML>";
	
	private ImageIcon img = new ImageIcon("res/howToPlay.jpg");
	private JLabel imgLabel = new JLabel(img);
	
	// JFrame 생성 : Swing Frame
	public WaitingView(String username, String ip_addr, String port_no) {
		this.userName = username;
		
		// client codes
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			SendObject(new ChatMsg(userName, C_LOGIN, ""));		// 로그인 정보를 서버에게 전송
			ListenNetwork net = new ListenNetwork();
			net.start();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("connect error");
		}

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
				if (name == null) {
					return;
				}
				else{
					SendObject(new ChatMsg(userName, C_MAKEROOM, key+" "+name));
				}
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
	
	// Room에서 Server로의 전송을 위해 WaitingView를 넘겨줘야 한다.
	public WaitingView getWaitingView() {
		return this;
	}
	
	public String getMyName() {
		return userName;
	}

	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					RoomMsg rm;
					GameMsg gm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s] %s", cm.getId(), cm.getData());
						
						String ccode = cm.getCode();
						String cdata = cm.getData();
						System.out.println("CLIENT GET DATA : "+ccode+" "+cdata);
						
						// 이하 Protocol 처리 - ChatMsg 수신

						// S_REQLIST(110)
						// Server -> Client 메세지의 정상 전송을 확인하고 현재 Room의 개수를 보낸다.
						if(ccode.equals(S_REQLIST)) {
							userName = cm.getId();			//중복 닉네임인 경우 다시 받아야 하므로
							roomNum = Integer.parseInt(cm.getData());
							SendObject(new ChatMsg(userName, C_ACKLIST, ""));
						}
							
						// S_SENLIST(120)
						// Server -> Client Room에 대한 정보 (Key, Name)를 받는다.
						else if(ccode.equals(S_SENLIST)) {
							// 정보를 받아 client의 HashMap room에 저장한다.
							String val[] = cm.getData().split(" ");
							int key = Integer.parseInt(val[0]);
							String name = val[1];
							String status = val[2];
							int pNum = Integer.parseInt(val[3]);
							rooms.put(key, name);
							roomListPanel.addRoom(key, name, status, pNum);	// 화면에 보이도록 처리
						}
						
						// S_UPDLIST(210)
						// Server -> Client Room 목록의 변경 감지하고 update 요청	
						// Client는 현재 roomView를 reset하고 다시 받아온다.
						else if(ccode.equals(S_UPDLIST)) {
							roomListPanel.clear();
							rooms.clear();
							roomNum = Integer.parseInt(cm.getData());
							SendObject(new ChatMsg(userName, C_ACKLIST, ""));
						}
						
						// S_CHATMSG(310)
						// Server -> Client GameRoom 내의 User가 보낸 메세지 수신
						else if(ccode.equals(S_CHATMSG)) {
							gameRoomView.AppendText("[" + cm.getId() + "] " + cm.getData());
						}
						
						// S_UPDROOM(320)
						// Server -> Client update 해줄게
						// userList의 size를 받아온다.
						else if (ccode.equals(S_UPDROOM)) {
							int userNum = Integer.parseInt(cm.getData());
							int key = gameRoom.getKey();
							gameRoomView.clear();
							SendObject(new ChatMsg(userName, C_ACKROOM, key+""));
						}
						
						// S_USRLIST(330)
						// Server -> Client userList update
						else if (ccode.equals(S_USRLIST)) {
							String val[] = cm.getData().split(" ");
							String name = val[0];
							String status = val[1];
							gameRoomView.addUser(name, status);
						}
						
						// S_UPDGAME(350)
						// Server -> Client 게임 업데이트
						else if (ccode.equals(S_UPDGAME)) {
							Integer keyCode = cm.getNumCode();
							gameRoomView.doKeyEvent(cm.getId(), keyCode);	// MapPanel로 넘겨주기
						}
						
						// S_UPDTIME(360)
						// Server -> Client Timer 갱신
						else if (ccode.equals(S_UPDTIME)) {
							int timeout = Integer.parseInt(cm.getData());
							gameRoomView.updateTimer(timeout);
						}
						
						// S_UPDSCORE(370)
						// Server -> Client Score 갱신
						// new ChatMsg(USERNAME, 307, roomKey +-/*, score) 형태
						// 같은 게임방 내 User들에게 전송
						else if (cm.getCode().matches(S_UPDSCORE)) {
							String val[] = cm.getData().split(" ");
							int key = Integer.parseInt(val[0]);
							String cal = val[1];
							Integer score = cm.getNumCode();
							gameRoomView.updateScore(cm.getId(), cal, score);
						}
					}
					else if (obcm instanceof RoomMsg) {
						rm = (RoomMsg) obcm;
						
						String rcode = rm.getCode();
						gameRoom = rm.getRoom();
						
						System.out.println("CLIENT CODE : "+rcode);
						
						// 이하 Protocol 처리 - RoomMsg 수신
						
						// S_ENTROOM(220)
						// Server -> Client 해당 client를 room에 입장하도록 허가.
						// GameView를 만들고 Room의 정보를 받아 모두 적는다.	
						if(rcode.equals(S_ENTROOM)) {
							// GameRoomView 입장
							gameRoomView = new GameRoomView(getWaitingView(), gameRoom);
							gameRoomView.setVisible(true);
							SendObject(new ChatMsg(userName, C_UPDROOM, gameRoom.getKey()+""));		// 업데이트좀
							getWaitingView().setVisible(false);			// 현재 WaitingView 퇴장 : 다른 방에 입장하지 못하도록 막음
						}
					}
					else if (obcm instanceof GameMsg) {
						gm = (GameMsg) obcm;
						
						String gcode = gm.getCode();
						GameMap gameMap = gm.getMap();
						
						System.out.println("CLIENT CODE : "+gcode);
						
						// 이하 Protocol 처리 - RoomMsg 수신

						// S_STRGAME(340)
						// Server -> Client 게임 시작 허가
						if (gcode.equals(S_STRGAME)) {
							gameRoomView.startGame(gameMap);
						}
					}
					else
						continue;

					
				} catch (IOException e) {
					System.out.println("ois.readObject() error");
					try {
						ois.close();
						oos.close();
						socket.close();
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
			}
		}
	} // End of ListenNetwork
	
	// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
	public byte[] MakePacket(String msg) {
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try {
			bb = msg.getBytes("euc-kr");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	} // End of MakePacket(msg)

	// Server에게 network으로 전송
	public void SendMessage(int key, String msg) {
		try {
			oos.writeObject(new ChatMsg(userName, C_CHATMSG, key + " " + msg));
		} catch (IOException e) {
			System.out.println("oos.writeObject() error");
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			ChatMsg cm = (ChatMsg) ob;
			oos.writeObject(ob);
		} catch (IOException e) {
			System.out.println("메세지 송신 에러!!\n");
		}
	}

	// RoomList를 표시하는 Panel
	public class RoomListPanel extends JPanel{

		private HashMap<Integer, String> roomInfo;		// 방의 ID, 이름을 저장한 HashMap
		private HashMap<Integer, RoomView> roomViews;	// 해당 방에 해당하는 RoomView를 저장한 HashMap
		
		// GameRoom에서의 STATUS 표시
		private final static String AVAIL = "AVAIL";
		private final static String FULL = "FULL";
		private final static String STARTED = "STARTED";
		
		// 누를 수 있는 버튼과 그렇지 않은 버튼 구분
		private Color btnEnable = new Color(180, 210, 255);
		private Color btnDisable = new Color(200, 200, 200);
		
		// 인원수를 표시하는 View는 Image로 처리
		private ImageIcon pEnable0 = new ImageIcon("res/player/pEnable0.jpg");
		private ImageIcon pEnable1 = new ImageIcon("res/player/pEnable1.jpg");
		private ImageIcon pEnable2 = new ImageIcon("res/player/pEnable2.jpg");
		private ImageIcon pEnable3 = new ImageIcon("res/player/pEnable3.jpg");
		private ImageIcon pDisable2 = new ImageIcon("res/player/pDisable2.jpg");
		private ImageIcon pDisable3 = new ImageIcon("res/player/pDisable3.jpg");
		private ImageIcon pDisable4 = new ImageIcon("res/player/pDisable4.jpg");
			
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
				add(roomView.pNum);
				add(roomView.enter);
			}
			
			setPreferredSize(new Dimension(680, roomInfo.size() * 60));
		}
		
		// RoomView를 다시 그리기 위해 모든 RoomView를 삭제한다.
		public void clear() {
			Set<Integer> roomInfoKeys = roomInfo.keySet();
			Iterator<Integer> roomInfoIt = roomInfoKeys.iterator();
			while(roomInfoIt.hasNext()) {
				int key = roomInfoIt.next();
				System.out.println("CLIENT REMOVE : "+key);
				RoomView roomView = roomViews.get(key);
				this.remove(roomView.name);
				this.remove(roomView.pNum);
				this.remove(roomView.enter);
			}
			
			roomInfo.clear();
			roomViews.clear();
			setPreferredSize(new Dimension(680, roomInfo.size() * 60));
			revalidate();
		}
		
		public boolean addRoom(int key, String name, String status, int pNum) {
			try {
				// Room의 정보를 hashMap (roomInfo, rooms)에 넣는다.
				RoomView roomView = new RoomView(key, name);
				roomInfo.put(key, name);
				roomViews.put(key, roomView);
				this.add(roomView.name);
				this.add(roomView.pNum);
				this.add(roomView.enter);
				
				switch(pNum) {
				case 0:
					roomView.pNum.setIcon(pEnable0);
					break;
				case 1:
					roomView.pNum.setIcon(pEnable1);
					break;
				case 2:
					roomView.pNum.setIcon(pEnable2);
					break;
				case 3:
					roomView.pNum.setIcon(pEnable3);
					break;
				}
				
				if(!status.equals(AVAIL)) {
					roomView.name.setForeground(btnDisable);
					roomView.enter.setBackground(btnDisable);
					roomView.enter.setEnabled(false);
					
					switch(pNum) {
					case 2:
						roomView.pNum.setIcon(pDisable2);
						break;
					case 3:
						roomView.pNum.setIcon(pDisable3);
						break;
					case 4:
						roomView.pNum.setIcon(pDisable4);
						break;
					}
				}
				
				setPreferredSize(new Dimension(680, roomInfo.size() * 60));
				revalidate();
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		public boolean delRoom(GameRoom gameRoom) {
			try {
				// Room 객체와 hashmap의 정보들을 제거한다.
				int key = gameRoom.getKey();
				roomInfo.remove(key);
				
				RoomView roomView = roomViews.get(key);
				this.remove(roomView.name);
				this.remove(roomView.pNum);
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
			private JLabel pNum = new JLabel();
			private JButton enter = new JButton(" ENTER ");

			public RoomView(int key, String roomName) {
				this.key = key;
				name.setText(" [" + key + "] " + roomName);
				name.setOpaque(true);
				name.setBackground(Color.WHITE);
				name.setPreferredSize(new Dimension(480, 50));
				name.setFont(new Font("맑은 고딕", Font.BOLD, 20));

				pNum.setPreferredSize(new Dimension(80, 50));
				
				enter.setOpaque(true);
				enter.setBackground(btnEnable);
				enter.setPreferredSize(new Dimension(100, 50));
				enter.setFont(new Font("Arial", Font.BOLD + Font.ITALIC, 15));
				enter.addActionListener(new EnterActionListener(key));
			} // End of RoomView()
		} // End of class RoomView
		
		// RoomListPanel을 위한 btnListener - 통신을 위해 WaitingView에 추가
		public class EnterActionListener implements ActionListener{
			private int key;
			public EnterActionListener(int key) {
				this.key = key;
			}
			public void actionPerformed(ActionEvent e) {
				SendObject(new ChatMsg(userName, C_ENTROOM, key+""));
			}
		} // End of class MouseListener
	} // End of class RoomListPanel
}