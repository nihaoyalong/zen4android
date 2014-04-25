package me.roger.zen.application;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class ZenApplication extends Application {

	private static Context appContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		ImageLoaderConfiguration DefaultConfig = ImageLoaderConfiguration.createDefault(this);
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(DefaultConfig);
		
		appContext = getApplicationContext();
	}
	
	
	
	public static Context getAppContext() {
		return appContext;
	}

}
