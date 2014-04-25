package me.roger.zen.view;

import me.roger.zen.R;
import me.roger.zen.model.ZenAssetsModel;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ZenCommentView extends LinearLayout {
	
	private InputMethodManager imm;
	private EditText mEditText;
	private OnZenClickListener mListener;
	private boolean mShouldDismiss;
	
	public ZenCommentView(Context context) {
		super(context);
		load();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		System.out.println("onSizeChanged, oldh: " + oldh + " h: " + h);
		
		if (oldh > 0 && oldh < h) {
			// hide edit text
			if (mShouldDismiss) {
				hide();
			}
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void load() {
		Context context = getContext();
		LayoutInflater.from(context).inflate(R.layout.zen_comment_view, this, true);
		imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mEditText = (EditText)findViewById(R.id.zen_edit_text);
		mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				System.out.println("EditText OnFocusChange. " + hasFocus);
				if (hasFocus) {
					imm.showSoftInput(v, 0);
				}
				
			}
		});
		View background = (View)findViewById(R.id.zen_edit_bg);
		background.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mShouldDismiss = true;
				close();
			}
		});
		
		Button sendBtn = (Button)findViewById(R.id.zen_send_btn);
		sendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mShouldDismiss = true;
					mListener.OnSendClick(mEditText.getText().toString());
				}
			}
		});
		
//		mAddPhoto = (ImageButton)findViewById(R.id.zen_add_photo);
//		mAddPhoto.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if (mListener != null) {
//					mShouldDismiss = false;
//					mListener.OnChooseImageClick();
//				}
//			}
//		});
		
		Activity ac = (Activity)getContext();
		ac.addContentView(this, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		this.setVisibility(View.GONE);
	}
	
	public void setOnSendClickListener(OnZenClickListener listener) {
		mListener = listener;
	}
	
	public void show() {
		this.setVisibility(View.VISIBLE);		
		mEditText.setFocusable(true);
		mEditText.setFocusableInTouchMode(true);
		mEditText.requestFocus();
		
	}
	
	public void hide() {
		if (this.getVisibility() != View.GONE) {
			System.out.println("comment view hide");
			mEditText.setText("");
			mEditText.clearFocus();
			this.setVisibility(View.GONE);
			ZenAssetsModel.getInstance().clear();
		}
	}
	
	public void openKeyboard() {
		System.out.println("openKeyboard...");
		mShouldDismiss = true;
		imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
	}
	
	public void close() {
		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	}
	
	public void setShouldDismiss(boolean shouldDismiss) {
		mShouldDismiss = shouldDismiss;
	}
	

	public interface OnZenClickListener {
		public void OnSendClick(String content);
		public void OnChooseImageClick();
	};
	
}
