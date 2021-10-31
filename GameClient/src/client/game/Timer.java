package client.game;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

public class Timer extends Thread{
	
	private JLabel timerLabel;		// timer�� ǥ���� JLabel
	private int timeout = 20;		// default : 60sec
	private static boolean timerStatus;	// timer�� �۵� ���� Ȯ��
	
	private int r = 240;
	private int g = 200;
	private int b = 200;
	
	public Timer(JLabel timerLabel, int height) {
		this.timerLabel = timerLabel;
		timerLabel.setOpaque(true);		// ������ �����ϱ� ����
		timerLabel.setBackground(new Color(r, g, b));
		timerLabel.setFont(new Font("���� ����", Font.BOLD, 20));
		timerLabel.setPreferredSize(new Dimension(190, height));	// �� ����
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
		timerStatus = false;
	}
	
	public static boolean getStatus() {
		return timerStatus;
	}
	
	public void update() {
		timerLabel.setText(" ���� �ð� : " + timeout + " �� ");
		// ���� �ð��� �˹��Ҽ��� ������ true RED�� ���������.
		if(timeout<=10)
			timerLabel.setBackground(new Color(r, g-(10-timeout)*15, b-(10-timeout)*15));
	}
	
	public void run() {
		timerStatus = true;		// timer �۵�
		update();
		while(true) {
			try {
				timeout--;
				Thread.sleep(1000);
				update();
				
				// timeout ����
				if(timeout <= 0) {
					timerStatus = false;		// timer ����
					return;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}