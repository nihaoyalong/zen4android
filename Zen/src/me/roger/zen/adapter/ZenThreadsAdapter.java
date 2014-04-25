package me.roger.zen.adapter;

import java.util.ArrayList;

import me.roger.zen.R;
import me.roger.zen.data.ZenThreadData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ZenThreadsAdapter extends BaseAdapter {
	public ArrayList<ZenThreadData> array;
	private LayoutInflater mLayoutInflater;
	
	public ZenThreadsAdapter(Context context) {
		mLayoutInflater = LayoutInflater.from(context);
	}
	
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
				convertView = mLayoutInflater.inflate(R.layout.zen_thread_item, null);
			}
			ZenThreadData data = array.get(position);
			TextView title = (TextView)convertView.findViewById(R.id.zen_thread_title);
			TextView date = (TextView)convertView.findViewById(R.id.zen_thread_date);
			TextView author = (TextView)convertView.findViewById(R.id.zen_thread_author);
			TextView replies = (TextView)convertView.findViewById(R.id.zen_thread_replies);
			TextView lights = (TextView)convertView.findViewById(R.id.zen_thread_light);

			title.setText(data.subject);
			date.setText(data.postdate);
			author.setText(data.author);
			
			int replyNum = Integer.parseInt(data.replies);
			int color = colorForReplies(replyNum);
			replies.setBackgroundResource(color);
			replies.setText(data.replies);
			
			if( Integer.parseInt(data.lights) > 0) {
				lights.setVisibility(View.VISIBLE);
				lights.setText(data.lights);
			}
			else {
				lights.setVisibility(View.INVISIBLE);
			}
			
			return convertView;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	private int colorForReplies(int replyNum) {
		int index = replyNum % colors.length;
		return colors[index];
	}
	
	private int colors [] = {
			R.drawable.orange,
			R.drawable.navy,
			R.drawable.real_blue,
			R.drawable.purple,
			R.drawable.green
	};
}
