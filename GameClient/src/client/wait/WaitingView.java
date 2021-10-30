package client.wait;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class WaitingView extends JFrame{
	
	// 서버에게 전송하기 위한 코드. 항상 "코드 메세지"의 형태로 전송할것.
	private static final String C_LOGIN = "100";		// 새로운 client 접속
	private static final String C_ACKLIST = "101";		// C->S 101을 정상적으로 수신
	private static final String S_REQLIST = "110";		// S->C 생성되어 있는 room 개수 전송
	private static final String S_SENLIST = "120";		// S->C 각 room의 key, name 전송
	private static final String C_MAKEROOM = "200";		// 새로운 방 생성
	private static final String C_ENTROOM = "201";		// 해당 방에 입장
	private static final String S_UPDROOM = "210";		// room 목록 update
	
	private static final int BUF_LEN = 128; //  Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String userName;
	
	private Container contentPane;
	private JScrollPane scrollPane;
	private RoomListPanel roomListPanel;
	private MakeRoomDialog makeDialog;

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
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
			
			SendMessage(C_LOGIN + " " + userName);		// 로그인 정보를 서버에게 전송
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
				if (name == null)	return;
				else{
					SendMessage(C_MAKEROOM+" "+key+" "+name+" "+userName);
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
	
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {
					// String msg = dis.readUTF();
					byte[] b = new byte[BUF_LEN];
					int ret;
					ret = dis.read(b);
					if (ret < 0) {
						System.out.println("dis.read() < 0 error");
						try {
							dos.close();
							dis.close();
							socket.close();
							break;
						} catch (Exception ee) {
							break;
						}// catch문 끝
					}
					String	msg = new String(b, "euc-kr");
					msg = msg.trim(); // 앞뒤 blank NULL, \n 모두 제거
					System.out.println("Client get : " + msg);
					
					// words[0] : 코드
					// words[1] : /to와 같은 cmd
					// words[2] : /to인 경우 보낼 UserName, 그 외 전송하고자 하는 메세지
					// 이하 : 전송하고자 하는 메세지
					String cmds[] = msg.split(" ");
					
					// S_REQLIST(110)
					// Server -> Client 메세지의 정상 전송을 확인하고 현재 Room의 개수를 보낸다.
					if(cmds[0].equals(S_REQLIST)) {
						roomNum = Integer.parseInt(cmds[1]);
						System.out.println("Client get : "+roomNum);
						SendMessage(C_ACKLIST+" "+userName);
					}
					
					// S_SENLIST(120)
					// Server -> Client Room에 대한 정보 (Key, Name)을 보낸다.
					else if(cmds[0].equals(S_SENLIST)) {
						// 정보를 받아 client의 HashMap room에 저장한다.
						int key = Integer.parseInt(cmds[1]);
						String name = cmds[2];
						rooms.put(key, name);
						roomListPanel.addRoom(key, name);	// 화면에 보이도록 처리
					}
					
					// S_UPDROOM(210)
					// Server -> Client Room 목록의 변경 감지하고 update 요청
					else if(cmds[0].equals(S_UPDROOM)) {
						int key = Integer.parseInt(cmds[1]);
						String name = cmds[2];
						makeDialog.setKey(key);				// 해당 key 중복 방지 처리
						roomListPanel.addRoom(key, name);	// 화면에 보이도록 처리
					}
					
				} catch (IOException e) {
					System.out.println("dis.read() error");
					try {
						dos.close();
						dis.close();
						socket.close();
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	}

	// Server에게 network으로 전송
	public void SendMessage(String msg) {
		try {
			// dos.writeUTF(msg);
			System.out.println("client send : " + msg);
			byte[] bb;
			bb = MakePacket(msg);
			dos.write(bb, 0, bb.length);
		} catch (IOException e) {
			System.out.println("dos.write() error");
			try {
				dos.close();
				dis.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
}