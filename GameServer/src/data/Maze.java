package data;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Maze{
 
	private static int n = 11;   		// Number of rooms in a row
	private static int mapSize;
 
	private static block[][] map;
 	private static int[][] m;
 	private ArrayList<block> list = new ArrayList<block>();

 	public Maze(){
 		mapSize = n * 2 + 1;
 		map = new block[mapSize][mapSize];
 		m = new int[mapSize][mapSize];
 		
		for(int i=0;i<mapSize;i++){
			for(int j=0;j<mapSize;j++){
				map[i][j] = new block(i, j, false);
			}
		}
 	}
	public int[][] generateMap(){
		int randX = (int)(Math.random()*n)*2+1;
		int randY = (int)(Math.random()*n)*2+1;
		map[randY][randX].visited = true;
  
		System.out.println("GENERATE MAP :: " + randX + "  " + randY + "  " + map.length + " " + n);
  
		if(randX-1 != 0){
			list.add(map[randY][randX-1]);
		}
		if(randX+1 != mapSize){
			list.add(map[randY][randX+1]);
		}
		if(randY-1 != 0){
			list.add(map[randY-1][randX]);
		}
		if(randY+1 != mapSize){
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
					if(wall.x+1 <= mapSize-1 && map[wall.y][wall.x+1].visited == false){
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
					if(wall.y+1 <= mapSize-1 && map[wall.y+1][wall.x].visited == false){
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
  
		for(int i=0;i<mapSize;i++){
			for(int j=0;j<mapSize;j++){
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
		for(int i=0;i<mapSize;i++){
			m[i][mapSize-1] = 1;
			m[i][0] = 1;
   
			m[mapSize-1][i] = 1;
			m[0][i] = 1;
		}
  
		m[0][1] = 0;
		m[mapSize-1][mapSize-2] = 0;
  
		//for(int i=0;i<mapSize;i++){
		//	for(int j=0;j<mapSize;j++){
		//		System.out.print(m[i][j] + " ");
		//	}
		//	System.out.println();
		//}
		return m;
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