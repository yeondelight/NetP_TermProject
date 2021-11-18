package client.room;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import data.ChatMsg;
import data.GameMap;

public class MapPanel extends JPanel implements Serializable{

	private static final long serialVersionUID = 3L;
	
	private final int ROWS = 23;	// map税 亜稽掩戚
	private final int COLS = 23;	// map税 室稽掩戚
	private final int UNIT = 20;	// map税 廃 牒税 掩戚 (pixel)
	
	private static final String C_UPDGAME = "305";		// Client -> Server 崇送績 硝顕
	private static final String C_UPDSCORE = "307";		// Client -> Server 繊呪 痕井 硝顕
	
	private int[][] map;			// main map
	
	private boolean gameover = false;	// game 衣引
	private PlayerKeyboardListener pListener;
	
	// Server人税 搭重聖 是廃 parent
	private WaitingView parent;
	private int roomKey;
	
	// item 舛左
	private Vector<Point> item = new Vector<Point>();
	private ImageIcon iIcon = new ImageIcon("res/item.png");
	private Image itemImg = iIcon.getImage();

	// ghost 舛左
	private Vector<Point> ghost = new Vector<Point>();
	private ImageIcon gIcon = new ImageIcon("res/ghost.png");
	private Image ghostImg = gIcon.getImage();
	private GhostThread ghostThread = null;
	
	//bullet
	private Vector<Point> bullet_location = new Vector<Point>();
	private Point bullet = null;
	private ImageIcon bIcon = new ImageIcon("res/bullet.png");
	private Image bulletImg = bIcon.getImage();
	private BulletThread bulletThread = null;
	
	// player 舛左
	private ImageIcon pIcon = new ImageIcon("res/smile.png");
	private ImageIcon pIcon2 = new ImageIcon("res/smile2.png");
	private Image player = pIcon.getImage();
	private Image player2 = pIcon2.getImage();
	
	private String myName;
	private HashMap<String, Point> playerXY;
	
	// 戚馬 DoubleBuffering聖 是廃 坪球
	private Image panelImage;
	private Graphics graphics;
	private Graphics graphics2;

	// num拭 魚虞 益拭 限澗 耕稽 Map聖 幻球澗 持失切
	public MapPanel(WaitingView parent, GameMap gameMap, int roomKey, String myName){
		this.parent = parent;
		this.roomKey = roomKey;
		this.myName = myName;
		
		map = gameMap.getMap();
		item = gameMap.getItem();
		ghost = gameMap.getGhost();
		playerXY = gameMap.getPlayerXY();

		setLayout(null);
		setPreferredSize(new Dimension(460, 460));
		
		graphics = this.getGraphics();
		
		pListener = new PlayerKeyboardListener();
		addKeyListener(pListener);
		

		for(Point ghostI: ghost) { 
			ghostThread = new GhostThread(this, ghostI);
			ghostThread.start(); 
		}
	}
	
	// row, col税 疎妊拭 企背 掩昔走 混昔走 伊紫馬澗 敗呪
	public int getXY(int row, int col) {
		if (row < 0 || row > ROWS - 1 || col < 0 || col > COLS - 1)		// error 坦軒
			return 1;
		else
			return map[col][row];
	}
	
	// repaint()
	public void paint(Graphics g) {
		if (panelImage == null) {
			panelImage = createImage(this.getWidth(), this.getHeight());
			if(panelImage == null)
				System.out.println("PANELIMAGE CREATE ERROR!!");
			else
				graphics2 = panelImage.getGraphics();
		}
		update(g);
	}

	// 惟績 遭楳 雌伐 益軒奄
	public void update(Graphics g) {
		// map 益軒奄
		for(int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if (getXY(i, j) == 1)	graphics2.setColor(Color.DARK_GRAY);
				else 					graphics2.setColor(Color.LIGHT_GRAY);
				graphics2.fillRect(i*UNIT, j*UNIT, UNIT, UNIT);
			}
		}
		
