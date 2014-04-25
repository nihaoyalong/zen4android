package me.roger.zen.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.roger.zen.application.ZenApplication;
import me.roger.zen.utils.ZenUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

public class ZenAssetsModel {
	public static final String ZEN_PARSER_FINISHED = "zen_parser_finished";
	public static final String ZEN_PARSER_FAILED = "zen_parser_failed";
	
	private static ZenAssetsModel instance;
	private ArrayList<Map<String, Object>> images;
	private Context mContext;
	public Bitmap album;
	
	public static ZenAssetsModel getInstance() {
		if (instance == null) {
			instance = new ZenAssetsModel();
		}
		
		return instance;
	}
	
	public ZenAssetsModel() {
		images = new ArrayList<Map<String, Object>>();
		mContext = ZenApplication.getAppContext();
	}
	
	public void clear() {
		images.clear();
	}
	
	@SuppressWarnings("unchecked")
	public void parser(ArrayList<String> paths) {
		ZenParserTask task = new ZenParserTask();
		task.execute(paths);
	}
	
	public ArrayList<Map<String, Object>> getAssets() {
		return images;
	}
	
	private class ZenParserTask extends AsyncTask<ArrayList<String>, Void, ArrayList<Map<String, Object>>> {

		@Override
		protected ArrayList<Map<String, Object>> doInBackground(ArrayList<String>... params) {
			try {
				ArrayList<String> paths = params[0];
				ArrayList<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
				if (paths.size() > 0) {
					String path = paths.get(0);
					album = ZenUtils.decodeImage(path);
				}
				
				for (int i = 0; i < paths.size(); i++) {
					String path = paths.get(i);
					if (path.toLowerCase().endsWith(".gif")) {
						File file = new File(path);
						if (file.exists()) {
							InputStream input = new FileInputStream(file);
							if (input != null) {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("type", "gif");
								map.put("input", input);
								array.add(map);
							}
						}
					}
					else {
						InputStream input = ZenUtils.resizeImage(path, "upload" + i);
						if (input != null) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("type", "jpg");
							map.put("input", input);
							array.add(map);
						}
					}
					
					
				}
				return array;
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			return null;
		}

		
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(ArrayList<Map<String, Object>> result) {
			images = result;
			mContext.sendBroadcast(new Intent(ZEN_PARSER_FINISHED));
		}
	}
}
