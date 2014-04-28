package com.RHCCServer.Model;
import org.java_websocket.WebSocket;

// this class represents a client and its methods and properties
public class Client {
	
	private String username;
	private WebSocket conn;
	private String latestFrame;
	private String addedFrame;

	public Client(String username, WebSocket conn){
		this.username = username;
		this.setConn(conn);
		this.latestFrame = "";
		this.addedFrame = "";
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public WebSocket getConn() {
		return conn;
	}

	public void setConn(WebSocket conn) {
		this.conn = conn;
	}

	public String getLatestFrame() {
		return latestFrame;
	}

	public void setLatestFrame(String latestFrame) {
		this.latestFrame = latestFrame;
	}

	public String getAddedFrame() {
		return addedFrame;
	}

	public void setAddedFrame(String addedFrame) {
		this.addedFrame = addedFrame;
	}
}
