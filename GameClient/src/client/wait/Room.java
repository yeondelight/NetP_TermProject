package client.wait;

// 게임방에 대한 정보를 담는 Room
public class Room {
	
	private final static int MAXPLAYER = 4;
	private final static String AVAIL = "AVAIL";
	private final static String FULL = "FULL";
	private final static String STARTED = "STARTED";
	
	private int key;
	private String name;
	
	private String status;
	
	public Room(int key, String name) {
		this.key = key;
		this.name = name;
		
		status = AVAIL;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getStatus() {
		return status;
	}

}
