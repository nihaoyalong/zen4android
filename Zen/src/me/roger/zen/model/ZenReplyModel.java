package me.roger.zen.model;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.roger.zen.data.ZenThreadData;
import me.roger.zen.data.ZenThreadReply;
import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;
import me.roger.zen.utils.ZenUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

public class ZenReplyModel {

	public static final String ZenThreadDataDidFinishedLoad = "ZenThreadDataDidFinishedLoad";
	public static final String ZenThreadDataDidFailedLoad = "ZenThreadDataDidFailedLoad";
	public static final String ZenRepliesDidFinishedLoad = "ZenRepliesDidFinishedLoad";
	public static final String ZenRepliesDidFailedLoad = "ZenRepliesDidFailedLoad";
	public static final String ZenRepliesEmpty = "ZenRepliesEmpty";
	
	private static final String ZenThreadDataURL = "http://mobileapi.hupu.com/1/1.1.1/bbs/getthreaddata?type=2&tid=%s&boardpw=&token=";
	private static final String ZenThreadRepliesURL = "http://mobileapi.hupu.com/1/1.1.1/bbs/getthreadreplies?sort=1&tid=%s&page=%d&pagecount=20&boardpw=&token=";
	
	public ZenThreadData threadData;
	public List<ZenThreadReply> replies;
	public List<ZenThreadReply> lightReplies;
	public int pageCount;
	public int currentPage;
	public int floor;
	
	private Context mContext;
	private String mTid;
	
	private ZenURLConnection mConnection;
	
	public ZenReplyModel(Context context, String tid) {
		mContext = context;
		mTid = tid;
		replies = new ArrayList<ZenThreadReply>();
		lightReplies = new ArrayList<ZenThreadReply>();
		threadData = new ZenThreadData();
	}
	
	private String dateFormat(long timeIntivalSince1970) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return sdf.format(new Date(timeIntivalSince1970 * 1000));
	}
	
	private String purge(String content) throws Exception {
		content = content.replace("'", "\\'");
		content = content.replace("\r", "");
		content = content.replace("\n", "");
		return content;
	}
	
	private void parserThreadDataResponse(String response) {
		try {
			
			Intent successIntent = new Intent(ZenReplyModel.ZenThreadDataDidFinishedLoad);
			mContext.sendBroadcast(successIntent);
			JSONObject json = new JSONObject(response);
			JSONObject dict = json.getJSONObject("result");
			
			threadData.fid = dict.getString("fid");
			threadData.tid = dict.getString("tid");
			threadData.author = dict.getString("username");
			String date = dict.getString("postdate");
			threadData.postdate = dateFormat(Long.parseLong(date));
			threadData.subject = dict.getString("subject");
			threadData.replies = dict.getString("replies");
			threadData.lights = dict.getString("lights");
			String content = dict.getString("content");
			threadData.content = purge(content);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Intent errorIntent = new Intent(ZenReplyModel.ZenThreadDataDidFailedLoad);
		mContext.sendBroadcast(errorIntent);
	}
	
	private ZenThreadReply zenReplyDataWithJSON(JSONObject json) throws Exception {
		ZenThreadReply data = new ZenThreadReply();
		data.author = json.getString("author");
		data.authorId = json.getString("authorid");
		String content = json.getString("content");
		data.content = purge(content);
		data.postDate = json.getString("postdate");
		if (json.has("light")) {
			data.light = json.getString("light");
		}
		else {
			data.light = "0";
		}
		data.pid = json.getString("pid");
		return data;
	}
	
	private void parserThreadRepliesResponse(String response) {
		try {
			JSONObject json = new JSONObject(response);
			JSONObject dict = json.getJSONObject("result");
			pageCount = dict.getInt("pagecount");
			currentPage = dict.getInt("page");
			floor = ((currentPage - 1) * 20 + 1);
			
			JSONObject data = dict.getJSONObject("data");
			replies.clear();
			if (data.has("replies")) {
				JSONArray repliesJSON = data.getJSONArray("replies");
				
				for (int i = 0; i < repliesJSON.length(); i++) {
					JSONObject object = repliesJSON.getJSONObject(i);
					ZenThreadReply reply = zenReplyDataWithJSON(object);
					if (reply != null) {
						replies.add(reply);
					}
				}
			}
			
			lightReplies.clear();
			if (data.has("lightReplies")) {
				JSONArray lightRepliesJSON = data.getJSONArray("lightReplies");
				
				for (int i = 0; i < lightRepliesJSON.length(); i++) {
					JSONObject object = lightRepliesJSON.getJSONObject(i);
					ZenThreadReply reply = zenReplyDataWithJSON(object);
					if (reply != null) {
						lightReplies.add(reply);
					}
				}
			}
			
			
			Intent successIntent = new Intent(ZenReplyModel.ZenRepliesDidFinishedLoad);
			mContext.sendBroadcast(successIntent);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(ZenReplyModel.ZenRepliesEmpty);
		mContext.sendBroadcast(intent);
	}
	
	public void loadThreadData() {
		try {
			String url = String.format(ZenThreadDataURL, mTid);
			String token = ZenUtils.getToken();
			if (token != null) {
				url = url + URLEncoder.encode(token, "utf-8");
			}
			else {
				url = url + "null";
			}
			System.out.println("loadThreadData: " + url);
			ZenURLConnection conn = new ZenURLConnection(url);
			mConnection = conn;
			conn.setOnResponseListener(new ZenOnResponseListener() {
				
				@Override
				public void OnResponse(String response) {
					parserThreadDataResponse(response);
				}
				
				@Override
				public void OnError(String msg) {
					Intent errorIntent = new Intent(ZenReplyModel.ZenThreadDataDidFailedLoad);
					mContext.sendBroadcast(errorIntent);
				}
			});
			mConnection.startAsychronous();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressLint("DefaultLocale")
	public void loadReplies(int page) {
		try {
			currentPage = page;
			floor = ((currentPage - 1) * 20 + 1);
			String url = String.format(ZenThreadRepliesURL, mTid, page);
			String token = ZenUtils.getToken();
			if (token != null) {
				url = url + URLEncoder.encode(token, "utf-8");
			}
			else {
				url = url + "null";
			}
			ZenURLConnection conn = new ZenURLConnection(url);
			mConnection = conn;
			mConnection.setOnResponseListener(new ZenOnResponseListener() {
				
				@Override
				public void OnResponse(String response) {
					parserThreadRepliesResponse(response);
				}
				
				@Override
				public void OnError(String msg) {
					Intent errorIntent = new Intent(ZenReplyModel.ZenRepliesDidFailedLoad);
					mContext.sendBroadcast(errorIntent);
				}
			});
			mConnection.startAsychronous();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//================================================================================
	// Utils for caller
	//================================================================================
	
	public void refresh() {
		if(currentPage == 1) {
			loadThreadData();
		}
		else {
			loadReplies(currentPage);
		}
	}
	
	public void prev() {
		currentPage--;
		if (currentPage == 1) {
			loadThreadData();
		}
		else {
			loadReplies(currentPage);
		}
	}
	
	public void next() {
		currentPage++;
		loadReplies(currentPage);
	}
}
