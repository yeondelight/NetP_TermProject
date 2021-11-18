package data;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

// �� ����ڿ��� �׸� Map ������ ��� GameMap
public class GameMap implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private static final int LEN = 23;		// �̷��� �� ���� ����
	private static final int ITEMNUM = 30;
	private static final int GHOSTNUM = 8;
		
	private int[][] map;
	private Vector<Point> item = new Vector<Point>();
	private Vector<Point> ghost = new Vector<Point>();
	private HashMap<String, Point> playerXY = new HashMap<String, Point>();
	private HashMap<String, Integer> playerScore = new HashMap<String, Integer>();
	
	public GameMap(Vector userList) {
		map = new Maze().generateMap();
		initItem(ITEMNUM);
		initGhost(GHOSTNUM);
		initPlayer(userList);
	}
	
	// ���� Getter
	public int[][] getMap(){
		return map;
	}
	public Vector<Point> getItem(){
		return item;
	}
	public Vector<Point> getGhost(){
		return ghost;
	}
	public HashMap<String, Point> getPlayerXY(){
		return playerXY;
	}
	
	// row, col�� ��ǥ�� ���� ������ ������ �˻��ϴ� �Լ�
	public int getXY(int row, int col) {
		return map[col][row];
	}

	// ghost�� ��ġ�� �����ϴ� �Լ�
	private void initGhost(int ghostNum) {
		int count = 0;
		Random random = new Random();
		do {
			int x = random.nextInt(LEN);
			int y = random.nextInt(LEN);
			
			if((getXY(x,y)==0) && (ghost.contains(new Point(x,y))==false) 
					&& (item.contains(new Point(x, y)) == false)) {
				ghost.add(new Point(x,y));
				count++;
			}
		}while(count < ghostNum);
	}
	
	// item�� ��ġ�� �����ϴ� �Լ�
	private void initItem(int itemNum) {
		int count = 0;
		Random random = new Random();
		do {
			int x = random.nextInt(LEN);
			int y = random.nextInt(LEN);
			
			// item ��ġ �ߺ� ���� ó��
			if((getXY(x, y) == 0) && (item.contains(new Point(x, y)) == false)) {
				item.add(new Point(x, y));
				count++;
			}
		}while(count < itemNum);
	}
	
	private void initPlayer(Vector userList) {
		for (int i = 0; i < userList.size(); i++) {
			String userName = (String)userList.get(i);	// �� player�� ����
			playerXY.put(userName, new Point(20, 0));	// ������ġ ����
		}
	}

}