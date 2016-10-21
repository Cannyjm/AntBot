package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import server.ServerModel;


public class CommandPanel extends JPanel implements ActionListener
{
	private ServerController controller;
	
	private JPanel labelPanel;
	private JLabel panelLabel;
	
	private JPanel manualCommandsPanel;
	
	
	
	private JPanel turnPanel;
	private JLabel turnLabel;
	private JTextField turnTextField;
	private JButton turnButton;
	private JButton queueTurnButton;
	
	private JPanel movePanel;
	private JLabel moveLabel;
	private JTextField moveTextField;
	private JButton moveButton;
	private JButton sendStringButton;
	
	private JButton turnMoveButton;
	private JButton haltButton;
	private JButton calcButton;
	private JButton learnButton;
	private JButton orientationButton;
	
	ServerModel model;
	
	public CommandPanel(ServerController controller)
	{
		super();
		model = controller.model;
		addKeyListener(listener);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(500, 110));
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
		
		this.controller = controller;
		
		// Top Panel - label
		labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panelLabel = new JLabel("Movement Commands");
		labelPanel.add(panelLabel);
		
		add(labelPanel);
		
		///////////////////////
		//  Manual Commands  //
		///////////////////////
		
		manualCommandsPanel = new JPanel();
		manualCommandsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		manualCommandsPanel.setPreferredSize(new Dimension(500, 100));
		
		// Turn
		turnPanel = new JPanel();
		
		turnLabel = new JLabel("Angle:");
		turnLabel.setPreferredSize(new Dimension(60, 20));
		turnPanel.add(turnLabel);
		
		turnTextField = new JTextField();
		turnTextField.addActionListener(this);
		turnTextField.setColumns(4);
		turnPanel.add(turnTextField);
		
		turnButton = new JButton("Turn");
		turnButton.addActionListener(this);
		turnButton.setPreferredSize(new Dimension(100, 26));
		turnPanel.add(turnButton);
		
		//queueTurnButton = new JButton("Queue Turn");
		//queueTurnButton.addActionListener(this);
		//queueTurnButton.setPreferredSize(new Dimension(100, 26));
		//turnPanel.add(queueTurnButton);
		
		moveLabel = new JLabel("Distance:");
		moveLabel.setPreferredSize(new Dimension(60, 20));
		turnPanel.add(moveLabel);
		
		moveTextField = new JTextField();
		moveTextField.addActionListener(this);
		moveTextField.setColumns(4);
		turnPanel.add(moveTextField);
		
		moveButton = new JButton("Move");
		moveButton.addActionListener(this);
		moveButton.setPreferredSize(new Dimension(100, 26));
		turnPanel.add(moveButton);
		
		manualCommandsPanel.add(turnPanel);
		
		// Move
		movePanel = new JPanel();
		
				
		manualCommandsPanel.add(movePanel);
		
		//Halt
		
		haltButton = new JButton("STOP");
		haltButton.addActionListener(this);
		manualCommandsPanel.add(haltButton);
		
		//Calculate Error
		
		calcButton = new JButton("Calc Error");
		calcButton.addActionListener(this);
		manualCommandsPanel.add(calcButton);
		
		//Learn
		
		learnButton = new JButton("Learn Image");
		learnButton.addActionListener(this);
		manualCommandsPanel.add(learnButton);
		
		//Orient towards minimum
		
		orientationButton = new JButton("Turn to Minimum");
		orientationButton.addActionListener(this);
		manualCommandsPanel.add(orientationButton);
		
