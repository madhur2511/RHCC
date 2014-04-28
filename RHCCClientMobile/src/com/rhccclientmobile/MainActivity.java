package com.rhccclientmobile;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

import com.rhccclientmobile.screen.HandWritingView;
import com.rhccclientmobile.utils.Constant;
import com.rhccclientmobile.websocket.Router;

// Landing class on start of application
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("NewApi")
public class MainActivity extends Activity {	
	
	private ListView navDrawerList;
	
	private Router router;
	private DrawerLayout navDrawer;
	private static LinearLayout drawFrame;	
	private static HandWritingView handWritingView;
	private Activity activity;
	private ActionBarDrawerToggle actionBarDrawerToggle;
		
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		router = null;
		navDrawerList = (ListView)findViewById(R.id.nav_drawer_list);
		navDrawer = (DrawerLayout)findViewById(R.id.nav_drawer);
		drawFrame = (LinearLayout)findViewById(R.id.draw_frame);		
		activity = this;
		handWritingView = new HandWritingView(this, null);
		drawFrame.addView(handWritingView, LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT); 		
		actionBarDrawerToggle = new ActionBarDrawerToggle(this,navDrawer, R.drawable.ic_navigation_drawer, R.string.app_name, R.string.app_name);
		actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
		navDrawer.setDrawerListener(actionBarDrawerToggle);		
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);       	
		
		ArrayAdapter<String> navDrawerListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.NAV_DRAWER_LIST_STRING);
		navDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				switch(position) {
					case 0:
						// Start Connection via Router class
						showUserDetailDialog();																				
					break;
					case 1:
						// Stop Connection Via Router class
						if(router != null) {
							router.stop();
						}
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
						// Clear screen
						handWritingView.clear();
						navDrawer.closeDrawers();
					break;
					case 4:
						// Pick Color;
						showColorPickerDialog();						
						handWritingView.setColor(Color.RED);											
					default:						
				}
				
			}
			
		});
		navDrawerList.setAdapter(navDrawerListAdapter);			
	}	
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
	
	public void showColorPickerDialog() {
		AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
		builder.setItems(R.array.color_array, new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				handWritingView.setColor(Constant.COLOR_LIST[which]);
				navDrawer.closeDrawers();				
			}
		});
		builder.show();
	}
	
	public void showUserDetailDialog() {
		AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
		LayoutInflater inflater = activity.getLayoutInflater();
		final View userDetailView = inflater.inflate(R.layout.user_detail, null);
		builder.setView(userDetailView)
        	.setTitle("Enter Details")        	
        	.setPositiveButton("Connect",new DialogInterface.OnClickListener() {
	            @SuppressLint("ShowToast")
				@Override
	            public void onClick(DialogInterface dialog, int which) {
	            	if(router == null) {
	            		EditText ipaddress = (EditText) userDetailView.findViewById(R.id.ipaddress);
	            		EditText username = (EditText) userDetailView.findViewById(R.id.username);
	            		EditText groupname = (EditText) userDetailView.findViewById(R.id.groupname);	            		
		            	router = new Router(ipaddress.getText().toString(), 1018, username.getText().toString(), groupname.getText().toString(), activity);											
						router.start();							
	            	} else {
	            		Toast.makeText(activity, "Connection already established", Toast.LENGTH_SHORT);
	            	}
	            	navDrawer.closeDrawers();
	            }	         
        }).create(); 
		builder.show();
	}

	public static View getCaptureFrame() {
		return drawFrame;
	}
	
	public static View getDrawFrame() {
		return handWritingView;
	}

}
