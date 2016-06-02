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

public final class ImagePicker {
	private static final String UTILITY_CLASS_ERROR = "This class cannot be instantiated";
	private static final String RETURN_DATA = "return-data";
	private static final String TEMP_IMAGE_NAME = "temp_image";
	private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;
	private static final int MAX_QUALITY = 100;

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

		final ArrayList<Intent> packageIntentList = createPackageIntentList(context, intentList);

		if (intentList.isEmpty()) {
			final Intent chooserIntent = Intent.createChooser(getAndRemoveLastIntent(intentList),
					context.getString(R.string.choose_action));
			chooserIntent.putParcelableArrayListExtra(Intent.EXTRA_INITIAL_INTENTS, packageIntentList);
			return chooserIntent;
		} else {
			return null;
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


	private static ArrayList<Intent> createPackageIntentList(@NonNull final Context context, @NonNull final Iterable<Intent> intents) {
		final ArrayList<Intent> packageIntentList = new ArrayList<>();
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

	public static BitmapImage getImageFromResult(final Context context, final int resultCode,
		final Intent imageReturnedIntent) {
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

	public static void saveRotatedBitmap(@NonNull final Uri imageUri, @NonNull final Bitmap bitmap) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(imageUri.getPath());
			bitmap.compress(Bitmap.CompressFormat.PNG, MAX_QUALITY, out);
		} catch (final Exception e) {
			//TODO: Handle error
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (final IOException e) {
				//TODO: Handle error
				e.printStackTrace();
			}
		}
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

	private static Bitmap decodeBitmap(@NonNull final Context context, @NonNull final Uri theUri, final int sampleSize) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;

		AssetFileDescriptor fileDescriptor = null;
		try {
			fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
		} catch (final FileNotFoundException e) {
			//TODO: Handle error
			e.printStackTrace();
		}

		return BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
	}

	/**
	 * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
	 **/
	private static Bitmap getImageResized(@NonNull final Context context, @NonNull final Uri selectedImage) {
		Bitmap bm = null;
		int[] sampleSizes = new int[]{5, 3, 2, 1};
		int i = 0;
		do {
			bm = decodeBitmap(context, selectedImage, sampleSizes[i]);
			i++;
		} while (bm.getWidth() < DEFAULT_MIN_WIDTH_QUALITY && i < sampleSizes.length);
		return bm;
	}


	private static int getRotation(@NonNull final Context context, @NonNull final Uri imageUri,
		final boolean isFromCamera) {
		int rotation;
		if (isFromCamera) {
			rotation = getRotationFromCamera(context, imageUri);
		} else {
			rotation = getRotationFromGallery(context, imageUri);
		}
		return rotation;
	}

	private static int getRotationFromCamera(@NonNull final Context context, @NonNull final Uri imageFile) {
		int rotate = 0;
		try {
			context.getContentResolver().notifyChange(imageFile, null);
			ExifInterface exif = new ExifInterface(imageFile.getPath());
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotate = 270;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotate = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_90:
					rotate = 90;
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rotate;
	}

	public static int getRotationFromGallery(@NonNull final Context context, @NonNull final Uri imageUri) {
		int result = 0;
		String[] columns = {MediaStore.Images.Media.ORIENTATION};
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
				result = cursor.getInt(orientationColumnIndex);
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}


	private static Bitmap rotate(@NonNull final Bitmap bm, final int rotation) {
		if (rotation != 0) {
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