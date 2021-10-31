package client.wait;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;


// 게임방에 대한 정보를 담는 Room
public class GameRoom {
	
	private final static int MAXPLAYER = 4;
	private final static String AVAIL = "AVAIL";
	private final static String FULL = "FULL";
	private final static String STARTED = "STARTED";
	
	private int key;		// 방의 고유 번호
	private String name;	// 방의 이름
	private String status;	// 방의 상태 - Button의 활성화 여부를 결정한다.
	private ArrayList userList;	// 방에 참여하는 User의 수
	
	public GameRoom(int key, String name) {
		this.key = key;
		this.name = name;
		
		userList = new ArrayList();
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
	public ArrayList getUserList() {
		return userList;
	}


}
