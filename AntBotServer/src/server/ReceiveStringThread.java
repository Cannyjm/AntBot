package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Handler;

import javax.swing.SwingWorker;

import ui.VideoStreamCanvas;

public class ReceiveStringThread extends SwingWorker<Void, String>
{
	Socket clientSocket;
	BufferedReader clientSocketInput;
	ServerModel model;
	VideoStreamCanvas videoStreamCanvas;
	ImageObject imageObject;
	
	public ReceiveStringThread(Socket client, BufferedReader in, ServerModel m, ImageObject imageObject)
	{
		this.imageObject = imageObject;
		clientSocket = client;
		clientSocketInput = in;
		model = m;
		
	}
	
	@Override
    protected Void doInBackground() throws Exception
    {
        String inputLine;
        boolean running = true;

        while(running){
        	try{
        		inputLine = clientSocketInput.readLine();
        		running = true;
        	} catch (IOException e){
        		running = false;
        		break;
        	} catch (NullPointerException e){
        		running = false;
        		break;
        	}
        	if(inputLine.contains("Image ")){
        		inputLine = inputLine.replace("Image ", "");
        		inputLine = inputLine.replace("[", "");
        		inputLine = inputLine.replace("]", "");
        		inputLine = inputLine.replace(", ", ",");
        		publish(inputLine);
          		
        	} else {
        		System.out.println("Received message: " + inputLine);
        		parseMessage(inputLine);
        	}
        }
        
		return null;
    }
	
	
	
	@Override
	protected void process(List<String> chunks){
		//System.out.println("Received message Image: " + chunks.get(0));
		imageObject.setImageString(chunks.get(0));
	}
	
	
    
    private void parseMessage(String message)
    {
    	String[] tokens  = message.split("\\s+");
    	
    	switch(tokens[0])
    	{
	    	case "PING":
	    		System.out.println("Recieved PING");
	    		break;
    	
    		case "HOMING":
    			if( tokens[1].equals("START") )
    			{
    				model.setHoming(true);
    			}
    			else if ( tokens[1].equals("STOP") )
    			{
    				model.setHoming(false);
    			}
    			break;
    			
    		case "ROUTING":
    			if( tokens[1].equals("START") )
    			{
    				model.setRouting(true);
    			}
    			else if ( tokens[1].equals("STOP") )
    			{
    				model.setRouting(false);
    			}
    			break;
    		
    		case "move":
    			double dist = Double.valueOf(tokens[1]);
    			System.out.println("Recv: Move " + dist);
    			model.updatePosition(dist);
    			break;
    			
    		case "turn":
    			double angle = Double.valueOf(tokens[1]);
    			System.out.println("Recv: Turn " + angle);
    			model.updateOrientation(angle);
    			break;
    			
    		case "HomeVector":
    			double homeAngle = Double.valueOf(tokens[1]);
    			double homeDist = Double.valueOf(tokens[2]);
    			System.out.println("Recv: Home " + homeAngle + ", " + homeDist);
    			model.setHomeVector(homeAngle, homeDist);
    			
    		case "VisionError":
    			int currentVisionError = Integer.valueOf(tokens[1]);
    			model.setCurrentVisionError(currentVisionError);
    			System.out.println("Error: " + currentVisionError);
    			break;
    			
    		case "Close":
    			model.stopServer();
    			System.out.println("App closed - Closing Server");
    			break;
    			
    		case "j":
    			System.out.println("Job Finished");
    			model.setFinished(true);
    			break;
    	}
    }
}

