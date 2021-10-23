
public class MainApp {
	
	public static void main(String[] args) {
		try {
			GameView frame = new GameView();
			frame.setVisible(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
