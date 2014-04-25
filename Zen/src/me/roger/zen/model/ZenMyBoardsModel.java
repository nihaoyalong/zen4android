package me.roger.zen.model;

import java.net.URLEncoder;

import me.roger.zen.utils.ZenJSONUtil;
import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;
import me.roger.zen.utils.ZenUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ZenMyBoardsModel {
	private static final String ZEN_MY_BOARDS_URL = "http://mobileapi.hupu.com/1/1.1.1/bbs/getusercollectedboards?token=";
	private static ZenMyBoardsModel instance;
	private ZenURLConnection mConnection;

	public static ZenMyBoardsModel getInstance() {
		if (instance == null) {
			instance = new ZenMyBoardsModel();
			
		}
		return instance;
	}
	
	public void load() {
		try {
			String token = ZenUtils.getToken();
			if (token != null) {
				if (mConnection != null) {
					mConnection.cancel();
				}
				String url = ZEN_MY_BOARDS_URL + URLEncoder.encode(token, "utf-8");
				mConnection = new ZenURLConnection(url);
				mConnection.setOnResponseListener(mOnResponseListener);
				mConnection.startAsychronous();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ZenOnResponseListener mOnResponseListener = new ZenOnResponseListener() {
		
		@Override
		public void OnResponse(String response) {
			System.out.println("my boards: " + response);
			try {
				if (response != null) {
					JSONObject json = new JSONObject(response);
					if (json.has("result")) {
						JSONArray boards = json.getJSONArray("result");
						String jsonString = boards.toString();
						ZenJSONUtil.WriteJSONToFile(ZenJSONUtil.ZEN_MY_BOARDS_JSON, jsonString);
						ZenJSONUtil.reloadMyBoards();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void OnError(String msg) {
			System.out.println("zen: load my boards failed.");
		}
	};
}
