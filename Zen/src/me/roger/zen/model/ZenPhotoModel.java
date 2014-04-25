package me.roger.zen.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.roger.zen.application.ZenApplication;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class ZenPhotoModel {
	
	public static final String ZEN_PHOTO_FINISHED = "zen_photo_finished";
	public static final String ZEN_PHOTO_FAILED = "zen_photo_failed";
	
	public static final String ZEN_TEMP_FILE = "zen_temp";
	
	private HttpURLConnection connection;
	
	private ZenLoadTask mTask;
	
	public static void writeToFile(InputStream inSream) throws IOException {
		Context context = ZenApplication.getAppContext();
        OutputStream outStream = context.openFileOutput(ZEN_TEMP_FILE, Context.MODE_PRIVATE);
        byte[] buffer = new byte[1024];
        int len = -1;
        while( (len = inSream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inSream.close();
    }
	
	public void load(String url) {
		if(mTask != null) {
			mTask.cancel(true);
			mTask = null;
		}
		mTask = new ZenLoadTask();
		mTask.execute(url);
	}
	
	private boolean download(String url) {
		try {
			cancel();
			connection = connection(url);
			connection.connect();
			InputStream in = connection.getInputStream();
			writeToFile(in);
			connection.disconnect();
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		Context context = ZenApplication.getAppContext();
		context.sendBroadcast(new Intent(ZEN_PHOTO_FAILED));
		return false;
	}

	private HttpURLConnection connection(String url) throws IOException {
		URL mURL = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)mURL.openConnection();
		conn.setReadTimeout(20000); // milliseconds
		conn.setConnectTimeout(20000); // milliseconds
		conn.setRequestMethod("GET");
		return conn;
	}
	
	public void cancel() {
		try {
			if (connection != null) {
				connection.disconnect();
				connection = null;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private class ZenLoadTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			
			String url = params[0];
			boolean flag = download(url);
			return Boolean.valueOf(flag);
		}

		
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			
		}



		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result.booleanValue()) {
				Context context = ZenApplication.getAppContext();
				context.sendBroadcast(new Intent(ZEN_PHOTO_FINISHED));
			}
		}
	}
	
	public interface ZenOnResponseListener {
		public void OnResponse(String response);
		public void OnError(String msg);
	}
	
}
