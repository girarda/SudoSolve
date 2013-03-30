package com.girarda.sudosolve.app;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.girarda.sudosolve.R;
import com.girarda.sudosolve.sudograb.SudokuGrabber;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	//YOU CAN EDIT THIS TO WHATEVER YOU WANT
	private static final int SELECT_PICTURE = 1;

	private String selectedImagePath;
	//ADDED
	private String filemanagerstring;

	private Bitmap myBitmap;

	private SudokuGrabber sudoGrabber;

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				//                    Log.i(TAG, "OpenCV loaded successfully");

			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Init opencv
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
		{
			Log.e("TEST", "Cannot connect to OpenCV Manager");
		}

		((Button) findViewById(R.id.Button01))
		.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				// in onCreate or any event where your want the user to
				// select a file
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent,
						"Select Picture"), SELECT_PICTURE);
			}
		});
	}

	//UPDATED
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();

				//OI FILE Manager
				filemanagerstring = selectedImageUri.getPath();

				//MEDIA GALLERY
				selectedImagePath = getPath(selectedImageUri);

				openImage(selectedImagePath);
			}
		}
	}

	private void openImage(String pathToImage) {
		try {
			myBitmap = BitmapFactory.decodeFile(pathToImage);
			sudoGrabber = new SudokuGrabber(myBitmap);
			myBitmap = sudoGrabber.getSolvedSudoku();
			displayImage();
		} catch (OutOfMemoryError e) {
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
				myBitmap = BitmapFactory.decodeFile(pathToImage, options);
				
				sudoGrabber = new SudokuGrabber(myBitmap);
				
				myBitmap = sudoGrabber.getSolvedSudoku();
				displayImage();
			} catch(Exception err) {
				System.out.println(err.getStackTrace());
			}
		}
	}

	private void displayImage() {
		ImageView myImage = (ImageView) findViewById(R.id.imageViewTest);
		myImage.setImageBitmap(myBitmap);
	}

	//UPDATED!
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if(cursor!=null)
		{
			//HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			//THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		else return null;
	}
}