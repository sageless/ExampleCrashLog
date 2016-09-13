package com.example;

import java.io.File;

import android.app.Application;
import android.os.Environment;

import com.example.log.CustomExceptionHandler;

public class MyApplication extends Application {

    CustomExceptionHandler customExceptionHandler;
    
    // put file path friendly app name here...
    private static final String APP_NAME = "MyApp";
    
	@Override
	public void onCreate() {
		String path = Environment.getExternalStorageDirectory().getPath() + File.separatorChar;
		if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
			customExceptionHandler = new CustomExceptionHandler(this, path
					+ APP_NAME);
			Thread.setDefaultUncaughtExceptionHandler(customExceptionHandler);
		}
	}

	/**
	 * used to specifically log exceptions besides the uncaught ones
	 * @param e
	 */
	public void logException(Throwable e) {
		customExceptionHandler.logException(e);
	}

}