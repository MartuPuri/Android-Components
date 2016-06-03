package com.masacre.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 *
 * A <code>BitmapImage</code> is a model that encapsulate a {@link Bitmap}
 * and his corresponding {@link Uri} path
 *
 * @author Martin Purita - martinpurita@gmail.com
 *
 */
public class BitmapImage {
	private final Bitmap bitmap;
	private final Uri uri;

	/**
	 * Create a bitmap image with his uri path
	 *
	 * @param bitmap the image data
	 * @param uri the path of the image
	 */
	public BitmapImage(@NonNull final Bitmap bitmap, @NonNull final Uri uri) {
		this.bitmap = bitmap;
		this.uri = uri;
	}

	@NonNull
	public Bitmap getBitmap() {
		return bitmap;
	}

	@NonNull
	public Uri getUri() {
		return uri;
	}
}