		// item 益軒奄
		for(int i = 0; i < item.size(); i++) {
			Point p = item.get(i);
			graphics2.drawImage(itemImg, p.x*UNIT, p.y*UNIT, UNIT, UNIT, this);
		}
		
		// ghost 益軒奄
		for(int i = 0; i < ghost.size(); i++) {
			Point g1 = ghost.get(i);
			graphics2.drawImage(ghostImg, g1.x*UNIT, g1.y*UNIT, UNIT, UNIT, this);
		}		

		// player 益軒奄
		Iterator<String> it = playerXY.keySet().iterator();
		while(it.hasNext()) {
			String userName = it.next();
			Point coordinate = playerXY.get(userName);
			if(userName.equals(myName))		// 蟹 益軒奄
				graphics2.drawImage(player, coordinate.x, coordinate.y, UNIT, UNIT, this);
			else
				graphics2.drawImage(player2, coordinate.x, coordinate.y, UNIT, UNIT, this);
		}
		
		// bullet 益軒奄
		for(Point p: bullet_location) {
			graphics2.drawImage(bulletImg, p.x, p.y, this);
		}
		
		g.drawImage(panelImage, 0, 0, this);

	}
	
	// player研 崇送戚澗 keyBoard callBack
	class PlayerKeyboardListener extends KeyAdapter{
		public void keyPressed(KeyEvent e) {
			parent.SendObject(new ChatMsg(myName, C_UPDGAME, roomKey+"", e.getKeyCode()));
		}
	}
	
	// Server稽採斗 誓岩聖 閤焼 叔霜旋生稽 戚坤闘研 坦軒馬澗 採歳
	public void doKeyEvent(String userName, int keyCode) {
		Point coordinate = playerXY.get(userName);
		System.out.println(userName + " :: " + coordinate.x + ", " + coordinate.y + ", " + keyCode);
		
		// 鉢詞妊税 号狽拭 魚虞 崇送戚奄
		switch(keyCode) {
		case KeyEvent.VK_UP:
			if(getXY(coordinate.x/UNIT, coordinate.y/UNIT - 1) != 1 && coordinate.y > 0)
				coordinate.y -= UNIT;
			break;
		case KeyEvent.VK_DOWN:
			if(getXY(coordinate.x/UNIT, coordinate.y/UNIT + 1) != 1 && coordinate.y < 460)
				coordinate.y += UNIT;
			break;
		case KeyEvent.VK_LEFT:
			if(getXY(coordinate.x/UNIT - 1, coordinate.y/UNIT) != 1 && coordinate.x > 0)
				coordinate.x -= UNIT;
			break;
		case KeyEvent.VK_RIGHT:
			if(getXY(coordinate.x/UNIT + 1, coordinate.y/UNIT) != 1 && coordinate.x < 460)
				coordinate.x += UNIT;
			break;
			
		// bullet
		case KeyEvent.VK_W:
			bullet = new Point(coordinate.x, coordinate.y);
			bulletThread = new BulletThread(this, bullet, 0, -1);
			bulletThread.start();
			break;
		case KeyEvent.VK_S:
			bullet = new Point(coordinate.x, coordinate.y);
			bulletThread = new BulletThread(this, bullet, 0, 1);
			bulletThread.start();
			break;
		case KeyEvent.VK_A:
			bullet = new Point(coordinate.x, coordinate.y);
			bulletThread = new BulletThread(this, bullet, -1, 0);
			bulletThread.start();
			break;
		case KeyEvent.VK_D:
			bullet = new Point(coordinate.x, coordinate.y);
			bulletThread = new BulletThread(this, bullet, 1, 0);
			bulletThread.start();
			break;
		}
		
		// item聖 股生檎 繊呪研 装亜獣轍陥.
		for(int i = 0; i < item.size(); i++) {
			Point p = item.get(i);
			if(p.x*UNIT == coordinate.x && p.y*UNIT == coordinate.y) {
				item.remove(i);
				if(userName.equals(myName))
					parent.SendObject(new ChatMsg(myName, C_UPDSCORE, roomKey + " " + "+", 10));
			}
		}
		
		// 陥獣 煽舌
		playerXY.put(userName, coordinate);
		
		repaint();
	}

	// bullet税 是帖 痕敗 昔走 + repaint
	public synchronized void bulletRepaint(int x, int y) {
		bullet_location.add(new Point(x,y));
		
		// 陥献 player人 bullet戚 限精 井酔
		Iterator<String> it = playerXY.keySet().iterator();
		while(it.hasNext()) {
			String userName = it.next();
			Point coordinate = playerXY.get(userName);
			if(!userName.equals(myName)) {
				if(coordinate.x == x && coordinate.y == y) {
					parent.SendObject(new ChatMsg(userName, C_UPDSCORE, roomKey + " " + "-", 5));
				}
			}
		}
				
		repaint();
	}
	public synchronized void bulletRemove(int x, int y) {
		bullet_location.remove(new Point(x,y));
		repaint();
	}

	public synchronized boolean checkLocationWithBullet(Point point) {
		//bullet引 是帖亜 旭陥檎
		for(Point bullet : bullet_location) {
			if(bullet.x == point.x*UNIT && bullet.y == point.y*UNIT) {
				return true;
			}
		}
		return false;
	}
	public synchronized boolean checkLocationWithUser(Point point) {
		// 蟹人 是帖亜 旭生檎 繊呪縁奄 // 辞獄拭惟 限製聖 硝鍵陥
		Iterator<String> it = playerXY.keySet().iterator();
		while(it.hasNext()) {
			String userName = it.next();
			Point coordinate = playerXY.get(userName);
			if(userName.equals(myName)) {
				if(coordinate.x == point.x*UNIT && coordinate.y == point.y*UNIT) {
					parent.SendObject(new ChatMsg(userName, C_UPDSCORE, roomKey + " " + "-", 5));
					return true;
				}
			}
		}
		return false;
	}
	public synchronized void removeGhost(Point point) {
		// 什傾球 蒸嬢走壱, 壱什闘 困斗拭亀 紫虞走壱
		ghost.remove(point);
		repaint();
	}

}

