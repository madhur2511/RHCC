package com.RHCCServer.Model;
import org.java_websocket.WebSocket;

public class Client {
	
	private String username;
	private WebSocket conn;
//	private String password;
//	private String name;

	public Client(String username, WebSocket conn){
//		this.name = name;
		this.username = username;
		this.setConn(conn);
//		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
//	public String getPassword() {
//		return password;
//	}
//	public void setPassword(String password) {
//		this.password = password;
//	}
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}		

	public WebSocket getConn() {
		return conn;
	}

	public void setConn(WebSocket conn) {
		this.conn = conn;
	}
}
