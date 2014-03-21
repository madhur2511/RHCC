package com.RHCCServer.Main;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.RHCCServer.Model.Client;
import com.RHCCServer.Model.Group;
import com.RHCCServer.Router.Router1;

public class Main 
{
	protected int serverPort;
	protected ServerSocket serverSocket = null;
	protected boolean isStopped = false;
	public ArrayList<Socket> clientSockets;
	
	public static void main(String[] args)
	{
		
//		Client c1 = new Client("Madhur Kapoor");
//		Client c2 = new Client("Ankur Sarda");
//		Client c3 = new Client("Netratav Gupta","netgupta","NG");
//		
//		Group g1 = new Group();
//		Group g2 = new Group();
//		
//		g1.addMember(c1);
//		g1.addMember(c2);
//		
//		g2.addMember(c1);
//		g2.addMember(c3);	
//		
//		//Assuming that clients provide the auth and verified and their socket is a part of their object. 
//		//And are somehow divided into groups.
//		
//		Router1 server = new Router1();
//		
//    	server.addGroup(g1);
//    	server.addGroup(g2);
//		
	}
}
