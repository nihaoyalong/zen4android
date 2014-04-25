package me.roger.zen.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.roger.zen.application.ZenApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;

public class ZenJSONUtil {
	public static final String ZEN_TOPIC_FAV_FID = "1119";
	public static final String ZEN_TOPIC_FID = "0212";
	public static List<Map<String, String>> headers;
	public static Map<String, List<Map<String, String>>> boards;
	private static final String ZEN_MY_BOARDS_FID = "1002";
	public static final String ZEN_MY_BOARDS_JSON = "myboards.json";
	
	public static void loadBoardsFromJSON(String fileName) {
		try {
			String json = ZenJSONUtil.JSONFromAssetsFile("boards.json");
			JSONArray array = new JSONArray(json);
			headers = new ArrayList<Map<String, String>>();
			boards = new HashMap<String, List<Map<String, String>>>();
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject board = array.getJSONObject(i);
				String fid = board.getString("fid");
				String name = board.getString("name");
				Map<String, String> map = new HashMap<String, String>();
				map.put("fid", fid);
				map.put("name", name);
				headers.add(map);
				if (fid.equals(ZEN_MY_BOARDS_FID)) {
					reloadMyBoards();
					continue;
				}
				List<Map<String, String>> list = ZenJSONUtil.boardsFromJSON("boards_" + fid + ".json");
				boards.put(fid, list);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void WriteJSONToFile(String fileName, String json) {
		try {
			Context context = ZenApplication.getAppContext();
			OutputStream output = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			output.write(json.getBytes("utf-8"));
			output.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String ReadJSONFromFile(String fileName) {
		try {
			Context context = ZenApplication.getAppContext();
			InputStream input = context.openFileInput(fileName);
			byte[] data = new byte[input.available()];
			input.read(data);
			input.close();
			return new String(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void reloadMyBoards() {
		try {
			String json = ReadJSONFromFile(ZEN_MY_BOARDS_JSON);
			if (json != null) {
				JSONArray array = new JSONArray(json);
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				
				Map<String, String> fav = new HashMap<String, String>();
				fav.put("fid", ZEN_TOPIC_FAV_FID);
				fav.put("name", "我的收藏");
				list.add(fav);
				
				Map<String, String> topic = new HashMap<String, String>();
				topic.put("fid", ZEN_TOPIC_FID);
				topic.put("name", "我的主题");
				list.add(topic);
				
				for(int i = 0; i < array.length(); i++) {
					JSONObject board = array.getJSONObject(i);
					String fid = board.getString("fid");
					String name = board.getString("name");
					Map<String, String> map = new HashMap<String, String>();
					map.put("fid", fid);
					map.put("name", name);
					list.add(map);
				}	
				boards.put(ZEN_MY_BOARDS_FID, list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	static String JSONFromAssetsFile(String fileName) {
		Context context = ZenApplication.getAppContext();
		AssetManager manager = context.getAssets();
		InputStream file;
		try {
			file = manager.open(fileName);
			byte[] data = new byte[file.available()];
			file.read(data);
			file.close();
			return new String(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static List<Map<String, String>> boardsFromJSON(String fileName) {
		try {
			String json = ZenJSONUtil.JSONFromAssetsFile(fileName);
			JSONArray array = new JSONArray(json);
			
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject board = array.getJSONObject(i);
				String fid = board.getString("fid");
				String name = board.getString("name");
				Map<String, String> map = new HashMap<String, String>();
				map.put("fid", fid);
				map.put("name", name);
				list.add(map);
			}
		
			return list;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
