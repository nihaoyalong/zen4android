package me.roger.zen.activity;

import me.roger.zen.R;
import me.roger.zen.fragment.ZenMenuFragment;
import me.roger.zen.fragment.ZenThreadsFragment;
import me.roger.zen.fragment.ZenTopicFragment;
import me.roger.zen.model.ZenAdsModel;
import me.roger.zen.model.ZenLoginModel;
import me.roger.zen.model.ZenMyBoardsModel;
import me.roger.zen.model.ZenNotificationModel;
import me.roger.zen.utils.ZenUpdateManager;
import me.roger.zen.view.ZenLoadingView;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;


public class ZenMainActivity extends SlidingFragmentActivity {
	private static final String UMENG_CHANNEL = "wandoujia";
	
	private static final int ZEN_COUNT_TO_LOAD = 10;
	private ZenLoadingView mLoading;
	private boolean mHasNewNotification;
	private int mCount;
	private String mFid;
	private String mURL;
	private Fragment mContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ZenAdsModel model = ZenAdsModel.getInstance();
		model.load();
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.zen_main_frame);
		mLoading = new ZenLoadingView(this);

		if (findViewById(R.id.zen_menu_frame) == null) {
			setBehindContentView(R.layout.zen_menu_frame);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			// show home as up so we can toggle

		} else {
			// add a dummy view
			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		// set the Behind View Fragment
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.zen_menu_frame, new ZenMenuFragment()).commit();

		// set the Front View Fragment
		ZenThreadsFragment threadsFragment = new ZenThreadsFragment();
		mContent = threadsFragment;
		setTitle("步行街");
		threadsFragment.fid = "34";
		mFid = "34";
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.zen_main_frame, threadsFragment).commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);
		ZenMyBoardsModel boardModel = ZenMyBoardsModel.getInstance();
		boardModel.load();
		mHasNewNotification = false;
		try {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ZenNotificationModel.ZEN_NEW_NOTIFICATION);
			filter.addAction(ZenNotificationModel.ZEN_NOTIFICATION_EMPTY);
			registerReceiver(mBroadcastReceiver, filter);

			IntentFilter update = new IntentFilter();
			update.addAction(ZenUpdateManager.ZEN_NEW_VERSION);
			registerReceiver(mUpdateReceiver, update);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ZenUpdateManager.getInstance().CheckUpdate();
	}

	public void onThreadClicked(String fid, String tid) {
		Intent intent = new Intent(this, ZenContentActivity.class);
		intent.putExtra("fid", fid);
		intent.putExtra("tid", tid); // 8367546 7122014
		intent.putExtra("page", "1");
		intent.putExtra("pid", "0");
		startActivity(intent);
		// overridePendingTransition(R.anim.zen_slide_in_from_left,
		// R.anim.zen_slide_out_to_right);
	}

	public void switchContent(final Fragment fragment, String title) {
		mContent = fragment;
		setTitle(title);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.zen_main_frame, fragment).commit();
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this, "5317d2e756240bb63a077751", UMENG_CHANNEL);
		if (mCount % ZEN_COUNT_TO_LOAD == 0) {
			mCount = 0;
			ZenNotificationModel.getInstance().load();
		}
		mCount++;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mBroadcastReceiver);
			unregisterReceiver(mUpdateReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			break;
		case R.id.zen_new_post:
			if (isLogin()) {
				Intent intent = new Intent(this, ZenPostActivity.class);
				intent.putExtra("fid", mFid);
				intent.putExtra("type", ZenPostActivity.ZEN_TYPE_POST);
				startActivity(intent);

			}
			break;
		case R.id.zen_notification:
			startActivity(new Intent(this, ZenNotificationActivity.class));
			break;
		case R.id.zen_refresh:
			refresh();
			break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mHasNewNotification) {
			getSupportMenuInflater().inflate(R.menu.zen_threads_red, menu);
		} else {
			getSupportMenuInflater().inflate(R.menu.zen_threads, menu);
		}
		return true;
	}

	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void setFid(String fid) {
		mFid = fid;
	}

	private void refresh() {
		if (mContent instanceof ZenThreadsFragment) {
			((ZenThreadsFragment)mContent).refresh();
		}
		else if (mContent instanceof ZenTopicFragment) {
			((ZenTopicFragment)mContent).refresh();
		}
		
		mLoading.show("正在加载...");
	}

	private boolean isLogin() {
		ZenLoginModel loginModel = ZenLoginModel.getInstance();
		if (!loginModel.isLogedin) {
			startActivity(new Intent(this, ZenLoginActivity.class));
		}
		return loginModel.isLogedin;
	}

	public void showLoadingView(boolean show) {
		if (show) {
			mLoading.show();
		} else {
			mLoading.hide();
		}
	}

	public boolean isLoading() {
		return mLoading.isLoading;
	}

	// for notification stuff

	private void OnNewNotification() {
		mHasNewNotification = true;
		super.invalidateOptionsMenu();
	}

	private void OnEmptyNotification() {
		mHasNewNotification = false;
		super.invalidateOptionsMenu();
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ZenNotificationModel.ZEN_NEW_NOTIFICATION)) {
				OnNewNotification();
			} else if (action
					.equals(ZenNotificationModel.ZEN_NOTIFICATION_EMPTY)) {
				OnEmptyNotification();
			}
		}

	};

	public void openUrl(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ZenUpdateManager.ZEN_NEW_VERSION)) {
				mURL = intent.getStringExtra("url");
				AlertDialog.Builder builder = new Builder(ZenMainActivity.this);
				builder.setMessage("有新版本，需要更新吗？");
				builder.setTitle("提示");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(Intent.ACTION_VIEW,
										Uri.parse(mURL));
								ZenMainActivity.this.startActivity(intent);
							}

						});

				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
			}
		}

	};
}