		add(manualCommandsPanel);
		
		
		// Heading
		/*
		turnMoveButton = new JButton("Both");
		turnMoveButton.addActionListener(this);
		manualCommandsPanel.add(turnMoveButton);
		

		*/
		
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		try
		{
	    	if(e.getSource() == turnButton )
	    	{
	    		controller.submit_turn( turnTextField.getText() );
	    	}
	    	else if( e.getSource() == moveButton )
	    	{
	    		controller.submit_move( moveTextField.getText() );
	    	}
	    	else if( e.getSource() == turnMoveButton )
	    	{
	    		controller.submit_both( turnTextField.getText(), moveTextField.getText() );
	    	}
	    	else if( e.getSource() == haltButton )
	    	{
	    		controller.submit_halt();
	    	}
	    	else if( e.getSource() == learnButton )
	    	{
	    		controller.submit_learn("learn");
	    	}
	    	else if(e.getSource() == calcButton){
	    		controller.submit_calc_error();
	    	}
	    	else if (e.getSource() == orientationButton){
	    		controller.submit_turnToMinimum();
	    	}
	    	else if(e.getSource() == sendStringButton){
	    		//controller.submit_string(message);
	    	}
		}
		catch(NumberFormatException ex)
		{
			System.err.println("Can't submit command - bad format");
		}
		catch(NullPointerException ex)
		{
			System.err.println("Can't submit command - server offline");
		}
	}
	
	boolean upPressed = false;
	boolean downPressed = false;
	boolean leftPressed = false;
	boolean rightPressed = false;
	boolean anyPressed = false;
	
	KeyListener listener = new KeyListener(){
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		char keyCode = arg0.getKeyChar();
		switch(keyCode){
			case 'w':
				upPressed = true;
				anyPressed = true;
				break;
			case 's':
				downPressed = true;
				anyPressed = true;
				break;
			case 'a':
				leftPressed = true;
				anyPressed = true;
				break;
			case 'd':
				rightPressed = true;
				anyPressed = true;
				break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		char keyCode = e.getKeyChar();
		switch(keyCode){
			case 'w':
				upPressed = false;
				if(!downPressed && !leftPressed && !rightPressed){
					anyPressed = false;
					handleKeyInput();
				}
				break;
			case 's':
				downPressed = false;
				if(!upPressed && !leftPressed && !rightPressed){
					anyPressed = false;
					handleKeyInput();
				}
				break;
			case 'a':
				leftPressed = false;
				if(!upPressed && !downPressed && !rightPressed){
					anyPressed = false;
					handleKeyInput();
				}
				break;
			case 'd':
				rightPressed = false;
				if(!upPressed && !downPressed && !leftPressed){
					anyPressed = false;
					handleKeyInput();
				}
				break;
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char keyCode = e.getKeyChar();
		switch(keyCode){
			case 'w':
				handleKeyInput();
				break;
			case 's':
				handleKeyInput();
				break;
			case 'a':
				handleKeyInput();
				break;
			case 'd':
				handleKeyInput();
				break;
		}
		
	}
	
	private void handleKeyInput(){
		System.out.println("Finished: " + model.finished);
		if(anyPressed && model.finished){
			if(upPressed){
				if(!downPressed){
					if(leftPressed == rightPressed){
						System.out.println("Forwards");
			    		controller.submit_move("t 0 m 0.1 n");
			    		model.setFinished(false);
					} else if(leftPressed){
						System.out.println("Curve Left Forwards");
			    		controller.submit_move("lt 0 m 0.1 n");
			    		model.setFinished(false);
					} else if(rightPressed){
						System.out.println("Curve Right Forwards");
			    		controller.submit_move("rt 0 m 0.1 n");
			    		model.setFinished(false);
					}
				} else {
					//Stop
				}
			} else if(downPressed){
				if(leftPressed == rightPressed){
					System.out.println("Backwards");
		    		controller.submit_move("t 0 m -0.1 n");
		    		model.setFinished(false);
				} else if(leftPressed){
					System.out.println("Curve Left Backwards");
		    		controller.submit_move("lt 0 m -0.1 n");
		    		model.setFinished(false);
				} else if(rightPressed){
					System.out.println("Curve Right Backwards");
		    		controller.submit_move("rt 0 m -0.1 n");
		    		model.setFinished(false);
				}
			} else if (leftPressed){
				if(!rightPressed){
					System.out.println("Turn Left");
		    		controller.submit_turn("t 8 m 0 n");
		    		model.setFinished(false);
				}
			} else if (rightPressed){
				System.out.println("Turn Right");
	    		controller.submit_turn("t -8 m 0 n");
	    		model.setFinished(false);
			}
		} else if(!anyPressed) {
			controller.submit_halt();
		}
	}
			
	};

	
}
