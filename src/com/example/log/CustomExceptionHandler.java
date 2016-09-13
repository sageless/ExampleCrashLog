package com.example.log;

/**
 * Based on code in stackoverflow.com answer:
 * 
 * http://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application/15221480
 *
 * http://stackoverflow.com/questions/19897628/need-to-handle-uncaught-exception-and-send-log-file
 *
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler defaultUEH;

    private String localPath;
    
    private Info info;
    
	// an 'Application' context, since called from Application and not an
	// Activity
	private Context context;
    
	// most error are from the uncaught handler so default is true, but can also
	// manually call the error logger
	private boolean isFatal = true;

	
    public CustomExceptionHandler(Context context, String localPath) {
    	this.context = context;
        this.localPath = localPath;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        info = new Info(context);
        
		// Try to create the files folder if it doesn't exist
        new File(localPath + "/").mkdir();
    }
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    public void uncaughtException(Thread t, Throwable e) {
    	isFatal = true;
    	String filename = reportException(e);
    	invokeLogActivity(filename);
		// not reached - since 'isFatal' system.exit() will be called before we
		// get here.  The default handler just shows the 'ok' dialog.
		defaultUEH.uncaughtException(t, e);
    }
    
    /**
     * used to log exception without exiting app
     * @param e
     */
    public void logException(Throwable e) {
    	isFatal = false;
    	String filename = reportException(e);
    	invokeLogActivity(filename);
    }
    
    private void invokeLogActivity(final String filename) {
    	if (isUIThread()) {
			startLogActivity(context, filename);
		} else { // handle non UI thread throw uncaught exception

			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					startLogActivity(context, filename);
				}
			});
		}
    }
    
    private static boolean isUIThread(){
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
    
	private void startLogActivity(Context context, String filename) {
		Intent intent = new Intent();
		intent.putExtra("isFatal", isFatal);
		intent.putExtra("filename", filename);
		intent.setAction("com.example.SEND_LOG");
		// required when starting from Application
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

		if (isFatal) {
			System.exit(1); // kill off the crashed app
		}
	}
    
	private String reportException(Throwable e) {
		String timestamp = dateFormat.format(new Date());
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		printWriter.close();
		//String filename = timestamp + ".stacktrace";
		String filename = timestamp + ".txt";

		if (localPath != null) {
			writeToFile(stacktrace, filename);
		}
//      if (url != null) {
//      sendToServer(stacktrace, filename);
//  }
		return localPath + "/" + filename;
	}

	/**
	 * this creates separate log files, and keeps them on internal sdcard
	 */
    private void writeToFile(String stacktrace, String filename) {
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(
                    localPath + "/" + filename));
            
            bos.write("\n\n");
			bos.write("Android version: " + Build.VERSION.SDK_INT + "\n");
			bos.write("Release: " + info.androidVersion + "\n");
			bos.write("Device: " + info.model + "\n");
			bos.write("App Version: " + info.appVersion + "\n");
			bos.write("Package: " + info.appPackage + "\n\n");
            
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*  not used
    private void sendToServer(String stacktrace, String filename) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("filename", filename));
        nvps.add(new BasicNameValuePair("stacktrace", stacktrace));
        try {
            httpPost.setEntity(
                    new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
    
    private static class Info {
    	private String model = "";
    	private String appVersion = "";
    	private String appPackage = "";
    	private String androidVersion = "";
    	
		private Info(Context context) {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = null;
			try {
				info = manager.getPackageInfo(context.getPackageName(), 0);
				model = Build.MODEL;
				if (!model.startsWith(Build.MANUFACTURER))
					model = Build.MANUFACTURER + " " + model;

				appVersion = info.versionName;
				appPackage = info.packageName;
				androidVersion = Build.VERSION.RELEASE;

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
