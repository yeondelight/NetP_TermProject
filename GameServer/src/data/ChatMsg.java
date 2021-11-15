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
	private ImageIcon img;

	private int numCode;

	// default
	public ChatMsg(String id, String code, String msg) {
		this.id = id;
		this.code = code;
		this.data = msg;
	}
	
	// Event, Score 등등...
	public ChatMsg(String id, String code, String data, int numCode) {
		this.id = id;
		this.code = code;
		this.data = data;
		this.numCode = numCode;
	}
	
	// Image
	public ChatMsg(String id, String code, ImageIcon img) {
		this.id = id;
		this.code = code;
		this.img = img;
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
	
	public int getNumCode() {
		return numCode;
	}
	
	public ImageIcon getImg() {
		return img;
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