package me.roger.zen.view;

import me.roger.zen.R;
import net.simonvt.numberpicker.NumberPicker;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class ZenPicker extends FrameLayout {
	
	private RelativeLayout mContainer;
	private NumberPicker mPicker;
	
	private Button mCancel;
	private Button mEnd;
	private Button mJump;
	
	private Animation mSlideInAnimation;
	private Animation mSlideOutAnimation;
	private OnJumpListener mListener;
	
	public ZenPicker(Context context) {
		super(context);
		load();
	}
	
	private void load() {
		Context context = getContext();
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.zen_picker_view, this, true);
		mContainer = (RelativeLayout)findViewById(R.id.zen_picker_container);
		
		mCancel = (Button)findViewById(R.id.zen_picker_cancel);
		mCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				hide();
			}
		});
		
		mEnd = (Button)findViewById(R.id.zen_picker_end);
		mEnd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				end();
			}
		});
		
		mJump = (Button)findViewById(R.id.zen_picker_jump);
		mJump.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				jump();
			}
		});
		View background = (View) findViewById(R.id.zen_picker_bg);
		background.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hide();
			}
		});
		
		View mask = (View)findViewById(R.id.zen_picker_mask);
		mask.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hide();
			}
		});
		
		mPicker = (NumberPicker) findViewById(R.id.zen_picker);
		mPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		mPicker.setFocusable(true);
		mPicker.setFocusableInTouchMode(true);
		
		mSlideInAnimation = AnimationUtils.loadAnimation(context, R.anim.zen_slide_in_from_bottom);
		mSlideInAnimation.setDuration(300);
		mSlideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.zen_slide_out_to_bottom);
		mSlideOutAnimation.setDuration(200);
		mSlideOutAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				setVisibility(View.GONE);
			}
		});
		Activity ac = (Activity)getContext();
		ac.addContentView(this, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		setVisibility(View.GONE);
	}
	
	public void show() {
		System.out.println("picker show.");
		if (getVisibility() == View.GONE) {
			System.out.println("picker show. do...");
			setVisibility(View.VISIBLE);
			mContainer.startAnimation(mSlideInAnimation);
		}
	}
	
	public void hide() {
		if (getVisibility() == View.VISIBLE) {
			mContainer.startAnimation(mSlideOutAnimation);
		}
	}
	
	private void jump() {
		int page = mPicker.getValue();
		if (mListener != null) {
			mListener.OnJump(page);
		}
		hide();
	}
	
	private void end() {
		int page = mPicker.getMaxValue();
		if (mListener != null) {
			mListener.OnJump(page);
		}
		hide();
	}
	
	public void setMin(int min) {
		mPicker.setMinValue(min);
	}
	
	public void setMax(int max) {
		mPicker.setMaxValue(max);
	}
	
	public void setValue(int value) {
		mPicker.setValue(value);
	}
	
	public void setOnJumpListener(OnJumpListener listener) {
		mListener = listener;
	}
	
	public interface OnJumpListener {
		public void OnJump(int page);
	};
}
