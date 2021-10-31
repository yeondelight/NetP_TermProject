package client.game;

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
	
	private final int MAXMAP = 2;	// map�� �� ����
	private final int ROWS = 20;	// map�� ���α���
	private final int COLS = 20;	// map�� ���α���
	private final int UNIT = 20;	// map�� �� ĭ�� ���� (pixel)
	
	private int[][] map;			// main map
	
	private boolean gameover = false;	// game ���
	private PlayerKeyboardListener pListener;
	
	// item ����
	private ArrayList<Point> items = new ArrayList<Point>();
	private ImageIcon iIcon = new ImageIcon("res/item.png");
	private Image item = iIcon.getImage();
	
	// player ����
	private ImageIcon pIcon = new ImageIcon("res/smile.png");
	private Image player = pIcon.getImage();
	private int playerX = UNIT;
	private int playerY = 0;

	// num�� ���� �׿� �´� �̷� Map�� ����� ������
	public MapPanel(int num, int itemNum, int width){
		initMap(num);
		initItem(itemNum);
		
		pListener = new PlayerKeyboardListener();
		addKeyListener(pListener);
	
		setLayout(null);
		setPreferredSize(new Dimension(width, width));
		
		// Timer Thread ���� : TIMEOVER�� �Ǹ� Thread�� ���� �����ϰ� reset��Ų��.
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
	
	// map1, map2�� ��Ÿ�� map�� �����ϴ� �Լ�
	private void initMap(int num) {
		int [][] map1 = 
			 {
				{1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
				{1,0,1,0,0,0,1,0,0,1,1,1,1,0,0,0,0,1,1,1},
				{1,0,1,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,1,1},
				{1,0,1,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,1},
				{1,0,1,1,1,1,0,0,1,0,1,0,0,0,0,0,0,0,0,1},
				{1,0,1,0,0,1,0,0,1,0,1,0,1,0,1,1,1,1,1,1},
				{1,0,1,0,0,1,0,0,1,0,1,0,1,0,1,0,0,0,0,1},
				{1,0,1,0,0,1,0,0,1,0,1,0,1,1,1,0,0,1,0,1},
				{1,0,1,1,0,1,1,0,0,0,1,0,0,0,0,0,0,1,0,1},
				{1,0,0,0,0,0,1,0,1,0,1,1,1,1,1,1,1,1,0,1},
				{1,1,0,0,1,1,1,0,1,1,0,0,1,0,0,0,0,1,0,1},
				{1,1,1,0,0,0,1,0,0,0,0,1,1,1,0,1,0,1,0,5},
				{1,0,1,1,0,0,1,1,1,1,0,1,0,0,0,1,0,1,1,1},
				{1,0,0,1,0,0,1,0,0,1,0,1,0,0,0,1,0,0,0,1},
				{1,0,1,1,0,0,1,0,0,1,0,1,1,1,1,1,1,0,1,1},
				{1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,1},
				{1,1,0,1,1,1,1,1,1,1,0,0,0,1,1,1,1,0,1,1},
				{1,1,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,1,1},
				{1,1,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
			};
		
		int [][] map2 = 
			{
				{1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
				{1,0,0,1,0,0,0,0,0,0,1,0,0,0,1,1,0,0,0,1},
				{1,0,1,1,1,1,1,0,1,1,1,0,1,0,0,0,0,1,0,1},
				{1,0,0,0,0,0,1,0,0,0,0,0,1,1,1,1,1,1,0,1},
				{1,0,1,1,0,0,1,0,1,1,1,1,1,0,0,0,0,0,0,1},
				{1,0,1,0,0,1,1,0,0,0,0,0,1,1,0,0,1,1,1,1},
				{1,0,1,0,0,0,0,0,0,1,1,0,0,0,1,0,1,0,0,1},
				{1,0,1,1,1,1,1,1,0,1,1,1,1,0,1,0,0,0,0,1},
				{1,0,1,0,0,1,0,1,0,1,0,0,0,0,1,1,0,1,1,1},
				{1,0,0,0,0,0,0,1,0,1,0,0,1,1,1,1,0,0,1,1},
				{1,0,1,1,1,1,0,1,0,1,0,0,0,0,0,0,1,0,0,1},
				{1,0,1,0,0,1,0,1,0,1,1,1,1,1,0,1,1,1,0,5},
				{1,0,1,0,1,1,0,1,0,0,0,1,0,1,0,0,0,1,0,1},
				{1,0,1,0,0,0,0,1,0,1,0,1,0,0,1,1,0,1,1,1},
				{1,0,1,1,1,0,0,1,0,1,0,0,0,0,0,1,0,0,1,1},
				{1,0,0,0,1,1,1,1,0,1,1,1,0,1,1,1,1,0,1,1},
				{1,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,1},
				{1,0,1,1,1,1,0,0,0,1,0,1,1,1,1,1,1,1,1,1},
				{1,0,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,1},
				{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
			};
		
		if (num % 2 == 1)	map = map1;
		else				map = map2;
	}
	
	// item�� ��ġ�� �����ϴ� �Լ�
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
	
	// row, col�� ��ǥ�� ���� ������ ������ �˻��ϴ� �Լ�
	public int getXY(int row, int col) {
		return map[col][row];
	}

	// ���� ���� ��Ȳ �׸���
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// map �׸���
		for(int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if (getXY(i, j) == 1)	g.setColor(Color.DARK_GRAY);
				else 					g.setColor(Color.LIGHT_GRAY);
				g.fillRect(i*UNIT, j*UNIT, UNIT, UNIT);
			}
		}
		
		// item �׸���
		for(int i = 0; i < items.size(); i++) {
			Point p = items.get(i);
			g.drawImage(item, p.x*UNIT, p.y*UNIT, UNIT, UNIT, this);
		}
		
		// player �׸���
		g.drawImage(player, playerX, playerY, UNIT, UNIT, this);
		
		// gameover �׸���
		if (gameover) {
			g.setColor(new Color(255, 255, 255));
			g.setFont(new Font("Arial", Font.ITALIC, 30));
			g.drawString(" GAME OVER!! ", 100, 200);
		}
		
	}
	
	// ������ ����� ��
	public void gameOver() {
		gameover = true;
		removeKeyListener(pListener);
		repaint();
	}
	
	// player�� �����̴� keyBoard callBack
	class PlayerKeyboardListener extends KeyAdapter{
		
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			
			System.out.println(playerX + ", " + playerY + ", " + keyCode);
			
			// ȭ��ǥ�� ���⿡ ���� �����̱�
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
			
			// item�� ������ ������ ������Ų��.
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