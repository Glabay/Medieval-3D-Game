package client.util.login;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;

public class LoginWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private boolean btnPressed = false;

	/**
	 * Create the frame.
	 */
	public LoginWindow() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
		setVisible(true);
		setBounds(100, 100, 765, 624);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLayeredPane layeredPane = new JLayeredPane();
		contentPane.add(layeredPane);
		
		usernameField = new JTextField();
		usernameField.setForeground(new Color(255, 102, 0));
		usernameField.setBackground(new Color(51, 51, 51));
		usernameField.setBounds(265, 223, 222, 28);
		layeredPane.add(usernameField);
		usernameField.setColumns(10);
		
		JLabel nameLabel = new JLabel("Username:");
		nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		nameLabel.setForeground(new Color(255, 102, 0));
		nameLabel.setBounds(265, 198, 82, 28);
		layeredPane.add(nameLabel);
		
		JLabel passwordLabel = new JLabel("Password:");
		passwordLabel.setForeground(new Color(255, 102, 0));
		passwordLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		passwordLabel.setBounds(265, 261, 82, 28);
		layeredPane.add(passwordLabel);
		
		JButton loginButtton = new JButton("");
		loginButtton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!btnPressed) {
					/**
					 * TODO:
					 * Change the button image to a different image to sho it have been pressed
					 */
					setBtnPressed(false);
					appendLogin();
				} else {
					/**
					 * TODO:
					 * Set message to user that the button was pressed
					 * Change the button image again.
					 */
				}
			}
		});
		loginButtton.setContentAreaFilled(false);
		loginButtton.setBorderPainted(false);
		loginButtton.setBackground(new Color(192, 192, 192));
		loginButtton.setBounds(311, 353, 127, 28);
		layeredPane.add(loginButtton);
		
		passwordField = new JPasswordField();
		passwordField.setForeground(new Color(255, 102, 0));
		passwordField.setBackground(new Color(51, 51, 51));
		passwordField.setBounds(265, 289, 222, 28);
		layeredPane.add(passwordField);
		
		JLabel loginBtn = new JLabel("");
		loginBtn.setIcon(new ImageIcon("C:/Users/The Source/Desktop/Eclipse Projects/Medieval 3D Game/file_storage/2D/3.png"));
		loginBtn.setBounds(304, 353, 142, 28);
		layeredPane.add(loginBtn);
		
		JLabel loginPanel = new JLabel("");
		loginPanel.setIcon(new ImageIcon("C:/Users/The Source/Desktop/Eclipse Projects/Medieval 3D Game/file_storage/2D/5.png"));
		loginPanel.setBounds(199, 143, 364, 360);
		layeredPane.add(loginPanel);
		
		JLabel backgroundImg = new JLabel("");
		backgroundImg.setBounds(0, 0, 755, 614);
		layeredPane.add(backgroundImg);
		backgroundImg.setIcon(new ImageIcon("C:/Users/The Source/Desktop/Eclipse Projects/Medieval 3D Game/file_storage/2D/loginBackground.png"));
	}
	
	public void appendLogin() {
		System.out.println("Login pending...");
		if(!usernameField.getText().isEmpty()) {
			if (!String.valueOf(passwordField.getPassword()).isEmpty()) {
				this.dispose();
				new LoginHandler().initLogin(getUsername(), getPassword());
			} else {
				System.out.println("Password is Empty");
			}
		} else {
			System.out.println("Username is Empty");
		}
	}
	
	public void loginAccepted() {
		dispose();
	}
	
	private String getUsername() {
		return usernameField.getText();
	}
	
	private String getPassword() {
		return String.valueOf(passwordField.getPassword());
	}

	public void setBtnPressed(boolean btnPressed) {
		this.btnPressed = btnPressed;
	}
}
