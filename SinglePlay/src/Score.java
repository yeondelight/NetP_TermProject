import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

public class Score{
	
	private static JLabel scoreLabel;			// score 표시할 JLabel
	private static int score = 0;
	
	private int r = 200;
	private int g = 200;
	private int b = 240;
	
	public Score(JLabel scoreLabel, int height) {
		this.scoreLabel = scoreLabel;
		scoreLabel.setOpaque(true);						// 배경색을 설정하기 위함
		scoreLabel.setBackground(new Color(r, g, b));
		scoreLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		scoreLabel.setPreferredSize(new Dimension(190, height));	// 폭 크기 고정
		update();
	}
	
	// 점수 update
	public static void update() {
		scoreLabel.setText(" 현재 점수 : " + score + " 점 ");
	}

	public static void addScore(int scores) {
		score += scores;
		update();
	}
	
	public static void delScore(int scores) {
		// 최저 점수는 0점이다.
		if (score - scores <= 0)
			score = 0;
		else
			score -= scores;
		update();
	}
}
