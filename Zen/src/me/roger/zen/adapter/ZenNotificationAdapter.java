package me.roger.zen.adapter;

import java.util.ArrayList;

import me.roger.zen.R;
import me.roger.zen.application.ZenApplication;
import me.roger.zen.data.ZenNotification;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ZenNotificationAdapter extends BaseAdapter {

	private ArrayList<ZenNotification> notifications;
	
	public ZenNotificationAdapter() {
		notifications = new ArrayList<ZenNotification>();
	}
	
	public void setNotifications(ArrayList<ZenNotification> array) {
		notifications = array;
	}
	
	public void clear() {
		notifications.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return notifications.size();
	}

	@Override
	public Object getItem(int pos) {
		return notifications.get(pos);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup root) {
		if (convertView == null) {
			Context context = ZenApplication.getAppContext();
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.zen_notification_item, null);
		}
		ZenNotification item = notifications.get(position);
		TextView title = (TextView)convertView.findViewById(R.id.zen_notification_title);
		title.setText(item.msg);
		return convertView;
	}

}
