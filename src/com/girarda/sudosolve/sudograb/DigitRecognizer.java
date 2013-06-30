package com.girarda.sudosolve.sudograb;

import java.io.File;
import java.io.FileOutputStream;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.CvKNearest;
import org.opencv.ml.CvSVM;

import com.girarda.sudosolve.imageprocessing.ImageProcessing;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.graphics.Bitmap;
import android.os.Environment;


public class DigitRecognizer {
	private TessBaseAPI baseAPI = new TessBaseAPI();

	public DigitRecognizer() {
		baseAPI.init(Environment.getExternalStorageDirectory().toString() + "/tesseract/", "eng");
		baseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "123456789");
		baseAPI.setVariable(TessBaseAPI.VAR_ACCURACYVSPEED, new Integer(TessBaseAPI.AVS_MOST_ACCURATE).toString());
	}

	public int[][] getSudokuNumbers(Mat[][] cells) {
		int[][] sudokuNumbers = new int[9][9];
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				sudokuNumbers[row][col] = getCellNumber(cells[row][col]);
			}
		}
		return sudokuNumbers;
	}

	public int getCellNumber(Mat cellMat) {
		Bitmap cellImg = Bitmap.createBitmap(cellMat.width(),cellMat.height(), Bitmap.Config.ARGB_8888);
		ImageProcessing.matrixToBitmap(cellMat, cellImg);
		cellImg = cellImg.copy(Bitmap.Config.ARGB_8888, true);
		baseAPI.setImage(cellImg);
		String recog = baseAPI.getUTF8Text();
		baseAPI.clear();
		int cellValue;
		try {
			cellValue = Integer.parseInt(recog);
		} 
		catch (RuntimeException e){
			cellValue = 0;
		}
		return cellValue;
	}

}