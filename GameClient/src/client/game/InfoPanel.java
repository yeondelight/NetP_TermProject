package client.game;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class InfoPanel extends JPanel{
	
	private final int HEIGHT = 30;
	
	private JLabel timerLabel = new JLabel();
	private JLabel scoreLabel = new JLabel();
	
	private Timer timer = new Timer(timerLabel, HEIGHT);
	private Score score = new Score(scoreLabel, HEIGHT);

	// num에 따라 그에 맞는 미로 Map을 만드는 생성자
	public InfoPanel(int width){
		setLayout(new FlowLayout());
		System.out.println(timerLabel.getHeight()+"");
		setPreferredSize(new Dimension((int)width, HEIGHT + 5));
		
		// Label 붙이기
		add(timerLabel);
		add(scoreLabel);
		
		// 작동 설정
		timer.start();
	}
}
