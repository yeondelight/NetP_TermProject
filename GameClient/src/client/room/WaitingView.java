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
	
	// ���� Protocol msg
	private static final String C_LOGIN = "100";		// ���ο� client ����
	private static final String C_ACKLIST = "101";		// C->S 101�� ���������� ����
	private static final String C_MAKEROOM = "200";		// ���ο� �� ����
	private static final String C_ENTROOM = "201";		// �ش� �濡 ���� 
	private static final String C_CHATMSG = "301";		// C->S GameRoom �� �Ϲ� ä�� �޼���
	private static final String C_UPDROOM = "302";		// C->S GameRoom ������Ʈ��
	private static final String C_ACKROOM = "303";		// C->S 320 ACK
	private static final String C_STRGAME = "304";		// C->S ���� �����ҷ�
	private static final String C_UPDGAME = "305";		// Client -> Server ������ �˸�
	private static final String C_UPDSCORE = "307";		// Client -> Server ���� ���� �˸�
	private static final String C_EXITROOM = "308";
	
	private static final String S_REQLIST = "110";		// S->C �����Ǿ� �ִ� room ���� ����
	private static final String S_SENLIST = "120";		// S->C �� room�� key, name ����
	private static final String S_UPDLIST = "210";		// room ��� update
	private static final String S_ENTROOM = "220";		// S->C room ���� �㰡
	private static final String S_CHATMSG = "310";		// S->C GameRoom �� ���
	private static final String S_UPDROOM = "320";		// GameRoom ���� update : user �� ��ȯ
	private static final String S_USRLIST = "330";		// userList update
	private static final String S_STRGAME = "340";		// S->C �׷� ������
	private static final String S_UPDGAME = "350";		// �� ���ӹ��� Client�� ���� ���� ���� �ϰ� ���� (Event)
	private static final String S_UPDTIME = "360";		// �� ���ӹ��� Client�� ���� ���� ���� �ϰ� ���� (Timer)
	private static final String S_UPDSCORE = "370";		// �� ���ӹ��� Client�� ���� ���� ���� �ϰ� ���� (Score)
	
	private static final int BUF_LEN = 128; //  Windows ó�� BUF_LEN �� ����
	private Socket socket; // �������
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
	private GameRoomView gameRoomView;		// ���� User�� ������ GameRoom
	private HashMap<Integer, String> rooms;
	private int roomNum = 0;
	
	private JButton makeRoom;
	private String btnText = "<HTML><body><center>MAKE<br>NEW ROOM</center></body></HTML>";
	
	private ImageIcon img = new ImageIcon("res/howToPlay.jpg");
	private JLabel imgLabel = new JLabel(img);
	
	// JFrame ���� : Swing Frame
	public WaitingView(String username, String ip_addr, String port_no) {
		this.userName = username;
		
		// client codes
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			SendObject(new ChatMsg(userName, C_LOGIN, ""));		// �α��� ������ �������� ����
			ListenNetwork net = new ListenNetwork();
			net.start();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("connect error");
		}

		// �⺻ ����
		setTitle("Network MAZE Game - Waiting Room");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = getContentPane();
		contentPane.setLayout(null);

		// �� ����Ʈ�� ǥ���ϴ� RoomListPanel ���̱�
		rooms = new HashMap<Integer, String>();
		roomListPanel = new RoomListPanel(rooms);
		scrollPane
			= new JScrollPane(roomListPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setLocation(0, 3);
		scrollPane.setSize(new Dimension(700, 500));
		contentPane.add(scrollPane);
		
		// Dialog �����
		makeDialog = new MakeRoomDialog(this, "Make New Room");
				
		// makeRoombtn ���̱�
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

				// make btn�� ���� return �� ���
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
		
		// ��� ���� ���̱�
		imgLabel.setLocation(705, 85);
		imgLabel.setSize(new Dimension(170, 360));
		contentPane.add(imgLabel, BorderLayout.EAST);

		// Frame ũ�� ����
		setSize(900, 500);
		setResizable(false);
		setVisible(true);
		
		// focus ���� - Mouse Listener�� ���� �� �ְ� ��
		contentPane.setFocusable(true);
		contentPane.requestFocus();
	}
	
	// Room���� Server���� ������ ���� WaitingView�� �Ѱ���� �Ѵ�.
	public WaitingView getWaitingView() {
		return this;
	}
	
	public String getMyName() {
		return userName;
	}

	// Server Message�� �����ؼ� ȭ�鿡 ǥ��
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
						
						// ���� Protocol ó�� - ChatMsg ����

						// S_REQLIST(110)
						// Server -> Client �޼����� ���� ������ Ȯ���ϰ� ���� Room�� ������ ������.
						if(ccode.equals(S_REQLIST)) {
							userName = cm.getId();			//�ߺ� �г����� ��� �ٽ� �޾ƾ� �ϹǷ�
							roomNum = Integer.parseInt(cm.getData());
							SendObject(new ChatMsg(userName, C_ACKLIST, ""));
						}
							
						// S_SENLIST(120)
						// Server -> Client Room�� ���� ���� (Key, Name)�� �޴´�.
						else if(ccode.equals(S_SENLIST)) {
							// ������ �޾� client�� HashMap room�� �����Ѵ�.
							String val[] = cm.getData().split(" ");
							int key = Integer.parseInt(val[0]);
							String name = val[1];
							String status = val[2];
							int pNum = Integer.parseInt(val[3]);
							rooms.put(key, name);
							roomListPanel.addRoom(key, name, status, pNum);	// ȭ�鿡 ���̵��� ó��
						}
						
						// S_UPDLIST(210)
						// Server -> Client Room ����� ���� �����ϰ� update ��û	
						// Client�� ���� roomView�� reset�ϰ� �ٽ� �޾ƿ´�.
						else if(ccode.equals(S_UPDLIST)) {
							roomListPanel.clear();
							rooms.clear();
							roomNum = Integer.parseInt(cm.getData());
							SendObject(new ChatMsg(userName, C_ACKLIST, ""));
						}
						
						// S_CHATMSG(310)
						// Server -> Client GameRoom ���� User�� ���� �޼��� ����
						else if(ccode.equals(S_CHATMSG)) {
							// Server�� ���� �޼����� ���
							if(cm.getId().equals("SERVER"))
								gameRoomView.AppendImage(cm.getImg());
							else
								gameRoomView.AppendText("[" + cm.getId() + "] " + cm.getData());
						}
						
						// S_UPDROOM(320)
						// Server -> Client update ���ٰ�
						// userList�� size�� �޾ƿ´�.
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
						// Server -> Client ���� ������Ʈ
						else if (ccode.equals(S_UPDGAME)) {
							Integer keyCode = cm.getNumCode();
							gameRoomView.doKeyEvent(cm.getId(), keyCode);	// MapPanel�� �Ѱ��ֱ�
						}
						
						// S_UPDTIME(360)
						// Server -> Client Timer ����
						else if (ccode.equals(S_UPDTIME)) {
							int timeout = Integer.parseInt(cm.getData());
							gameRoomView.updateTimer(timeout);
						}
						
						// S_UPDSCORE(370)
						// Server -> Client Score ����
						// new ChatMsg(USERNAME, 307, roomKey +-/*, score) ����
						// ���� ���ӹ� �� User�鿡�� ����
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
						
						// ���� Protocol ó�� - RoomMsg ����
						
						// S_ENTROOM(220)
						// Server -> Client �ش� client�� room�� �����ϵ��� �㰡.
						// GameView�� ����� Room�� ������ �޾� ��� ���´�.	
						if(rcode.equals(S_ENTROOM)) {
							// GameRoomView ����
							gameRoomView = new GameRoomView(getWaitingView(), gameRoom);
							gameRoomView.setVisible(true);
							SendObject(new ChatMsg(userName, C_UPDROOM, gameRoom.getKey()+""));		// ������Ʈ��
							getWaitingView().setVisible(false);			// ���� WaitingView ���� : �ٸ� �濡 �������� ���ϵ��� ����
						}
					}
					else if (obcm instanceof GameMsg) {
						gm = (GameMsg) obcm;
						
						String gcode = gm.getCode();
						GameMap gameMap = gm.getMap();
						
						System.out.println("CLIENT CODE : "+gcode);
						
						// ���� Protocol ó�� - RoomMsg ����

						// S_STRGAME(340)
						// Server -> Client ���� ���� �㰡
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
					} // catch�� ��
				} // �ٱ� catch����
			}
		}
	} // End of ListenNetwork
	
	// Windows ó�� message ������ ������ �κ��� NULL �� ����� ���� �Լ�
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

	// Server���� network���� ����
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
	
	public void SendObject(Object ob) { // ������ �޼����� ������ �޼ҵ�
		try {
			ChatMsg cm = (ChatMsg) ob;
			oos.writeObject(ob);
		} catch (IOException e) {
			System.out.println("�޼��� �۽� ����!!\n");
		}
	}

	// RoomList�� ǥ���ϴ� Panel
	public class RoomListPanel extends JPanel{

		private HashMap<Integer, String> roomInfo;		// ���� ID, �̸��� ������ HashMap
		private HashMap<Integer, RoomView> roomViews;	// �ش� �濡 �ش��ϴ� RoomView�� ������ HashMap
		
		// GameRoom������ STATUS ǥ��
		private final static String AVAIL = "AVAIL";
		private final static String FULL = "FULL";
		private final static String STARTED = "STARTED";
		
		// ���� �� �ִ� ��ư�� �׷��� ���� ��ư ����
		private ImageIcon btnEnable = new ImageIcon("res/buttons/enterBtn.jpg");
		private ImageIcon btnHover = new ImageIcon("res/buttons/enterBtn_hover.jpg");
		
		private Color disable = new Color(200, 200, 200);
		
		// �ο����� ǥ���ϴ� View�� Image�� ó��
		private ImageIcon pEnable0 = new ImageIcon("res/player/pEnable0.jpg");
		private ImageIcon pEnable1 = new ImageIcon("res/player/pEnable1.jpg");
		private ImageIcon pEnable2 = new ImageIcon("res/player/pEnable2.jpg");
		private ImageIcon pEnable3 = new ImageIcon("res/player/pEnable3.jpg");
		private ImageIcon pDisable2 = new ImageIcon("res/player/pDisable2.jpg");
		private ImageIcon pDisable3 = new ImageIcon("res/player/pDisable3.jpg");
		private ImageIcon pDisable4 = new ImageIcon("res/player/pDisable4.jpg");
			
		public RoomListPanel(HashMap<Integer, String> roomInfo) {
			this.roomInfo = roomInfo;
			
			// room�� ���� JLabel, JButton ����
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
		
		// RoomView�� �ٽ� �׸��� ���� ��� RoomView�� �����Ѵ�.
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
				// Room�� ������ hashMap (roomInfo, rooms)�� �ִ´�.
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
					roomView.name.setForeground(disable);
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
				// Room ��ü�� hashmap�� �������� �����Ѵ�.
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
			private JButton enter = new JButton(btnEnable);

			public RoomView(int key, String roomName) {
				this.key = key;
				name.setText(" [" + key + "] " + roomName);
				name.setOpaque(true);
				name.setBackground(Color.WHITE);
				name.setPreferredSize(new Dimension(480, 50));
				name.setFont(new Font("���� ���", Font.BOLD, 20));

				pNum.setPreferredSize(new Dimension(80, 50));
				
				enter.setPreferredSize(new Dimension(100, 50));
				enter.setFont(new Font("Arial", Font.BOLD + Font.ITALIC, 15));
				enter.addActionListener(new EnterActionListener(key));
				enter.setRolloverIcon(btnHover);
			} // End of RoomView()
		} // End of class RoomView
		
		// RoomListPanel�� ���� btnListener - ����� ���� WaitingView�� �߰�
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