package me.roger.zen.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import me.roger.zen.R;
import me.roger.zen.data.ZenThreadData;
import me.roger.zen.data.ZenThreadReply;
import me.roger.zen.model.ZenArchiveModel;
import me.roger.zen.model.ZenCommentModel;
import me.roger.zen.model.ZenLoginModel;
import me.roger.zen.model.ZenReplyModel;
import me.roger.zen.utils.ZenJSONUtil;
import me.roger.zen.utils.ZenUtils;
import me.roger.zen.utils.ZenVideoParser;
import me.roger.zen.view.ZenCommentView.OnZenClickListener;
import me.roger.zen.view.ZenLoadingView;
import me.roger.zen.view.ZenMenuBar;
import me.roger.zen.view.ZenMenuBar.OnItemClickListener;
import me.roger.zen.view.ZenPicker;
import me.roger.zen.view.ZenPicker.OnJumpListener;

import org.jsoup.Jsoup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;

@SuppressLint("SetJavaScriptEnabled")
public class ZenContentActivity extends Activity {

	private static final int ZEN_TYPE_COMMENT = 1001;
	private static final int ZEN_TYPE_REPLY = 1002;
	private static final int ZEN_TYPE_PM = 1003;
	private static final int ZEN_TYPE_RECOMMEND = 1004;

