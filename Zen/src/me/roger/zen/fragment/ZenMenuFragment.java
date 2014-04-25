package me.roger.zen.fragment;

import java.util.Map;

import me.roger.zen.R;
import me.roger.zen.activity.ZenMainActivity;
import me.roger.zen.adapter.ZenMenuAdapter;
import me.roger.zen.model.ZenMyBoardsModel;
import me.roger.zen.model.ZenTopicsModel;
import me.roger.zen.utils.ZenJSONUtil;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

public class ZenMenuFragment extends Fragment {

	ExpandableListView mBoards;
	ZenMenuAdapter mAdapter;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Context context = getActivity();
		ZenJSONUtil.loadBoardsFromJSON("boards.json");
		mAdapter = new ZenMenuAdapter(context, ZenJSONUtil.headers, ZenJSONUtil.boards);
		mBoards = (ExpandableListView)getView().findViewById(R.id.zen_boards);
		mBoards.setGroupIndicator(null);
		mBoards.setAdapter(mAdapter);
		mBoards.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				@SuppressWarnings("unchecked")
				Map<String, String> child = (Map<String, String>)mAdapter.getChild(groupPosition, childPosition);
				Map<String, String> board = child;
				switchContent(board.get("fid"), board.get("name"));
				return false;
			}
		});
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.zen_menu_list, null);
	}

	private void switchContent(String fid, String name) {
		if (fid != null && name != null) {
			if (
				fid.equals(ZenJSONUtil.ZEN_TOPIC_FAV_FID)) {
				ZenTopicFragment fragment = new ZenTopicFragment();
				fragment.type = ZenTopicsModel.ZEN_TOPIC_FAV;
				ZenMainActivity ac = (ZenMainActivity)getActivity();
				ac.setFid(fid);
				ac.switchContent(fragment, name);
			}
			else if (fid.equals(ZenJSONUtil.ZEN_TOPIC_FID)) {
				ZenTopicFragment fragment = new ZenTopicFragment();
				fragment.type = ZenTopicsModel.ZEN_TOPIC_LIST;
				ZenMainActivity ac = (ZenMainActivity)getActivity();
				ac.setFid(fid);
				ac.switchContent(fragment, name);
			}
			else {
				ZenThreadsFragment fragment = new ZenThreadsFragment();
				fragment.fid = fid;
				ZenMainActivity ac = (ZenMainActivity)getActivity();
				ac.setFid(fid);
				ac.switchContent(fragment, name);
			}
			
		}
	}
}