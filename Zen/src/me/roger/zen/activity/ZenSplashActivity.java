package me.roger.zen.activity;

import me.roger.zen.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.mobisage.android.MobiSageAdSplash;
import com.mobisage.android.MobiSageAdSplashListener;
import com.mobisage.android.MobiSageManager;

public class ZenSplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zen_splash_frame);
		
		// 9509761d7389455899be11c503ec75a1 // test
		// f206f9a1275149b9a1a1cec6ad1ab2d3 // mine
		
		MobiSageManager.getInstance().setPublisherID("f206f9a1275149b9a1a1cec6ad1ab2d3", "Zen");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				MobiSageAdSplash splash = new MobiSageAdSplash(
						ZenSplashActivity.this, findViewById(R.id.zen_splash),
						MobiSageAdSplash.ORIENTATION_PORTRAIT);
				splash.setMobiSageAdSplashListener(mListener);
			}
		}, 1);
		
		//AppConnect.getInstance("ea378d42ccf91e021165da18678d8225","github",this);
	}
	
	private void jump() {
		Intent intent = new Intent(this, ZenMainActivity.class);
		startActivity(intent);
		finish();
	}

	private MobiSageAdSplashListener mListener = new MobiSageAdSplashListener() {

		@Override
		public void onMobiSageSplashShow() {
			Log.i("MobisageSample", "onMobiSageSplashShow");
		}

		@Override
		public void onMobiSageSplashError() {
			Log.i("Zen", "onMobiSageSplashError");
			jump();
		}

		@Override
		public void onMobiSageSplashClose() {
			Log.i("Zen", "onMobiSageSplashClose");
			jump();
		}

		@Override
		public void onMobiSageSplashClick() {
			Log.i("Zen", "onMobiSageSplashClick");
		}
	};
	
}
