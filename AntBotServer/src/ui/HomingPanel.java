package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class HomingPanel extends JPanel implements ActionListener
{
	ServerController controller;
	JButton homingButton;
	JButton sendStringButton;
	JButton plusButton;
	JTextField stringTextField;
	
	private JPanel labelPanel;
	private JLabel panelLabel;
	
	private JPanel bottomPanel;
	
	
	
	public HomingPanel(ServerController controller)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(500, 70));
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
		
		this.controller = controller;
		
		// Top Panel - label
		labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panelLabel = new JLabel("Homing");
		labelPanel.add(panelLabel);
		
		add(labelPanel);
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		homingButton = new JButton("Start Homing");
		homingButton.addActionListener(this);
		bottomPanel.add(homingButton);
		
		
		stringTextField = new JTextField();
		stringTextField.addActionListener(this);
		stringTextField.setColumns(4);
		stringTextField.setText("%0_0#0");
		bottomPanel.add(stringTextField);

		sendStringButton = new JButton("Send String");
		sendStringButton.addActionListener(this);
		sendStringButton.setPreferredSize(new Dimension(100, 26));
		bottomPanel.add(sendStringButton);
		
		
		add(bottomPanel);
	}
	
	public void actionPerformed(ActionEvent e)
	{
    	if( e.getSource() == homingButton )
    	{
    		if( !controller.isHoming() )
    		{
    			controller.submit_start_homing();
    		}
    		else
    		{
    			controller.submit_stop_homing();
    		}
    	}
    	else if (e.getSource() == sendStringButton){
    		String text = stringTextField.getText();
    		controller.submit_string(text);
    		String[] tokens = text.split("#");
    		int lastValue = Integer.parseInt(tokens[1]);
    		lastValue = lastValue + 5;
    		stringTextField.setText(tokens[0] + "#" + lastValue);
    	}
	}
	
	public void onSuccessfulHomingStart()
	{
		homingButton.setText("Stop Homing");
	}
	
	public void onSuccessfulHomingStop()
	{
		homingButton.setText("Start Homing");
	}
}
