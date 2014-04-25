package me.roger.zen.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;

public class ZenURLConnection {
	
	private HttpURLConnection mConnection;
	public String mURL;
	private String mHttpMethod;
	private Map<String, String> mRequestHeaders;
	private byte[] mHttpBody;
	private String mReadCharsetName;
	private String mWriteCharsetName;
	private ZenOnResponseListener mResponseListener;
	private ZenConnectionTask mTask;
	private InputStream mHttpInputStream;
	
	public ZenURLConnection() {
		mHttpMethod = "GET";
		mRequestHeaders = new HashMap<String, String>();
		mReadCharsetName = "utf-8";
		mWriteCharsetName = "utf-8";
	}
	
	public ZenURLConnection(String url) {
		mURL = url;
		mHttpMethod = "GET";
		mRequestHeaders = new HashMap<String, String>();
		mReadCharsetName = "utf-8";
		mWriteCharsetName = "utf-8";
	}
	
	public void setReadCharsetName(String name) {
		mReadCharsetName = name;		
	}
	
	public void setWriteCharsetName(String name) {
		mWriteCharsetName = name;
	}
	
	public void setOnResponseListener(ZenOnResponseListener listener) {
		mResponseListener = listener;
	}
	
	public void setHttpMethod(String method) {
		mHttpMethod = method;
	}
	
	public void setHttpInputStream(InputStream input) {
		mHttpInputStream = input;
	}
	
	public void addRequestHeader(String key, String value) {
		mRequestHeaders.put(key, value);		
	}
	
	public void setHttpBody(String body) {
		try {
			mHttpBody = body.getBytes(mWriteCharsetName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void setHttpBodyBytes(byte[] body) {
		mHttpBody = body;
	}
	
	private HttpURLConnection connection() throws IOException {
		URL url = new URL(mURL);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setReadTimeout(20000); // milliseconds
		conn.setConnectTimeout(20000); // milliseconds
		conn.setRequestMethod(mHttpMethod);
		conn.setDoInput(true);
		if(mHttpMethod.equalsIgnoreCase("POST")) {
			conn.setDoOutput(true);
			conn.setUseCaches(false);
		}
		for(String key : mRequestHeaders.keySet()) {
			String value = mRequestHeaders.get(key);
			conn.setRequestProperty(key, value);
			//System.out.println("header: key: " + key + " value: " + value);
		}
		
		return conn;
	}
	
	public static String readData(InputStream inSream, String charsetName) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while( (len = inSream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inSream.close();
        return new String(data, charsetName);
    }
	
	public void cancel() {
		
		if(mTask != null) {
			mResponseListener = null;
			mTask.cancel(true);
		}
		else {
			mConnection.disconnect();
		}
	}
    
	
	public String startSychronous() {
		try {
			mConnection = connection();
			mConnection.connect();
			
			if(mConnection.getRequestMethod().equalsIgnoreCase("POST")) {
				OutputStream out = (OutputStream)mConnection.getOutputStream();
				if (mHttpBody != null) {
					out.write(mHttpBody);
				}
				else if (mHttpInputStream != null){
					byte[] buffer = new byte[1024];
					int len = -1;
					while ((len = mHttpInputStream.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
					mHttpInputStream.close();
					out.close();
				}
				
			}
			
			InputStream in = mConnection.getInputStream();
			String response = readData(in, mReadCharsetName);
			mConnection.disconnect();
			return response;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(mResponseListener != null) {
			mResponseListener.OnError("error");
		}
		
		if (mConnection != null) {
			mConnection.disconnect();
		}
		return null;
	}
	
	public void startAsychronous() {
		mTask = new ZenConnectionTask();
		mTask.execute();
	}
	
	private class ZenConnectionTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			
			String response = startSychronous();
			return response;
			
		}

		
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			if(mConnection != null) {
				mConnection.disconnect();
			}
		}



		@Override
		protected void onPostExecute(String result) {
			if(mResponseListener != null) {
				mResponseListener.OnResponse(result);
				mResponseListener = null;
			}
		}
	}
	
	public interface ZenOnResponseListener {
		public void OnResponse(String response);
		public void OnError(String msg);
	}
}
