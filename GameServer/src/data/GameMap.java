package data;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

// 각 사용자에게 그릴 Map 정보를 담는 GameMap
public class GameMap implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private static final int LEN = 23;		// 미로의 한 변의 길이
	private static final int ITEMNUM = 10;
		
	private int[][] map;
	private Vector<Point> item = new Vector<Point>();
	private HashMap<String, Point> playerXY = new HashMap<String, Point>();
	private HashMap<String, Integer> playerScore = new HashMap<String, Integer>();
	
	public GameMap(Vector userList) {
		map = new Maze().generateMap();
		initItem(ITEMNUM);
		initPlayer(userList);
	}
	
	// 이하 Getter
	public int[][] getMap(){
		return map;
	}
	public Vector<Point> getItem(){
		return item;
	}
	public HashMap<String, Point> getPlayerXY(){
		return playerXY;
	}
	
	// row, col의 좌표에 대해 길인지 벽인지 검사하는 함수
	public int getXY(int row, int col) {
		return map[col][row];
	}
	
	// item의 위치를 결정하는 함수
	private void initItem(int itemNum) {
		int count = 0;
		Random random = new Random();
		do {
			int x = random.nextInt(LEN);
			int y = random.nextInt(LEN);
			// item 위치 중복 방지 처리
			if((getXY(x, y) == 0) && (item.contains(new Point(x, y)) == false)) {
				item.add(new Point(x, y));
				count++;
			}
		}while(count < itemNum);
	}
	
	private void initPlayer(Vector userList) {
		for (int i = 0; i < userList.size(); i++) {
			String userName = (String)userList.get(i);	// 각 player에 대해
			playerScore.put(userName, 0);				// 점수를 0으로 reset하고
			playerXY.put(userName, new Point(0, 1));	// 시작 위치를 설정한다. : ERROR 발생 - 0, 1 맞는데 왜 인식 안돼?
		}
	}

}