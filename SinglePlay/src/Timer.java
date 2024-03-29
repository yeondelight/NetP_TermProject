import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

public class Timer extends Thread{
	
	private JLabel timerLabel;		// timer를 표시할 JLabel
	private int timeout = 2000;		// default : 60sec
	private static boolean timerStatus;	// timer의 작동 상태 확인
	
	private int r = 240;
	private int g = 200;
	private int b = 200;
	
	public Timer(JLabel timerLabel, int height) {
		this.timerLabel = timerLabel;
		timerLabel.setOpaque(true);		// 배경색을 설정하기 위함
		timerLabel.setBackground(new Color(r, g, b));
		timerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		timerLabel.setPreferredSize(new Dimension(190, height));	// 폭 고정
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
		timerStatus = false;
	}
	
	public static boolean getStatus() {
		return timerStatus;
	}
	
	public void update() {
		timerLabel.setText(" 남은 시간 : " + timeout + " 초 ");
		// 남은 시간이 촉박할수록 배경색이 true RED에 가까워진다.
		if(timeout<=10)
			timerLabel.setBackground(new Color(r, g-(10-timeout)*15, b-(10-timeout)*15));
	}
	
	public void run() {
		timerStatus = true;		// timer 작동
		update();
		while(true) {
			try {
				timeout--;
				Thread.sleep(1000);
				update();
				
				// timeout 설정
				if(timeout <= 0) {
					timerStatus = false;		// timer 멈춤
					return;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
