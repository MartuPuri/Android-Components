package com.masacre.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PullDownSwipeRefreshLayout extends SwipeRefreshLayout {

	private PullDownListener pullDownListener;

	/**
	 *
	 * Inherited from {@link SwipeRefreshLayout#SwipeRefreshLayout(Context, AttributeSet)}
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 */
	public PullDownSwipeRefreshLayout(@NonNull final Context context, @Nullable final AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Register a callback to be invoked when you pull down the swipe refresh layout
	 * before calling {@link OnRefreshListener#onRefresh()}
	 *
	 * @param pullDownListener The callback that will run
	 */
	public void setPullDownListener(@Nullable final PullDownListener pullDownListener) {
		this.pullDownListener = pullDownListener;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		if (ev.getAction() == ev.ACTION_UP && pullDownListener != null) {
			pullDownListener.onPullDown();
		}
		return super.onTouchEvent(ev);
	}

	public interface PullDownListener {
		/**
		 *
		 * Called when you pull down the swipe refresh layout
		 * before calling {@link OnRefreshListener#onRefresh()}
		 *
		 */
		void onPullDown();
	}
}
