package client.room;

import java.util.Comparator;

import javax.swing.JLabel;

public class UserScore {
	
	private String userName;
	private int score;
	private JLabel nameLabel;
	private JLabel scoreLabel;
	
	public UserScore(String userName) {
		this.userName = userName;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public int getScore() {
		return score;
	}
	
	public JLabel getNameLabel() {
		return nameLabel;
	}
	
	public JLabel getScoreLabel() {
		return scoreLabel;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public void setNameLabel(JLabel nameLabel) {
		this.nameLabel = nameLabel;
	}
	
	public void setScoreLabel(JLabel scoreLabel) {
		this.scoreLabel = scoreLabel;
	}
}

// for sorting - 점수에 따라 내림차순 정렬
class UserScoreComparator implements Comparator {
	public int compare(Object arg1, Object arg2) {
		int score1 = ((UserScore)arg1).getScore();
		int score2 = ((UserScore)arg2).getScore();
		return score1 < score2 ? 1 : -1;
		
	}
}
