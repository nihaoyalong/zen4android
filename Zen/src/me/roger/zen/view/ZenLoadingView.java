package me.roger.zen.view;

import me.roger.zen.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ZenLoadingView extends FrameLayout {
	private Animation mSlideInAnimation;
	private Animation mSlideOutAnimation;
	private TextView mTitle;
	public boolean isLoading;
	
	public ZenLoadingView(Context context) {
		super(context);
		load();
	}
	
	private void load() {
		Context context = getContext();
		LayoutInflater.from(context).inflate(R.layout.zen_loading_view, this, true);
		mSlideInAnimation = AnimationUtils.loadAnimation(context, R.anim.zen_slide_in_from_top);
		mSlideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.zen_slide_out_to_top);
		mSlideOutAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				ZenLoadingView.this.setVisibility(View.GONE);
			}
		});
		
		mTitle = (TextView)findViewById(R.id.zen_loading_title);
		
		Activity ac = (Activity)getContext();
		ac.addContentView(this, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		this.setVisibility(View.INVISIBLE);
		isLoading = false;
	}
	
	public void setTitle(String text) {
		mTitle.setText(text);
	}
	
	public void show() {
		isLoading = true;
		mTitle.setText("ÕýÔÚ¼ÓÔØ...");
		this.setVisibility(View.VISIBLE);
		this.startAnimation(mSlideInAnimation);
	}
	
	public void show(String title) {
		mTitle.setText(title);
		isLoading = true;
		this.setVisibility(View.VISIBLE);
		this.startAnimation(mSlideInAnimation);
	}
	
	public void hide() {
		isLoading = false;
		if (this.getVisibility() == View.GONE) {
			return;
		}
		this.startAnimation(mSlideOutAnimation);
	}

}
