// ChatMsg.java 채팅 메시지 ObjectStream 용.
package data;

import java.io.Serializable;
import javax.swing.ImageIcon;
import data.GameRoom;

public class ChatMsg implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String code;
	private String data;
	private GameRoom room;
	public ImageIcon img;

	public ChatMsg(String id, String code, String msg) {
		this.id = id;
		this.code = code;
		this.data = msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getData() {
		return data;
	}

	public String getId() {
		return id;
	}
	
	public GameRoom getGameRoom() {
		return room;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setImg(ImageIcon img) {
		this.img = img;
	}
	
	public void setRoom(GameRoom room) {
		this.room = room;
	}
}