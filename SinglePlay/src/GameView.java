import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;

public class GameView extends JFrame{
	
	private final int MAPNUM = 1;		// 임시로 1로 설정 : 방마다 다르게 해보기
	private final int ITEMS = 5;		// map에서 나타날 아이템의 수
	
	private final int WIDTH = 420;		// component의 가로 갈이를 맞추기 위해 설정
	
	private Container contentPane;
	private MapPanel mapPanel;			// map이 나타나는 Panel
	private InfoPanel infoPanel;		// Timer와 Score가 나타나는 Panel
	
	// JFrame 생성 : Swing Frame
	public GameView() {
		// 기본 설정
		setTitle("SinglePlay Test");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(5, 5));
		
		// Map 붙이기
		mapPanel = new MapPanel(MAPNUM, ITEMS, WIDTH);
		contentPane.add(mapPanel, BorderLayout.CENTER);
		
		// Timer Thread, Score 붙이기
		infoPanel = new InfoPanel(WIDTH);
		contentPane.add(infoPanel, BorderLayout.NORTH);
	

		// Frame 크기 설정
		setSize(WIDTH, 500);
		setResizable(false);
		setVisible(true);
		
		// focus 지정
		mapPanel.setFocusable(true);
		mapPanel.requestFocus();
	}

}
