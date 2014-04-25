package me.roger.zen.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import me.roger.zen.application.ZenApplication;
import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenUtils;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;


public class ZenPostModel {
	
	public static final String ZEN_POST_SUCCESS = "zen_post_success";
	public static final String ZEN_POST_FAILED = "zen_post_failed";
	
	private static final String ZEN_UPLOAD_URL = "http://bbs.hupu.com/template/fbd/coldwindeditor/upimage.php";
	private static final String ZEN_UPDATE_SIGN_URL = "http://bbs.hupu.com/post.php?fid=";
	private static final String ZEN_NEW_POST_URL = "http://bbs.hupu.com/post.php?";
	private static final String ZEN_TAIL = "<br><br><a href='http://rogerqian.github.io/zen_1.2.1.apk'>ю╢вт Zen For Android </a>";
	private String mTime;
	private String mFid;
	private String mSign;

	private ZenURLConnection mConnection;
	private ZenURLConnection mUploadConnection;

	public ZenPostModel(String fid) {
		mFid = fid;
	}
	
	private InputStream bodyForUploadImage(InputStream image, String boundary, boolean isGif) {
		try {
			Context context = ZenApplication.getAppContext();
			String boundaryNormal = "\r\n--" + boundary + "\r\n";
			String boundaryEnd = "\r\n--" + boundary + "--\r\n";

			String timestamp = ZenUtils.timestamp();
			StringBuffer buf = new StringBuffer();
			OutputStream out = context.openFileOutput("zen_upload", Context.MODE_PRIVATE);
			buf.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"name\"\r\n\r\n"
							+ timestamp + (isGif ? ".gif" : ".jpg"))
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"pic_upload\"\r\n\r\n0")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"watermark\"\r\n\r\n0")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"albumId\"\r\n\r\ndefault")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"imagefile[]\"; filename=\""
							+ timestamp
							+ (isGif ? ".gif\"" : ".jpg\"") + "\r\nContent-Type: " + (isGif ? "image/gif\r\n\r\n" : "image/jpeg\r\n\r\n"));
			
			out.write(buf.toString().getBytes("utf-8"));
			byte[] buffer = new byte[1024];
	        int len = -1;
	        while( (len = image.read(buffer)) != -1 ){
	            out.write(buffer, 0, len);
	        }
	        image.close();
			out.write(boundaryEnd.toString().getBytes("utf-8"));
			out.close();
			InputStream input = context.openFileInput("zen_upload");
			return input;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public String uploadImage(InputStream image, boolean isGif) {
		try {
			String token = ZenUtils.getToken();
			if (token != null) {
				String boundary = "----pluploadboundary" + ZenUtils.timestamp();
				mUploadConnection = new ZenURLConnection(ZEN_UPLOAD_URL);
				mUploadConnection.setHttpMethod("POST");
				mUploadConnection.addRequestHeader("Cookie",
						"u=" + URLEncoder.encode(token, "utf-8"));
				mUploadConnection.addRequestHeader("Content-Type",
						"multipart/form-data; boundary=" + boundary);
				InputStream body = bodyForUploadImage(image, boundary, isGif);
				if (body != null) {
					mUploadConnection.setHttpInputStream(body);
					String response = mUploadConnection.startSychronous();
					if (response != null) {
						System.out.println("upload: " + response);
						JSONObject json = new JSONObject(response);
						if (json.has("pic")) {
							String url = json.getString("pic");
							return url;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void UpdateSign() {
		try {
			mTime = "";
			mSign = "";
			String token = ZenUtils.getToken();
			if (token != null) {
				ZenURLConnection connection = new ZenURLConnection(ZEN_UPDATE_SIGN_URL + mFid);
				connection.addRequestHeader("Cookie", "u=" + URLEncoder.encode(token, "utf-8") + ";");
				connection.addRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:25.0) Gecko/20100101 Firefox/25.0");
				String response = connection.startSychronous();
				if (response != null) {
					Document doc = Jsoup.parse(response);
					
					Element signEle = doc.select("input[name=sign]").first();
					mSign = signEle.attr("value");
					Element timeEle = doc.select("input[name=time]").first();
					mTime = timeEle.attr("value");
					
					System.out.println("time: " + mTime + " sign: " + mSign);
				}
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] bodyForPost(String title, String content,
			ArrayList<String> urls, String boundary) {

		try {
			String boundaryNormal = "\r\n--" + boundary + "\r\n";
			String boundaryEnd = "\r\n--" + boundary + "--\r\n";

			StringBuffer buf = new StringBuffer();
			buf.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"atc_title\"\r\n\r\n"
							+ title)
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"editnum\"\r\n\r\n3")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"nowitnum\"\r\n\r\n3")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"usevote\"\r\n\r\n0")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"douid\"\r\n\r\n1")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"votetype\"\r\n\r\nbbs")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"time\"\r\n\r\n"
							+ mTime)
					.append(boundaryNormal)

					.append("Content-Disposition: form-data; name=\"votetitle\"\r\n\r\n")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"votename[]\"\r\n\r\n")
					.append(boundaryNormal)

					.append("Content-Disposition: form-data; name=\"votename[]\"\r\n\r\n")
					.append(boundaryNormal)

					.append("Content-Disposition: form-data; name=\"votename[]\"\r\n\r\n")
					.append(boundaryNormal)

					.append("Content-Disposition: form-data; name=\"voteclass\"\r\n\r\n")
					.append(boundaryNormal)

					.append("Content-Disposition: form-data; name=\"atc_usesign\"\r\n\r\n1")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"atc_content\"\r\n\r\n"
							+ "<br>" + content + "<br>");

			if (urls != null) {
				for (String url : urls) {
					buf.append("<br><br><img src=\"" + url + "\"><br><br>");
				}
			}
			buf.append(ZEN_TAIL)
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"atc_autourl\"\r\n\r\n1")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"atc_convert\"\r\n\r\n1")
					.append(boundaryNormal);
			for (int i = 1; i < 11; i++) {
				buf.append(
						String.format(
								"Content-Disposition: form-data; name=\"atc_attachment%d\"; filename=\"\"\r\nContent-Type: application/octet-stream\r\n\r\n",
								i))
						.append(boundaryNormal)
						.append(String
								.format("Content-Disposition: form-data; name=\"atc_desc%d\"\r\n\r\n",
										i)).append(boundaryNormal);
			}

			buf.append("Content-Disposition: form-data; name=\"step\"\r\n\r\n2")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"pid\"\r\n\r\n")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"actionset\"\r\n\r\nnew")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"fid\"\r\n\r\n"
							+ mFid)
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"tid\"\r\n\r\n0")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"article\"\r\n\r\n")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"sale\"\r\n\r\n")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"sign\"\r\n\r\n"
							+ mSign)
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"replayofpage\"\r\n\r\n1")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"replaymeta\"\r\n\r\n1")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"classfbd\"\r\n\r\n0")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"classname\"\r\n\r\n")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"halfcourt\"\r\n\r\n1")
					.append(boundaryNormal)
					.append("Content-Disposition: form-data; name=\"atc_html\"\r\n\r\n1")
					.append(boundaryEnd);
			System.out.println("body for post: " + buf.toString());
			return buf.toString().getBytes("GBK");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean sendPost(String title, String content, ArrayList<String> urls) {
		boolean flag = false;
		try {
			String token = ZenUtils.getToken();
			String boundary = ZenUtils.timestamp();
			mConnection = new ZenURLConnection(ZEN_NEW_POST_URL);
			mConnection.setHttpMethod("POST");
			mConnection.addRequestHeader("Referer", ZEN_UPDATE_SIGN_URL + mFid);
			mConnection.addRequestHeader("Cookie", "u=" + token + ";");
			mConnection.addRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:25.0) Gecko/20100101 Firefox/25.0");
			mConnection.addRequestHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
			
			byte[] body = bodyForPost(title, content, urls, boundary);
			mConnection.setHttpBodyBytes(body);
			mConnection.startSychronous();
			flag = true;
			return flag;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;	
	}
	
	public void post(String title, String content) {
		ZenPostTask task = new ZenPostTask();
		task.execute(title, content);
	}
	
	private class ZenPostTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				boolean flag = false;
				String title = params[0];
				String content = params[1];
				ZenAssetsModel model = ZenAssetsModel.getInstance();
				ArrayList<Map<String, Object>> images = model.getAssets();
				ArrayList<String> urls = new ArrayList<String>();
				for (Map<String, Object> image : images) {
					String type = (String)image.get("type");
					InputStream obj = (InputStream)image.get("input");
					String url = null;
					if (type.equals("jpg")) {
						url = uploadImage(obj, false);
					}
					else {
						url = uploadImage(obj, true);
					}
					 
					if (url != null) {
						urls.add(url);
					}
				}
				UpdateSign();
				if (mSign != null && mTime != null) {
					flag = sendPost(title, content, urls);
					return Boolean.valueOf(flag);
				}
				
			} catch (Exception e) {
				System.out.print("ZenPostModel doInBackground exception");
				e.printStackTrace();
			}
			return Boolean.valueOf(false);						
		}

		
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Context context = ZenApplication.getAppContext();
			if (result.booleanValue()) {
				context.sendBroadcast(new Intent(ZenPostModel.ZEN_POST_SUCCESS));
			}
			else {
				context.sendBroadcast(new Intent(ZenPostModel.ZEN_POST_FAILED));
			}
		}
	}

}
