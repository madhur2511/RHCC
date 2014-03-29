package com.rhccclientmobile;

import java.io.File;
import java.io.FileOutputStream;

import com.rhccclientmobile.screen.HandWritingView;
import com.rhccclientmobile.websocket.Router;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

// Landing class on start of application
public class MainActivity extends Activity {	
	
	private ListView navDrawerList;
	private String[] NAV_DRAWER_LIST_STRING = { "START", "STOP", "SAVE", "CLEAR", "RED", "BLUE", "GREEN", "YELLOW"};
	private Router router;
	private DrawerLayout navDrawer;
	private static LinearLayout drawFrame;	
	private static HandWritingView handWritingView;
	private Activity activity;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		navDrawerList = (ListView)findViewById(R.id.nav_drawer_list);
		navDrawer = (DrawerLayout)findViewById(R.id.nav_drawer);
		drawFrame = (LinearLayout)findViewById(R.id.draw_frame);		
		activity = this;
		handWritingView = new HandWritingView(this, null);
		drawFrame.addView(handWritingView, LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT); 
		
		ArrayAdapter<String> navDrawerListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, NAV_DRAWER_LIST_STRING);
		navDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				switch(position) {
					case 0:
						// Start Connection via Router class
						router = new Router("192.168.2.13", 1018, activity);											
						router.start();										
						navDrawer.closeDrawers();
					break;
					case 1:
						// Stop Connection Via Router class
						router.stop();
						navDrawer.closeDrawers();
					break;
					case 2:
						// Save Image
						String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sketch";
						File dir = new File(file_path);
						if(!dir.exists())
							dir.mkdirs();
						File file = new File(dir, "sketchpad" + ".png");
						try {
							FileOutputStream fOut = new FileOutputStream(file);
							drawFrame.setDrawingCacheEnabled(true);
							Bitmap resized = Bitmap.createScaledBitmap(drawFrame.getDrawingCache(), 320, 240, false);
							resized.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fOut);
							fOut.flush();
							fOut.close();
							drawFrame.setDrawingCacheEnabled(false);
						} catch(Exception e) {
							
						}
						navDrawer.closeDrawers();
					break;
					case 3:
						// Clear Image		
						//drawFrame.removeView(handWritingView);
						//handWritingView = new HandWritingView(activity, null);
						//drawFrame.addView(handWritingView, LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
						handWritingView.clear();
						navDrawer.closeDrawers();
					break;
					case 4:
						// Red
						handWritingView.setColor(Color.RED);						
					break;
					case 5:
						// Blue
						handWritingView.setColor(Color.BLUE);						
					break;
					case 6:
						// Green
						handWritingView.setColor(Color.GREEN);
					break;
					case 7:
						// Yellow
						handWritingView.setColor(Color.YELLOW);
					break;
					default:						
				}
				
			}
			
		});
		navDrawerList.setAdapter(navDrawerListAdapter);			
	}	
	
	public static View getCaptureFrame() {
		return drawFrame;
	}
	
	public static View getDrawFrame() {
		return handWritingView;
	}

}