class BulletThread extends Thread {
	private final int UNIT = 20;	// map税 廃 牒税 掩戚 (pixel)
	private MapPanel mapPanel = null;
	private Point bullet = null;
	private int change_x;
	private int change_y;
	
	public BulletThread(MapPanel mapPanel, Point bullet, int change_x, int change_y) {
		this.mapPanel = mapPanel;
		this.bullet = bullet;
		this.change_x=change_x;
		this.change_y=change_y;
	}
	
	public void run() {
		while(true) {
			if(change_y < 0) {
				if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) != 1 && bullet.y > 0) {
					bullet.y -= UNIT;
					mapPanel.bulletRepaint(bullet.x, bullet.y);
				} 
				else if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) == 1) {
					mapPanel.bulletRemove(bullet.x, bullet.y);
					return;
				}
			}
			else if(change_y > 0) {
				if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) != 1 && bullet.y < 460) {
					bullet.y += UNIT;
					mapPanel.bulletRepaint(bullet.x, bullet.y);
				} 
				else if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) == 1) {
					mapPanel.bulletRemove(bullet.x, bullet.y);
					return;
				}
			}
			else if(change_x < 0) {
				if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) != 1 && bullet.x > 0) {
					bullet.x -= UNIT;
					mapPanel.bulletRepaint(bullet.x, bullet.y);
				} 
				else if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) == 1) {
					mapPanel.bulletRemove(bullet.x, bullet.y);
					return;
				}
			}
			else if(change_x > 0) {
				if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) != 1 && bullet.x < 460) {
					bullet.x += UNIT;
					mapPanel.bulletRepaint(bullet.x, bullet.y);
				} 
				else if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) == 1) {
					mapPanel.bulletRemove(bullet.x, bullet.y);
					return;
				}
			}
			 
			try { sleep(20); } catch (InterruptedException e) {e.printStackTrace();}
			mapPanel.bulletRemove(bullet.x, bullet.y);
		}
	 } // end of run
	
}// end of BulletThread

