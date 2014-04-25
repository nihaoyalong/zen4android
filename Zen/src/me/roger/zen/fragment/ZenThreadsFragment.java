package me.roger.zen.fragment;

import java.util.ArrayList;

import me.roger.zen.R;
import me.roger.zen.activity.ZenMainActivity;
import me.roger.zen.adapter.ZenThreadsAdapter;
import me.roger.zen.application.ZenApplication;
import me.roger.zen.data.ZenThreadData;
import me.roger.zen.model.ZenAdsModel;
import me.roger.zen.model.ZenPhotoModel;
import me.roger.zen.model.ZenThreadsModel;
import me.roger.zen.utils.ZenUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.devspark.appmsg.AppMsg;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;



public class ZenThreadsFragment extends Fragment {
	
	static final String LOG_TAG = "Zen";
	public String fid;
	private PullToRefreshListView mThreadsListView;
	private ZenThreadsAdapter mThreadsAdapter;
	private ListView mList;
	private ZenThreadsModel mModel;
	private Context mContext;
	private boolean isFirstTime;
	private ImageView adsView;
	private ZenPhotoModel photoModel;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		LinearLayout ads = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.zen_ads_frame, null);
		adsView = (ImageView)ads.findViewById(R.id.zen_ads);
		adsView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mContext instanceof ZenMainActivity) {
					ZenAdsModel model = ZenAdsModel.getInstance();
					ZenMainActivity main = (ZenMainActivity)mContext;
					main.openUrl(model.getUrl());
				}
				
			}
		});
		mThreadsAdapter = new ZenThreadsAdapter(mContext);
		mThreadsAdapter.array = new ArrayList<ZenThreadData>();
		mThreadsListView = (PullToRefreshListView)getView().findViewById(R.id.zen_threads_list);
		
		mThreadsListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				ZenMainActivity ac = (ZenMainActivity)mContext;
				if(ac.isLoading()) {
					mThreadsListView.onRefreshComplete();
					return;
				}
				mModel.refresh();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				ZenMainActivity ac = (ZenMainActivity)mContext;
				if(ac.isLoading()) {
					mThreadsListView.onRefreshComplete();
					return;
				}
				Log.v(LOG_TAG, "Pull To Load More Task Execute.");
				mModel.loadMore();
			}
			
		});
		
		ListView actureListView = (ListView)mThreadsListView.getRefreshableView();
		mList = actureListView;
		photoModel = null;
		if (ZenAdsModel.getInstance().isEnabled(fid)) {
			mList.addHeaderView(ads);
			photoModel = new ZenPhotoModel();
		}
		actureListView.setAdapter(mThreadsAdapter);
		actureListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				System.out.println("position: " + position);
				if (getActivity() instanceof ZenMainActivity) {
					ZenMainActivity ar = (ZenMainActivity)getActivity();
					ZenThreadData data = (ZenThreadData)parent.getAdapter().getItem(position);
					//ZenThreadData data = (ZenThreadData)mModel.threads.get(position);
					ar.onThreadClicked(data.fid, data.tid);
				}
			}
			
		});
		mModel = new ZenThreadsModel(mContext, fid);
		isFirstTime = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.zen_threads_list, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ZenThreadsModel.DidFinishedLoad);
		filter.addAction(ZenThreadsModel.DidFailedLoad);
		mContext.registerReceiver(mBroadcastReceiver, filter);
		
		IntentFilter adsFilter = new IntentFilter();
		adsFilter.addAction(ZenPhotoModel.ZEN_PHOTO_FINISHED);
		mContext.registerReceiver(mAdsReceiver, adsFilter);
		
		if (isFirstTime) {
			mModel.refresh();
			ZenMainActivity ac = (ZenMainActivity)mContext;
			ac.showLoadingView(true);
			isFirstTime = false;
			
			if (photoModel != null) {
				ZenAdsModel model = ZenAdsModel.getInstance();
				photoModel.load(model.getTheme());
			}
		}
		
	}
	
	@Override
	public void onPause() {
		try {
			mModel.cancel();
			mContext.unregisterReceiver(mBroadcastReceiver);
			mContext.unregisterReceiver(mAdsReceiver);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		super.onPause();
	}
	
	public void refresh() {
		mModel.refresh();
	}
	
	private BroadcastReceiver mAdsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			ZenAdsModel model = ZenAdsModel.getInstance();
			String action = intent.getAction();
			if (model.isEnabled(fid) && action.equals(ZenPhotoModel.ZEN_PHOTO_FINISHED)) {
				Context appContext = ZenApplication.getAppContext();

				Bitmap bm = ZenUtils.decodeImage(appContext
						.getFilesDir()
						+ "/"
						+ ZenPhotoModel.ZEN_TEMP_FILE);

				if (bm != null) {
					adsView.setImageBitmap(bm);
					return;
				}

			}
		}
		
	};

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// model finished load threads
			Log.d("zen", "onReceive");
			ZenMainActivity ac = (ZenMainActivity)mContext;
			ac.showLoadingView(false);
			mThreadsListView.onRefreshComplete();
			String action = intent.getAction();
			if (action.equals(ZenThreadsModel.DidFinishedLoad)) {
				
				mThreadsAdapter.array = mModel.threads;
				mThreadsAdapter.notifyDataSetChanged();
				mList.scrollTo(0, 0);
			}
			else if(action.equals(ZenThreadsModel.DidFailedLoad)) {
				AppMsg appmsg = AppMsg.makeText(ac, "º”‘ÿ ß∞‹...", AppMsg.STYLE_ALERT);
				appmsg.show();
			}
		}
	};
}
