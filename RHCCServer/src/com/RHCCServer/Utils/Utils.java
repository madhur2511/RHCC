package com.RHCCServer.Utils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

// A utilities class having reusable functions
public class Utils 
{
	
    // gets the port number stored in a configuration file on the server
	public static int getPortFromFile()
	{
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("C:/Users/Developer/Desktop/RHCC/RHCC/port.txt"));
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String line = null;
		try 
		{
			while ((line = reader.readLine()) != null){
					reader.close();
					return Integer.parseInt(line);
			}
		} 
		catch (IOException e){
				e.printStackTrace();}
		catch(NumberFormatException e){
			e.printStackTrace();}
		
		if(reader!=null){
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		return -1;
	}
	
	
	// gets the parameters from the URL hit by the client 
	public static String[] getGETParamsFromURL(String s){
		int i=0;
		String[] args = s.split("&");
		String[] strArr = new String[args.length];
		for(String part:args){
			String[] temp = part.split("=");
			if(temp.length > 1)	
				strArr[i++] = temp[1];
		}
		return strArr;
	}
	
}