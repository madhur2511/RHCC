package com.RHCCClient.Router;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import javax.imageio.ImageIO;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Base64;

public class Router {
	
	private WebSocketClient cc;

	public Router( String defaultlocation ) {		
		
		Draft[] drafts = { new Draft_17(), new Draft_10(), new Draft_76(), new Draft_75() };
				
		try {
				// cc = new ChatClient(new URI(uriField.getText()), area, ( Draft ) draft.getSelectedItem() );
				cc = new WebSocketClient( new URI( defaultlocation ), drafts[0] ) {

					@Override
					public void onMessage( String message ) {
						//System.out.println("Message :" + message);
					}

					@Override
					public void onOpen( ServerHandshake handshake ) {
						System.out.println( "You are connected to ChatServer: " + getURI() + "\n" );						
					}

					@Override
					public void onClose( int code, String reason, boolean remote ) {
						System.out.println("Closed");
					}

					@Override
					public void onError( Exception ex ) {
						System.out.println(ex.toString());
					}
				};
				cc.connect();				
			
			} catch ( URISyntaxException ex ) {
				System.out.println("not a valid WebSocket URI\n" );
			}						
	}

	public static void main( String[] args ) throws InterruptedException, IOException {
		WebSocketImpl.DEBUG = false;
		String location;
		if( args.length != 0 ) {
			location = args[ 0 ];
			System.out.println( "Default server url specified: \'" + location + "\'" );
		} else {
			location = "ws://localhost:8889";
			System.out.println( "Default server url not specified: defaulting to \'" + location + "\'" );
		}
		Router r = new Router( location );
		Thread.sleep(1000);
		
		File img_file = new File("C:/Users/Madhur/workspace/OpenCV/fig/lena.png");
		BufferedImage img = ImageIO.read(img_file);
		
		String base64 = Base64.encodeFromFile("C:/Users/Madhur/workspace/OpenCV/fig/lena.png");
		Date d1 = new Date();
		r.cc.send(base64);
		System.out.println(d1.getSeconds());
			
		//r.cc.close();
		
	}

}

