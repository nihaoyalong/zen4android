package me.roger.zen.model;

import java.net.URLEncoder;

import me.roger.zen.application.ZenApplication;
import me.roger.zen.data.ZenUserInfo;
import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;
import me.roger.zen.utils.ZenUtils;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ZenLoginModel {
	
	// login broadcast msgs
	public static final String ZEN_LOGIN_FINISHED = "zen_login_finished";
	public static final String ZEN_LOGIN_FAILED = "zen_login_failed";
	
	// other
	private static final String ZEN_LOGIN_URL = "http://mobileapi.hupu.com/1/1.1.1/passport/login";
	private static final String ZEN_MSG_TOKEN_URL = "http://my.hupu.com/mymsg.php?action=send";
	
	private static final String USER_INFO_KEY = "user_info";
	private static final String DEF_VALUE = "null";
	private static ZenLoginModel sharedInstance;
	public boolean isLogedin;
	public ZenUserInfo userInfo;
	private ZenURLConnection connection;
	private SharedPreferences.Editor editor;
	private Context mContext;
	
	public static synchronized ZenLoginModel getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new ZenLoginModel();
			sharedInstance.load();
		}
		return sharedInstance;
	}

	private void load() {
		mContext = ZenApplication.getAppContext();
		isLogedin = false;
		userInfo = new ZenUserInfo();
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(USER_INFO_KEY, 0);
		editor = sharedPreferences.edit();
		
		// get preferences from the sharedPreferences
		if (sharedPreferences.contains("userName") && 
				sharedPreferences.contains("token") && 
				sharedPreferences.contains("uid") ) {
			isLogedin = true;
			userInfo.userName = sharedPreferences.getString("userName", DEF_VALUE);
			userInfo.token = sharedPreferences.getString("token", DEF_VALUE);
			userInfo.uid = sharedPreferences.getString("uid", DEF_VALUE);
			userInfo.msgToken = sharedPreferences.getString("msgToken", DEF_VALUE);
			// verify values
			if(userInfo.userName.equals(DEF_VALUE) ||
					userInfo.token.equals(DEF_VALUE)||
					userInfo.uid.equals(DEF_VALUE)) {
				isLogedin = false;
			}
		}
	}
	
	public void login(String userName, String password) {
		if (userName == null || password == null) {
			mContext.sendBroadcast(new Intent(ZEN_LOGIN_FAILED));
			return;
		}
		if (connection != null) {
			connection.cancel();
			connection = null;
		}
		connection = new ZenURLConnection(ZEN_LOGIN_URL);
		connection.setHttpMethod("POST");
		connection.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		connection.setHttpBody("username=" + userName + "&password=" + ZenUtils.md5(password));
		connection.setOnResponseListener(mOnResponseListener);
		connection.startAsychronous();
	}
	
	public void logout() {
		userInfo = new ZenUserInfo();
		isLogedin = false;
		editor.clear();
		editor.commit();
	}
	
	private ZenOnResponseListener mOnResponseListener = new ZenOnResponseListener() {
		
		@Override
		public void OnResponse(String response) {
			try {
				if (response != null) {
					System.out.println("response: " + response);
					JSONObject json = new JSONObject(response);
					JSONObject result = json.getJSONObject("result");
					JSONObject user = result.getJSONObject("user");
					userInfo.userName = user.getString("username");
					userInfo.uid = user.getString("uid");
					userInfo.token = user.getString("token");
					isLogedin = true;
					save();
					fetchMSGToken();
					mContext.sendBroadcast(new Intent(ZEN_LOGIN_FINISHED));
				}
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("zen login failed!");
			mContext.sendBroadcast(new Intent(ZEN_LOGIN_FAILED));
		}
		
		@Override
		public void OnError(String msg) {
			mContext.sendBroadcast(new Intent(ZEN_LOGIN_FAILED));
		}
	};
	
	private void save() {
		if (editor != null) {
			System.out.println("User Info Saved");
			editor.putString("userName", userInfo.userName);
			editor.putString("uid", userInfo.uid);
			editor.putString("token", userInfo.token);
			editor.commit();
		}
	}
	
	private void parser(String response) {
		try {
			Document doc = Jsoup.parse(response);
			Element ele = doc.select("input#token").first();
			String token = ele.attr("value");
			userInfo.msgToken = token;
			editor.putString("msgToken", token);
			editor.commit();
			System.out.println("msgToken: " + token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void fetchMSGToken() {
		try {
			ZenURLConnection msgConnection = new ZenURLConnection(ZEN_MSG_TOKEN_URL);
			msgConnection.addRequestHeader("Accept-Encoding", "utf-8");
			msgConnection.addRequestHeader("Cookie", "u=" + URLEncoder.encode(userInfo.token, "utf-8"));
			msgConnection.setOnResponseListener(new ZenOnResponseListener() {
				
				@Override
				public void OnResponse(String response) {
					if (response != null) {
						parser(response);
						return;
					}
					System.out.println("Zen Fetch Msg Token Response is null");
				}
				
				@Override
				public void OnError(String msg) {
					System.out.println("Zen Fetch Msg Token error.");
				}
			});
			msgConnection.startAsychronous();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
