package me.roger.zen.utils;

import me.roger.zen.application.ZenApplication;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

import com.mobisage.android.MobiSageAdPoster;
import com.mobisage.android.MobiSageAdPosterListener;

public class ZenAdsManager {
	private static final int ZEN_TIME_TO_SHOW = 10;
	private static ZenAdsManager instance;
	private Context mContext;
	private MobiSageAdPoster mPoster;
	private int counter;

	public static ZenAdsManager getInstance(Context context) {
		if (instance == null) {
			instance = new ZenAdsManager();
		}
		instance.mContext = context;
		return instance;
	}

	public ZenAdsManager() {
		counter = 1;
	}

	private boolean isWifiAvailable() {
		Context context = ZenApplication.getAppContext();
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		State wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if(wifi == State.CONNECTED || wifi == State.CONNECTING) {
			System.out.println("wifi available");
			return true;
		}
		return false;
	}

	// ================================================================================
	// MobiSage Stuff
	// ================================================================================
	public void createPoster() {
		if (counter % ZEN_TIME_TO_SHOW == 0 && isWifiAvailable()) {
			counter = 1;
			mPoster = new MobiSageAdPoster(mContext);
			mPoster.setMobiSageAdPosterListener(mListener);
			System.out.println("show ad");
		}
		counter++;
	}

	private MobiSageAdPosterListener mListener = new MobiSageAdPosterListener() {

		@Override
		public void onMobiSagePosterPreShow() {
			Log.i("Zen", "onMobiSagePosterPreShow");
			mPoster.show();
		}

		@Override
		public void onMobiSagePosterError() {
			Log.i("Zen", "onMobiSagePosterError");
		}

		@Override
		public void onMobiSagePosterClick() {
			Log.i("Zen", "onMobiSagePosterClick");

		}

		@Override
		public void onMobiSagePosterClose() {
			Log.i("Zen", "onMobiSagePosterClose");
		}
	};
}
