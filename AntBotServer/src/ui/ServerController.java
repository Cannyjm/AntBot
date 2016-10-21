package ui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import server.ImageObject;
import server.ServerModel;

/**
 * Checks if the commands being issued are valid (when applicable).
 * 
 * Most of the other commands are redundant, simply calling something in the model - they exist
 * basically to do a proper MVC implementation.
 */
public class ServerController
{
	ServerModel model;
	public ImageObject imageObject;
	
	public ServerController(ServerModel model)
	{
		this.model = model;
		this.imageObject = model.imageObject;
	}
	
	public boolean serverOnline()
	{
		return model.isOnline();
	}
	
	public void startServer()
	{
		model.startServer();
	}
	
	public void stopServer()
	{
		model.stopServer();
	}
	
	public void setPort(int port)
	{
		if(port > 65535 || port < 0)
		{
			
		}
		else
		{
			model.setPort(port);
		}
	}
	
	public void submit_turn(String angle)
	{
		if (angle.contains("t") && angle.contains("m") && angle.contains("n")){
			model.sendString("SerialTurn" + angle);
		} else {
			model.sendString("SerialTurnt " + angle + " m " + 0 + " n");
		}
	}
	
	public void submit_move(String distance)
	{
		if (distance.contains("t") && distance.contains("m") && distance.contains("n")){
			model.sendString("SerialMove" + distance);
		} else {
		model.sendString("SerialMovet " + 0 + " m " + distance + " n");
		}
	}
	
	public void submit_both(String angle, String distance)
	{
		model.sendString("Serialt " + angle + " m " + distance + " n");
	}
	
	public void submit_halt()
	{
		model.sendString("Serialh");
	}
	
	public void submit_start_homing()
	{
		model.sendString("Combinerhoming");
	}
	
	public void submit_stop_homing()
	{
		model.sendString("stop homing");
	}
	
	public void submit_route(String route)
	{
		model.sendString("route " + route.replaceAll("[\n\r]", " "));
	}
	
	public void submit_start_routing()
	{
		model.sendString("start routing");
	}
	
	public void submit_stop_routing()
	{
		model.sendString("stop routing");
	}
	
	public void submit_learn(String message)
	{
		model.sendString("learn");
	}
	
	public void submit_string(String message)
	{
		model.sendString("String" + message);
	}
	
	public void submit_calc_error(){
		model.sendString("calc");
	}
	
	public void submit_turnToMinimum(){
		model.sendString("minimum");
	}
	
	public boolean isHoming()
	{
		return model.isHoming();
	}
	
    public boolean isRouting()
    {
    	return model.isRouting();
    }
    
    public String getLastMessage()
    {
    	return model.getLastMessage();
    }
    
    public Point getAntbotStartPoint()
    {
    	return model.getAntbotStartPoint();
    }
    
    public void setAntbotStartPoint(Point point)
    {
    	model.setAntbotStartPoint(point);
    }
    
    public Point getAntbotDirectionPoint()
    {
    	return model.getAntbotDirectionPoint();
    }
    
    public void setAntbotDirectionPoint(Point point)
    {
    	model.setAntbotDirectionPoint(point);
    }
    
    public ArrayList<Point> getPath()
    {
    	return model.getPath();
    }
    
    public void clearpath()
    {
    	model.clearPath();
    }
    
    public Point getHomeVector()
    {
    	return model.getHomeVector();
    }
}
