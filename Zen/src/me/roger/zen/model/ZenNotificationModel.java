package me.roger.zen.model;

import java.net.URLEncoder;
import java.util.ArrayList;

import me.roger.zen.application.ZenApplication;
import me.roger.zen.data.ZenNotification;
import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;
import me.roger.zen.utils.ZenUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.content.Intent;

public class ZenNotificationModel {
	
	public static final String ZEN_NEW_NOTIFICATION = "zen_new_notification";
	public static final String ZEN_NOTIFICATION_EMPTY = "zen_notification_empty";
	public static final String ZEN_LOAD_NOTIFICATION_FAILED = "zen_load_notification_failed";
	
	private static ZenNotificationModel instance;
	private static final String ZEN_NOTIFICATION_URL = "http://my.hupu.com/notifications/json/";
	private static final String ZEN_PM_URL = "http://my.hupu.com/mymsg.php";
	private static final String ZEN_IGNORE_URL = "http://my.hupu.com/include/remind_act.php?jsoncallback=jsonp";
	
	private Context mContext;
	private ZenURLConnection mConnection;
	public ArrayList<ZenNotification> notifications;
	
	public static ZenNotificationModel getInstance() {
		if (instance == null) {
			instance = new ZenNotificationModel();
		}
		return instance;
	}
	
	public ZenNotificationModel() {
		notifications = new ArrayList<ZenNotification>();
		mContext = ZenApplication.getAppContext();
	}
	
	public void load() {
		try {
			String token = ZenUtils.getToken();
			if (token != null) {
				if (mConnection != null) {
					mConnection.cancel();
				}
				mConnection = new ZenURLConnection(ZEN_NOTIFICATION_URL);
				mConnection.addRequestHeader("Accept-Encoding", "utf-8");
				mConnection.addRequestHeader("Cookie", "u=" + URLEncoder.encode(token, "utf-8") + ";");
				mConnection.setOnResponseListener(mResponseListener);
				mConnection.startAsychronous();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
				
	}
	
	private ZenOnResponseListener mResponseListener = new ZenOnResponseListener() {
		
		@Override
		public void OnResponse(String response) {
			System.out.println("notification: " + response);
			parser(response);
		}
		
		@Override
		public void OnError(String msg) {
			mContext.sendBroadcast(new Intent(ZEN_LOAD_NOTIFICATION_FAILED));
		}
	};
	
	
	private void parser(String response) {
		try {
			notifications.clear();
			JSONObject json = new JSONObject(response);
			if (json.has("msg")) {
				int msg = json.getInt("msg");
				if (msg > 0) {
					ZenNotification item = new ZenNotification();
					item.msg = "¶ÌÏûÏ¢(" + msg + "ÌõÎ´¶Á)";
					item.type = ZenNotification.ZEN_TYPE_MESSAGE;
					item.href= ZEN_PM_URL;
					item.nid = "0";
					notifications.add(item);
				}
			}
			if (json.has("notificationsinfo")) {
				JSONArray array = json.getJSONArray("notificationsinfo");
				for (int i = 0; i < array.length(); i++) {
					ZenNotification item = new ZenNotification();
					JSONObject obj = array.getJSONObject(i);
					String id = obj.getString("id");
					String title = obj.getString("title");
					Document doc = Jsoup.parse(title);
					Element info = doc.select("a").first();
					String msg = doc.body().text();
					String href = info.attr("href");
					item.nid = id;
					item.msg = msg;
					item.href = href;
					item.type = ZenNotification.ZEN_TYPE_MESSAGE;
					// parser href
					int lastIndexOfSlash = href.lastIndexOf("/");
					String sub = href.substring(lastIndexOfSlash + 1);
					String pid = "0";
					int indexOfPound = sub.indexOf("#");
					
					if (indexOfPound != -1) {
						pid = sub.substring(indexOfPound + 1);
						item.pid = pid;
						int indexOfDot = sub.indexOf(".");
						sub = sub.substring(0, indexOfDot);
						String [] elements = sub.split("-");
						if (elements != null && elements.length == 2) {
							item.tid = elements[0];
							item.page = elements[1];
						}
						else {
							item.tid = sub;
							item.page = "1";
						}
						item.type = ZenNotification.ZEN_TYPE_THREAD;
					}
					
					notifications.add(item);
					
					System.out.println("id: " + item.nid + "\nmsg: " + item.msg + "\nhref: " + item.href + 
							"\ntid: " + item.tid + "\npage: " + item.page + "\npid: " + item.pid);

				}
				
			}
			if (notifications.size() > 0) {
				mContext.sendBroadcast(new Intent(ZEN_NEW_NOTIFICATION));
			}
			else {
				mContext.sendBroadcast(new Intent(ZEN_NOTIFICATION_EMPTY));
			}
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		mContext.sendBroadcast(new Intent(ZEN_LOAD_NOTIFICATION_FAILED));
	}
	
	public void clear() {
		notifications.clear();
		mContext.sendBroadcast(new Intent(ZEN_NOTIFICATION_EMPTY));
	}
	
	public void ignore(String nid) {
		try {
			String token = ZenUtils.getToken();
			if (token != null) {
				String url = ZEN_IGNORE_URL + ZenUtils.timestamp() + "&id=" + nid;
				System.out.println("ignore url: " + url);
				ZenURLConnection connection = new ZenURLConnection(url);
				connection.addRequestHeader("Cookie", "u=" + URLEncoder.encode(token, "utf-8") + ";");
				connection.startAsychronous();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
