package me.roger.zen.utils;

import me.roger.zen.application.ZenApplication;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;


public class ZenUpdateManager {
	public static final String ZEN_NEW_VERSION = "zen_new_version";
	
	private static final String ZEN_CHECK_UPDATE_URL = "http://rogerqian.github.io/zen.json";
	private static final int ZEN_VRESION = 4;
	private static ZenUpdateManager instance;
	
	public static ZenUpdateManager getInstance() {
		if (instance == null) {
			instance = new ZenUpdateManager();
		}
		return instance;
	}
	
	public void CheckUpdate() {
		ZenURLConnection connection = new ZenURLConnection(ZEN_CHECK_UPDATE_URL);
		connection.setOnResponseListener(new ZenOnResponseListener() {
			
			@Override
			public void OnResponse(String response) {
				try {
					if (response != null) {
						JSONObject json = new JSONObject(response);
						int version = json.getInt("version");
						if (version > ZEN_VRESION) {
							String url = json.getString("url");
							if (url != null) {
								Context context = ZenApplication.getAppContext();
								Intent intent = new Intent(ZEN_NEW_VERSION);
								intent.putExtra("url", url);
								context.sendBroadcast(intent);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void OnError(String msg) {
				// no need to handle error
			}
		});
		connection.startAsychronous();
	}
	
}
