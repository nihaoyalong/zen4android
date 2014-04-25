package me.roger.zen.model;

import java.net.URLEncoder;

import me.roger.zen.application.ZenApplication;
import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;
import me.roger.zen.utils.ZenUtils;
import android.content.Context;
import android.content.Intent;

public class ZenArchiveModel {
	public static final String ZEN_ARCHIVE_SUCCESS = "zen_archive_success";
	public static final String ZEN_ARCHIVE_FAILED = "zen_archive_failed";
	
	private static String ZEN_ARCHIVE_URL = "http://bbs.hupu.com/job.php?action=favor&job=add&tid=";
	private ZenURLConnection mConnection;
	private Context mContext;
	
	public ZenArchiveModel() {
		mContext = ZenApplication.getAppContext();
	}
	
	public void archive(String tid) {
		try {
			if (mConnection != null) {
				mConnection.cancel();
				mConnection = null;
			}
			String token = ZenUtils.getToken();
			String tokenEncoded = "u=" + URLEncoder.encode(token, "utf-8") + ";";
			System.out.println("token:" + tokenEncoded);
			String url = ZEN_ARCHIVE_URL + tid;
			String body = "tid=" + tid + "&skin=fbd";
			mConnection = new ZenURLConnection(url);
			mConnection.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			mConnection.addRequestHeader("Cookie", "u="+token+";");
			mConnection.setHttpMethod("POST");
			mConnection.setHttpBody(body);
			mConnection.setOnResponseListener(new ZenOnResponseListener() {
				
				@Override
				public void OnResponse(String response) {
					System.out.println("archive reponse: " + response);
					if (response != null && response.equals("2")) {
						mContext.sendBroadcast(new Intent(ZEN_ARCHIVE_SUCCESS));
						return;
					}
					mContext.sendBroadcast(new Intent(ZEN_ARCHIVE_FAILED));
					
				}
				
				@Override
				public void OnError(String msg) {
					mContext.sendBroadcast(new Intent(ZEN_ARCHIVE_FAILED));				
				}
			});
			mConnection.startAsychronous();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
