package me.roger.zen.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;

public class ZenAdsModel {
	private static ZenAdsModel instance;
	private static final String ZEN_DEFAULT_THEME = "http://rogerqian.github.io/dqd_1_1.png";
	private static final String ZEN_DEFAULT_URL = "http://static.ballpo.com/app/apk/Dongqiudi_Zen.apk";
	private static final String ZEN_RULES_URL = "http://rogerqian.github.io/config_android.json";

	private boolean isEnabled;
	private boolean isEnabledAll;
	private ArrayList<String> mFids;
	private String mTheme;
	private String mUrl;
	private ZenURLConnection mConnection;

	
	public static ZenAdsModel getInstance() {
		if (instance == null) {
			instance = new ZenAdsModel();
		}
		return instance;
	}

	public ZenAdsModel() {
		isEnabled = false;
		isEnabledAll = false;
		mFids = new ArrayList<String>();
		mTheme = ZEN_DEFAULT_THEME;
		mUrl = ZEN_DEFAULT_URL;
	}

	public boolean isEnabled(String fid) {
		if (isEnabled) {
			if (isEnabledAll) {
				return true;
			}
			if (mFids.size() > 0) {
				for (String item : mFids) {
					if (item.equals(fid)) {
						return true;
					}
				}
			}
		}
		return false;
	}


	public void load() {
		try {
			if (mConnection != null) {
				mConnection.cancel();
				mConnection = null;
			}
			
			mConnection = new ZenURLConnection(ZEN_RULES_URL);
			mConnection.setOnResponseListener(new ZenOnResponseListener() {
				
				@Override
				public void OnResponse(String response) {
					try {
						if (response != null) {
							JSONObject json = new JSONObject(response);
							int flag = json.getInt("enabled");
							mTheme = json.getString("theme");
							mUrl = json.getString("url");
							isEnabled = (flag == 1);
							flag = json.getInt("enableAll");
							isEnabledAll = (flag == 1);
							if (!isEnabledAll) {
								JSONArray fids = json.getJSONArray("fids");
								mFids.clear();
								for (int i = 0; i < fids.length(); i++) {
									JSONObject obj = (JSONObject)fids.get(i);
									String fid = obj.getString("fid");
									mFids.add(fid);
								}
							}
							System.out.println("enable: " + isEnabled + "\nenabledAll: " + isEnabledAll + "\nmTheme: " + mTheme
									+ "\nmUrl: " + mUrl);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}				
				}
				
				@Override
				public void OnError(String msg) {
									
				}
			});
			mConnection.startAsychronous();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getTheme() {
		return mTheme;
	}
	
	public String getUrl() {
		return mUrl;
	}
}
