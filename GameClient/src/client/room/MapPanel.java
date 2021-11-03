package client.room;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import data.GameMap;

public class MapPanel extends JPanel implements Serializable{

	private static final long serialVersionUID = 3L;
	
	private final int ROWS = 23;	// map의 가로길이
	private final int COLS = 23;	// map의 세로길이
	private final int UNIT = 20;	// map의 한 칸의 길이 (pixel)
	
	private int[][] map;			// main map
	
	private boolean gameover = false;	// game 결과
	private PlayerKeyboardListener pListener;
	
	// item 정보
	private Vector<Point> item = new Vector<Point>();
	private ImageIcon iIcon = new ImageIcon("res/item.png");
	private Image itemImg = iIcon.getImage();
	
	// player 정보
	private ImageIcon pIcon = new ImageIcon("res/smile.png");
	private Image player = pIcon.getImage();
	private String myName;
	private Point myXY;
	private HashMap<String, Point> playerXY;

	// num에 따라 그에 맞는 미로 Map을 만드는 생성자
	public MapPanel(GameMap gameMap, String myName){
		map = gameMap.getMap();
		item = gameMap.getItem();
		playerXY = gameMap.getPlayerXY();
		
		myXY = playerXY.get(myName);
		
		pListener = new PlayerKeyboardListener();
		addKeyListener(pListener);
	
		setLayout(null);
		setPreferredSize(new Dimension(460, 460));

	}
	
	// row, col의 좌표에 대해 길인지 벽인지 검사하는 함수
	public int getXY(int row, int col) {
		return map[col][row];
	}

	// 게임 진행 상황 그리기
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// map 그리기
		for(int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if (getXY(i, j) == 1)	g.setColor(Color.DARK_GRAY);
				else 					g.setColor(Color.LIGHT_GRAY);
				g.fillRect(i*UNIT, j*UNIT, UNIT, UNIT);
			}
		}
		
		// item 그리기
		for(int i = 0; i < item.size(); i++) {
			Point p = item.get(i);
			g.drawImage(itemImg, p.x*UNIT, p.y*UNIT, UNIT, UNIT, this);
		}
		
		// player 그리기
		g.drawImage(player, myXY.x, myXY.y, UNIT, UNIT, this);

	}
	
	// player를 움직이는 keyBoard callBack
	class PlayerKeyboardListener extends KeyAdapter{
		
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			
			System.out.println(myXY.x + ", " + myXY.y + ", " + keyCode);
			
			// 화살표의 방향에 따라 움직이기
			switch(keyCode) {
			case KeyEvent.VK_UP:
				if(getXY(myXY.x/UNIT, myXY.y/UNIT - 1) != 1 && myXY.y > 0)
					myXY.y -= UNIT;
				break;
			case KeyEvent.VK_DOWN:
				if(getXY(myXY.x/UNIT, myXY.y/UNIT + 1) != 1 && myXY.y < 460)
					myXY.y += UNIT;
				break;
			case KeyEvent.VK_LEFT:
				if(getXY(myXY.x/UNIT - 1, myXY.y/UNIT) != 1 && myXY.x > 0)
					myXY.x -= UNIT;
				break;
			case KeyEvent.VK_RIGHT:
				if(getXY(myXY.x/UNIT + 1, myXY.y/UNIT) != 1 && myXY.x < 460)
					myXY.x += UNIT;
				break;
			}
			
			// item을 먹으면 점수를 증가시킨다.
			for(int i = 0; i < item.size(); i++) {
				Point p = item.get(i);
				if(p.x*UNIT == myXY.x && p.y*UNIT == myXY.y) {
					item.remove(i);
					//Score.addScore(10);
				}
			}
			
			repaint();
		}
	}

}