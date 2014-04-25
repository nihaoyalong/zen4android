package me.roger.zen.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import me.roger.zen.data.ZenThreadData;
import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ZenThreadsModel {
	public static final String DidFinishedLoad = "ZenThreadsDidFinishedLoad";
	public static final String DidFailedLoad = "ZenThreadsDidFailedLoad";
	private static final String ZenThreadsURL = "http://mobileapi.hupu.com/1/1.1.1/bbs/getboardthreads?order=1&fid=%s&page=%d&num=20&boardpw=&token=null";
	private Context mContext;
	private String mFid;
	private int mPage;
	private boolean isCancelled;
	private boolean clearFirst;
	
	private ZenURLConnection mConnection;
	public ArrayList<ZenThreadData> threads;
	
	public ZenThreadsModel(Context context, String fid) {
		mContext = context;
		mFid = fid;
		threads = new ArrayList<ZenThreadData>();
	}
	
	private boolean contains(ZenThreadData data) {
		for(ZenThreadData thread : threads) {
			if (data.tid.equals(thread.tid)) {
				return true;
			}
		}
		return false;
	}
	
	private String dateFormat(long timeIntivalSince1970) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return sdf.format(new Date(timeIntivalSince1970 * 1000));
	}
	
	private void parser(String response, boolean clearFlag) {
		if(response == null) {
			if (mContext != null) {
				Intent intent = new Intent(ZenThreadsModel.DidFailedLoad);
				mContext.sendBroadcast(intent);
			}
		}
		try {
			JSONObject object = new JSONObject(response);
			
			JSONArray array = object.getJSONArray("result");
			if(clearFlag) {
				threads.clear();
			}
			for (int i = 0; i < array.length(); i++) {
				ZenThreadData thread = new ZenThreadData();
				JSONObject json = array.getJSONObject(i);
				thread.author = json.getString("username");
				thread.fid = json.getString("fid");
				thread.tid = json.getString("tid");
				thread.lights = json.getString("lights");
				String replies = json.getString("replies");
				
				if (Integer.parseInt(replies) > 999) {
					replies = "999";
				}
				thread.replies = replies;
				thread.subject = json.getString("subject");
				String postdate = json.getString("postdate");
				thread.postdate = dateFormat(Long.parseLong(postdate));
				if (!contains(thread)) {
					threads.add(thread);
				}
			}
			
			Intent intent = new Intent(ZenThreadsModel.DidFinishedLoad);
			mContext.sendBroadcast(intent);
			return;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (mContext != null) {
			Intent intent = new Intent(ZenThreadsModel.DidFailedLoad);
			mContext.sendBroadcast(intent);
		}
	}
	
	@SuppressLint("DefaultLocale")
	private void load(int page) {
		isCancelled = false;
		if(mConnection != null){
			mConnection.cancel();
		}
		String url = String.format(ZenThreadsModel.ZenThreadsURL, mFid, page);
		mConnection = new ZenURLConnection(url);
		mConnection.setOnResponseListener(new ZenOnResponseListener() {
			
			@Override
			public void OnResponse(String response) {
				if (response == null) {
					Intent intent = new Intent(ZenThreadsModel.DidFailedLoad);
					mContext.sendBroadcast(intent);
					return;
				}
				if(!isCancelled) {
					parser(response, clearFirst);
				}
			}
			
			@Override
			public void OnError(String msg) {
				Log.d("Zen", msg);
				Intent intent = new Intent(ZenThreadsModel.DidFailedLoad);
				mContext.sendBroadcast(intent);
			}
		});
		mConnection.startAsychronous();
	}
	
	public void cancel() {
		if(mConnection != null){
			mConnection.cancel();
		}
		isCancelled = true;
	}
	
	public void refresh() {
		clearFirst = true;
		mPage = 1;
		load(mPage);
	}
	
	public void loadMore() {
		clearFirst = false;
		mPage++;
		load(mPage);
	}
}
