package com.rhccclientmobile.dialog;

import com.rhccclientmobile.R;
import com.rhccclientmobile.screen.HandWritingView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ColorPicker extends DialogFragment {
	
	private HandWritingView handWritingView;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.pick_color)
	           .setItems(R.array.color_array, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               
	           }
	    });
	    return builder.create();
	}

}
