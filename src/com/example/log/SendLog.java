package com.example.log;

/**
 * send email code based on:
 * 
 * http://stackoverflow.com/questions/2197741/how-can-i-send-emails-from-my-android-application
 * 
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class SendLog extends Activity {
	
	/**
	 * put your development email address here...
	 */
	private static final String DEVELOPMENT_EMAIL_ADDRESS = "nobody@nowhere.com";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setFinishOnTouchOutside(false);

		Intent intent = getIntent();
		if (intent == null) {
			finish();
		}
		// get parameters
		Bundle args = intent.getExtras();
		boolean isFatal = args.getBoolean("isFatal");
		final String filename = args.getString("filename");

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		// Setting Dialog Title
		alertDialog.setTitle("Sorry!");

		// Setting Dialog Message
		if (isFatal) {
			alertDialog
					.setMessage("The application has stopped unexpectedly.  If this is the first time seeing this error, please send error report to development.");
		} else {
			alertDialog
					.setMessage("An unexpected error has occurred.  If this is the first time seeing this error, please send error report to development.");
		}
		
		// Setting Icon to Dialog
		alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

		// Setting Buttons
		alertDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				sendLogFile(filename);
				finish();
			}
		});
		
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		
		AlertDialog dialog = alertDialog.create();
		
		dialog.setCanceledOnTouchOutside(false);
		
		// may not need this
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

		// Showing Alert Message
		dialog.show();
	}
	
	
	
	private void sendLogFile(String filename) {

		// it's not ACTION_SEND
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setType("message/rfc822");
		intent.setData(new Uri.Builder().scheme("mailto").build());
		intent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { DEVELOPMENT_EMAIL_ADDRESS });
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	  
		intent.putExtra(Intent.EXTRA_SUBJECT, "Crash log file");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filename));
		// do this so some email clients don't complain about empty body.
		intent.putExtra(Intent.EXTRA_TEXT, "Log file attached.");
	  
		// this is for emulator will work along with devices
		ComponentName emailApp = intent.resolveActivity(this
				.getPackageManager());
		ComponentName unsupportedAction = ComponentName
				.unflattenFromString("com.android.fallback/.Fallback");
		if (emailApp != null && !emailApp.equals(unsupportedAction))
			try {
				// Needed to customize the chooser dialog title since it might
				// default to "Share with"
				// Note that the chooser will still be skipped if only one app
				// is matched
				Intent chooser = Intent
						.createChooser(intent, "Send email with");
				startActivity(chooser);
				return;
			} catch (ActivityNotFoundException ignored) {
			}

		Toast.makeText(this, "Couldn't find an email app and account",
				Toast.LENGTH_LONG).show();
	}
}
