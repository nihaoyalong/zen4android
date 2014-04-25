package me.roger.zen.adapter;

import java.util.ArrayList;

import me.roger.zen.R;
import me.roger.zen.application.ZenApplication;
import me.roger.zen.data.ZenTopicData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ZenTopicsAdapter extends BaseAdapter {
	public ArrayList <ZenTopicData> array;

	@Override
	public int getCount() {
		return array.size();
	}

	@Override
	public Object getItem(int position) {
		return array.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		try {
			if (convertView == null) {
				Context context = ZenApplication.getAppContext();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.zen_topic_item, null);
			}
			
			TextView subject = (TextView)convertView.findViewById(R.id.zen_topic_title);
			TextView board = (TextView)convertView.findViewById(R.id.zen_topic_board);
			TextView replies = (TextView)convertView.findViewById(R.id.zen_topic_replies);
			
			ZenTopicData topic = array.get(position);
			subject.setText(topic.subject);
			board.setText(topic.board);
			replies.setText(topic.replies);
			return convertView;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
