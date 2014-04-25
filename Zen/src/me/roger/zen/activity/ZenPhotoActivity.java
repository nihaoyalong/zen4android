package me.roger.zen.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import me.roger.zen.R;
import me.roger.zen.application.ZenApplication;
import me.roger.zen.model.ZenPhotoModel;
import me.roger.zen.utils.ZenUtils;
import me.roger.zen.view.GifMovieView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.appmsg.AppMsg;

public class ZenPhotoActivity extends SherlockActivity {
	private ImageView mImageView;
	private ProgressBar progress;
	private PhotoViewAttacher mAttacher;
	private boolean isGif;
	private ZenPhotoModel model;
	private GifMovieView mGif;
	private String mURL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle("Õº∆¨");

		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		mURL = url;
		if (url.toLowerCase().endsWith(".gif")) {
			isGif = true;
			setContentView(R.layout.zen_gif_frame);
			mGif = (GifMovieView) findViewById(R.id.zen_gif);
			progress = (ProgressBar) findViewById(R.id.zen_gif_waiting);
		} else {
			isGif = false;
			setContentView(R.layout.zen_photo_frame);
			mImageView = (ImageView) findViewById(R.id.zen_photo);
			mAttacher = new PhotoViewAttacher(mImageView);
			progress = (ProgressBar) findViewById(R.id.zen_photo_waiting);
		}
		model = new ZenPhotoModel();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ZenPhotoModel.ZEN_PHOTO_FINISHED);
		filter.addAction(ZenPhotoModel.ZEN_PHOTO_FAILED);
		registerReceiver(mBroadcastReceiver, filter);

		load(url);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			if (mAttacher != null) {
				mAttacher.cleanup();
			}
			unregisterReceiver(mBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.zen_photo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.zen_save:
			save();
			break;
		}

		return true;
	}

	private void load(String url) {
		progress.setVisibility(View.VISIBLE);
		model.load(url);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			progress.setVisibility(View.GONE);
			String action = intent.getAction();
			if (action.equals(ZenPhotoModel.ZEN_PHOTO_FAILED)) {
				AppMsg appmsg = AppMsg.makeText(ZenPhotoActivity.this, "º”‘ÿ ß∞‹",
						AppMsg.STYLE_ALERT);
				appmsg.show();
			} else if (action.equals(ZenPhotoModel.ZEN_PHOTO_FINISHED)) {
				try {
					if (isGif) {
						Context appContext = ZenApplication.getAppContext();
						InputStream is = appContext
								.openFileInput(ZenPhotoModel.ZEN_TEMP_FILE);
						if (is != null) {
							mGif.setMovieInputStream(is);
							return;
						}
					} else {
						Context appContext = ZenApplication.getAppContext();

						Bitmap bm = ZenUtils.decodeImage(appContext
								.getFilesDir()
								+ "/"
								+ ZenPhotoModel.ZEN_TEMP_FILE);

						if (bm != null) {
							mImageView.setImageBitmap(bm);
							return;
						}

					}
					AppMsg appmsg = AppMsg.makeText(ZenPhotoActivity.this,
							"Õº∆¨º”‘ÿ ß∞‹", AppMsg.STYLE_ALERT);
					appmsg.show();
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
				AppMsg appmsg = AppMsg.makeText(ZenPhotoActivity.this,
						"Õº∆¨Ω‚¬Î ß∞‹", AppMsg.STYLE_ALERT);
				appmsg.show();
			}
		}

	};

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static File getAlbumStorageDir() {
		// Get the directory for the user's public pictures directory.
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"Zen");
		if (!file.mkdirs()) {
			Log.e("ZEN", "Directory not created");
		}
		return file;
	}

	private static void scanPhoto(File file) {
		Context ctx = ZenApplication.getAppContext();
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri contentUri = Uri.fromFile(file);
		mediaScanIntent.setData(contentUri);
		ctx.sendBroadcast(mediaScanIntent);
	}

	private void saveGIF() {
		try {
			File file = new File(getFilesDir(), ZenPhotoModel.ZEN_TEMP_FILE);

			String fileName = ZenUtils.md5(mURL) + ".gif";
			File target = new File(getAlbumStorageDir(), fileName);

			if (!target.exists()) {
				InputStream input = new FileInputStream(file);
				FileOutputStream output = new FileOutputStream(target);
				byte[] buffer = new byte[1024];
				int len = -1;
				while ((len = input.read(buffer)) != -1) {
					output.write(buffer, 0, len);
				}
				input.close();
				output.close();
				AppMsg appmsg = AppMsg
						.makeText(this, "±£¥Ê≥…π¶", AppMsg.STYLE_INFO);
				appmsg.show();
				scanPhoto(target);
			} else {
				AppMsg appmsg = AppMsg.makeText(this, "Õº∆¨“—¥Ê‘⁄...",
						AppMsg.STYLE_ALERT);
				appmsg.show();
			}

			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		AppMsg appmsg = AppMsg.makeText(this, "±£¥Ê ß∞‹", AppMsg.STYLE_ALERT);
		appmsg.show();

	}

	private void saveJPEG() {
		try {
			File file = new File(getFilesDir(), ZenPhotoModel.ZEN_TEMP_FILE);
			String fileName = ZenUtils.md5(mURL) + ".jpg";
			
			File target = new File(getAlbumStorageDir(), fileName);
			
			if (!target.exists()) {
				InputStream input = new FileInputStream(file);
				FileOutputStream output = new FileOutputStream(target);
				byte[] buffer = new byte[1024];
				int len = -1;
				while ((len = input.read(buffer)) != -1) {
					output.write(buffer, 0, len);
				}
				input.close();
				output.close();
				AppMsg appmsg = AppMsg
						.makeText(this, "±£¥Ê≥…π¶", AppMsg.STYLE_INFO);
				appmsg.show();
				scanPhoto(target);
			} else {
				AppMsg appmsg = AppMsg.makeText(this, "Õº∆¨“—¥Ê‘⁄...",
						AppMsg.STYLE_ALERT);
				appmsg.show();
			}
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		AppMsg appmsg = AppMsg.makeText(this, "±£¥Ê ß∞‹", AppMsg.STYLE_ALERT);
		appmsg.show();
	}

	private void save() {
		if (!isExternalStorageWritable()) {
			AppMsg appmsg = AppMsg.makeText(this, "±£¥Ê ß∞‹£¨«Î≤Â»ÎSDø®",
					AppMsg.STYLE_ALERT);
			appmsg.show();
			return;
		}
		if (isGif) {
			saveGIF();
		} else {
			saveJPEG();
		}
	}

}
