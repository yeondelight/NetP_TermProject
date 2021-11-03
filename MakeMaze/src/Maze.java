import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Maze extends JFrame implements Runnable{
 
	Thread t = new Thread(this);
 
	static final int WINDOWSIZE = 500;  // Size of Applet window
	static final int N = 10;    // Number of rooms in a row
  
	static final int MAPSIZE = N * 2 + 1;    // Don't change
	static final int BSIZE = (int)(WINDOWSIZE/MAPSIZE); // Don't change
 
	static block [][]map = new block[MAPSIZE][MAPSIZE];
 	static int [][]m = new int[MAPSIZE][MAPSIZE];
 	ArrayList<block> list = new ArrayList<block>();

 	public Maze(){
 		init();
 		
		setSize(MAPSIZE*BSIZE,MAPSIZE*BSIZE);
		setVisible(true);
 	}
 
	public void init(){
		for(int i=0;i<MAPSIZE;i++){
				for(int j=0;j<MAPSIZE;j++){
					map[i][j] = new block(i, j, false);
				}
		}
  
		generateMap();
	}
 
	public static void main(String[] args){
		new Maze();
	}
 
	public void run(){
		while(true){
   
		}
	} 

 
	public void generateMap(){
		int randX = (int)(Math.random()*N)*2+1;
		int randY = (int)(Math.random()*N)*2+1;
		map[randY][randX].visited = true;
  
		System.out.println(randX + "  " + randY + "  " + map.length + " " + N);
  
		if(randX-1 != 0){
			list.add(map[randY][randX-1]);
		}
		if(randX+1 != MAPSIZE){
			list.add(map[randY][randX+1]);
		}
		if(randY-1 != 0){
			list.add(map[randY-1][randX]);
		}
		if(randY+1 != MAPSIZE){
			list.add(map[randY+1][randX]);
		}
  
  
		while(!list.isEmpty()){
			int index = (int)(Math.random()*list.size());
   
			block wall = list.get(index);
   
			if(wall.y%2==1){ // 2nd row
				if(wall.x-1 != 0 && map[wall.y][wall.x-1].visited == false){
					map[wall.y][wall.x-1].visited = true;
					map[wall.y][wall.x].visited = true;
     
					if(wall.x-2 != 0){
						list.add(map[wall.y][wall.x-2]);
					}
					if(wall.y-1 != 0){
						list.add(map[wall.y-1][wall.x-1]);
					}
					if(wall.y+1 != 0){
						list.add(map[wall.y+1][wall.x-1]);
					}
				}else
					if(wall.x+1 <= MAPSIZE-1 && map[wall.y][wall.x+1].visited == false){
						map[wall.y][wall.x+1].visited = true;
						map[wall.y][wall.x].visited = true;
     
						if(wall.x+2 != 0){
							list.add(map[wall.y][wall.x+2]);
						}
						if(wall.y-1 != 0){
							list.add(map[wall.y-1][wall.x+1]);
						}
						if(wall.y+1 != 0){
							list.add(map[wall.y+1][wall.x+1]);
						}
					}
			}else{  // 3rd row
				if(wall.y-1 != 0 && map[wall.y-1][wall.x].visited == false){
					map[wall.y-1][wall.x].visited = true;
					map[wall.y][wall.x].visited = true;
     
					if(wall.y-2 != 0){
						list.add(map[wall.y-2][wall.x]);
					}
					if(wall.x-1 != 0){
						list.add(map[wall.y-1][wall.x-1]);
					}
					if(wall.x+1 != 0){
						list.add(map[wall.y-1][wall.x+1]);
					}
				}else
					if(wall.y+1 <= MAPSIZE-1 && map[wall.y+1][wall.x].visited == false){
						map[wall.y+1][wall.x].visited = true;
						map[wall.y][wall.x].visited = true;
     
						if(wall.y+2 != 0){
							list.add(map[wall.y+2][wall.x]);
						}
						if(wall.x-1 != 0){
							list.add(map[wall.y+1][wall.x-1]);
						}
						if(wall.x+1 != 0){
							list.add(map[wall.y+1][wall.x+1]);
						}
					}
			}
   
			list.remove(index);
   
		}
  
		for(int i=0;i<MAPSIZE;i++){
			for(int j=0;j<MAPSIZE;j++){
				if(map[i][j].visited == false){
					if(i%2 == 1){
						if(j%2 == 0){
							m[i][j] = 1;
						}
					}else{
						m[i][j] = 1;
					}
				}
			}
		}
		for(int i=0;i<MAPSIZE;i++){
			m[i][MAPSIZE-1] = 1;
			m[i][0] = 1;
   
			m[MAPSIZE-1][i] = 1;
			m[0][i] = 1;
		}
  
		m[0][1] = 0;
		m[MAPSIZE-1][MAPSIZE-2] = 0;
  
		for(int i=0;i<MAPSIZE;i++){
			for(int j=0;j<MAPSIZE;j++){
				System.out.print(m[i][j] + " ");
			}
			System.out.println();
		}
	}
 
	public void paint(Graphics g){
		for(int i=0;i<MAPSIZE;i++){
			for(int j=0;j<MAPSIZE;j++){
				if(m[i][j] == 1) drawBlock(g, j, i);
			}
		}
	}
 
	public void drawBlock(Graphics g, int x, int y){
		g.setColor(Color.black);
  
		Color t = new Color(100,100,50);
		g.setColor(t);
		g.fillRect(x*BSIZE, y*BSIZE, BSIZE, BSIZE);
	}
}


class block{
	int x;
	int y;
	boolean visited;
	public block(int y, int x, boolean visited){
		this.x = x;
		this.y = y;
		this.visited = visited;
	}
	public String toString(){
		return "x = "+x+"  y = "+y+"  visit = "+visited+"\n";
	}
}

/*
 *  Pick Random_Cell
 RandomCell->Visited=true;
 wallList Add(Random_Cell's Surrounding_walls)
 while( wallList Not Empty) loop
 
     wall = wallList->getRandomWall();
     if(wall has unvisited cell on either side)
     {
       unvisited cell = visited;
       wallList Add(wall->newly_visited_cell's- Surrounding_walls);
     }
     Remove wall from wallList;
 }
 */