class GhostThread extends Thread {
	private final int UNIT = 20;	// map税 廃 牒税 掩戚 (pixel)
	private MapPanel mapPanel = null;
	private Point ghost = null;
	private boolean goUp = false;
	private boolean goDown = false;
	private boolean goLeft = false;
	private boolean goRight = false;

	public GhostThread(MapPanel mapPanel, Point ghost) {
		this.mapPanel = mapPanel;
		this.ghost = ghost;
		movingdirection(ghost);
	}
	
	private void movingdirection(Point ghost) {
		// ghost亜 紫号生稽 陥 哀 呪 赤聖凶澗..........嬢胸惟 拝猿推......
		if(mapPanel.getXY(ghost.x, ghost.y - 1)!=1 && ghost.y > 0) { // ghost亜 是稽 哀 凶 混戚 蒸生檎 -> 是焼掘稽 戚疑
			goUp = true;
			return;
		}
		else if (mapPanel.getXY(ghost.x, ghost.y + 1)!=1 && ghost.y < 460) { // ghost亜 焼掘稽 哀 凶 混戚 蒸生檎 -> 是焼掘稽 戚疑
			goDown = true;
			return;
		}
		else if (mapPanel.getXY(ghost.x - 1, ghost.y)!=1 && ghost.x > 0) { // ghost亜 図楕生稽 哀 凶 混戚 蒸生檎 -> 新生稽 戚疑
			goLeft = true;
			return;
		}
		else if (mapPanel.getXY(ghost.x + 1, ghost.y)!=1 && ghost.x < 460) {
			goRight = true;
			return;
		}
	}

	@Override
	public void run() {
		while(true) {
			if(mapPanel.checkLocationWithBullet(ghost)) { // bullet引 是帖亜 旭陥檎	// 戚暗 設 照限焼ばばばばばば
				mapPanel.removeGhost(ghost); // ghostThread 曽戟, ghost 困斗亀 remove
				return;
			}
			else if(mapPanel.checkLocationWithUser(ghost)){ // 紫遂切人 是帖亜 旭陥檎 繊呪縁奄				
				try { sleep(800); } catch (InterruptedException e) {e.printStackTrace();}
				continue;
			}
			else // 繕闇拭 限走 省澗陥檎 崇送食虞
				moveGhost(ghost);
			
			try { sleep(200); } catch (InterruptedException e) {e.printStackTrace();}
		}
	}

	private void moveGhost(Point point) {
		if(goUp) {
			if(mapPanel.getXY(ghost.x, ghost.y - 1)==1 || ghost.y <= 0) {
				goUp = false;
				goDown = true;
				return;
			}
			ghost.y -= 1;
		}
		else if(goDown) {
			if(mapPanel.getXY(ghost.x, ghost.y + 1)==1 || ghost.y >= 460) {
				goDown = false;
				goUp = true;
				return;
			}
			ghost.y += 1;
		}
		else if(goLeft) {
			if(mapPanel.getXY(ghost.x - 1, ghost.y)==1 || ghost.x <= 0) {
				goLeft = false;
				goRight = true;
				return;
			}
			ghost.x -= 1;
		}
		else if(goRight) {
			if(mapPanel.getXY(ghost.x + 1, ghost.y)==1 || ghost.x >= 460) {
				goRight = false;
				goLeft = true;
				return;
			}
			ghost.x += 1;
		}
		mapPanel.repaint();
	}
	
}// end of GhostThread