package com.rhccclientmobile.websocket;

import java.net.URI;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;

class WebClient extends WebSocketClient
{
	private volatile String message = "";
	
	public String getMessage() {
		return message;
	}
	
	public WebClient(URI uri, Draft draft) {
		super(uri, draft);
	}
	
	@Override
	public void onMessage( String message ) {
		this.message = message;		
	}

	@Override
	public void onOpen( ServerHandshake handshake ) {
		System.out.println( "You are connected to RHCCServer: " + getURI() + "\n" );						
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		System.out.println("Closed");		
	}

	@Override
	public void onError( Exception ex ) {
		System.out.println(ex.toString());
	}			
}
