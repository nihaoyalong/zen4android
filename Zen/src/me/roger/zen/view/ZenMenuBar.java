package me.roger.zen.view;

import me.roger.zen.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class ZenMenuBar {

	public static final int MENU_NONE = 1000;
	public static final int MENU_LIGHT = 1001;
	public static final int MENU_REPLY = 1002;
	public static final int MENU_COPY = 1003;
	public static final int MENU_PM = 1004;

	public static final int MENU_REFRESH = 1005;
	public static final int MENU_COMMENT = 1006;
	public static final int MENU_RECOMMEND = 1007;
	public static final int MENU_ARCHIVE = 1008;

	public static final int MENUBAR_TYPE_MORE = 888;

	private Context mContext;
	private View mMenuView;
	private View mMenuBg;
	private View mMenuBar;

	private Animation SlideInAnimation;
	private Animation SlideOutAnimation;
	private OnItemClickListener mListener;
	private int selected;

	public static synchronized ZenMenuBar getInstance(Context c, int type) {
		ZenMenuBar instance = new ZenMenuBar();
		instance.mContext = c;
		instance.inflate(type);
		return instance;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mListener = listener;
	}

	private void inflate(int type) {
		// init view
		LayoutInflater inflate = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (ZenMenuBar.MENUBAR_TYPE_MORE != type) {
			mMenuView = inflate.inflate(R.layout.zen_menu_bar, null);
			mMenuBar = mMenuView.findViewById(R.id.zen_menu_bar);
			
			Button button = (Button) mMenuBar.findViewById(R.id.menu_light);
			button.setOnClickListener(mOnClickListener);

			button = (Button) mMenuBar.findViewById(R.id.menu_reply);
			button.setOnClickListener(mOnClickListener);

			button = (Button) mMenuBar.findViewById(R.id.menu_copy);
			button.setOnClickListener(mOnClickListener);

			button = (Button) mMenuBar.findViewById(R.id.menu_pm);
			button.setOnClickListener(mOnClickListener);

		} else {
			mMenuView = inflate.inflate(R.layout.zen_menu_bar_more, null);
			mMenuBar = mMenuView.findViewById(R.id.zen_menu_bar);

			ImageButton button = (ImageButton) mMenuBar.findViewById(R.id.menu_refresh);
			button.setOnClickListener(mOnClickListener);

			button = (ImageButton) mMenuBar.findViewById(R.id.menu_comment);
			button.setOnClickListener(mOnClickListener);

			button = (ImageButton) mMenuBar.findViewById(R.id.menu_recommend);
			button.setOnClickListener(mOnClickListener);

			button = (ImageButton) mMenuBar.findViewById(R.id.menu_archive);
			button.setOnClickListener(mOnClickListener);

		}

		mMenuBg = mMenuView.findViewById(R.id.zen_menu_bg);
		mMenuBar = mMenuView.findViewById(R.id.zen_menu_bar);
		mMenuBg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selected = ZenMenuBar.MENU_NONE;
				hide();
			}
		});
		// init animation
		SlideInAnimation = AnimationUtils.loadAnimation(mContext,
				R.anim.zen_slide_in_from_bottom);
		SlideOutAnimation = AnimationUtils.loadAnimation(mContext,
				R.anim.zen_slide_out_to_bottom);
		SlideOutAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				removeFromSuperView(mMenuView);
				if (selected != ZenMenuBar.MENU_NONE && mListener != null) {
					mListener.OnMenuItemClick(selected);
					selected = ZenMenuBar.MENU_NONE;
				}
			}
		});
		removeFromSuperView(mMenuView);
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			selected = ZenMenuBar.MENU_NONE;
			if (mListener != null) {
				switch (v.getId()) {
				case R.id.menu_light:
					selected = ZenMenuBar.MENU_LIGHT;
					break;
				case R.id.menu_reply:
					selected = ZenMenuBar.MENU_REPLY;
					break;
				case R.id.menu_copy:
					selected = ZenMenuBar.MENU_COPY;
					break;
				case R.id.menu_pm:
					selected = ZenMenuBar.MENU_PM;
					break;
				case R.id.menu_refresh:
					selected = ZenMenuBar.MENU_REFRESH;
					break;
				case R.id.menu_comment:
					selected = ZenMenuBar.MENU_COMMENT;
					break;
				case R.id.menu_recommend:
					selected = ZenMenuBar.MENU_RECOMMEND;
					break;
				case R.id.menu_archive:
					selected = ZenMenuBar.MENU_ARCHIVE;
					break;
				}
				hide();
			}
		}
	};

	public void toggle() {
		selected = ZenMenuBar.MENU_NONE;
		Activity ac = (Activity) mContext;
		if (ac.findViewById(R.id.zen_menu_container) != null) {
			hide();
		}
		else {
			show();
		}
	}
	
	public void show() {
		Activity ac = (Activity) mContext;
		if (ac.findViewById(R.id.zen_menu_container) != null) {
			System.out.println("error: menu bar already exists.");
			return;
		}

		ac.addContentView(mMenuView, new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mMenuBar.startAnimation(SlideInAnimation);
	}

	private void hide() {
		mMenuBar.startAnimation(SlideOutAnimation);
	}

	private void removeFromSuperView(View view) {
		ViewGroup superView = (ViewGroup) view.getParent();
		if (superView != null) {
			superView.removeView(view);
		}
	}

	public interface OnItemClickListener {
		public void OnMenuItemClick(int type);
	}
}
