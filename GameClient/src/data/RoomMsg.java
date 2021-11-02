package data;

import java.io.Serializable;

import javax.swing.ImageIcon;

public class RoomMsg implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String id;
	private String code;
	private GameRoom room;
	
	public RoomMsg(String id, String code, GameRoom room) {
		this.id = id;
		this.code = code;
		this.room = room;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public GameRoom getRoom() {
		return room;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRoom(GameRoom room) {
		this.room = room;
	}
}
