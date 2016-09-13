package com.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;

/***
 * This is being lazy and just using most of the SendLog Alert code as a driver 
 * activity for demo.  (but also requires no project resources)
 * 
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setFinishOnTouchOutside(false);

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
		.setTitle("Crash Me!")
		.setMessage("Do you want to crash the app?")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton("Crash",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// create a null pointer exception
						//Number num  = null;
						//num.floatValue();
						
						// or can just throw something
						throw new RuntimeException("bad day");
					}
				})
		.setNegativeButton("Cancel", 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

		AlertDialog dialog = alertDialog.create();
		dialog.setCanceledOnTouchOutside(false);

		/*
		dialog.getWindow()
				.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		*/

		// Showing Alert Message
		dialog.show();
	}
}
