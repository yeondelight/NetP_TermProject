package server.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import data.GameRoom;
import data.RoomMsg;

public class GameServer extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private ServerSocket socket; // 서버소켓
	private Socket client_socket; // accept() 에서 생성된 client 소켓
	private Vector UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의

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
				btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 300, 300, 35);
		contentPane.add(btnServerStart);
	}

	// 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					AppendText("Waiting clients ...");
					client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
					AppendText("새로운 참가자 from " + client_socket);
					// User 당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // 새로운 참가자 배열에 추가
					AppendText("사용자 입장. 현재 참가자 수 " + UserVec.size());
					new_user.start(); // 만든 객체의 스레드 실행
				} catch (IOException e) {
					AppendText("!!!! accept 에러 발생... !!!!");
				}
			}
		}
	}

	public void AppendText(String str) {
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}
	
	public void AppendObject(ChatMsg msg) {
		// textArea.append("사용자로부터 들어온 object : " + str+"\n");
		textArea.append("code = " + msg.getCode() + "\n");
		textArea.append("id = " + msg.getId() + "\n");
		textArea.append("data = " + msg.getData() + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// User 당 생성되는 Thread
	// Read One 에서 대기 -> Write All
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
		
		// 이하 userStatus 처리를 위한 변수
		private static final String ONLINE = "ONLINE";
		private static final String SLEEP = "SLEEP";
		private static final String READYON = "READYON";
		private static final String READYOFF = "READYOFF";
		private static final String GAME = "GAME";
		
		// 이하 GameRoom 처리를 위한 변수
		private GameRoom gameRoom = null;			// 초기에는 게임방에 입장하지 않았으므로
		
		// 이하 Protocol msg
		private static final String C_LOGIN = "100";		// 새로운 client 접속
		private static final String C_ACKLIST = "101";		// C->S 101을 정상적으로 수신
		private static final String C_MAKEROOM = "200";		// 새로운 방 생성
		private static final String C_ENTROOM = "201";		// 해당 방에 입장 
		private static final String C_CHATMSG = "301";		// C->S GameRoom 내 일반 채팅 메세지
		private static final String C_UPDROOM = "302";		// C->S GameRoom 업데이트좀
		private static final String C_ACKROOM = "303";		// C->S 320 ACK
		private static final String C_STRGAME = "304";		// C->S 게임 시작할래
		
		private static final String S_REQLIST = "110";		// S->C 생성되어 있는 room 개수 전송
		private static final String S_SENLIST = "120";		// S->C 각 room의 key, name 전송
		private static final String S_UPDLIST = "210";		// room 목록 update
		private static final String S_ENTROOM = "220";		// S->C room 입장 허가
		private static final String S_CHATMSG = "310";		// S->C GameRoom 내 방송
		private static final String S_UPDROOM = "320";		// GameRoom 정보 update : user 수 반환
		private static final String S_USRLIST = "330";		// userList update
		private static final String S_STRGAME = "340";		// S->C 그래 시작해
		
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
			// 매개변수로 넘어온 자료 저장
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
			String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
			UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			WriteAll(msg); // 나를 제외한 다른 User들에게 전송
			AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
		}
		
		// UserService Thread가 담당하는 Client 에게 1:1 전송
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

		// 모든 User들에게 방송. 각각의 UserService Thread의 WriteOne() 을 호출한다.
		public void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				// online 상태인 User에게만 보낸다. (WaitingRoom만 보내게 됨)
				if(user.getUserStatus().equals(ONLINE))
					user.WriteOne(str);
			}
		}
		
		// 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.getUserStatus().equals(ONLINE))
					user.WriteOneObject(ob);
			}
		}
		
		// 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteOne() 을 호출한다.
		public void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.getUserStatus().equals(ONLINE))
					user.WriteOne(str);
			}
		}

		// 내가 속한 GameRoom의 다른 User들에게 전송. 각 GameRoom Thread의 WriteOne()을 호출한다.
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
				e.printStackTrace();
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}
		
		// GameRoom 입장 처리
		public void enterRoom(GameRoom gameRoom) {
			this.gameRoom = gameRoom;
		}
		
		// UserName으로 해당 User의 Status 찾기
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
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
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
					
					// 이하 Protocol 처리
					
					// C_LOGIN(100)
					if(cm.getCode().matches(C_LOGIN)) {
						UserName = cm.getId();
						// 중복닉네임 설정
						for(int i = 0; i < user_vc.size()-1; i++) {
							UserService user = (UserService)UserVec.get(i);
							if(user.getUserName().equals(UserName)) {
								String hash = String.valueOf((int)(Math.random()*100));
								UserName += hash;
							}
						}
						AppendText("새로운 참가자 " + UserName + " 입장.");
						AppendObject(cm);
						WriteOneObject(new ChatMsg(UserName, S_REQLIST, roomManager.getSize()+""));
					}
					
					// C_ACKLIST(101)
					// Client -> Server S_REQLIST가 정상적으로 왔음을 알림
					// Waiting Room에 표시할 Room들의 배열을 보낸다.
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
					// client -> Server 새로운 방을 만들어주세요
					// Server의 hashMap에 key와 name을 넣은 Room을 만들고
					// 모든 client에게 update 요청
					else if(cm.getCode().matches(C_MAKEROOM)){
						String val[] = cm.getData().split(" ");
						int key = Integer.parseInt(val[0]);
						String name = val[1];
						GameRoom room = new GameRoom(key, name);
						roomManager.addRoom(room);
						WriteOneObject(new RoomMsg(UserName, S_ENTROOM, room));
						WriteAllObject(new ChatMsg(UserName, S_UPDLIST, roomManager.getSize()+""));
					}
					
					// C_ENTROOM(201)
					// client -> Server 이 방에 들어갈래
					// key에 해당하는 방 객체를 불러와 Client에게 전송 (S_ENTROOM)
					else if (cm.getCode().matches(C_ENTROOM)) {
						int key = Integer.parseInt(cm.getData());
						GameRoom room = roomManager.getRoom(key);
						this.enterRoom(room);
						this.setUserStatus(READYOFF);
						room.enterUser(UserName);
						RoomMsg enter = new RoomMsg(UserName, S_ENTROOM, room);
						WriteOneObject(enter);
						WriteAllObject(new ChatMsg(UserName, S_UPDLIST, roomManager.getSize()+""));		// 입장 후 방의 상태가 변경될 수 있으므로
					}
					
					// C_CHATMSG(301)
					// Client -> Server 방 내에서의 일반 채팅
					// new ChatMsg(USERNAME, 301, key msg) 형태
					// 방 내의 Client들에게 방송 (S_CHATMSG)
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
					// Client -> Server 나 방 상태 바꿨으니까 업데이트좀
					// new ChatMsg(USERNAME, 302, roomKey myStatus) 형태
					// 같은 게임방 내 User들에게 전송
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
					// Client -> Server User 받을 준비 완
					// new ChatMsg(USERNAME, 303, roomKey) 형태
					// 같은 게임방 내 User들에게 전송
					else if (cm.getCode().matches(C_ACKROOM)) {
						int key = Integer.parseInt(cm.getData());
						GameRoom room = roomManager.getRoom(key);
						Vector userList = room.getUserList();
						for (int i = 0; i < userList.size(); i++) {
							String name = (String) userList.get(i);
							String status = getStatusWithName(name);
							String contents = name + " " + status;
							System.out.println("ROOMOBJ 전송 : "+contents);
							WriteOneObject(new ChatMsg(UserName, S_USRLIST, contents));
						}
					}
					
					// C_STRGAME(304)
					// Client -> Server 게임 시작할래
					// new ChatMsg(USERNAME, 304, roomKey) 형태
					// 같은 게임방 내 User들에게 전송
					else if (cm.getCode().matches(C_STRGAME)) {
						int key = Integer.parseInt(cm.getData());
						GameRoom room = roomManager.getRoom(key);
						WriteRoomObject(key, new ChatMsg(UserName, S_STRGAME, ""));
					}
					
					// exit 처리
					//else if(cmds[1].equals("/exit")) {
					//	dos.close();
					//	dis.close();
					//	client_socket.close();
					//	UserVec.removeElement(this);
					//	WriteAll("[" + UserName + "] 님이 퇴장하셨습니다.\n");
					//	AppendText("사용자 퇴장. 남은 참가자 수 " + UserVec.size());
					//	break;
					//}
					
					else { // 일반 채팅 메시지
						UserStatus = ONLINE;
						WriteAllObject(cm);
					}
					
				} catch (IOException e) {
					AppendText("dis.read() error");
					try {
						ois.close();
						oos.close();
						client_socket.close();
						if(this.getGameRoom()!=null) {		// 게임방에 있는 경우
							int pNum = gameRoom.exitUser(this.UserName);
							WriteRoomObject(gameRoom.getKey(), new ChatMsg(UserName, S_UPDROOM, pNum+""));		// 게임방에서 해당 user를 제거한다.
							WriteAllObject(new ChatMsg(UserName, S_UPDLIST, roomManager.getSize()+""));			// WaitingView를 update 한다.
						}
						UserVec.removeElement(this); // 에러가난 현재 객체를 벡터에서 지운다
						AppendText("사용자 퇴장. 남은 참가자 수 " + UserVec.size());
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
			} // while
		} // run
	}
}
