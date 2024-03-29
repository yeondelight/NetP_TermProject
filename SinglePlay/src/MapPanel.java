
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MapPanel extends JPanel{
	
	private final int ROWS = 21;	// map의 가로길이
	private final int COLS = 21;	// map의 세로길이
	private final int UNIT = 15;	// map의 한 칸의 길이 (pixel)
	
	private int[][] map;			// main map
	
	private boolean gameover = false;	// game 결과
	private PlayerKeyboardListener pListener;
	
	// item 정보
	private ArrayList<Point> items = new ArrayList<Point>();
	private ImageIcon iIcon = new ImageIcon("res/item.png");
	private Image item = iIcon.getImage();
	
	// player 정보
	private ImageIcon pIcon = new ImageIcon("res/smile.png");
	private Image player = pIcon.getImage();
	private int playerX = UNIT;
	private int playerY = 0;

	// num에 따라 그에 맞는 미로 Map을 만드는 생성자
	public MapPanel(int num, int itemNum, int width){
		//initMap(num);
		map = new Maze(10).generateMap();
		initItem(itemNum);
		
		pListener = new PlayerKeyboardListener();
		addKeyListener(pListener);
	
		setLayout(null);
		setPreferredSize(new Dimension(width, width));
		
		// Timer Thread 감시 : TIMEOVER가 되면 Thread를 강제 종료하고 reset시킨다.
		new Thread() {
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
						if(Timer.getStatus() == false) {
							gameOver();
							return;
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	// item의 위치를 결정하는 함수
	private void initItem(int itemNum) {
		int count = 0;
		Random random = new Random();
		do {
			int x = random.nextInt(ROWS);
			int y = random.nextInt(COLS);
			if(getXY(x, y) == 0) {
				items.add(new Point(x, y));
				count++;
			}
		}while(count < itemNum);
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
		for(int i = 0; i < items.size(); i++) {
			Point p = items.get(i);
			g.drawImage(item, p.x*UNIT, p.y*UNIT, UNIT, UNIT, this);
		}
		
		// player 그리기
		g.drawImage(player, playerX, playerY, UNIT, UNIT, this);
		
		// gameover 그리기
		if (gameover) {
			g.setColor(new Color(255, 255, 255));
			g.setFont(new Font("Arial", Font.ITALIC, 30));
			g.drawString(" GAME OVER!! ", 100, 200);
		}
		
	}
	
	// 게임이 종료된 후
	public void gameOver() {
		gameover = true;
		removeKeyListener(pListener);
		repaint();
	}
	
	// player를 움직이는 keyBoard callBack
	class PlayerKeyboardListener extends KeyAdapter{
		
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			
			System.out.println(playerX + ", " + playerY + ", " + keyCode);
			
			// 화살표의 방향에 따라 움직이기
			switch(keyCode) {
			case KeyEvent.VK_UP:
				if(getXY(playerX/UNIT, playerY/UNIT - 1) != 1 && playerY > 0)
					playerY -= UNIT;
				break;
			case KeyEvent.VK_DOWN:
				if(getXY(playerX/UNIT, playerY/UNIT + 1) != 1 && playerY < 400)
					playerY += UNIT;
				break;
			case KeyEvent.VK_LEFT:
				if(getXY(playerX/UNIT - 1, playerY/UNIT) != 1 && playerX > 0)
					playerX -= UNIT;
				break;
			case KeyEvent.VK_RIGHT:
				if(getXY(playerX/UNIT + 1, playerY/UNIT) != 1 && playerX < 400)
					playerX += UNIT;
				break;
			}
			
			// item을 먹으면 점수를 증가시킨다.
			for(int i = 0; i < items.size(); i++) {
				Point p = items.get(i);
				if(p.x*UNIT == playerX && p.y*UNIT == playerY) {
					items.remove(i);
					Score.addScore(10);
				}
			}
			
			repaint();
		}
	}

}