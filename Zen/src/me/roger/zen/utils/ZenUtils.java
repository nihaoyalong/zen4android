package me.roger.zen.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import me.roger.zen.application.ZenApplication;
import me.roger.zen.model.ZenLoginModel;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ZenUtils {

	private static final int ZEN_IMAGE_WIDTH = 480;
	
	public static String md5(String string) {
		byte[] hash;
		try {
			hash = MessageDigest.getInstance("MD5").digest(
					string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Huh, MD5 should be supported?", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Huh, UTF-8 should be supported?", e);
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
	}

	public static String getToken() {
		ZenLoginModel model = ZenLoginModel.getInstance();
		if (model.isLogedin) {
			return model.userInfo.token;
		}
		return null;
	}
	
	public static String getUid() {
		ZenLoginModel model = ZenLoginModel.getInstance();
		if (model.isLogedin) {
			return model.userInfo.uid;
		}
		return null;
	}
	
	public static String getMsgToken() {
		ZenLoginModel model = ZenLoginModel.getInstance();
		if (model.isLogedin) {
			return model.userInfo.msgToken;
		}
		return null;
	}

	public static String timestamp() {
		Date date = new Date();
		long time = date.getTime();
		return "" + time;
	}

	public static InputStream resizeImage(String path, String name) {
		try {
			Context context = ZenApplication.getAppContext();
			Bitmap bm = decodeImage(path);
			if (bm != null) {
				OutputStream out = context.openFileOutput(name, Context.MODE_PRIVATE);
				bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();
				InputStream input = context.openFileInput(name);
				return input;
			}
						
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap decodeImage(String path) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			options.inJustDecodeBounds = false;
			int be = (int) (options.outWidth/(float)ZEN_IMAGE_WIDTH);
			
			if (be <= 0) {
				be = 1;
			}
				
			options.inSampleSize = be;
			Bitmap bitmap = BitmapFactory.decodeFile(path, options);
			return bitmap;
		}catch (OutOfMemoryError error) {
			error.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap decodeInputStream(InputStream is) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, options);
			options.inJustDecodeBounds = false;
			int be = (int) (options.outWidth/(float)ZEN_IMAGE_WIDTH);
			
			if (be <= 0) {
				be = 1;
			}
				
			options.inSampleSize = be;
			Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap decodeByteArray(byte[] data) {
		try {
			if (data == null) {
				return null;
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, options);
			options.inJustDecodeBounds = false;
			int be = (int) (options.outWidth/(float)ZEN_IMAGE_WIDTH);
			
			if (be <= 0) {
				be = 1;
			}
				
			options.inSampleSize = be;
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String sub(String begin, String end, String src) throws Exception {
		int indexBegin = src.indexOf(begin);
		if (indexBegin != -1) {
			String sub = src.substring(indexBegin + begin.length());
			if (end == null) {
				return sub;
			}
			int indexEnd = sub.indexOf(end);
			if (indexEnd != -1) {
				return sub.substring(0, indexEnd);
			}
		}
		return null;
	}
}
