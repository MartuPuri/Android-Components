/*
 * Copyright (C) 2016 Martin Purita
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.masacre.components;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * <p>
 * A <code>ClickableRecyclerView</code> improve the {@link RecyclerView} by adding
 * method to handle the {@link android.view.View.OnClickListener} and {@link android.view.View.OnLongClickListener}
 * of each {@link android.support.v7.widget.RecyclerView.ViewHolder}.
 * </p>
 *
 * There are two exclusive ways of handle the click event.
 * <ul>
 *     <li>Register the following listeners {@link OnViewHolderClickListener}
 *     and/or {@link OnViewHolderLongClickListener} in the recycler view</li>
 *     <li>Override the methods {@link android.view.View.OnClickListener#onClick(View)} and/or
 *     {@link android.view.View.OnLongClickListener#onLongClick(View)} in the view holder</li>
 * </ul>
 *
 * @author Martin Purita - martinpurita@gmail.com
 *
 * @see OnViewHolderClickListener
 */
public class ClickableRecyclerView extends RecyclerView {
	private OnViewHolderClickListener onViewHolderClickListener;
	private OnViewHolderLongClickListener onViewHolderLongClickListener;

	/**
	 *
	 * Inherited from {@link View#View(Context)}
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 */
	public ClickableRecyclerView(final Context context) {
		super(context);
	}

	/**
	 *
	 * Inherited from {@link View#View(Context, AttributeSet)}
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 */
	public ClickableRecyclerView(final Context context, @Nullable final AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 *
	 * Inherited from {@link View#View(Context, AttributeSet, int)}
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 * @param defStyleAttr An attribute in the current theme that contains a
	 *        reference to a style resource that supplies default values for
	 *        the view. Can be 0 to not look for defaults.
	 */
	public ClickableRecyclerView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Register a callback to be invoked when this view holder is clicked. If this view is not
	 * clickable, it becomes clickable.
	 * If the callback is null you must override {@link ClickableViewHolder#onClick(View)} to handle
	 * the click event.
	 *
	 * @param listener The callback that will run
	 * @param <V> The view holder type
	 *
	 * @see #setClickable(boolean)
	 */
	public <V extends ClickableViewHolder> void setOnViewHolderClickListener(
			@Nullable final OnViewHolderClickListener<V> listener) {
		this.onViewHolderClickListener = listener;
		setupViewHolderListener(getAdapter());
	}

	/**
	 * Register a callback to be invoked when this view holder has been clicked and held. If this view is not
	 * clickable, it becomes clickable.
	 * If the callback is null you must override {@link ClickableViewHolder#onLongClick(View)} to handle
	 * the long click event.
	 *
	 * @param listener The callback that will run
	 * @param <V> The view holder type
	 *
	 * @see #setClickable(boolean)
	 */
	public <V extends ClickableViewHolder> void setOnViewHolderLongClickListener(
			@Nullable final OnViewHolderLongClickListener<V> listener) {
		this.onViewHolderLongClickListener = listener;
		setupViewHolderListener(getAdapter());
	}

	@Override
	public void setAdapter(final Adapter adapter) {
		super.setAdapter(adapter);
		setupViewHolderListener(adapter);
	}

	private void setupViewHolderListener(final Adapter adapter) {
		if (adapter instanceof Adapter) {
			((ClickableAdapter) adapter).setOnViewHolderClickListener(onViewHolderClickListener);
			((ClickableAdapter) adapter).setOnViewHolderLongClickListener(onViewHolderLongClickListener);
		}
	}

	public abstract static class ClickableAdapter<V extends ClickableViewHolder> extends Adapter<V> {
		private OnViewHolderClickListener<ClickableViewHolder> onViewHolderClickListener;
		private OnViewHolderLongClickListener<ClickableViewHolder> onViewHolderLongClickListener;

		@Override
		public void onBindViewHolder(final V holder, final int position, final List<Object> payloads) {
			holder.setPosition(position);
			holder.setOnViewHolderClickListener(onViewHolderClickListener);
			holder.setOnViewHolderLongClickListener(onViewHolderLongClickListener);
			super.onBindViewHolder(holder, position, payloads);
		}

		/* default */ void setOnViewHolderClickListener(
				@Nullable final OnViewHolderClickListener<ClickableViewHolder> listener) {
			this.onViewHolderClickListener = listener;
		}

		/* default */ void setOnViewHolderLongClickListener(
				@Nullable final OnViewHolderLongClickListener<ClickableViewHolder> listener) {
			this.onViewHolderLongClickListener = listener;
		}
	}

	public abstract static class ClickableViewHolder extends ViewHolder implements OnClickListener,
			OnLongClickListener {
		private int position;
		private OnViewHolderClickListener<ClickableViewHolder> onViewHolderClickListener;
		private OnViewHolderLongClickListener<ClickableViewHolder> onViewHolderLongClickListener;

		/**
		 * Initialize a ViewHolder
		 *
		 * @param itemView The view that was inflated
		 */
		public ClickableViewHolder(final View itemView) {
			super(itemView);
			setupClickListeners();
		}

		private void setupClickListeners() {
			setupOnClickListener();
			setupOnLongClickListener();
		}

		private void setupOnClickListener() {
			itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					if (onViewHolderClickListener == null) {
						ClickableViewHolder.this.onClick(v);
					} else {
						onViewHolderClickListener.onItemClick(ClickableViewHolder.this, position);
					}
				}
			});
		}

		private void setupOnLongClickListener() {
			itemView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(final View v) {
					if (onViewHolderLongClickListener == null) {
						return ClickableViewHolder.this.onLongClick(v);
					} else {
						return onViewHolderLongClickListener.onLongItemClick(ClickableViewHolder.this, position);
					}
				}
			});
		}

		/* default */ void setPosition(final int position) {
			this.position = position;
		}

		/* default */ void setOnViewHolderClickListener(@Nullable  final OnViewHolderClickListener<ClickableViewHolder> listener) {
			this.onViewHolderClickListener = listener;
		}

		/* default */ void setOnViewHolderLongClickListener(@Nullable  final OnViewHolderLongClickListener<ClickableViewHolder> listener) {
			this.onViewHolderLongClickListener = listener;
		}

		@Override
		public void onClick(final View v) {
			Log.d("", "");
			// To handle the onClick the subclass must override the method
		}

		@Override
		public boolean onLongClick(final View v) {
			// To handle the onLongClick the subclass must override the method
			return false;
		}

		public View getItemView() {
			return this.itemView;
		}
	}

	/**
	 *
	 * Interface definition for a callback to be invoked when a view holder is clicked and
	 * when a vie holder has been clicked and held.
	 *
	 * @param <V> The {@link ClickableViewHolder} that you add to the adapter
	 *
	 */
	public interface OnViewHolderClickListener<V extends ClickableViewHolder> {
		/**
		 *
		 * Called when a view holder has been clicked.
		 *
		 * @param holder The view holder that was clicked
		 * @param position The item position in the the adapter
		 */
		void onItemClick(final V holder, final int position);
	}

	/**
	 *
	 * Interface definition for a callback to be invoked when a view holder has been clicked and held
	 *
	 * @param <V> The {@link ClickableViewHolder} that you add to the adapter
	 *
	 */
	public interface OnViewHolderLongClickListener<V extends ClickableViewHolder> {
		/**
		 *
		 * Called when a view holder has been clicked and held.
		 *
		 * @param holder The view holder that was clicked
		 * @param position The item position in the the adapter
		 * @return true if the callback consumed the long click, false otherwise.
		 */
		boolean onLongItemClick(final V holder, final int position);
	}
}
