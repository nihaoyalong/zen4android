package me.roger.zen.activity;

import me.roger.zen.R;
import me.roger.zen.model.ZenLoginModel;
import me.roger.zen.model.ZenMyBoardsModel;
import me.roger.zen.model.ZenNotificationModel;
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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.appmsg.AppMsg;

public class ZenLoginActivity extends SherlockActivity {

	private ZenLoginModel model;
	private ZenLoadingView mLoading;
	private EditText userName;
	private EditText password;
	private InputMethodManager imm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(R.string.zen_login);
		setContentView(R.layout.zen_login_frame);

		userName = (EditText) findViewById(R.id.zen_login_username);
		password = (EditText) findViewById(R.id.zen_login_password);

		mLoading = new ZenLoadingView(this);
		model = ZenLoginModel.getInstance();

		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ZenLoginModel.ZEN_LOGIN_FINISHED);
			filter.addAction(ZenLoginModel.ZEN_LOGIN_FAILED);
			registerReceiver(mBoradcastReceiver, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			unregisterReceiver(mBoradcastReceiver);
			imm.hideSoftInputFromWindow(password.getWindowToken(), 0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void login(View v) {
		String user = userName.getText().toString();
		String pwd = password.getText().toString();
		if (user.matches("")) {
			AppMsg appMsg = AppMsg.makeText(ZenLoginActivity.this, "请输入用户名",
					AppMsg.STYLE_ALERT);
			appMsg.show();
			return;
		} else if (pwd.matches("")) {
			AppMsg appMsg = AppMsg.makeText(ZenLoginActivity.this, "请输入密码",
					AppMsg.STYLE_ALERT);
			appMsg.show();
			return;
		}

		mLoading.show("正在登录...");
		model.login(user, pwd);
	}

	private BroadcastReceiver mBoradcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mLoading.hide();
			String action = (String) intent.getAction();
			if (action.equals(ZenLoginModel.ZEN_LOGIN_FINISHED)) {
				ZenMyBoardsModel boardModel = ZenMyBoardsModel.getInstance();
				boardModel.load();
				ZenNotificationModel.getInstance().load();
				AppMsg appMsg = AppMsg.makeText(ZenLoginActivity.this, "登录成功",
						AppMsg.STYLE_INFO);
				appMsg.show();
				Handler handler = new Handler(getMainLooper());
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						finish();
					}
				}, 2000);
			} else if (action.equals(ZenLoginModel.ZEN_LOGIN_FAILED)) {
				AppMsg appMsg = AppMsg.makeText(ZenLoginActivity.this, "登录失败",
						AppMsg.STYLE_ALERT);
				appMsg.show();
			}
		}

	};
}
