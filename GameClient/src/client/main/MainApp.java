package client.main;

import client.wait.WaitingView;

public class MainApp {
	
	public static void main(String[] args) {
		try {
			//GameView frame = new GameView();
			WaitingView frame = new WaitingView();
			frame.setVisible(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
