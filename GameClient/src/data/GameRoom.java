package data;

import java.io.Serializable;
import java.util.Vector;

// 게임방에 대한 정보를 담는 Room
// ObjectStream 전송을 위해 몇몇 속성 추가
public class GameRoom implements Serializable{
	
	private static final long serialVersionUID = 2L;
	
	private final static int MAXPLAYER = 4;
	private final static String AVAIL = "AVAIL";
	private final static String FULL = "FULL";
	private final static String STARTED = "STARTED";
	
	private int key;		// 방의 고유 번호
	private String name;	// 방의 이름
	private String status;	// 방의 상태 - Button의 활성화 여부를 결정한다.
	private Vector<String> userList;	// 방에 참여하는 User의 수
	
	public GameRoom(int key, String name) {
		this.key = key;
		this.name = name;
		
		userList = new Vector<String>();
		status = AVAIL;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	// UserList 얻기
	public Vector getUserList() {
		return userList;
	}
	
	// new player enter
	public void enterUser(String userName) {
		if(status!=AVAIL){
			System.out.println("CANNOT ENTER");
			return;
		}
		
		userList.add(userName);
		
		if(userList.size() == MAXPLAYER)
			setStatus(FULL);
	}
	
	// player exit
		public int exitUser(String userName) {
			userList.remove(userName);
			return userList.size();
		}

}
