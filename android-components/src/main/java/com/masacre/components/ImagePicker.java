package com.masacre.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.masacre.model.BitmapImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * The <code>ImagePicker</code> class provides you a full component, that allow you to select a picture
 * from each gallery and camera apps that your device has
 *
 * @author Martin Purita - martinpurita@gmail.com
 *
 */
public final class ImagePicker {
	private static final String UTILITY_CLASS_ERROR = "This class cannot be instantiated";
	private static final String RETURN_DATA = "return-data";
	private static final String TEMP_IMAGE_NAME = "temp_image";
	private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;
	private static final int MAX_QUALITY = 100;
	private static final int ROTATION_0 = 0;
	private static final int ROTATION_90 = 90;
	private static final int ROTATION_180 = 180;
	private static final int ROTATION_270 = 270;
	private static final int ROTATION_360 = 360;



	private ImagePicker() {
		throw new IllegalAccessError(UTILITY_CLASS_ERROR);
	}

	/**
	 *
	 * Create an intent with the gallery and/or camera intent. This method is use when
	 * you want to retrieve a photo from the gallery or camera.
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 *
	 * @return An intent where you can choose pick a image from gallery and/or camera
	 */
	public static Intent getPickImageIntent(@NonNull final Context context, @NonNull final ImagePickerIntentType... imagePickerIntentTypes) {
		final List<Intent> intentList = new ArrayList<>();

		for (final ImagePickerIntentType imagePickerIntentType : imagePickerIntentTypes) {
			if (ImagePickerIntentType.CAMERA == imagePickerIntentType) {
				final Intent cameraIntent = getCameraIntent(context);
				intentList.add(cameraIntent);
			} else if (ImagePickerIntentType.GALLERY == imagePickerIntentType) {
				final Intent galleryIntent = getGalleryIntent();
				intentList.add(galleryIntent);
			}
		}

		final List<Intent> packageIntentList = createPackageIntentList(context, intentList);

		if (intentList.isEmpty()) {
			return null;
		} else {
			final Intent chooserIntent = Intent.createChooser(getAndRemoveLastIntent(packageIntentList),
					context.getString(R.string.choose_action));
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, packageIntentList.toArray(new Parcelable[]{}));
			return chooserIntent;
		}
	}

	@NonNull
	private static Intent getGalleryIntent() {
		return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	}

	@NonNull
	private static Intent getCameraIntent(@NonNull final Context context) {
		final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(RETURN_DATA, true);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getUriImageFromCamera(getTempFile(context)));
		return cameraIntent;
	}

	@NonNull
	private static Intent getAndRemoveLastIntent(@NonNull final List<Intent> intentList) {
		return intentList.remove(intentList.size() - 1);
	}


	private static List<Intent> createPackageIntentList(@NonNull final Context context, @NonNull final Iterable<Intent> intents) {
		final List<Intent> packageIntentList = new ArrayList<>();
		for (final Intent intent : intents) {
			final List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
			for (final ResolveInfo resolveInfo : resInfo) {
				final String packageName = resolveInfo.activityInfo.packageName;
				final Intent targetedIntent = new Intent(intent);
				targetedIntent.setPackage(packageName);
				packageIntentList.add(targetedIntent);
			}
		}
		return packageIntentList;
	}

	/**
	 *
	 * Retrieve the bitmap image in {@link Activity#onActivityResult(int, int, Intent)} or
	 * {@link android.support.v4.app.Fragment#onActivityResult(int, int, Intent)}
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 * @param resultCode The integer result code returned by the child activity
	 *                   through its setResult().
	 * @param imageReturnedIntent a An Intent, which can return result data to the caller
	 *               (various data can be attached to Intent "extras").
	 *
	 * @return The bitmap image that you have selected
	 * @throws IOException if file cannot be opened for writing or if a problem occurs while closing this channel
	 *
	 */
	public static BitmapImage getImageFromResult(final Context context, final int resultCode,
		final Intent imageReturnedIntent) throws IOException {
		final File imageFile = getTempFile(context);
		if (resultCode == Activity.RESULT_OK) {
			final Uri selectedImageUri;
			final Uri uriImageFromGallery = getUriImageFromGallery(imageReturnedIntent);
			boolean isFromCamera = isImageFromCamera(imageReturnedIntent, imageFile, uriImageFromGallery);
			if (isFromCamera) {
				selectedImageUri = getUriImageFromCamera(imageFile);
			} else {
				selectedImageUri = uriImageFromGallery;
			}

			Bitmap bitmap = getImageResized(context, selectedImageUri);
			final int rotation = getRotation(context, selectedImageUri, isFromCamera);
			bitmap = rotate(bitmap, rotation);

			return new BitmapImage(bitmap, selectedImageUri);
		}
		return null;
	}

	private static Uri getUriImageFromGallery(@Nullable final Intent imageReturnedIntent) {
		return imageReturnedIntent == null ? null : imageReturnedIntent.getData();
	}

	/**
	 *
	 * Save the bitmap image to disk
	 *
	 * @param imageUri The path where the image will be saved
	 * @param bitmap The image data
	 *
	 * @throws IOException if file cannot be opened for writing or if a problem occurs while closing this channel
	 *
	 */
	public static void saveBitmaptoDisk(@NonNull final Uri imageUri, @NonNull final Bitmap bitmap) throws IOException {
		final FileOutputStream out = new FileOutputStream(imageUri.getPath());
		bitmap.compress(Bitmap.CompressFormat.PNG, MAX_QUALITY, out);
		out.close();
	}

	private static boolean isImageFromCamera(@Nullable final Intent imageReturnedIntent, @
			NonNull final File imageFile, final @Nullable Uri uriImageFromGallery) {
		return imageReturnedIntent == null || uriImageFromGallery == null ||
				uriImageFromGallery.toString().contains(imageFile.toString());
	}

	private static Uri getUriImageFromCamera(@NonNull final File imageFile) {
		return Uri.fromFile(imageFile);
	}

	private static File getTempFile(@NonNull final Context context) {
		final File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
		imageFile.getParentFile().mkdirs();
		return imageFile;
	}

	private static Bitmap decodeBitmap(@NonNull final Context context, @NonNull final Uri theUri,
		final int sampleSize) throws FileNotFoundException {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;

		final AssetFileDescriptor fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");

		return BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
	}

	private static Bitmap getImageResized(@NonNull final Context context,
		@NonNull final Uri imageUri) throws FileNotFoundException {
		Bitmap bm = null;
		int[] sampleSizes = new int[]{5, 3, 2, 1};
		int i = 0;
		do {
			bm = decodeBitmap(context, imageUri, sampleSizes[i]);
			i++;
		} while (bm.getWidth() < DEFAULT_MIN_WIDTH_QUALITY && i < sampleSizes.length);
		return bm;
	}

	private static int getRotation(@NonNull final Context context, @NonNull final Uri imageUri,
		final boolean isFromCamera) throws IOException {
		final int rotation;
		if (isFromCamera) {
			rotation = getRotationFromCamera(context, imageUri);
		} else {
			rotation = getRotationFromGallery(context, imageUri);
		}
		return rotation;
	}

	/**
	 *
	 * Get the rotation of the image that was pick from the camera
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 * @param imageUri The uri path of the image
	 * @return The rotation of the image
	 */
	private static int getRotationFromCamera(@NonNull final Context context,
		@NonNull final Uri imageUri) throws IOException {
		context.getContentResolver().notifyChange(imageUri, null);
		final ExifInterface exif = new ExifInterface(imageUri.getPath());
		final int orientation = exif.getAttributeInt(
				ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_NORMAL);

		final int rotate;
		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotate = ROTATION_270;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotate = ROTATION_180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotate = ROTATION_90;
			break;
		default:
			rotate = ROTATION_0;
			break;
		}
		return rotate;
	}

	/**
	 *
	 * Get the rotation of the image that was pick from gallery
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 * @param imageUri The uri path of the image
	 * @return The rotation of the image
	 */
	private static int getRotationFromGallery(@NonNull final Context context, @NonNull final Uri imageUri) {
		int result = 0;
		final String[] columns = {MediaStore.Images.Media.ORIENTATION};
		final Cursor cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			final int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
			result = cursor.getInt(orientationColumnIndex);
			cursor.close();
		}

		return result;
	}


	private static Bitmap rotate(@NonNull final Bitmap bm, final int rotation) {
		if (rotation != ROTATION_0 && rotation != ROTATION_360) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotation);
			Bitmap bmOut = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
			return bmOut;
		}
		return bm;
	}

	public enum ImagePickerIntentType {
		CAMERA, GALLERY
	}
}