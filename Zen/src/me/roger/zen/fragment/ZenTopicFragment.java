package me.roger.zen.fragment;

import java.util.ArrayList;

import me.roger.zen.R;
import me.roger.zen.activity.ZenMainActivity;
import me.roger.zen.adapter.ZenTopicsAdapter;
import me.roger.zen.data.ZenTopicData;
import me.roger.zen.model.ZenTopicsModel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.devspark.appmsg.AppMsg;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;



public class ZenTopicFragment extends Fragment {
	
	static final String LOG_TAG = "Zen";
	public int type;
	private PullToRefreshListView mTopicsListView;
	private ZenTopicsAdapter mTopicsAdapter;
	private ListView mList;
	private ZenTopicsModel mModel;
	private Context mContext;
	private boolean isFirstTime;
		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		
		mTopicsAdapter = new ZenTopicsAdapter();
		mTopicsAdapter.array = new ArrayList<ZenTopicData>();
		mTopicsListView = (PullToRefreshListView)getView().findViewById(R.id.zen_topics_list);
		
		mTopicsListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				ZenMainActivity ac = (ZenMainActivity)mContext;
				if(ac.isLoading()) {
					mTopicsListView.onRefreshComplete();
					return;
				}
				mModel.load(type);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				ZenMainActivity ac = (ZenMainActivity)mContext;
				if(ac.isLoading()) {
					mTopicsListView.onRefreshComplete();
					return;
				}
				Log.v(LOG_TAG, "Pull To Load More Task Execute.");
				mModel.next(type);
			}
			
		});
		
		ListView actureListView = (ListView)mTopicsListView.getRefreshableView();
		mList = actureListView;
		actureListView.setAdapter(mTopicsAdapter);
		actureListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				System.out.println("position: " + position);
				if (getActivity() instanceof ZenMainActivity) {
					ZenMainActivity ar = (ZenMainActivity)getActivity();
					ZenTopicData data = (ZenTopicData)parent.getAdapter().getItem(position);
					ar.onThreadClicked("34", data.tid);
				}
			}
			
		});
		mModel = new ZenTopicsModel();
		isFirstTime = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.zen_topics_frame, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ZenTopicsModel.DidFinishedLoad);
		filter.addAction(ZenTopicsModel.DidFailedLoad);
		filter.addAction(ZenTopicsModel.ISUPTODATE);
		mContext.registerReceiver(mBroadcastReceiver, filter);		
		
		if (isFirstTime) {
			mModel.load(type);
			ZenMainActivity ac = (ZenMainActivity)mContext;
			ac.showLoadingView(true);
			isFirstTime = false;
		}
		
	}
	
	@Override
	public void onPause() {
		try {
			mModel.cancel();
			mContext.unregisterReceiver(mBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		super.onPause();
	}
	
	public void refresh() {
		mModel.load(type);
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// model finished load topics
			Log.d("zen", "onReceive");
			ZenMainActivity ac = (ZenMainActivity)mContext;
			ac.showLoadingView(false);
			mTopicsListView.onRefreshComplete();
			String action = intent.getAction();
			if (action.equals(ZenTopicsModel.DidFinishedLoad)) {
				
				mTopicsAdapter.array = mModel.topics;
				mTopicsAdapter.notifyDataSetChanged();
				mList.scrollTo(0, 0);
			}
			else if (action.equals(ZenTopicsModel.ISUPTODATE)) {
				AppMsg appmsg = AppMsg.makeText(ac, "没有更多了...", AppMsg.STYLE_INFO);
				appmsg.show();	
			}
			
			else if(action.equals(ZenTopicsModel.DidFailedLoad)) {
				AppMsg appmsg = AppMsg.makeText(ac, "加载失败...", AppMsg.STYLE_ALERT);
				appmsg.show();
			}
		}
	};
}
