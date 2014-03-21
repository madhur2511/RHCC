package com.RHCCServer.Router;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.RHCCServer.Model.Client;
import com.RHCCServer.Model.Group;
import com.RHCCServer.Utils.Utils;

public class Router extends WebSocketServer {
	
	private ArrayList<Group> groups=null;

	public Router( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
		groups = new ArrayList<Group>();
	}

	public Router( InetSocketAddress address ) {
		super( address );
		groups = new ArrayList<Group>();
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the collaboration!" );
		
		String[] params = Utils.getGETParamsFromURL(handshake.getResourceDescriptor());
		
		if(params.length > 1){
			System.out.println(params[0]+" joined");
			
			Client c = new Client(params[0],conn);
			Group g = new Group(params[1]);
			Group gStar=null;
			
			for(Group giterate : groups){
				
				if(giterate.getGroupName().equalsIgnoreCase(g.getGroupName())){

					System.out.println("GROUP ALREADY EXISTS");			
					giterate.addMember(c);
					gStar = giterate;
				}
			}
			
			if(gStar == null){				
				System.out.println("NEW GROUP CREATED");

				g.addMember(c);
				groups.add(g);
				gStar = g;				
			}
			
			Group g1 = getGroupForClient(conn);
			System.out.println(g1.getAllMembers().size());
			
			this.sendToAllInGroupExcept( "new connection: " + params[0], getGroupForClient(conn), conn);
		}
		
		else{
			System.out.println(handshake.getResourceDescriptor());
			this.sendToAllExcept("new connection: " + handshake.getResourceDescriptor(),conn);
		}
		
//		try {
//			this.stop();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) { 	
//			e.printStackTrace();
//		}
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
	
		Group g = getGroupForClient(conn);
		if(g != null){
			
			String leftUser = getNameForConnection(conn);
			
			this.sendToAllInGroupExcept( leftUser + " has left the collaboration!",g,conn);
			System.out.println( leftUser + " has left the collaboration!" );
			
			for(int i=0; i<groups.size(); i++){
				Group tempGrp = groups.get(i);
				
				for(int j=0; j< tempGrp.getAllMembers().size(); ++j){
					
					Client tempCli = tempGrp.getAllMembers().get(j);
					
					if(tempCli.getConn().equals(conn)){
						tempGrp.removeMember(tempCli);
					}
					if(tempGrp.getAllMembers().size() == 0){
						groups.remove(tempGrp);
					}
				}
			}
		}
		else{
			this.sendToAllExcept(conn + " has left the collaboration!",conn);
		}
	}

	
	@Override
	public void onMessage( WebSocket conn, String message ) {
		
		Group g = getGroupForClient(conn);
		if(g==null) 
			this.sendToAllExcept(message,conn);
		else{
			this.sendToAllInGroupExcept(message,g,conn);
		}
	}
	

	@Override
	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = false;
		
		int port=9000;
		port = Utils.getPortFromFile();
		
		Router s = new Router( port );
		s.start();
		System.out.println( "RHCCServer started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		
	}
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	
	public void sendToAll( String text ) {
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
			}
		}
	}
	
	
	public void sendToAllExcept( String text,WebSocket conn ) {
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				if(c.equals(conn)){
					continue;
				}
				c.send( text );
			}
		}
	}
	
	
	
	public void sendToAllInGroupExcept(String text , Group g, WebSocket conn){
			for(Client ctemp : g.getAllMembers()){
				if(ctemp.getConn().equals(conn)){
					continue;
				}
				ctemp.getConn().send(text);				
			}
	}
	
	
	public Group getGroupForClient(WebSocket conn){
		for(Group gtemp:groups){
			for(Client ctemp : gtemp.getAllMembers()){
				if(ctemp.getConn().equals(conn)){
					return gtemp;
				}
			}
		}
		return null;
	}
	
	public String getNameForConnection(WebSocket conn){
		for(Group gtemp:groups){
			for(Client ctemp : gtemp.getAllMembers()){
				if(ctemp.getConn().equals(conn)){
					return ctemp.getUsername();
				}
			}
		}
		return null;
	}
	
}

