package me.roger.zen.activity;

import me.roger.zen.R;
import me.roger.zen.adapter.ZenNotificationAdapter;
import me.roger.zen.data.ZenNotification;
import me.roger.zen.data.ZenUserInfo;
import me.roger.zen.model.ZenLoginModel;
import me.roger.zen.model.ZenNotificationModel;
import me.roger.zen.utils.ZenUtils;
import me.roger.zen.view.ZenLoadingView;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.appmsg.AppMsg;

public class ZenNotificationActivity extends SherlockActivity {
	
	private static final String ZEN_HOME_PAGE_URL = "http://my.hupu.com/";
	
	private ListView mNotifications;
	private ZenNotificationAdapter mAdapter;
	private ZenLoadingView mLoadingView;
	private TextView mUserName;
	private ImageView mUserAvatar;
	private ZenNotificationModel model;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zen_notification_frame);
		

		setTitle("半场提醒");
		
		model = ZenNotificationModel.getInstance();
		
		mAdapter = new ZenNotificationAdapter();
		mNotifications = (ListView)findViewById(R.id.zen_notification_list);
		mNotifications.setAdapter(mAdapter);
		mNotifications.setOnItemClickListener(mOnItemClickListener);

		mUserName = (TextView)findViewById(R.id.zen_user_name);
		
		mUserAvatar = (ImageView)findViewById(R.id.zen_notification_user);
		mUserAvatar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggle();
			}
		});
		
		mLoadingView = new ZenLoadingView(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			
			IntentFilter filter = new IntentFilter();
			filter.addAction(ZenNotificationModel.ZEN_NEW_NOTIFICATION);
			filter.addAction(ZenNotificationModel.ZEN_NOTIFICATION_EMPTY);
			filter.addAction(ZenNotificationModel.ZEN_LOAD_NOTIFICATION_FAILED);
			registerReceiver(mBroadcastReceiver, filter);
			
			if (ZenUtils.getToken() != null) {
				// loged in
				mLoadingView.show();
				model.load();
			}
			refresh();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause() {
		try {
			unregisterReceiver(mBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (ZenUtils.getToken() != null) {
			getSupportMenuInflater().inflate(R.menu.zen_notification, menu);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.zen_feedback:
			Intent intent = new Intent(this, ZenPostActivity.class);
			intent.putExtra("type", ZenPostActivity.ZEN_TYPE_FEEDBACK);
			intent.putExtra("fid", "34");
			intent.putExtra("tid", "8161385");
			startActivity(intent);
			break;
		case R.id.zen_logout:
			logout();
			break;
		case R.id.zen_refresh:
			if (ZenUtils.getToken() != null) {
				// loged in
				mLoadingView.show();
				model.load();
			}
			break;
		}
		return true;
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("Receive Notification: " + intent.getAction());
			String action = intent.getAction();
			mLoadingView.hide();
			if (action.equals(ZenNotificationModel.ZEN_NEW_NOTIFICATION)) {
				mAdapter.setNotifications(model.notifications);
				mAdapter.notifyDataSetChanged();
			}
			else if (action.equals(ZenNotificationModel.ZEN_NOTIFICATION_EMPTY)) {
				mAdapter.clear();
			}
			else if (action.equals(ZenNotificationModel.ZEN_LOAD_NOTIFICATION_FAILED)) {
				AppMsg appmsg = AppMsg.makeText(ZenNotificationActivity.this, "半场提醒加载失败", AppMsg.STYLE_ALERT);
				appmsg.show();
			}
		}
		
	};
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View parent, int position,
				long id) {
			try {
				
				ZenNotification notification = (ZenNotification)mAdapter.getItem(position);
				if ((notification.type == ZenNotification.ZEN_TYPE_THREAD) && (notification.tid != null)) {
					model.ignore(notification.nid);
					Intent intent = new Intent(ZenNotificationActivity.this, ZenContentActivity.class);
					intent.putExtra("fid", "34");
					intent.putExtra("tid", notification.tid);
					intent.putExtra("page", notification.page);
					intent.putExtra("pid", notification.pid);
					startActivity(intent);
				}
				else {
					model.ignore(notification.nid);
					Intent intent = new Intent(ZenNotificationActivity.this, ZenBrowserActivity.class);
					intent.putExtra("url", notification.href);
					startActivity(intent);
				}
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
			AppMsg appmsg = AppMsg.makeText(ZenNotificationActivity.this, "半场提醒异常...", AppMsg.STYLE_ALERT);
			appmsg.show();
		}
		
	};
	
	private void refresh() {
		ZenLoginModel loginModel = ZenLoginModel.getInstance();
		if (loginModel.isLogedin) {
			ZenUserInfo userInfo = loginModel.userInfo;
			mUserName.setText(userInfo.userName);
		}
		else {
			mUserName.setText(R.string.zen_not_logedin);
		}
		super.invalidateOptionsMenu();
	}
	
	private void logout() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("确定要注销吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ZenLoginModel.getInstance().logout();
				refresh();
				mAdapter.clear();
				model.clear();
			}
			
		});
		
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	private void toggle() {
		System.out.println("ZeNotificationActivity: toggle()");
		ZenLoginModel loginModel = ZenLoginModel.getInstance();
		if (loginModel.isLogedin) {
			Intent intent = new Intent(this, ZenBrowserActivity.class);
			intent.putExtra("url", ZEN_HOME_PAGE_URL);
			startActivity(intent);
		}
		else {
			Intent intent = new Intent(this, ZenLoginActivity.class);
			startActivity(intent);
		}
	}
	
}
