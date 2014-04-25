package me.roger.zen.activity;

import me.roger.zen.R;
import me.roger.zen.model.ZenAssetsModel;
import me.roger.zen.model.ZenCommentModel;
import me.roger.zen.model.ZenPostModel;
import me.roger.zen.view.ZenLoadingView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.appmsg.AppMsg;

public class ZenPostActivity extends SherlockActivity {

	public static final int ZEN_TYPE_COMMENT = 1001;
	public static final int ZEN_TYPE_REPLY = 1002;
	public static final int ZEN_TYPE_POST = 1003;
	public static final int ZEN_TYPE_PM = 1004;
	public static final int ZEN_TYPE_FEEDBACK = 1005;
	
	private EditText mPostTitle;

	private EditText mPostContent;
	private ZenLoadingView mLoadingView;
	private ImageButton mChooseImage;
	private InputMethodManager imm;
	private ZenPostModel model;
	private ZenCommentModel comment;
	private String mFid;
	private String mTid;
	private String mPid;
	private String mTo;
	private String mSubject;
	private int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.zen_post_frame);

		Intent intent = getIntent();
		
		type = intent.getIntExtra("type", ZEN_TYPE_POST);

		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		mPostTitle = (EditText) findViewById(R.id.zen_post_title);
		mPostContent = (EditText) findViewById(R.id.zen_post_content);
		mChooseImage = (ImageButton) findViewById(R.id.zen_choose_image);

		if (type == ZEN_TYPE_COMMENT) {
			setTitle("评论");
			String title = intent.getStringExtra("title");
			mFid = intent.getStringExtra("fid");
			mTid = intent.getStringExtra("tid");
			mPid = intent.getStringExtra("pid");
			mSubject = intent.getStringExtra("subject");
			mPostTitle.setFocusable(false);
			mPostTitle.setFocusableInTouchMode(false);
			mPostTitle.setText("Reply:" + title);
			mPostContent.setHint("请输入评论内容");
			comment = new ZenCommentModel(this, mFid, mTid);
		} else if (type == ZEN_TYPE_REPLY) {
			setTitle("引用");
			String title = intent.getStringExtra("title");
			mFid = intent.getStringExtra("fid");
			mTid = intent.getStringExtra("tid");
			mPid = intent.getStringExtra("pid");
			mSubject = intent.getStringExtra("subject");
			mPostTitle.setFocusable(false);
			mPostTitle.setFocusableInTouchMode(false);
			mPostTitle.setText("Quote:" + title);
			mPostContent.setHint("请输入评论内容");
			comment = new ZenCommentModel(this, mFid, mTid);
		} else if(type == ZEN_TYPE_PM) { 
			setTitle("PM");
			mTo = intent.getStringExtra("to");
			mPostTitle.setFocusable(false);
			mPostTitle.setFocusableInTouchMode(false);
			mPostTitle.setText("TO:" + mTo);
			mPostContent.setHint("请输入短消息内容");
			mChooseImage.setVisibility(View.GONE);
			comment = new ZenCommentModel(this);
		} else if (type == ZEN_TYPE_FEEDBACK) {
			setTitle("反馈");
			mFid = intent.getStringExtra("fid");
			mTid = intent.getStringExtra("tid");
			mPid = intent.getStringExtra("pid");
			mPostTitle.setFocusable(false);
			mPostTitle.setFocusableInTouchMode(false);
			mPostTitle.setText("Feedback: Zen For Android");
			mPostContent.setHint("请输入反馈内容");
			comment = new ZenCommentModel(this, mFid, mTid);
		} else {
			mFid = intent.getStringExtra("fid");
			setTitle("发新帖");
			model = new ZenPostModel(mFid);
		}

		mLoadingView = new ZenLoadingView(this);
		registerBroadcast();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			imm.hideSoftInputFromWindow(mPostContent.getWindowToken(), 0);
			if (type == ZEN_TYPE_POST) {
				unregisterReceiver(mPostReceiver);
			}
			else {
				unregisterReceiver(mCommentReceiver);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		try {
			if (type == ZEN_TYPE_POST) {
				IntentFilter filter = new IntentFilter();
				filter.addAction(ZenPostModel.ZEN_POST_SUCCESS);
				filter.addAction(ZenPostModel.ZEN_POST_FAILED);
				registerReceiver(mPostReceiver, filter);
			}
			else {
				IntentFilter filter = new IntentFilter();
				filter.addAction(ZenCommentModel.ZEN_COMMENT_FINISHED);
				filter.addAction(ZenCommentModel.ZEN_COMMENT_FAILED);
				filter.addAction(ZenCommentModel.ZEN_PM_FINISHED);
				filter.addAction(ZenCommentModel.ZEN_PM_FAILED);
				registerReceiver(mCommentReceiver, filter);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mAssetsReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.zen_post, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.zen_send_post:
			send();
			break;
		}
		return true;
	}

	private void send() {
		if (type == ZEN_TYPE_COMMENT || type == ZEN_TYPE_REPLY) {
			String content = mPostContent.getText().toString();
			if (content.matches("")) {
				AppMsg appmsg = AppMsg.makeText(this, "请输入评论内容...",
						AppMsg.STYLE_ALERT);
				appmsg.show();
				return;
			}
			mLoadingView.show("正在发送");
			comment.comment(content, mPid, mSubject);
		} else if (type == ZEN_TYPE_FEEDBACK){
			String content = mPostContent.getText().toString();
			if (content.matches("")) {
				AppMsg appmsg = AppMsg.makeText(this, "请输入反馈内容...",
						AppMsg.STYLE_ALERT);
				appmsg.show();
				return;
			}
			mLoadingView.show("正在发送");
			comment.comment(content, "", "Zen For Android");

		} else if (type == ZEN_TYPE_PM) {
			String content = mPostContent.getText().toString();
			if (content.matches("")) {
				AppMsg appmsg = AppMsg.makeText(this, "请输入短信内容...",
						AppMsg.STYLE_ALERT);
				appmsg.show();
				return;
			}
			mLoadingView.show("正在发送");
			comment.send(content, mTo);
		} else {
			String title = mPostTitle.getText().toString();
			String content = mPostContent.getText().toString();
			if (title.matches("")) {
				AppMsg appmsg = AppMsg.makeText(this, "请输入标题...",
						AppMsg.STYLE_ALERT);
				appmsg.show();
			} else if (title.length() > 75) {
				AppMsg appmsg = AppMsg.makeText(this, "标题太长了...",
						AppMsg.STYLE_ALERT);
				appmsg.show();
			} else if (content.matches("")) {
				AppMsg appmsg = AppMsg.makeText(this, "请输入帖子内容...",
						AppMsg.STYLE_ALERT);
				appmsg.show();
			} else {
				// send post
				mLoadingView.show("正在发送");
				model.post(title, content);
			}
		}

	}

	public void OnSelectImages(View v) {
		Intent intent = new Intent(this, ZenGalleryActivity.class);
		startActivity(intent);
	}

	private void registerBroadcast() {
		try {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ZenAssetsModel.ZEN_PARSER_FINISHED);
			registerReceiver(mAssetsReceiver, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private BroadcastReceiver mAssetsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			ZenAssetsModel assets = ZenAssetsModel.getInstance();

			if (action.equals(ZenAssetsModel.ZEN_PARSER_FINISHED)) {
				if (assets.album != null) {
					mChooseImage.setImageBitmap(assets.album);
				}
			}
		}

	};
	
	private void dismiss() {
		Handler handler = new Handler(getMainLooper());
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				finish();
			}
		}, 2000);
	}

	private BroadcastReceiver mPostReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mLoadingView.hide();
			String action = intent.getAction();
			if (action.equals(ZenPostModel.ZEN_POST_SUCCESS)) {
				ZenAssetsModel.getInstance().clear();
				AppMsg appmsg = AppMsg.makeText(ZenPostActivity.this,
						"发帖成功...", AppMsg.STYLE_INFO);
				appmsg.show();
				dismiss();
			} else if (action.equals(ZenPostModel.ZEN_POST_FAILED)) {
				AppMsg appmsg = AppMsg.makeText(ZenPostActivity.this,
						"发帖失败, 请重试...", AppMsg.STYLE_ALERT);
				appmsg.show();
			}
		}

	};
	
	private BroadcastReceiver mCommentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ZenCommentModel.ZEN_COMMENT_FINISHED)) {
				mLoadingView.hide();
				String msg = null;
				if (type == ZEN_TYPE_COMMENT) {
					msg = "评论成功";
				}
				else if (type == ZEN_TYPE_REPLY) {
					msg = "引用成功";
				} else {
					msg = "反馈成功";
				}
				
				AppMsg appMsg = AppMsg.makeText(ZenPostActivity.this, msg,
						AppMsg.STYLE_INFO);
				appMsg.show();
				dismiss();

			} else if (action.equals(ZenCommentModel.ZEN_COMMENT_FAILED)) {
				mLoadingView.hide();
				String msg = intent.getStringExtra("msg");
				AppMsg appMsg = AppMsg.makeText(ZenPostActivity.this, msg,
						AppMsg.STYLE_ALERT);
				appMsg.show();
			} else if (action.equals(ZenCommentModel.ZEN_PM_FINISHED)) {
				mLoadingView.hide();
				AppMsg appMsg = AppMsg.makeText(ZenPostActivity.this, "发送成功",
						AppMsg.STYLE_INFO);
				appMsg.show();
				dismiss();
				
			} else if (action.equals(ZenCommentModel.ZEN_PM_FAILED)) {
				mLoadingView.hide();
				AppMsg appMsg = AppMsg.makeText(ZenPostActivity.this, "发送失败, 请重试",
						AppMsg.STYLE_ALERT);
				appMsg.show();
			}
		}

	};

}
