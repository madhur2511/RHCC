package com.RHCCServer.Router;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.RHCCServer.Model.Group;
import com.RHCCServer.Utils.Utils;

public class Router1
{
    protected int serverPort;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected ArrayList<Group> groups;

    public Router1()
    {
    	this.serverPort = Utils.getPortFromFile();
    	
        openServerSocket();
        
        while(!isStopped())
        {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } 
            catch (IOException e) {
                if(isStopped()) 
                {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            
            new Thread( new WorkerThread(clientSocket, "Multithreaded Server")).start();
        }
    	
    }

    public void addGroup(Group g)
    {
    	groups.add(g);
    }
    
    private synchronized boolean isStopped() 
    {
        return this.isStopped;
    }

    public synchronized void stop()
    {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() 
    {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } 
        catch (IOException e) {
            throw new RuntimeException("Cannot open port "+this.serverPort, e);
        }
    }
}