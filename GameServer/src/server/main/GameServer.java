package server.main;

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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import data.ChatMsg;
import data.GameMap;
import data.GameMsg;
import data.GameRoom;
import data.RoomMsg;

public class GameServer extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private ServerSocket socket; // ��������
	private Socket client_socket; // accept() ���� ������ client ����
	private Vector UserVec = new Vector(); // ����� ����ڸ� ������ ����
	private static final int BUF_LEN = 128; // Windows ó�� BUF_LEN �� ����

	private RoomManager roomManager = new RoomManager();

	// Create the frame.
	public GameServer() {
		
		//testCode
		roomManager.addRoom(new GameRoom(100000, "test1"));
		roomManager.addRoom(new GameRoom(200000, "test2"));
		roomManager.addRoom(new GameRoom(300000, "test3"));
		roomManager.addRoom(new GameRoom(400000, "test4"));
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 386);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 244);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(12, 264, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(111, 264, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // ������ ���̻� �����Ű�� �� �ϰ� ���´�
				txtPortNumber.setEnabled(false); // ���̻� ��Ʈ��ȣ ������ �ϰ� ���´�
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 300, 300, 35);
		contentPane.add(btnServerStart);
	}

	// ���ο� ������ accept() �ϰ� user thread�� ���� �����Ѵ�.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					AppendText("Waiting clients ...");
					client_socket = socket.accept(); // accept�� �Ͼ�� �������� ���� �����
					AppendText("���ο� ������ from " + client_socket);
					// User �� �ϳ��� Thread ����
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // ���ο� ������ �迭�� �߰�
					AppendText("����� ����. ���� ������ �� " + UserVec.size());
					new_user.start(); // ���� ��ü�� ������ ����
				} catch (IOException e) {
					AppendText("!!!! accept ���� �߻�... !!!!");
				}
			}
		}
	}

	public void AppendText(String str) {
		// textArea.append("����ڷκ��� ���� �޼��� : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}
	
	public void AppendObject(ChatMsg msg) {
		// textArea.append("����ڷκ��� ���� object : " + str+"\n");
		textArea.append("code = " + msg.getCode() + "\n");
		textArea.append("id = " + msg.getId() + "\n");
		textArea.append("data = " + msg.getData() + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// User �� �����Ǵ� Thread
	// Read One ���� ��� -> Write All
	class UserService extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		
		private Socket client_socket;
		private Vector user_vc;
		private String UserName = "";
		private String UserStatus = ONLINE;
		
		// ���� userStatus ó���� ���� ����
		private static final String ONLINE = "ONLINE";
		private static final String SLEEP = "SLEEP";
		private static final String READYON = "READYON";
		private static final String READYOFF = "READYOFF";
		
		// ���� GameRoom ó���� ���� ����
		private GameRoom gameRoom = null;			// �ʱ⿡�� ���ӹ濡 �������� �ʾ����Ƿ�
		private final static String AVAIL = "AVAIL";
		private final static String FULL = "FULL";
		private final static String STARTED = "STARTED";
		
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
		private static final String C_EXITROOM = "308";		// Client -> Server �� ������
		private static final String C_ENDGAME = "309";		// Client -> Server ���� ������
		
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
		
		public String getUserName() {
			return UserName;
		}
		
		public String getUserStatus() {
			return UserStatus;
		}
		
		public void setUserStatus(String status) {
			this.UserStatus = status;
		}
		
		public GameRoom getGameRoom() {
			return gameRoom;
		}
		
		public Socket getClientSocket() {
			return client_socket;
		}

		public UserService(Socket client_socket) {
			// �Ű������� �Ѿ�� �ڷ� ����
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			try {
				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());
				
			} catch (Exception e) {
				AppendText("userService error");
				e.printStackTrace();
			}
		}
		
		public void Logout() {
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			UserVec.removeElement(this); // Logout�� ���� ��ü�� ���Ϳ��� �����
			WriteAll(msg); // ���� ������ �ٸ� User�鿡�� ����
			AppendText("����� " + "[" + UserName + "] ����. ���� ������ �� " + UserVec.size());
		}
		
		// UserService Thread�� ����ϴ� Client ���� 1:1 ����
		public void WriteOne(String msg) {
			try {
				ChatMsg obcm = new ChatMsg("SERVER", "310", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.write() error");
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout();
			}
		}
				
		public void WriteOneObject(Object ob) {
			try {
			    oos.writeObject(ob);
			} 
			catch (IOException e) {
				AppendText("oos.writeObject(ob) error");		
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout();
			}
		}

		// ��� User�鿡�� ���. ������ UserService Thread�� WriteOne() �� ȣ���Ѵ�.
		public void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				// online ������ User���Ը� ������. (WaitingRoom�� ������ ��)
				if(user.getUserStatus().equals(ONLINE))
					user.WriteOne(str);
			}
		}
		
		// ��� User�鿡�� Object�� ���. ä�� message�� image object�� ���� �� �ִ�
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.getUserStatus().equals(ONLINE))
					user.WriteOneObject(ob);
			}
		}
		
		// ���� ������ User�鿡�� ���. ������ UserService Thread�� WriteOne() �� ȣ���Ѵ�.
		public void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.getUserStatus().equals(ONLINE))
					user.WriteOne(str);
			}
		}

		// ���� ���� GameRoom�� �ٸ� User�鿡�� ����. �� GameRoom Thread�� WriteOne()�� ȣ���Ѵ�.
		public void WriteRoomObject(int key, Object ob) {
			GameRoom room = roomManager.getRoom(key);
			Vector userList = room.getUserList();
			for(int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				for (int j = 0; j < userList.size(); j++) {
					if(user.UserName.equals(userList.get(j))) {
						user.WriteOneObject(ob);
					}
				}
			}
		}
		
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
				e.printStackTrace();
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}
		
		// GameRoom ���� ó��
		public void enterRoom(GameRoom gameRoom) {
			this.gameRoom = gameRoom;
		}
		
		// GameRoom ���� ó��
		public void exitRoom() {
			this.gameRoom = null;
		}
		
		// UserName���� �ش� User�� Status ã��
		public String getStatusWithName(String userName) {
			for(int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if(user.getUserName().equals(userName)) {
					return user.getUserStatus();
				}
			}
			return null;
		}

		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm = null;
					if(socket == null)
						break;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						return;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						AppendObject(cm);
					} else
						continue;
					
					// ���� Protocol ó��
					
					// C_LOGIN(100)
					if(cm.getCode().matches(C_LOGIN)) {
						UserName = cm.getId();
						// �ߺ��г��� ����
						for(int i = 0; i < user_vc.size()-1; i++) {
							UserService user = (UserService)UserVec.get(i);
							if(user.getUserName().equals(UserName)) {
								String hash = String.valueOf((int)(Math.random()*100));
								UserName += hash;
							}
						}
						AppendText("���ο� ������ " + UserName + " ����.");
						AppendObject(cm);
						WriteOneObject(new ChatMsg(UserName, S_REQLIST, roomManager.getSize()+""));
					}
					
					// C_ACKLIST(101)
					// Client -> Server S_REQLIST�� ���������� ������ �˸�
					// Waiting Room�� ǥ���� Room���� �迭�� ������.
					if(cm.getCode().matches(C_ACKLIST)){
						Set<Integer> roomKeys = roomManager.getRoomKeys();
						Iterator<Integer> roomKeysIt = roomKeys.iterator();
						for(int i = 0; i < roomManager.getSize(); i++) {
							int key = roomKeysIt.next();
							String name = roomManager.getRoom(key).getName();
							String status = roomManager.getRoom(key).getStatus();
							int players = roomManager.getRoom(key).getUserList().size();
							WriteOneObject(new ChatMsg(UserName, S_SENLIST, key+" "+name+" "+status+" "+players));
						}
					}
					
					// C_MAKEROOM(200)
					// client -> Server ���ο� ���� ������ּ���
					// Server�� hashMap�� key�� name�� ���� Room�� �����
					// ��� client���� update ��û
					else if(cm.getCode().matches(C_MAKEROOM)){
						String val[] = cm.getData().split(" ");
						int key = Integer.parseInt(val[0]);
						String name = val[1];
						GameRoom room = new GameRoom(key, name);
						roomManager.addRoom(room);
						// First User ���� ó��
						this.enterRoom(room);
						this.setUserStatus(READYOFF);
						room.enterUser(UserName);
						WriteOneObject(new RoomMsg(UserName, S_ENTROOM, room));
						WriteAllObject(new ChatMsg(UserName, S_UPDLIST, roomManager.getSize()+""));
					}
					
					// C_ENTROOM(201)
					// client -> Server �� �濡 ����
					// key�� �ش��ϴ� �� ��ü�� �ҷ��� Client���� ���� (S_ENTROOM)
					else if (cm.getCode().matches(C_ENTROOM)) {
						int key = Integer.parseInt(cm.getData());
						GameRoom room = roomManager.getRoom(key);
						this.enterRoom(room);
						this.setUserStatus(READYOFF);
						room.enterUser(UserName);
						RoomMsg enter = new RoomMsg(UserName, S_ENTROOM, room);
						WriteOneObject(enter);
						WriteAllObject(new ChatMsg(UserName, S_UPDLIST, roomManager.getSize()+""));		// ���� �� ���� ���°� ����� �� �����Ƿ�
					}
					
					// C_CHATMSG(301)
					// Client -> Server �� �������� �Ϲ� ä��
					// new ChatMsg(USERNAME, 301, key msg) ����
					// �� ���� Client�鿡�� ��� (S_CHATMSG)
					else if (cm.getCode().matches(C_CHATMSG)) {
						String val[] = cm.getData().split(" ");
						int key = Integer.parseInt(val[0]);
						String chatMsg = val[1];
						for (int i = 2; i < val.length; i++) {
							chatMsg += (" "+val[i]);
						}
						WriteRoomObject(key, new ChatMsg(UserName, S_CHATMSG, chatMsg));
					}
					
					// C_UPDROOM(302)
					// Client -> Server �� �� ���� �ٲ����ϱ� ������Ʈ��
					// new ChatMsg(USERNAME, 302, roomKey myStatus) ����
					// ���� ���ӹ� �� User�鿡�� ����
					else if (cm.getCode().matches(C_UPDROOM)) {
						String val[] = cm.getData().split(" ");
						int key = Integer.parseInt(val[0]);
						if(val.length > 1) {
							String status = val[1];
							if (val[1].equals("true"))
								this.setUserStatus(READYON);
							else
								this.setUserStatus(READYOFF);
						}
						GameRoom room = roomManager.getRoom(key);
						WriteRoomObject(key, new ChatMsg(UserName, S_UPDROOM, room.getUserList().size()+""));
					}
					
					// C_ACKROOM(303)
					// Client -> Server User ���� �غ� ��
					// new ChatMsg(USERNAME, 303, roomKey) ����
					// ���� ���ӹ� �� User�鿡�� ����
					else if (cm.getCode().matches(C_ACKROOM)) {
						int key = Integer.parseInt(cm.getData());
						GameRoom room = roomManager.getRoom(key);
						Vector userList = room.getUserList();
						for (int i = 0; i < userList.size(); i++) {
							String name = (String) userList.get(i);
							String status = getStatusWithName(name);
							String contents = name + " " + status;
							System.out.println("ROOMOBJ ���� : "+contents);
							WriteOneObject(new ChatMsg(UserName, S_USRLIST, contents));
						}
					}
					
					// C_STRGAME(304)
					// Client -> Server ���� �����ҷ�
					// new ChatMsg(USERNAME, 304, roomKey) ����
					// ���� ���ӹ� �� User�鿡�� ����
					else if (cm.getCode().matches(C_STRGAME)) {
						int key = Integer.parseInt(cm.getData());
						GameRoom room = roomManager.getRoom(key);
						GameMap gameMap = new GameMap(room.getUserList());
						room.setStatus(STARTED);		// �� ���� ����
						room.setGameMap(gameMap);		// �濡 Map ���� ���
						WriteRoomObject(key, new GameMsg(UserName, S_STRGAME, gameMap));
						WriteAllObject(new ChatMsg(UserName, S_UPDLIST, roomManager.getSize()+""));		// ���� ���°� ����Ǿ����Ƿ�
						// ���ο� Thread ��� Timer Check �ʿ�
						new Thread() {
							int timeout = 60;
							public void run() {
								WriteRoomObject(key, new ChatMsg(UserName, S_UPDTIME, timeout+""));
								while(true) {
									try {
										timeout--;
										Thread.sleep(1000);
										WriteRoomObject(key, new ChatMsg(UserName, S_UPDTIME, timeout+""));
										
										// timeout ����
										if(timeout <= 0) {
											// timer ����
											return;
										}
									}
									catch(Exception e) {
										e.printStackTrace();
									}
								}
							}
						}.start();
					}
					
					// C_UPDGAME(305)
					// Client -> Server �� ��������
					// new ChatMsg(USERNAME, 305, roomKey, keyCode) ����
					// ���� ���ӹ� �� User�鿡�� ����
					else if (cm.getCode().matches(C_UPDGAME)) {
						int key = Integer.parseInt(cm.getData());
						GameRoom room = roomManager.getRoom(key);
						Integer keyCode = cm.getNumCode();
						if (keyCode != null) {		// Keyboard Event�� �Ѿ������
							WriteRoomObject(key, new ChatMsg(UserName, S_UPDGAME, cm.getData(), keyCode));
						}
					}
					
					// C_UPDGAME(307)
					// Client -> Server �� ���� �����
					// new ChatMsg(USERNAME, 307, roomKey +-/*, score) ����
					// ���� ���ӹ� �� User�鿡�� ����
					else if (cm.getCode().matches(C_UPDSCORE)) {
						String val[] = cm.getData().split(" ");
						int key = Integer.parseInt(val[0]);
						GameRoom room = roomManager.getRoom(key);
						Integer score = cm.getNumCode();
						WriteRoomObject(key, new ChatMsg(cm.getId(), S_UPDSCORE, cm.getData(), score));
					}
					
					// C_EXITROOM(308)
					// Client -> Server �� �� �� ������
					// new ChatMsg(USERNAME, 308, roomKey WaitingViewrefresh����) ����
					else if (cm.getCode().matches(C_EXITROOM)) {
						String val[] = cm.getData().split(" ");
						int key = Integer.parseInt(val[0]);
						boolean refresh = Boolean.parseBoolean(val[1]);
						GameRoom room = roomManager.getRoom(key);
						this.exitRoom();
						this.setUserStatus(ONLINE);
						room.exitUser(UserName);
						WriteRoomObject(key, new ChatMsg(UserName, S_UPDROOM, room.getUserList().size()+""));	// user�� �������Ƿ�
						if (refresh)					// ���� �� ���� ���°� ����� �� �����Ƿ�. �׷��� �ٷ� �������ϴ� ��츦 ����� true�϶��� �����Ѵ�.
							WriteAllObject(new ChatMsg(UserName, S_UPDLIST, roomManager.getSize()+""));
					}
					
					// C_ENDGAME(309)
					// Client -> Server ���� ������
					// new ChatMsg(USERNAME, 308, roomKey) ����
					else if (cm.getCode().matches(C_ENDGAME)) {
						int key = Integer.parseInt(cm.getData());
						GameRoom room = roomManager.getRoom(key);
						
						// GameOver�� �˸��� �̹��� ����
						ChatMsg serverMsg = new ChatMsg("SERVER", S_CHATMSG, new ImageIcon("res/gameoverMsg.png"));
						WriteOneObject(serverMsg);
						
						this.exitRoom();
						this.setUserStatus(ONLINE);
						room.exitUser(UserName);
						
						if(room.getUserList().size() < room.MAXPLAYER)
							room.setStatus(AVAIL);
						else
							room.setStatus(FULL);
					}
					
					// exit ó��
					//else if(cmds[1].equals("/exit")) {
					//	dos.close();
					//	dis.close();
					//	client_socket.close();
					//	UserVec.removeElement(this);
					//	WriteAll("[" + UserName + "] ���� �����ϼ̽��ϴ�.\n");
					//	AppendText("����� ����. ���� ������ �� " + UserVec.size());
					//	break;
					//}
					
					else { // �Ϲ� ä�� �޽���
						UserStatus = ONLINE;
						WriteAllObject(cm);
					}
					
				} catch (IOException e) {
					AppendText("dis.read() error");
					try {
						ois.close();
						oos.close();
						client_socket.close();
						if(this.getGameRoom()!=null) {		// ���ӹ濡 �ִ� ���
							int pNum = gameRoom.exitUser(this.UserName);
							if(this.getGameRoom().getStatus().equals(STARTED)) {
								this.exitRoom();
								this.getGameRoom().exitUser(UserName);
							}
							else
								WriteRoomObject(gameRoom.getKey(), new ChatMsg(UserName, S_UPDROOM, pNum+""));		// ���ӹ濡�� �ش� user�� �����Ѵ�.	
							WriteAllObject(new ChatMsg(UserName, S_UPDLIST, roomManager.getSize()+""));			// WaitingView�� update �Ѵ�.
						}
						UserVec.removeElement(this); // �������� ���� ��ü�� ���Ϳ��� �����
						AppendText("����� ����. ���� ������ �� " + UserVec.size());
						break;
					} catch (Exception ee) {
						break;
					} // catch�� ��
				} // �ٱ� catch����
			} // while
		} // run
	}
}
