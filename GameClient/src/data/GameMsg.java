package data;

import java.io.Serializable;

public class GameMsg implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String code;
	private GameMap gameMap;

	// 초기 gameMap 설정시 사용
	public GameMsg(String id, String code, GameMap gameMap) {
		this.id = id;
		this.code = code;
		this.gameMap = gameMap;
	}
	

	public String getId() {
		return id;
	}
	
	public String getCode() {
		return code;
	}

	public GameMap getMap() {
		return gameMap;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public void setMap(GameMap gameMap) {
		this.gameMap = gameMap;
	}
}
