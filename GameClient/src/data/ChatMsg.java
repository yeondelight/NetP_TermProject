// ChatMsg.java 채팅 메시지 ObjectStream 용.
package data;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import javax.swing.ImageIcon;
import data.GameRoom;

public class ChatMsg implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String code;
	private String data;
	public ImageIcon img;

	private int keyCode;

	// default
	public ChatMsg(String id, String code, String msg) {
		this.id = id;
		this.code = code;
		this.data = msg;
	}
	
	// Event
	public ChatMsg(String id, String code, String data, int keyCode) {
		this.id = id;
		this.code = code;
		this.data = data;
		this.keyCode = keyCode;
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
	
	public int getKeyCode() {
		return keyCode;
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
}