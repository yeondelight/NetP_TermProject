package client.main;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import client.room.WaitingView;

public class GameClient extends JFrame{
	
	private JPanel contentPane;
	private JTextField txtUserName;
	private JTextField txtIpAddress;
	private JTextField txtPortNumber;
	
	private ImageIcon background = new ImageIcon("res/background.jpg");
	private ImageIcon connectBtn = new ImageIcon("res/buttons/connectBtn.png");
	private ImageIcon connectBtn_hover = new ImageIcon("res/buttons/connectBtn_hover.png");

	// Create the frame.
	public GameClient() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(900, 500);
		
		contentPane = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				setOpaque(true);
				g.drawImage(background.getImage(), 0, 0, null);
			}
		};
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtUserName = new JTextField();
		txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
		txtUserName.setBounds(380, 340, 140, 30);
		contentPane.add(txtUserName);
		txtUserName.setColumns(10);
		
		txtIpAddress = new JTextField();
		txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
		txtIpAddress.setText("127.0.0.1");
		txtIpAddress.setColumns(10);
		txtIpAddress.setBounds(795, 10, 80, 20);
		txtIpAddress.setEditable(false);		// 변경 불가하도록
		contentPane.add(txtIpAddress);
		
		txtPortNumber = new JTextField();
		txtPortNumber.setText("30000");
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setColumns(10);
		txtPortNumber.setBounds(795, 35, 80, 20);
		txtPortNumber.setEditable(false);		// 변경 불가하도록
		contentPane.add(txtPortNumber);
		
		JButton btnConnect = new JButton(connectBtn);
		btnConnect.setRolloverIcon(connectBtn_hover);
		btnConnect.setBounds(410, 380, 80, 30);
		btnConnect.setContentAreaFilled(false);
		btnConnect.setBorderPainted(false);
		btnConnect.setOpaque(false);
		contentPane.add(btnConnect);
		
		Myaction action = new Myaction();
		btnConnect.addActionListener(action);
		txtUserName.addActionListener(action);

		setResizable(false);
		setVisible(true);
	}
	
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			String username = txtUserName.getText().trim();
			String ip_addr = txtIpAddress.getText().trim();
			String port_no = txtPortNumber.getText().trim();
			WaitingView view = new WaitingView(username, ip_addr, port_no);
			setVisible(false);
		}
	}
}
