package data;

import java.io.Serializable;
import java.util.Vector;

import javax.swing.JPanel;

// ���ӹ濡 ���� ������ ��� Room
// ObjectStream ������ ���� ��� �Ӽ� �߰�
public class GameRoom implements Serializable {

	private static final long serialVersionUID = 2L;

	public final static int MAXPLAYER = 4;
	private final static String AVAIL = "AVAIL";
	private final static String FULL = "FULL";
	private final static String STARTED = "STARTED";

	private final static int ITEMNUM = 5;
	private final static int WIDTH = 460;

	private int key; // ���� ���� ��ȣ
	private String name; // ���� �̸�
	private String status; // ���� ���� - Button�� Ȱ��ȭ ���θ� �����Ѵ�.
	private Vector<String> userList; // �濡 �����ϴ� User�� ��
	private Vector<String> spectatorList; // �濡 �����ϴ� ������ User�� ��

	transient private GameMap gameMap; // ���߿� ���� update�ϱ� ���� Serializable ����

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

	// UserList ���
	public Vector getUserList() {
		return userList;
	}

	// SpectatorList ���
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
