package client.room;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class MakeRoomDialog extends JDialog{
	
	private int key = 100000;
	private JLabel keyLabel = new JLabel("  ROOM CODE : ");
	private JLabel keyValue = new JLabel("  # " + key);
	private JLabel nameLabel = new JLabel(" ROOM NAME : ");
	private JTextField name = new JTextField(10);
	private JButton makebtn = new JButton(" MAKE ");
	
	private Color bgColor = new Color(220, 220, 220);
	
	private Vector<Integer> keys = new Vector<Integer>();
	
	public MakeRoomDialog(JFrame frame, String title) {
		super(frame, title, true);
		setSize(300, 150);
		setLayout(new FlowLayout());
		
		getKey();
		
		keyLabel.setOpaque(true);
		keyLabel.setFont(new Font("Arial", Font.BOLD, 15));
		keyLabel.setPreferredSize(new Dimension(120, 25));
		add(keyLabel);
		
		keyValue.setOpaque(true);
		keyValue.setBackground(bgColor);
		keyValue.setFont(new Font("Arial", Font.BOLD + Font.ITALIC, 17));
		keyValue.setPreferredSize(new Dimension(140, 25));
		add(keyValue);
		
		nameLabel.setOpaque(true);
		nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
		nameLabel.setPreferredSize(new Dimension(120, 25));
		add(nameLabel);
		
		name.setText("");
		name.setFont(new Font("Arial", Font.PLAIN, 15));
		name.setPreferredSize(new Dimension(150, 25));
		add(name);
		
		makebtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		add(makebtn);
		
		setResizable(false);
	} // End of RoomMakeDialog()
	
	public int getKey() {
		do{
			key = new Random().nextInt(899999) + 100000;
		} while(keys.contains(key));
		keyValue.setText("  # " + key);
		return key;
	} // End of getKey()
	
	public void setKey(int key) {
		keys.add(key);
	}
	
	public String getInput() {
		String input = name.getText();
		if(input.length() == 0)
			return null;
		else {
			name.setText("");
			return input;
		}
	} // End of getInput()
	
} // End of MakeRoomDialog
