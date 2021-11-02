package client.room;

import java.util.HashMap;
import java.util.Set;

import data.GameRoom;

public class RoomManager {
	
	// Room에 대한 변수 설정
	private static HashMap<Integer, GameRoom> rooms = new HashMap<Integer, GameRoom>();

	
	public int getSize() {
		return rooms.size();
	}
	
	public Set getRoomKeys() {
		return rooms.keySet();
	}
	
	public GameRoom getRoom(int key) {
		return rooms.get(key);
	}
	
	public void addRoom(GameRoom room) {
		int key = room.getKey();
		rooms.put(key, room);
	}
	
	public static void delRoom(GameRoom gameRoom) {
		rooms.remove(gameRoom);
	}
}
