package me.roger.zen.activity;

import java.util.ArrayList;

import me.roger.zen.R;
import me.roger.zen.model.ZenAssetsModel;
import me.roger.zen.view.ZenLoadingView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.appmsg.AppMsg;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class ZenGalleryActivity extends SherlockActivity {
	private static final int ZEN_MAX_ITEM_COUNT = 5;
	private ArrayList<String> imageUrls;
	private DisplayImageOptions options;
	private ImageAdapter imageAdapter;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private ZenLoadingView mLoadingView; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zen_gallery_frame);
		
		setTitle("选择照片");
		
		final String[] columns = { MediaStore.Images.Media.DATA,
				MediaStore.Images.Media._ID };
		final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
		// Cursor imagecursor = managedQuery(
		// MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
		// null, orderBy + " DESC");

		Cursor imagecursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
		
		this.imageUrls = new ArrayList<String>();
		if (imagecursor != null) {
			for (int i = 0; i < imagecursor.getCount(); i++) {
				imagecursor.moveToPosition(i);
				int dataColumnIndex = imagecursor
						.getColumnIndex(MediaStore.Images.Media.DATA);
				imageUrls.add(imagecursor.getString(dataColumnIndex));

				System.out.println("image path: " + imageUrls.get(i));
			}
		}
		
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.no_media)
				.showImageForEmptyUri(R.drawable.no_media).cacheInMemory()
				.cacheOnDisc().build();

		imageAdapter = new ImageAdapter(this, imageUrls);

		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(imageAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long id) {
				ArrayList<String> selectedItems = imageAdapter.getCheckedItems();
				if (!imageAdapter.isSelected(position) && selectedItems.size() >= ZEN_MAX_ITEM_COUNT) {
					AppMsg appmsg = AppMsg.makeText(ZenGalleryActivity.this, "最多只能选5张...", AppMsg.STYLE_ALERT);
					appmsg.show();
					return;
				}
				
				imageAdapter.setChecked(v, position);
			}

		});
		/*
		 * gridView.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { startImageGalleryActivity(position); } });
		 */

		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mLoadingView = new ZenLoadingView(this);
	}

	@Override
	protected void onPause() {
		try {
			unregisterReceiver(mBroadcastReceiver);
		} catch (Exception e) {
			// mBroadCastReceiver is not registered in some case
			e.printStackTrace();
		}

		super.onPause();
	}

	@Override
	protected void onResume() {
		registerBroadcast();
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		imageLoader.stop();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.zen_gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.zen_done:
			ProcessSelectedImages();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void registerBroadcast()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(ZenAssetsModel.ZEN_PARSER_FINISHED);
		filter.addAction(ZenAssetsModel.ZEN_PARSER_FAILED);
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ZenAssetsModel.ZEN_PARSER_FINISHED)) {
				mLoadingView.hide();
				finish();
			}
		}
		
	};
	
	private void ProcessSelectedImages() {
		ArrayList<String> selectedItems = imageAdapter.getCheckedItems();
		ZenAssetsModel model = ZenAssetsModel.getInstance();
		mLoadingView.show("正在处理...");
		model.parser(selectedItems);
	}

	public void btnChoosePhotosClick(View v) {

		ArrayList<String> selectedItems = imageAdapter.getCheckedItems();
		Toast.makeText(ZenGalleryActivity.this,
				"Total photos selected: " + selectedItems.size(),
				Toast.LENGTH_SHORT).show();
		Log.d(ZenGalleryActivity.class.getSimpleName(), "Selected Items: "
				+ selectedItems.toString());
	}
	
	/**
	 * 
	 * @author roger
	 *
	 * ImageAdapter for grid view
	 *
	 */

	public class ImageAdapter extends BaseAdapter {

		ArrayList<String> mList;
		LayoutInflater mInflater;
		Context mContext;
		SparseBooleanArray mSparseBooleanArray;

		public ImageAdapter(Context context, ArrayList<String> imageList) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
			mSparseBooleanArray = new SparseBooleanArray();
			mList = new ArrayList<String>();
			this.mList = imageList;

		}

		public ArrayList<String> getCheckedItems() {
			ArrayList<String> mTempArry = new ArrayList<String>();

			for (int i = 0; i < mList.size(); i++) {
				if (mSparseBooleanArray.get(i)) {
					mTempArry.add(mList.get(i));
				}
			}

			return mTempArry;
		}

		public boolean isSelected(int postion) {
			return mSparseBooleanArray.get(postion);
		}
		
		public void setChecked(View v, int position) {
			
			if (v != null) {
				ImageView GallerySelector = (ImageView) v
						.findViewById(R.id.zen_gallery_selector);
				if (GallerySelector != null) {
					boolean selected = mSparseBooleanArray.get(position);
					int selector = (selected ? R.drawable.checkbox_normal : R.drawable.checkbox_selected);
					GallerySelector.setImageResource(selector);
					mSparseBooleanArray.put(position, !selected);
				}
			}
		}

		@Override
		public int getCount() {
			return imageUrls.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflater
						.inflate(R.layout.zen_gallery_item, null);
			}

			ImageView GallerySelector = (ImageView) convertView
					.findViewById(R.id.zen_gallery_selector);
			final ImageView imageView = (ImageView) convertView
					.findViewById(R.id.zen_gallery_item);

			imageLoader.displayImage("file://" + imageUrls.get(position),
					imageView, options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(Bitmap loadedImage) {
							Animation anim = AnimationUtils
									.loadAnimation(ZenGalleryActivity.this,
											R.anim.zen_fade_in);
							imageView.setAnimation(anim);
							anim.start();
						}
					});

			int selector = (mSparseBooleanArray.get(position) ? R.drawable.checkbox_selected : R.drawable.checkbox_normal);
			GallerySelector.setTag(position);
			GallerySelector.setImageResource(selector);
			
			return convertView;
		}
	}
}
