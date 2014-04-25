package me.roger.zen.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import me.roger.zen.application.ZenApplication;
import me.roger.zen.data.ZenTopicData;
import me.roger.zen.utils.ZenURLConnection;
import me.roger.zen.utils.ZenURLConnection.ZenOnResponseListener;
import me.roger.zen.utils.ZenUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Intent;

public class ZenTopicsModel {
	public static final String DidFinishedLoad = "ZenTopicsDidFinishedLoad";
	public static final String DidFailedLoad = "ZenTopicsDidFailedLoad";
	public static final String ISUPTODATE = "ZenTopicsIsUptodate";
	public static int ZEN_TOPIC_LIST = 1001;
	public static int ZEN_TOPIC_FAV = 1002;

	public ArrayList<ZenTopicData> topics;

	private static final int ZEN_SUBJECT_INDEX = 0;
	private static final int ZEN_BOARD_INDEX = 1;
	private static final int ZEN_REPLIES_INDEX = 3;

	private static final String ZEN_TOPIC_URL = "http://my.hupu.com/%s/topic-list-%d";
	private static final String ZEN_FAV_URL = "http://my.hupu.com/%s/topic-fav-%d";
	
	private ZenURLConnection mConnection;
	private Context mContext;
	private int page;

	public ZenTopicsModel() {
		mContext = ZenApplication.getAppContext();
		topics = new ArrayList<ZenTopicData>();
	}

	public void cancel() {
		if (mConnection != null) {
			mConnection.cancel();
		}
	}

	private boolean contains(ZenTopicData item) {
		for (ZenTopicData topic : topics) {

			if (item != null && item.tid != null && topic.tid != null) {
				if (topic.tid.equals(item.tid)) {
					return true;
				}
			}
		}
		return false;
	}

	private int parser(String response, boolean clear) {

		try {

			Document doc = Jsoup.parse(response);
			Elements eles = doc.select("tbody tr");

			if (eles != null) {
				if (clear) {
					topics.clear();
				}
				for (int i = 0; i < eles.size(); i++) {
					Element element = eles.get(i);
					Elements attrs = element.select("td");
					if (attrs != null) {
						ZenTopicData topic = new ZenTopicData();
						for (int j = 0; j < attrs.size(); j++) {
							Element td = attrs.get(j);
							if (j == ZEN_SUBJECT_INDEX) {
								Element a = td.select("a").first();
								if (a != null) {
									String url = a.attr("href");
									String tid = ZenUtils.sub(".com/", ".html",
											url);
									topic.tid = tid;
								}
								topic.subject = td.text();
							} else if (j == ZEN_BOARD_INDEX) {
								topic.board = td.text();

							} else if (j == ZEN_REPLIES_INDEX) {
								topic.replies = td.text();
							}
						}
						if (topic != null && topic.tid != null && !contains(topic)) {
							topics.add(topic);
						}

					}
				}

			}
			return 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	public void load(int type) {
		if (type != ZEN_TOPIC_LIST && type != ZEN_TOPIC_FAV) {
			System.out.println("Invalid Type!");
			mContext.sendBroadcast(new Intent(DidFailedLoad));
			return;
		}
		page = 1;
		String token = ZenUtils.getToken();
		String uid = ZenUtils.getUid();
		if (token != null && uid != null) {
			String url = null;
			if (type == ZEN_TOPIC_LIST) {
				url = String.format(Locale.getDefault(), ZEN_TOPIC_URL, uid,
						page);
			} else {
				url = String
						.format(Locale.getDefault(), ZEN_FAV_URL, uid, page);
			}
			mConnection = new ZenURLConnection(url);
			mConnection.setReadCharsetName("gb2312");
			mConnection.addRequestHeader("Cookie", "u=" + token + ";");
			mConnection.setOnResponseListener(new ZenOnResponseListener() {

				@Override
				public void OnResponse(String response) {
					if (response != null) {
						int result = parser(response, true);
						if (result == 0) {
							mContext.sendBroadcast(new Intent(
									DidFinishedLoad));
							return;
						}
					}
					page--;
					mContext.sendBroadcast(new Intent(ISUPTODATE));
				}

				@Override
				public void OnError(String msg) {
					page--;
					mContext.sendBroadcast(new Intent(DidFailedLoad));
				}
			});
			mConnection.startAsychronous();
		} else {
			System.out.println("Invalid User Info.");
			mContext.sendBroadcast(new Intent(DidFailedLoad));
		}
	}

	public void next(int type) {
		page++;
		if (type != ZEN_TOPIC_LIST && type != ZEN_TOPIC_FAV) {
			System.out.println("Invalid Type!");
			mContext.sendBroadcast(new Intent(DidFailedLoad));
			return;
		}
		String token = ZenUtils.getToken();
		String uid = ZenUtils.getUid();

		if (token != null && uid != null) {
			String url = null;
			if (type == ZEN_TOPIC_LIST) {
				url = String.format(Locale.getDefault(), ZEN_TOPIC_URL, uid,
						page);
			} else {
				url = String
						.format(Locale.getDefault(), ZEN_FAV_URL, uid, page);
			}
			mConnection = new ZenURLConnection(url);
			mConnection.setReadCharsetName("gb2312");
			mConnection.addRequestHeader("Cookie", "u=" + token + ";");
			mConnection.setOnResponseListener(new ZenOnResponseListener() {

				@Override
				public void OnResponse(String response) {
					if (response != null) {
						int result = parser(response, false);
						if (result == 0) {
							mContext.sendBroadcast(new Intent(
									DidFinishedLoad));
							return;
						}
					}
					page--;
					mContext.sendBroadcast(new Intent(ISUPTODATE));
				}

				@Override
				public void OnError(String msg) {
					page--;
					mContext.sendBroadcast(new Intent(DidFailedLoad));
				}			});
			mConnection.startAsychronous();
		} else {
			System.out.println("Invalid User Info.");
			mContext.sendBroadcast(new Intent(DidFailedLoad));
		}
	}
}
