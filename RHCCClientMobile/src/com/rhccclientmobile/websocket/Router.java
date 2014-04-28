package com.rhccclientmobile.websocket;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;

import com.rhccclientmobile.screen.HandWritingView;
import com.rhccclientmobile.utils.ImageUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Router {

	private int port;
	private String IPv4Addr;	
	private String location;
	private String username;
	private String groupname;
	WebClient webclient;
	Draft[] drafts = { new Draft_17(), new Draft_10(), new Draft_76(), new Draft_75() };
	private Boolean begin = false;		
	private Activity mainActivity;	
	
	public Router(String IPv4Addr, int port, String username , String groupname, Activity activity) {
		
		this.port = port;
		this.IPv4Addr = IPv4Addr;
		this.username = username;
		this.groupname = groupname;
		
		this.location = "ws://" + IPv4Addr + ":" + port + "?name=" + username + "&group=" + groupname + "&type=mobile";	
		this.mainActivity = activity;		
		WebSocketImpl.DEBUG = false;		
	}
	
	public void start() {
		try {			
			
			if(!begin) {
				webclient = new WebClient(new URI( location ), drafts[0]);
				webclient.connect();	
				begin = true;			
				final View captureFrame = com.rhccclientmobile.MainActivity.getCaptureFrame();				
				final HandWritingView drawFrame = (HandWritingView)com.rhccclientmobile.MainActivity.getDrawFrame();				
				final Handler handler = new Handler();
				
				final Runnable runnable = new Runnable(){
					
					@SuppressWarnings("deprecation")
					@SuppressLint("NewApi")					
					public void run()
					{					
						handler.removeCallbacks(this);
						synchronized(webclient) {
							if(webclient.isOpen()) {
								// set linear layout background
								String message = webclient.getMessage();							
								Bitmap bitmapImage = ImageUtils.decodeImage(message);
								if(!(message.equals("/")) && (bitmapImage != null)) {
									captureFrame.setBackground(new BitmapDrawable(bitmapImage));								
									//handWritingView.setImageBitmap(bitmapImage);
								}														
								// send screen capture							
								String encodedString = ImageUtils.encodeImage(drawFrame.getBitmap());									
								webclient.send(encodedString);								
							}
						}
						if(begin)
							handler.postDelayed(this, 100);
					}
				};					
				handler.postDelayed(runnable, 100);
			}
			
			
			
		} catch (URISyntaxException e) {						
			Log.e("Router.java", "URL malformed");
		} catch (Exception e) {
			Log.e("Router.java", "Unkown Error");
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			if(begin) {
				webclient.close();
				begin = false;
			}
		} catch(Exception e) {
			Log.e("Router.java", "Unkown Error");
			e.printStackTrace();
		}
	}
	
	public void send(String message) {
		webclient.send(message);
	}
}