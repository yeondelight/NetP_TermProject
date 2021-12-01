package data;

import java.io.Serializable;
import java.util.Vector;

import javax.swing.JPanel;

// 게임방에 대한 정보를 담는 Room
// ObjectStream 전송을 위해 몇몇 속성 추가
public class GameRoom implements Serializable {

	private static final long serialVersionUID = 2L;

	public final static int MAXPLAYER = 4;
	private final static String AVAIL = "AVAIL";
	private final static String FULL = "FULL";
	private final static String STARTED = "STARTED";

	private final static int ITEMNUM = 5;
	private final static int WIDTH = 460;

	private int key; // 방의 고유 번호
	private String name; // 방의 이름
	private String status; // 방의 상태 - Button의 활성화 여부를 결정한다.
	private Vector<String> userList; // 방에 참여하는 User의 수
	private Vector<String> spectatorList; // 방에 참여하는 관전자 User의 수

	transient private GameMap gameMap; // 나중에 따로 update하기 위해 Serializable 제외

	public GameRoom(int key, String name) {
		this.key = key;
		this.name = name;

		userList = new Vector<String>();
		spectatorList = new Vector<String>();
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

	public void setGameMap(GameMap gameMap) {
		this.gameMap = gameMap;
	}

	// UserList 얻기
	public Vector getUserList() {
		return userList;
	}

	// SpectatorList 얻기
	public Vector getSpectatorList() {
		if (spectatorList == null)
			return null;
		return spectatorList;
	}

	// new player enter
	public void enterUser(String userName) {
		if (status != AVAIL) {
			System.out.println("CANNOT ENTER");
			return;
		}

		userList.add(userName);

		if (userList.size() == MAXPLAYER)
			setStatus(FULL);
	}

	// new spectator player enter
	public void enterSpectatorUser(String userName) {
		spectatorList.add(userName);
	}

	// player exit
	public int exitUser(String userName) {
		userList.remove(userName);
		if (userList.size() < MAXPLAYER && status != STARTED)
			setStatus(AVAIL);

		return userList.size();
	}
	
	public int exitSpectator(String userName) {
		spectatorList.remove(userName);

		return spectatorList.size();
	}

}
