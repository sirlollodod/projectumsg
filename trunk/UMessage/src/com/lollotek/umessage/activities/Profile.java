package com.lollotek.umessage.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class Profile extends Activity {

	private ImageView iv;
	private Button b;
	private Context context;
	private final int TAKE_IMAGE = 0, CROP_IMAGE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;

		setContentView(R.layout.activity_profile);

		iv = (ImageView) findViewById(R.id.imageView1);
		b = (Button) findViewById(R.id.button1);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File mainFolder = Utility.getMainFolder(context);

				File myNewProfileImageTemp = new File(mainFolder.toString()
						+ Settings.MY_PROFILE_IMAGE_SRC_TEMP);

				Intent i = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(myNewProfileImageTemp));
				startActivityForResult(i, TAKE_IMAGE);
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		loadProfileImage();
	}

	public void loadProfileImage() {
		File mainFolder = Utility.getMainFolder(UMessageApplication
				.getContext());
		File myProfileImage = new File(mainFolder.toString()
				+ Settings.MY_PROFILE_IMAGE_SRC);
		if (myProfileImage.exists()) {
			iv.setImageURI(Uri.fromFile(myProfileImage));
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case TAKE_IMAGE:
			try {
				File mainFolder = Utility.getMainFolder(context);

				File myNewProfileImageTemp = new File(mainFolder.toString()
						+ Settings.MY_PROFILE_IMAGE_SRC_TEMP);

				// compressImage(myNewProfileImage.toString());

				Intent cropIntent = new Intent("com.android.camera.action.CROP");
				// indicate image type and Uri
				cropIntent.setDataAndType(Uri.fromFile(myNewProfileImageTemp),
						"image/*");
				// set crop properties
				cropIntent.putExtra("crop", "true");
				// indicate aspect of desired crop
				cropIntent.putExtra("aspectX", 1);
				cropIntent.putExtra("aspectY", 1);
				// indicate output X and Y
				cropIntent.putExtra("outputX", 128);
				cropIntent.putExtra("outputY", 128);
				// retrieve data on return
				cropIntent.putExtra("return-data", true);
				// start the activity - we handle returning in onActivityResult
				startActivityForResult(cropIntent, CROP_IMAGE);

			} catch (Exception e) {
				// Toast.makeText(context, e.toString(),
				// Toast.LENGTH_LONG).show();
			}
			break;

		case CROP_IMAGE:

			Bitmap thePic;
			File mainFolder = Utility.getMainFolder(context);

			File myNewProfileImageTemp = new File(mainFolder.toString()
					+ Settings.MY_PROFILE_IMAGE_SRC_TEMP);

			try {
				Bundle extras = data.getExtras();
				thePic = extras.getParcelable("data");

				iv.setImageBitmap(thePic);

				FileOutputStream out = new FileOutputStream(
						myNewProfileImageTemp);
				thePic.compress(Bitmap.CompressFormat.JPEG, 75, out);
				out.close();

				File myNewProfileImage = new File(mainFolder.toString()
						+ Settings.MY_PROFILE_IMAGE_SRC);
				myNewProfileImageTemp.renameTo(myNewProfileImage);

				// compressImage(myNewProfileImage.toString());

				Configuration configuration = Utility.getConfiguration(context);
				configuration.setProfileImageToUpload(true);
				Utility.setConfiguration(context, configuration);
				
				Intent service = new Intent(UMessageApplication.getContext(),
						com.lollotek.umessage.services.UMessageService.class);
				service.putExtra("action", MessageTypes.UPLOAD_MY_PROFILE_IMAGE);

				startService(service);

			} catch (Exception e) {
				if (myNewProfileImageTemp.isFile()) {
					myNewProfileImageTemp.delete();
				}
				// Toast.makeText(context, e.toString(),
				// Toast.LENGTH_LONG).show();
			}
			break;
		}

	}

	private String compressImage(String imageUri) {

		String filePath = getRealPathFromURI(imageUri);
		Bitmap scaledBitmap = null;

		BitmapFactory.Options options = new BitmapFactory.Options();

		// by setting this field as true, the actual bitmap pixels are not
		// loaded in the memory. Just the bounds are loaded. If
		// you try the use the bitmap here, you will get null.
		options.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

		int actualHeight = options.outHeight;
		int actualWidth = options.outWidth;

		// max Height and width values of the compressed image is taken as
		// 816x612

		float maxHeight = 816.0f;
		float maxWidth = 612.0f;
		float imgRatio = actualWidth / actualHeight;
		float maxRatio = maxWidth / maxHeight;

		// width and height values are set maintaining the aspect ratio of the
		// image

		if (actualHeight > maxHeight || actualWidth > maxWidth) {
			if (imgRatio < maxRatio) {
				imgRatio = maxHeight / actualHeight;
				actualWidth = (int) (imgRatio * actualWidth);
				actualHeight = (int) maxHeight;
			} else if (imgRatio > maxRatio) {
				imgRatio = maxWidth / actualWidth;
				actualHeight = (int) (imgRatio * actualHeight);
				actualWidth = (int) maxWidth;
			} else {
				actualHeight = (int) maxHeight;
				actualWidth = (int) maxWidth;

			}
		}

		// setting inSampleSize value allows to load a scaled down version of
		// the original image

		options.inSampleSize = calculateInSampleSize(options, actualWidth,
				actualHeight);

		// inJustDecodeBounds set to false to load the actual bitmap
		options.inJustDecodeBounds = false;

		// this options allow android to claim the bitmap memory if it runs low
		// on memory
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[16 * 1024];

		try {
			// load the bitmap from its path
			bmp = BitmapFactory.decodeFile(filePath, options);
		} catch (OutOfMemoryError exception) {
			exception.printStackTrace();

		}
		try {
			scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,
					Bitmap.Config.ARGB_8888);
		} catch (OutOfMemoryError exception) {
			exception.printStackTrace();
		}

		float ratioX = actualWidth / (float) options.outWidth;
		float ratioY = actualHeight / (float) options.outHeight;
		float middleX = actualWidth / 2.0f;
		float middleY = actualHeight / 2.0f;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2,
				middleY - bmp.getHeight() / 2, new Paint(
						Paint.FILTER_BITMAP_FLAG));

		// check the rotation of the image and display it properly
		ExifInterface exif;
		try {
			exif = new ExifInterface(filePath);

			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, 0);
			Log.d("EXIF", "Exif: " + orientation);
			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
				Log.d("EXIF", "Exif: " + orientation);
			} else if (orientation == 3) {
				matrix.postRotate(180);
				// Log.d("EXIF", "Exif: " + orientation);
			} else if (orientation == 8) {
				matrix.postRotate(270);
				// Log.d("EXIF", "Exif: " + orientation);
			}
			scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
					scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
					true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileOutputStream out = null;
		String filename = imageUri;
		try {
			out = new FileOutputStream(filename);

			// write the compressed bitmap at the destination specified by
			// filename.
			scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return filename;

	}

	private String getFilename() {
		File file = new File(Environment.getExternalStorageDirectory()
				.getPath(), "MyFolder/Images");
		if (!file.exists()) {
			file.mkdirs();
		}
		String uriSting = (file.getAbsolutePath() + "/"
				+ System.currentTimeMillis() + ".jpg");
		return uriSting;

	}

	private String getRealPathFromURI(String contentURI) {
		Uri contentUri = Uri.parse(contentURI);
		Cursor cursor = getContentResolver().query(contentUri, null, null,
				null, null);
		if (cursor == null) {
			return contentUri.getPath();
		} else {
			cursor.moveToFirst();
			int index = cursor
					.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			return cursor.getString(index);
		}
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		final float totalPixels = width * height;
		final float totalReqPixelsCap = reqWidth * reqHeight * 2;
		while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
			inSampleSize++;
		}

		return inSampleSize;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			try {
				NavUtils.navigateUpFromSameTask(this);
			} catch (Exception e) {
				finish();
			}

			break;

		}

		return super.onOptionsItemSelected(item);
	}

}