	private static final String ZEN_HINT = "{\"hint\":1}";
	private static final String ZEN_FILE = "hint.json";
	private PullToRefreshWebView pullToRefreshWebView;
	private WebView thread;
	private ZenReplyModel mModel;
	private ZenCommentModel mCommentModel;
	private ZenArchiveModel archiveModel;
	private ZenThreadReply mReplyData;
	private int mArea;
	private boolean isFirstTime;
	private boolean fired;
	private String mPostInfo;
	private ZenMenuBar mMenuBar;
	private ZenMenuBar mMoreMenuBar;
	private ZenLoadingView mLoading;
	private TextView mZenCounter;
	private ZenPicker mPicker;
	private int mType;
	private int mPage;
	private String mFid;
	private String mTid;
	private String mPid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zen_content_frame);
		Intent intent = getIntent();
		mTid = intent.getStringExtra("tid");
		mFid = intent.getStringExtra("fid");
		String page = intent.getStringExtra("page");
		mPid = intent.getStringExtra("pid");
		mPage = Integer.parseInt(page);

		mModel = new ZenReplyModel(this, mTid);
		mCommentModel = new ZenCommentModel(this, mFid, mTid);
		archiveModel = new ZenArchiveModel();
		pullToRefreshWebView = (PullToRefreshWebView) findViewById(R.id.zen_content_webview);
		pullToRefreshWebView
				.setOnRefreshListener(new OnRefreshListener2<WebView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<WebView> refreshView) {
						mModel.refresh();
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<WebView> refreshView) {
						hint();
						mModel.next();
					}

				});
		mZenCounter = (TextView) findViewById(R.id.zen_counter);

		thread = (WebView) pullToRefreshWebView.getRefreshableView();
		thread.getSettings().setJavaScriptEnabled(true);

		thread.addJavascriptInterface(new ZenBridge(this), "ZenBridge");

		thread.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				System.out.println("ZenContentActivity: onPageFinished");
				super.onPageFinished(view, url);
				if (!fired) {
					registerBroadcast();
					// pullToRefreshWebView.setRefreshing(true);

					mLoading.show("正在加载...");
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							loadThreadData();
						}
					}, 1000);
					fired = true;
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				System.out.println("shouldOverrideUrlLoading url: " + url);
				return true;
			}

		});

		fired = false;

		String html = stringFromAssetsFile("hupu_post.html");
		thread.loadDataWithBaseURL("file:///android_asset/", html, "text/html",
				"utf-8", null);
		// thread.loadUrl("file:///android_asset/hupu_post.html");
		isFirstTime = true;

		mMenuBar = ZenMenuBar.getInstance(this, 0);
		mMenuBar.setOnItemClickListener(mOnItemClickListener);

		mMoreMenuBar = ZenMenuBar.getInstance(this,
				ZenMenuBar.MENUBAR_TYPE_MORE);
		mMoreMenuBar.setOnItemClickListener(mOnItemClickListener);
		mPicker = new ZenPicker(this);
		mPicker.setOnJumpListener(new OnJumpListener() {
			
			@Override
			public void OnJump(int page) {
				jump(page);
			}
		});
		//ZenAdsManager ads = ZenAdsManager.getInstance(this);
		//ads.createPoster();
		
		mLoading = new ZenLoadingView(this);
	}

	@Override
	protected void onPause() {
		try {
			unregisterReceiver(mBroadcastReceiver);
		} catch (Exception e) {
			// mBroadCastReceiver is not registered in some case
			e.printStackTrace();
		}

		super.onPause();
	}

	@Override
	protected void onResume() {
		if (!isFirstTime) {
			registerBroadcast();
		}
		isFirstTime = false;
		super.onResume();
	}
	
	// ================================================================================
	// Events Handler and Broadcast Handler
	// ================================================================================
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void OnMenuItemClick(int type) {
			switch (type) {
			case ZenMenuBar.MENU_LIGHT:
				light();
				break;
			case ZenMenuBar.MENU_REPLY:
				reply();
				break;
			case ZenMenuBar.MENU_COPY:
				copy();
				break;
			case ZenMenuBar.MENU_PM:
				pm();
				break;
			case ZenMenuBar.MENU_REFRESH:
				refresh();
				break;
			case ZenMenuBar.MENU_COMMENT:
				comment();
				break;
			case ZenMenuBar.MENU_RECOMMEND:
				recommend();
				break;
			case ZenMenuBar.MENU_ARCHIVE:
				archive();
				break;
			}

		}
	};

	private OnZenClickListener mOnClickListener = new OnZenClickListener() {

		@Override
		public void OnSendClick(String content) {
			ZenLoginModel loginModel = ZenLoginModel.getInstance();
			String token = loginModel.userInfo.token;
			if (content.matches("")) {
				AppMsg appmsg = AppMsg.makeText(ZenContentActivity.this,
						"请输入内容...", AppMsg.STYLE_ALERT);
				appmsg.show();
				return;
			}
			switch (mType) {
			case ZEN_TYPE_REPLY:
				mLoading.show("正在发送...");
				mCommentModel.comment(content, mReplyData.pid, token);
				break;
			case ZEN_TYPE_PM:
				mLoading.show("正在发送...");
				mCommentModel.send(content, "roger.qian");
				break;
			case ZEN_TYPE_COMMENT:
				mLoading.show("正在发送...");
				mCommentModel.comment(content, "", mModel.threadData.subject);
				break;
			case ZEN_TYPE_RECOMMEND:
				mLoading.show("正在发送...");
				mCommentModel.recommend(mModel.threadData.subject, content,
						token);
				break;
			}
		}

		@Override
		public void OnChooseImageClick() {
			Intent intent = new Intent(ZenContentActivity.this, ZenGalleryActivity.class);
			startActivity(intent);
		}
		
		
	};

	private void registerBroadcast() {
		try {
			IntentFilter filter = new IntentFilter();

			// for reply model
			filter.addAction(ZenReplyModel.ZenThreadDataDidFinishedLoad);
			filter.addAction(ZenReplyModel.ZenThreadDataDidFailedLoad);
			filter.addAction(ZenReplyModel.ZenRepliesDidFinishedLoad);
			filter.addAction(ZenReplyModel.ZenRepliesDidFailedLoad);
			filter.addAction(ZenReplyModel.ZenRepliesEmpty);
			
			// for comment model
			filter.addAction(ZenCommentModel.ZEN_LIGHT_FINISHED);
			filter.addAction(ZenCommentModel.ZEN_LIGHT_FAILED);
			filter.addAction(ZenCommentModel.ZEN_RECOMMEND_FINISHED);
			filter.addAction(ZenCommentModel.ZEN_RECOMMEND_FAILED);
			
			// for archive model
			filter.addAction(ZenArchiveModel.ZEN_ARCHIVE_FAILED);
			filter.addAction(ZenArchiveModel.ZEN_ARCHIVE_SUCCESS);
			
			registerReceiver(mBroadcastReceiver, filter);
		} catch (Exception e) {
			// mBroadcastReceiver register failed
			e.printStackTrace();
		}

	}

	public void OnBottomItemClick(View v) {
		switch (v.getId()) {
		case R.id.zen_back_btn:
			finish();
			break;
		case R.id.zen_prev_btn:
			hint();
			mLoading.show("正在加载...");
			mModel.prev();
			break;
		case R.id.zen_next_btn:
			hint();
			mLoading.show("正在加载...");
			mModel.next();
			break;
		case R.id.zen_more_btn:
			mMoreMenuBar.show();
			break;
		case R.id.zen_counter:
			ZenJSONUtil.WriteJSONToFile(ZEN_FILE, ZEN_HINT);
			mPicker.setMin(1);
			mPicker.setValue(mModel.currentPage);
			mPicker.setMax(mModel.pageCount);
			mPicker.show();
			break;
		}
	}

	private void onThreadDataFinished(ZenThreadData data) {
		System.out.println("onThreadDataFinished");
		thread.loadUrl("javascript:clearPost('')");
		if (data != null) {
			String postInfo = String.format("%s亮 %s回复", data.lights,
					data.replies);
			mPostInfo = postInfo;
			String js = "";
			if (mPage != 1) {
				js = String
						.format("javascript:addPostInfo('%s', '%s', '%s', '%s', '%s');",
								data.subject, postInfo, data.postdate,
								data.author, "楼主");
			} else {
				js = String
						.format("javascript:addMainPost('%s', '%s', '%s', '%s', '%s', '%s');",
								data.subject, postInfo, data.postdate,
								data.author, "楼主", data.content);
			}

			//System.out.println("js: " + js);
			thread.loadUrl(js);
		}
		mModel.loadReplies(mPage);
	}

	@SuppressLint("DefaultLocale")
	private void onThreadRepliesFinished() {
		// hide loading views
		pullToRefreshWebView.onRefreshComplete();
		mLoading.hide();

		// refresh counter
		String count = String.format(Locale.getDefault(), "%d/%d",
				mModel.currentPage, mModel.pageCount);
		mZenCounter.setText(count);

		ZenReplyModel model = mModel;
		if (model.currentPage > 1) {
			thread.loadUrl("javascript:clearPost('" + mPostInfo + "')");
		}

		if (model.lightReplies.size() > 0) {
			if (model.currentPage == 1) {
				thread.loadUrl("javascript:addLightTitle('热门跟帖', 'true')");
			}
			for (int i = 0; i < model.lightReplies.size(); i++) {
				ZenThreadReply reply = model.lightReplies.get(i);
				String js = String
						.format("javascript:addLightPost('%s', '%s', '%s', '%s', '%d', '%s')",
								reply.author, "", reply.light, reply.content,
								i, reply.pid);
				thread.loadUrl(js);
			}
		}

		if (model.replies.size() > 0) {
			if (model.currentPage == 1) {
				thread.loadUrl("javascript:addAllTitle('所有跟帖', 'true');");
			}

			for (int i = 0; i < model.replies.size(); i++) {
				ZenThreadReply reply = model.replies.get(i);
				String position = String.format("%d 楼", model.floor + i);
				String js = String
						.format("javascript:addAllPost('%s', '%s', '%s', '%s', '%d', '%s');",
								reply.author, position, reply.light,
								reply.content, i, reply.pid);
				//System.out.println(js);
				thread.loadUrl(js);
			}
		}
		if (mPage != 1 && !mPid.equals("0")) {
			String js = "javascript:scrollToVisible('" + mPid + "');";
			System.out.println("scroll: " + js);
			thread.loadUrl(js);
			mPage = 1;
		}
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ZenReplyModel.ZenThreadDataDidFinishedLoad)) {
				onThreadDataFinished(mModel.threadData);
			} else if (action.equals(ZenReplyModel.ZenThreadDataDidFailedLoad)) {
				mLoading.hide();
				pullToRefreshWebView.onRefreshComplete();
				AppMsg appmsg = AppMsg.makeText(ZenContentActivity.this,
						"加载失败...", AppMsg.STYLE_ALERT);
				appmsg.show();
			} else if (action.equals(ZenReplyModel.ZenRepliesDidFinishedLoad)) {
				onThreadRepliesFinished();
			} else if (action.equals(ZenReplyModel.ZenRepliesDidFailedLoad)) {
				mLoading.hide();
				pullToRefreshWebView.onRefreshComplete();
				AppMsg appmsg = AppMsg.makeText(ZenContentActivity.this,
						"加载失败...", AppMsg.STYLE_ALERT);
				appmsg.show();
			} else if (action.equals(ZenReplyModel.ZenRepliesEmpty)) {
				mLoading.hide();
			} 
			else if (action.equals(ZenCommentModel.ZEN_LIGHT_FINISHED)) {
				mLoading.hide();
				String lights = intent.getStringExtra("light");
				String js = String.format(Locale.getDefault(),
						"javascript:lightSuccess('%s', '%s', %d)", lights,
						mReplyData.pid, mArea);
				thread.loadUrl(js);
				AppMsg appMsg = AppMsg.makeText(ZenContentActivity.this,
						"点亮成功", AppMsg.STYLE_INFO);
				appMsg.show();
			} else if (action.equals(ZenCommentModel.ZEN_LIGHT_FAILED)) {
				mLoading.hide();
				String msg = intent.getStringExtra("msg");
				AppMsg appMsg = AppMsg.makeText(ZenContentActivity.this, msg,
						AppMsg.STYLE_ALERT);
				appMsg.show();
			}  else if (action.equals(ZenCommentModel.ZEN_RECOMMEND_FINISHED)) {
				mLoading.hide();
				String msg = intent.getStringExtra("msg");
				AppMsg appMsg = AppMsg.makeText(ZenContentActivity.this, msg,
						AppMsg.STYLE_INFO);
				appMsg.show();
			} else if (action.equals(ZenCommentModel.ZEN_RECOMMEND_FINISHED)) {
				mLoading.hide();
				String msg = intent.getStringExtra("msg");
				AppMsg appMsg = AppMsg.makeText(ZenContentActivity.this, msg,
						AppMsg.STYLE_ALERT);
				appMsg.show();
			} else if (action.equals(ZenArchiveModel.ZEN_ARCHIVE_SUCCESS)) {
				mLoading.hide();
				AppMsg appMsg = AppMsg.makeText(ZenContentActivity.this, "收藏成功!",
						AppMsg.STYLE_INFO);
				appMsg.show();
			} else if (action.equals(ZenArchiveModel.ZEN_ARCHIVE_FAILED)) {
				mLoading.hide();
				AppMsg appMsg = AppMsg.makeText(ZenContentActivity.this, "收藏失败!",
						AppMsg.STYLE_ALERT);
				appMsg.show();
			} 
			
		}

	};

	// ================================================================================
	// Utils
	// ================================================================================

	private void jump(int page) {
		mLoading.show("正在加载...");
		if (page == 1) {
			loadThreadData();
		}
		else {
			mModel.loadReplies(page);
		}
	}
	
	private String stringFromAssetsFile(String fileName) {
		AssetManager manager = getAssets();
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

	private void loadThreadData() {
		if (mModel != null) {
			mModel.loadThreadData();
		}
	}

	private boolean isLogin() {
		ZenLoginModel loginModel = ZenLoginModel.getInstance();
		if (!loginModel.isLogedin) {
			startLoginActivity();
		}
		return loginModel.isLogedin;
	}

	private void startLoginActivity() {
		Intent intent = new Intent(this, ZenLoginActivity.class);
		startActivity(intent);
	}

	private void chooseReplyData(int position, int area) {
		try {
			mArea = area;
			if (area == 0) {
				mReplyData = mModel.lightReplies.get(position);
			} else {
				mReplyData = mModel.replies.get(position);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void light() {
		if (isLogin() && mReplyData != null && mReplyData.pid != null) {
			mLoading.show("正在点亮...");
			mCommentModel.light(mReplyData.pid, ZenUtils.getToken());
		}
	}

	private void reply() {
		if (isLogin() && mReplyData != null && mReplyData.pid != null) {
			Intent intent = new Intent(this, ZenPostActivity.class);
			intent.putExtra("type", ZenPostActivity.ZEN_TYPE_REPLY);
			intent.putExtra("title", mReplyData.content);
			intent.putExtra("fid", mFid);
			intent.putExtra("tid", mTid);
			intent.putExtra("pid", mReplyData.pid);
			intent.putExtra("subject","");
			startActivity(intent);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			mMoreMenuBar.toggle();
		}
		return super.onKeyDown(keyCode, event);
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void copy() {
		if (mReplyData != null && mReplyData.content != null) {
			String stripped = Jsoup.parse(mReplyData.content).text();
			System.out.println("copy: " + stripped);
			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(stripped);
			} else {
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				android.content.ClipData clip = android.content.ClipData
						.newPlainText("content", stripped);
				clipboard.setPrimaryClip(clip);
			}
			AppMsg appMsg = AppMsg.makeText(this, "复制成功", AppMsg.STYLE_INFO);
			appMsg.show();
		} else {
			AppMsg appMsg = AppMsg.makeText(this, "复制失败", AppMsg.STYLE_ALERT);
			appMsg.show();
		}
	}

	private void pm() {
		if (isLogin() && mReplyData != null && mReplyData.author != null) {
			Intent intent = new Intent(this, ZenPostActivity.class);
			intent.putExtra("type", ZenPostActivity.ZEN_TYPE_PM);
			intent.putExtra("to", mReplyData.author);
			startActivity(intent);
		}
	}

	private void refresh() {
		mLoading.show();
		mModel.refresh();
	}

	private void comment() {
		if (mModel.threadData.subject == null) {
			AppMsg appMsg = AppMsg.makeText(this, "帖子加载完成后才能评论", AppMsg.STYLE_ALERT);
			appMsg.show();
			return;
		}
		if (isLogin()) {
			Intent intent = new Intent(this, ZenPostActivity.class);
			intent.putExtra("type", ZenPostActivity.ZEN_TYPE_COMMENT);
			intent.putExtra("title", mModel.threadData.subject);
			intent.putExtra("fid", mFid);
			intent.putExtra("tid", mTid);
			intent.putExtra("subject", mModel.threadData.subject);
			intent.putExtra("pid", "");
			startActivity(intent);
		}
	}

	private void recommend() {
		if (isLogin()) {
			mLoading.show("正在推荐...");
			mCommentModel.recommend("recommend", "from zen for android",
					ZenUtils.getToken());
		}
	}

	private void archive() {
		mLoading.show("正在收藏...");
		archiveModel.archive(mTid);
	}
	
	private void hint() {
		String json = ZenJSONUtil.ReadJSONFromFile(ZEN_FILE);
		if (json == null || !json.contains("hint")) {
			AppMsg msg = AppMsg.makeText(this, "点击页码，页码跳转更快速...", AppMsg.STYLE_INFO);
			msg.show();
		}
	}
	
	public class ZenOpenPhoto implements Runnable {
		
		private Context mContext;
		private String mUrl;
		
		public ZenOpenPhoto(Context context, String url) {
			mContext = context;
			mUrl = url;
		}
		
		public void run() {
			Activity ac = (Activity)mContext;
			Intent intent = new Intent(mContext, ZenPhotoActivity.class);
			intent.putExtra("url", mUrl);
			ac.startActivity(intent);
		}
	} 
	
	public class ZenOpenBrowser implements Runnable {
			
		private Context mContext;
		private String mUrl;
		
		public ZenOpenBrowser(Context context, String url) {
			mContext = context;
			mUrl = url;
		}
		
		public void run() {
			Activity ac = (Activity)mContext;
			Intent intent = new Intent(mContext, ZenBrowserActivity.class);
			intent.putExtra("url", mUrl);
			ac.startActivity(intent);
		}
	} 
	
	public class ZenOpenChrome implements Runnable {
		
		private Context mContext;
		private String mUrl;
		
		public ZenOpenChrome(Context context, String url) {
			mContext = context;
			mUrl = url;
		}
		
		public void run() {
			Activity ac = (Activity)mContext;
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
			ac.startActivity(intent);
		}
	} 

	// ================================================================================
	// Javascript Stuff
	// ================================================================================

	class ZenBridge {
		private Handler mHandler;

		public ZenBridge(Context context) {
			mHandler = new Handler(getMainLooper());
		}

		@JavascriptInterface
		public void OnCloseSoftKeyboard() {
			System.out.println("OnCloseSoftKeyboard");
		}

		@JavascriptInterface
		public void OnReplyTouchUp(int position, int area) {
			
			chooseReplyData(position, area);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mMenuBar.show();
				}
			});

		}

		@JavascriptInterface
		public void OnClick(String url) {
			if (url != null && url.contains("http")) {
				mHandler.post(new ZenOpenBrowser(ZenContentActivity.this, url));
			}
		}
		
		@JavascriptInterface
		public void OnOpenVideo(String url) {
			System.out.println("OnOpenVideo:" + url);
			if (url != null) {
				String videoUrl = ZenVideoParser.parser(url);
				System.out.println("OnOpenVideo: after parser" + videoUrl);
				if (videoUrl != null) {
					mHandler.post(new ZenOpenChrome(ZenContentActivity.this, videoUrl));
				}
				else {
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							AppMsg appmsg = AppMsg.makeText(ZenContentActivity.this,
									"暂时不支持该视频格式", AppMsg.STYLE_ALERT);
							appmsg.show();
						}
					});
				}
				
			}
		}
		
		@JavascriptInterface
		public void OnOpenImage(String url) {
			System.out.println("open image: " + url);
			mHandler.post(new ZenOpenPhoto(ZenContentActivity.this, url));
		}
	};
}
