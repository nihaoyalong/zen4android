package me.roger.zen.model;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import me.roger.zen.application.ZenApplication;
import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;
import me.roger.zen.utils.ZenUtils;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class ZenCommentModel {

	public static final String ZEN_LIGHT_FINISHED = "zen_light_finished";
	public static final String ZEN_LIGHT_FAILED = "zen_light_failed";
	public static final String ZEN_COMMENT_FINISHED = "zen_comment_finished";
	public static final String ZEN_COMMENT_FAILED = "zen_comment_failed";
	public static final String ZEN_RECOMMEND_FINISHED = "zen_recommend_finished";
	public static final String ZEN_RECOMMEND_FAILED = "zen_recommend_failed";
	public static final String ZEN_PM_FINISHED = "zen_pm_finished";
	public static final String ZEN_PM_FAILED = "zen_pm_failed";

	private static final String ZEN_LIGHT_URL = "http://mobileapi.hupu.com/1/1.1.1/bbs/setbbslights?fid=%s&tid=%s&pid=%s&token=%s";
	private static final String ZEN_COMMENT_URL = "http://mobileapi.hupu.com/1/1.1.1/bbs/replythread?boardpw=&token=";
	private static final String ZEN_NEW_COMMENT_URL = "http://bbs.hupu.com/post.php?";
	private static final String ZEN_RECOMMEND_URL = "http://bbs.hupu.com/indexinfo/buddys.php";
	private static final String ZEN_PM_URL = "http://my.hupu.com/mymsg.php?action=send";
	private static final String ZEN_TAIL = "<br><small class=\"f666\"><a style=\"color:#666\" href=\"http://rogerqian.github.com/zen_1.2.1.apk\" target=\"_blank\">来自 Zen For Android</a></small>";

	private Context mContext;
	private String mFid;
	private String mTid;
	private ZenURLConnection mConnection;

	public ZenCommentModel(Context context, String fid, String tid) {
		mContext = context;
		mFid = fid;
		mTid = tid;
	}
	
	public ZenCommentModel(Context context) {
		mContext = context;
	}

	// ================================================================================
	// light ...
	// ================================================================================

	public void light(String pid, String token) {
		try {
			if (mConnection != null) {
				mConnection.cancel();
				mConnection = null;
			}

			String url = String.format(Locale.getDefault(), ZEN_LIGHT_URL,
					mFid, mTid, pid, URLEncoder.encode(token, "utf-8"));
			mConnection = new ZenURLConnection(url);
			mConnection.setOnResponseListener(mLightListener);
			mConnection.startAsychronous();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private ZenOnResponseListener mLightListener = new ZenOnResponseListener() {

		@Override
		public void OnResponse(String response) {
			try {
				if (response != null) {
					JSONObject json = new JSONObject(response);
					if (json.has("result")) {
						JSONObject result = json.getJSONObject("result");
						String light = result.getString("light");
						Intent intent = new Intent(ZEN_LIGHT_FINISHED);
						intent.putExtra("light", light);
						mContext.sendBroadcast(intent);
					} else if (json.has("error")) {
						JSONObject error = json.getJSONObject("error");
						String msg = error.getString("text");
						Intent intent = new Intent(ZEN_LIGHT_FAILED);
						intent.putExtra("msg", msg);
						mContext.sendBroadcast(intent);
					}

					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Intent intent = new Intent(ZEN_LIGHT_FAILED);
			intent.putExtra("msg", "点亮失败");
			mContext.sendBroadcast(intent);
		}

		@Override
		public void OnError(String msg) {
			Intent intent = new Intent(ZEN_LIGHT_FAILED);
			intent.putExtra("msg", "点亮失败");
			mContext.sendBroadcast(intent);
		}
	};

	// ================================================================================
	// comment stuff
	// ================================================================================

	public String oldcomment(String content, String pid,
			ArrayList<String> urls) {
		try {
			if (mConnection != null) {
				mConnection.cancel();
				mConnection = null;
			}
			String token = ZenUtils.getToken();
			String tokenEncoded = URLEncoder.encode(token, "utf-8");
			mConnection = new ZenURLConnection(ZEN_COMMENT_URL + tokenEncoded);
			mConnection.setHttpMethod("POST");
			mConnection.addRequestHeader("Content-Type",
					"application/x-www-form-urlencoded ; charset=UTF-8");
			mConnection.addRequestHeader("Cookie", "u=" + tokenEncoded + ";");
			StringBuffer buf = new StringBuffer();
			buf.append(content);
			if (urls != null) {
				for (String url : urls) {
					buf.append("<br><br><img src=\"" + url + "\"><br><br>");
				}
			}

			String body = URLEncoder.encode(buf.toString(), "utf-8");
			String httpBody = "tid=" + mTid + "&content=" + body + "&quotepid="
					+ pid + "&boardpw=";
			mConnection.setHttpBody(httpBody);
			String response = mConnection.startSychronous();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void parser(String response) {
		try {
			if (response != null) {
				JSONObject json = new JSONObject(response);
				if (json.has("result")) {
					Intent intent = new Intent(ZEN_COMMENT_FINISHED);
					mContext.sendBroadcast(intent);
				} else if (json.has("error")) {
					JSONObject error = json.getJSONObject("error");
					String msg = error.getString("text");
					Intent intent = new Intent(ZEN_COMMENT_FAILED);
					intent.putExtra("msg", msg);
					mContext.sendBroadcast(intent);
				}
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent intent = new Intent(ZEN_COMMENT_FAILED);
		intent.putExtra("msg", "评论失败");
		mContext.sendBroadcast(intent);
	}

	// ================================================================================
	// new comment stuff
	// ================================================================================

	public void comment(String content, String pid, String title) {
		ZenCommentTask task = new ZenCommentTask();
		task.execute(content, pid, title);
	}

	private String newcomment(String content, String pid, String title,
			ArrayList<String> urls) {
		try {
			if (mConnection != null) {
				mConnection.cancel();
				mConnection = null;
			}
			String token = ZenUtils.getToken();
			String tokenEncoded = URLEncoder.encode(token, "utf-8");
			StringBuffer buf = new StringBuffer();
			StringBuffer contentBuf = new StringBuffer();
			contentBuf.append(content);
			if (urls != null) {
				for (String url : urls) {
					contentBuf.append("<br><br><img src=\"" + url
							+ "\"><br><br>");
				}
			}
			contentBuf.append(ZEN_TAIL);
			buf.append("atc_content="
					+ URLEncoder.encode(contentBuf.toString(), "GBK"));
			buf.append("&usevote=0&douid=1&votetype=bbs&votetitle=&votename[]=&votename[]=&votename[]=&editnum=3&nowitnum=3&voteclass=&postfast=2&atc_title=Re:" + title + "&atc_usesign=1&atc_convert=1&atc_autourl=1&step=2&");
			if (pid != null && !pid.equals("")) {
				buf.append("action=quote&pid=" + pid);
			} else {
				buf.append("action=reply");
			}
			buf.append("&fid=" + mFid);
			buf.append("&tid=" + mTid);
			buf.append("&subject=" + title + "&editor=0&atc_attachment=none&replayofpage=&replaymeta=1");

			String body = buf.toString();

			System.out.println("body: " + body);
			mConnection = new ZenURLConnection(ZEN_NEW_COMMENT_URL);
			mConnection.setHttpMethod("POST");
			mConnection.addRequestHeader("Content-Type",
					"application/x-www-form-urlencoded");
			mConnection
					.addRequestHeader(
							"User-Agent",
							"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:26.0) Gecko/20100101 Firefox/26.0");
			mConnection.addRequestHeader("Referer", "	http://bbs.hupu.com/"
					+ mTid + ".html");
			mConnection.addRequestHeader("Cookie", "u=" + tokenEncoded + ";");
			mConnection.setHttpBody(body);
			String response = mConnection.startSychronous();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private class ZenCommentTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				String content = params[0];
				String pid = params[1];
				String title = params[2];
				ZenAssetsModel model = ZenAssetsModel.getInstance();
				ArrayList<Map<String, Object>> images = model.getAssets();
				ArrayList<String> urls = new ArrayList<String>();
				ZenPostModel postModel = new ZenPostModel(mFid);
				for (Map<String, Object> image : images) {
					String type = (String) image.get("type");
					InputStream obj = (InputStream) image.get("input");
					String url = null;
					if (type.equals("jpg")) {
						url = postModel.uploadImage(obj, false);
					} else {
						url = postModel.uploadImage(obj, true);
					}

					if (url != null) {
						urls.add(url);
					}
				}

				if (pid != null && !pid.equals("")) {
					String response = oldcomment(content, pid, urls);
					return response;
				} else {
					String response = newcomment(content, pid, title, urls);
					if (response != null) {
						return "zen";
					}
				}

			} catch (Exception e) {
				System.out.print("ZenPostModel doInBackground exception");
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			Context context = ZenApplication.getAppContext();
			if (result != null) {
				if (result.indexOf("zen") != -1) {
					context.sendBroadcast(new Intent(ZEN_COMMENT_FINISHED));
				} else {
					parser(result);
				}

			} else {
				Intent intent = new Intent(ZEN_COMMENT_FAILED);
				intent.putExtra("msg", "发送失败");
				context.sendBroadcast(intent);
			}
		}
	}

	// ================================================================================
	// recommend stuff
	// ================================================================================

	public void recommend(String title, String content, String token) {
		try {
			if (mConnection != null) {
				mConnection.cancel();
				mConnection = null;
			}

			String tokenEncoded = URLEncoder.encode(token, "utf-8");
			mConnection = new ZenURLConnection(ZEN_RECOMMEND_URL);
			mConnection.setOnResponseListener(mRecommendListener);
			mConnection.setHttpMethod("POST");
			mConnection.addRequestHeader("Content-Type",
					"application/x-www-form-urlencoded ; charset=UTF-8");
			mConnection.addRequestHeader("Cookie", "u=" + tokenEncoded + ";");
			mConnection.addRequestHeader("X-Requested-With", "XMLHttpRequest");
			mConnection.addRequestHeader("Referer", "http://bbs.hupu.com/"
					+ mTid + ".html");

			String httpBody = "fid=" + mFid + "&act=rc" + "&cid=" + mTid
					+ "&title=" + URLEncoder.encode(title, "utf-8") + "&rmmsg="
					+ URLEncoder.encode(content, "utf-8") + "&type=1";
			mConnection.setHttpBody(httpBody);
			mConnection.startAsychronous();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(ZEN_RECOMMEND_FAILED);
		intent.putExtra("msg", "推荐失败...");
		mContext.sendBroadcast(intent);
	}

	private ZenOnResponseListener mRecommendListener = new ZenOnResponseListener() {

		@Override
		public void OnResponse(String response) {
			try {
				if (response != null) {
					String result = response.toLowerCase(Locale.getDefault());
					if (result.equals("1\n")) {
						Intent intent = new Intent(ZEN_RECOMMEND_FINISHED);
						intent.putExtra("msg", "推荐成功");
						mContext.sendBroadcast(intent);
					} else if (result.equals("level")) {
						Intent intent = new Intent(ZEN_RECOMMEND_FAILED);
						intent.putExtra("msg", "你的等级不够...");
						mContext.sendBroadcast(intent);
					}
					return;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			Intent intent = new Intent(ZEN_RECOMMEND_FAILED);
			intent.putExtra("msg", "推荐失败...");
			mContext.sendBroadcast(intent);
		}

		@Override
		public void OnError(String msg) {
			Intent intent = new Intent(ZEN_RECOMMEND_FAILED);
			intent.putExtra("msg", "推荐失败...");
			mContext.sendBroadcast(intent);
		}
	};

	// ================================================================================
	// recommend stuff
	// ================================================================================

	private String boundary() {
		return "-------------------------" + ZenUtils.timestamp();
	}

	private String bodyForPM(String msg, String to, String boundary) {
		String boundaryNormal = "\r\n--" + boundary + "\r\n";
		String boundaryEnd = "\r\n--" + boundary + "--\r\n";

		StringBuffer buf = new StringBuffer();

		buf.append(boundaryNormal)
				.append("Content-Disposition: form-data; name=\"userarr\"\r\n\r\n"
						+ to)
				.append(boundaryNormal)
				.append("Content-Disposition: form-data; name=\"msg_content\"\r\n\r\n"
						+ msg + ZEN_TAIL)
				.append(boundaryNormal)
				.append("Content-Disposition: form-data; name=\"uppic[]\"; filename=\"\" Content-Type: application/octet-stream\r\n\r\n")
				.append(boundaryNormal)
				.append("Content-Disposition: form-data; name=\"token\"\r\n\r\n"
						+ ZenUtils.getMsgToken()).append(boundaryEnd);
		return buf.toString();
	}

	public void send(String msg, String to) {
		try {
			String boundary = boundary();
			
			if (mConnection != null) {
				mConnection.cancel();
				mConnection = null;
			}

			mConnection = new ZenURLConnection(ZEN_PM_URL);
			mConnection.setHttpMethod("POST");
			mConnection.addRequestHeader("Cookie",
					"u=" + URLEncoder.encode(ZenUtils.getToken(), "utf-8")
							+ ";");
			mConnection.addRequestHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
			String body = bodyForPM(msg, to, boundary);
			mConnection.setHttpBodyBytes(body.getBytes("GBK"));
			mConnection.setOnResponseListener(new ZenOnResponseListener() {
				
				@Override
				public void OnResponse(String response) {
					Context context = ZenApplication.getAppContext();
					context.sendBroadcast(new Intent(ZEN_PM_FINISHED));
				}
				
				@Override
				public void OnError(String msg) {
					Context context = ZenApplication.getAppContext();
					context.sendBroadcast(new Intent(ZEN_PM_FAILED));
				}
			});
			mConnection.startAsychronous();
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Context context = ZenApplication.getAppContext();
		context.sendBroadcast(new Intent(ZEN_PM_FAILED));
	}
}
