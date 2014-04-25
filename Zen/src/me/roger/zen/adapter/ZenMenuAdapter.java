package me.roger.zen.adapter;

import java.util.List;
import java.util.Map;

import me.roger.zen.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ZenMenuAdapter extends BaseExpandableListAdapter {
	
	private LayoutInflater mInflater;
	private List<Map<String, String>> mHeaders;
	private Map<String, List<Map<String, String>>> mBoards;
	
	public ZenMenuAdapter(Context context, List<Map<String, String>> headers, Map<String, List<Map<String, String>>> boards) {
		mInflater = LayoutInflater.from(context);
		mHeaders = headers;
		mBoards = boards;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		Map<String, String> header = (Map<String, String>) mHeaders.get(groupPosition);
		String key = (String)header.get("fid");
		List<Map<String, String>> boards = (List<Map<String, String>>) mBoards.get(key);
		Map<String, String> board = (Map<String, String>)boards.get(childPosition);
		return board;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Map<String, String> header = (Map<String, String>) mHeaders.get(groupPosition);
		String key = (String)header.get("fid");
		List<Map<String, String>> boards = (List<Map<String, String>>) mBoards.get(key);
		Map<String, String> board = (Map<String, String>)boards.get(childPosition);
		String text = (String)board.get("name");
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.zen_menu_item, null);
		}
		TextView textView = (TextView)convertView.findViewById(R.id.zen_menu_item);
		textView.setText(text);
		return convertView;
		
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		
		try {
			Map<String, String> header = (Map<String, String>) mHeaders.get(groupPosition);
			String key = (String)header.get("fid");
			List<Map<String, String>> boards = (List<Map<String, String>>) mBoards.get(key);
			return boards.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
		
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mHeaders.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mHeaders.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		Map<String, String> map = (Map<String, String>)mHeaders.get(groupPosition);
		String text = (String)map.get("name");
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.zen_menu_group, null);			
		}
		ImageButton itemLeft = (ImageButton)convertView.findViewById(R.id.zen_menu_header_left);
		Button itemRight = (Button)convertView.findViewById(R.id.zen_menu_header_right);
		itemRight.setText(text);
		
		itemLeft.setImageResource(R.drawable.menu_right_arrow);
		if(isExpanded) {
			itemLeft.setImageResource(R.drawable.menu_down_arrow);
		}
		
		int background = menu_item_backgrounds[groupPosition%9];
		itemLeft.setBackgroundResource(background);
		itemRight.setBackgroundResource(background);
		itemLeft.setFocusable(false);
		itemRight.setFocusable(false);
		itemLeft.setClickable(false);
		itemRight.setClickable(false);
		
		
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private int menu_item_backgrounds [] = {
			R.drawable.orange_btn,
			R.drawable.navy_btn,
			R.drawable.real_blue_btn,
			R.drawable.red_btn,
			R.drawable.purple_btn,
			R.drawable.mid_green_btn,
			R.drawable.light_green_btn,
			R.drawable.light_blue_btn,
			R.drawable.gray_btn
			
	};
}
