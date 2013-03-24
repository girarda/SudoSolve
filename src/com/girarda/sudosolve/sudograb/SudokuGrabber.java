package com.girarda.sudosolve.sudograb;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;

public class SudokuGrabber {

	private Bitmap originalImg;
	private Mat imgMatrix = new Mat();
	private Mat intermediateMat;
	private Mat undistorted;

	public SudokuGrabber(Bitmap bitmapImg) {
		originalImg = bitmapImg;
		intermediateMat = new Mat(originalImg.getHeight(), originalImg.getWidth(), CvType.CV_32F);
		bitmapToMatrix(originalImg, imgMatrix);
	}

	private void bitmapToMatrix(Bitmap bitmapImg, Mat matrix) {
		Utils.bitmapToMat(bitmapImg, matrix);
		Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2GRAY);
	}

	public Bitmap getSolvedSudoku() {
		applyThreshold(imgMatrix);
		detectSudokuGrid(intermediateMat);
		//	detectLines(intermediateMat);
		//	findExtremeLines(intermediateMat);
		return getConvertedResult();
	}

	private void detectLines(Mat matrix) {
		/*		Mat lines = new Mat();
		Imgproc.HoughLinesP(matrix, lines, 1, Math.PI/180, 200);
		detectExtremeLines(lines, matrix);


		for (int x = 0; x < lines.cols(); x++) 
	    {
	          double[] vec = lines.get(0, x);
	          double x1 = vec[0], 
	                 y1 = vec[1],
	                 x2 = vec[2],
	                 y2 = vec[3];
	          Point start = new Point(x1, y1);
	          Point end = new Point(x2, y2);

	          Core.line(matrix, start, end, new Scalar(255,0,0), 3);
	    }
		detectExtremeLines(lines, matrix);
		 */
	}

	private void detectExtremeLines(Mat lines, Mat image) {
		double[] topEdge = new double[]{1000,1000};    double topYIntercept=100000, topXIntercept=0;
		double[] bottomEdge = new double[]{-1000,-1000};        double bottomYIntercept=0, bottomXIntercept=0;
		double[] leftEdge = new double[]{1000,1000};    double leftXIntercept=100000, leftYIntercept=0;
		double[] rightEdge = new double[]{-1000,-1000};        double rightXIntercept=0, rightYIntercept=0;
		for(int i=0;i < lines.size().height;i++)
		{
			for (int j = 0; j < lines.size().width; j++) {
				double[] current = lines.get(i, j);

				double p=current[0];
				double theta=current[1];

				if(p==0 && theta==-100)
					continue;
				double xIntercept, yIntercept;
				xIntercept = p/Math.cos(theta);
				yIntercept = p/(Math.cos(theta)*Math.sin(theta));
				if(theta > Math.PI*80/180 && theta < Math.PI*100/180)
				{
					if(p <topEdge[0])
						topEdge = current;

					if(p > bottomEdge[0])
						bottomEdge = current;
				}
				else if(theta < Math.PI*10/180 || theta > Math.PI*170/180)
				{
					if(xIntercept > rightXIntercept)
					{
						rightEdge = current;
						rightXIntercept = xIntercept;
					}
					else if(xIntercept <= leftXIntercept)
					{
						leftEdge = current;
						leftXIntercept = xIntercept;
					}
				}
			}
		}
		Point left1 = new Point(), left2 = new Point(), right1 = new Point(), right2 = new Point(), bottom1 = new Point(), bottom2 = new Point(), top1 = new Point(), top2 = new Point();

		double height=image.size().height;
		double width=image.size().width;

		if(leftEdge[1]!=0)
		{
			left1.x=0;        left1.y=leftEdge[0]/Math.sin(leftEdge[1]);
			left2.x=width;    left2.y=-left2.x/Math.tan(leftEdge[1]) + left1.y;
		}
		else
		{
			left1.y=0;        left1.x=leftEdge[0]/Math.cos(leftEdge[1]);
			left2.y=height;    left2.x=left1.x - height*Math.tan(leftEdge[1]);
		}

		if(rightEdge[1]!=0)
		{
			right1.x=0;        right1.y=rightEdge[0]/Math.sin(rightEdge[1]);
			right2.x=width;    right2.y=-right2.x/Math.tan(rightEdge[1]) + right1.y;
		}
		else
		{
			right1.y=0;        right1.x=rightEdge[0]/Math.cos(rightEdge[1]);
			right2.y=height;    right2.x=right1.x - height*Math.tan(rightEdge[1]);
		}

		bottom1.x=0;    bottom1.y=bottomEdge[0]/Math.sin(bottomEdge[1]);
		bottom2.x=width;bottom2.y=-bottom2.x/Math.tan(bottomEdge[1]) + bottom1.y;

		top1.x=0;        top1.y=topEdge[0]/Math.sin(topEdge[1]);
		top2.x=width;    top2.y=-top2.x/Math.tan(topEdge[1]) + top1.y;

		// Next, we find the intersection of  these four lines
		double leftA = left2.y-left1.y;
		double leftB = left1.x-left2.x;
		double leftC = leftA*left1.x + leftB*left1.y;

		double rightA = right2.y-right1.y;
		double rightB = right1.x-right2.x;
		double rightC = rightA*right1.x + rightB*right1.y;

		double topA = top2.y-top1.y;
		double topB = top1.x-top2.x;
		double topC = topA*top1.x + topB*top1.y;

		double bottomA = bottom2.y-bottom1.y;
		double bottomB = bottom1.x-bottom2.x;
		double bottomC = bottomA*bottom1.x + bottomB*bottom1.y;  

		// Intersection of left and top
		double detTopLeft = leftA*topB - leftB*topA;
		Point ptTopLeft = new Point((topB*leftC - leftB*topC)/detTopLeft, (leftA*topC - topA*leftC)/detTopLeft);

		// Intersection of top and right
		double detTopRight = rightA*topB - rightB*topA;
		Point ptTopRight = new Point((topB*rightC-rightB*topC)/detTopRight, (rightA*topC-topA*rightC)/detTopRight);

		// Intersection of right and bottom
		double detBottomRight = rightA*bottomB - rightB*bottomA;
		Point ptBottomRight = new Point((bottomB*rightC-rightB*bottomC)/detBottomRight, (rightA*bottomC-bottomA*rightC)/detBottomRight);// Intersection of bottom and left
		double detBottomLeft = leftA*bottomB-leftB*bottomA;
		Point ptBottomLeft = new Point((bottomB*leftC-leftB*bottomC)/detBottomLeft, (leftA*bottomC-bottomA*leftC)/detBottomLeft);



		Core.circle(image, ptTopLeft, 5, new Scalar(255, 0, 0));
		Core.circle(image, ptTopRight, 5, new Scalar(255, 0, 0));
		Core.circle(image, ptBottomLeft, 5, new Scalar(255, 0, 0));
		Core.circle(image, ptBottomRight, 5, new Scalar(255, 0, 0));
		Core.line(image, bottom1, bottom2, new Scalar(255,0,0));
		Core.line(image, top1, top2, new Scalar(255,0,0));


	}

	private void applyThreshold(Mat matrix) {
		// TODO Change size to (9,9)? it is slower but it finds the contour of the second picture.
		Imgproc.GaussianBlur(matrix, matrix, new Size(11, 11), 0);

		Imgproc.adaptiveThreshold(matrix, intermediateMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
		Core.bitwise_not(intermediateMat, intermediateMat);

		dilate(intermediateMat);

		Imgproc.cvtColor(intermediateMat, matrix, Imgproc.COLOR_GRAY2BGRA, 4);

	}

	private void dilate(Mat matrix) {
		// TODO add erode method to cancel dilate?
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
		Imgproc.dilate(matrix, matrix, kernel);
	}


	private void detectSudokuGrid(Mat matrix) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(matrix, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		double maxArea = -1;
		int maxAreaIdx = -1;

		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint contour = contours.get(i);
			double contourarea = Imgproc.contourArea(contour);
			if (contourarea > maxArea) {
				maxArea = contourarea;
				maxAreaIdx = i;
			}
		}
		for (int i = 0; i < contours.size(); i++) {
			if (i == maxAreaIdx) {
				Imgproc.drawContours(matrix, contours, maxAreaIdx, new Scalar(255, 255 ,255));
			}
			else {
				Imgproc.drawContours(matrix, contours, i, new Scalar(0, 0 ,0));
			}
		}
		//Imgproc.drawContours(matrix, contours, maxAreaIdx, new Scalar(0, 0 ,0));

		// Find corners
		Point[] points = contours.get(maxAreaIdx).toArray();

		//	Double[] topEdge = new Double[]{(double) 1000,(double) 1000};    double topYIntercept=100000, topXIntercept=0;
		//	Double[] bottomEdge = new Double[]{(double) -1000,(double) -1000};        double bottomYIntercept=0, bottomXIntercept=0;
		//	Double[] leftEdge = new Double[]{(double) 1000,(double) 1000};    double leftXIntercept=100000, leftYIntercept=0;
		//	Double[] rightEdge = new Double[]{(double) -1000,(double) -1000};        double rightXIntercept=0, rightYIntercept=0;
		//	
		//	for (int i = 0; i < points.length; i++) {
		//		for (int j = 0; j < points.length; j++) {
		//			Double[] current = new Double[]{points[j].x, points[i].y};
		//			
		//		}
		//	}
		//detectCorners(matrix, points);
		MatOfPoint2f approxCurve =  new MatOfPoint2f();
		MatOfPoint2f source = new MatOfPoint2f(points);
		Imgproc.approxPolyDP(source, approxCurve, 100.0, true);
		
		Point[] points2 = approxCurve.toArray();
		for (Point p: points2) {
			Core.circle(matrix, p, 100, new Scalar(255,0,0));
		}
	}


	private void detectCorners(Mat matrix, Point[] points) {

		Point topLeftCorner = new Point(10000,10000);			Point topRightCorner = new Point(-10000, 10000);
		Point bottomLeftCorner = new Point(10000, -10000);		Point bottomRightCorner = new Point(-10000, -10000);

		for (Point p: points) {
			//Core.circle(matrix, p, 5, new Scalar(255,0,0));

		}

//		for(Point p: points) {
//			if (p.x <= topLeftCorner.x && p.y <= topLeftCorner.y) {
//				topLeftCorner = p;
//			}
//			if (p.x <= bottomLeftCorner.x && p.y >= bottomLeftCorner.y) {
//				bottomLeftCorner = p;
//			}
//			if (p.x >= topRightCorner.x && p.y <= topRightCorner.y) {
//				topRightCorner = p;
//			}
//			if (p.x >= bottomRightCorner.x && p.y >= bottomRightCorner.y) {
//				bottomRightCorner = p;
//			}
//		}
		
		for(Point p: points) {
		if (p.x <= topLeftCorner.x && p.y <= topLeftCorner.y) {
			topLeftCorner = p;
		}
		if (p.x <= bottomLeftCorner.x && p.y >= bottomLeftCorner.y) {
			bottomLeftCorner = p;
		}
		if (p.y < topRightCorner.y) {
			topRightCorner = p;
		}
		if (p.x > bottomRightCorner.x) {
			bottomRightCorner = p;
		}
	}

		Core.circle(matrix, topLeftCorner, 20, new Scalar(255,0,0));
		Core.circle(matrix, topRightCorner, 20, new Scalar(255,0,0));
		Core.circle(matrix, bottomLeftCorner, 20, new Scalar(255,0,0));
		Core.circle(matrix, bottomRightCorner, 20, new Scalar(255,0,0));

		Core.line(matrix, topLeftCorner, topRightCorner, new Scalar(255,0,0));
		Core.line(matrix, topLeftCorner, bottomLeftCorner, new Scalar(255,0,0));


//		int maxLength = (int) ((bottomLeftCorner.x-bottomRightCorner.x)*(bottomLeftCorner.x-bottomRightCorner.x) + (bottomLeftCorner.y-bottomRightCorner.y)*(bottomLeftCorner.y-bottomRightCorner.y));
//
//		int temp = (int) ((topRightCorner.x-bottomRightCorner.x)*(topRightCorner.x-bottomRightCorner.x) + (topRightCorner.y-bottomRightCorner.y)*(topRightCorner.y-bottomRightCorner.y));
//		if(temp>maxLength) maxLength = temp;
//
//		temp = (int) ((topRightCorner.x-topLeftCorner.x)*(topRightCorner.x-topLeftCorner.x) + (topRightCorner.y-topLeftCorner.y)*(topRightCorner.y-topLeftCorner.y));
//		if(temp>maxLength) maxLength = temp;
//
//		temp = (int) ((double) ((bottomLeftCorner.x-topLeftCorner.x)*(bottomLeftCorner.x-topLeftCorner.x)) + (bottomLeftCorner.y-topLeftCorner.y)*(bottomLeftCorner.y-topLeftCorner.y));
//		if(temp>maxLength) maxLength = temp;
//
//		maxLength = (int) Math.sqrt((double)maxLength);

//		Point[] src = new Point[4], dst = new Point[4];
//
//		src[0] = topLeftCorner;            dst[0] = new Point(0,0);
//		src[1] = topRightCorner;        	dst[1] = new Point (maxLength-1, 0);
//		src[2] = bottomRightCorner;        dst[2] = new Point(maxLength-1, maxLength-1);
//		src[3] = bottomLeftCorner;        dst[3] = new Point(0, maxLength-1);
//
//		Mat srcMat = new Mat(4,1,CvType.CV_32F);
//		srcMat.put(0, 0, src[0].x, src[0].y,src[3].x, src[3].y,src[1].x, src[1].y,src[2].x, src[2].y);
		//		srcMat.put(0,0, new double[]{src[0].x, src[0].y});	
		//		srcMat.put(1,0, new double[]{src[3].x, src[3].y});
		//		srcMat.put(2,0, new double[]{src[1].x, src[1].y});
		//		srcMat.put(3,0, new double[]{src[2].x, src[2].y});


//		Mat dstMat = new Mat(4,1,CvType.CV_32F);
//		dstMat.put(0, 0, dst[0].x,src[0].y,dst[3].x,src[3].y,dst[1].x, src[1].y,dst[2].x, src[2].y);
		//dstMat.put(0,0, new double[]{dst[0].x, src[0].y});
		//dstMat.put(1,0, new double[]{dst[3].x, src[3].y});
		//dstMat.put(2,0, new double[]{dst[1].x, src[1].y});
		//dstMat.put(3,0, new double[]{dst[2].x, src[2].y});


		//undistorted = new Mat(maxLength, maxLength, CvType.CV_32F);
		//Imgproc.warpPerspective(matrix, undistorted, Imgproc.getPerspectiveTransform(srcMat, dstMat), new Size(maxLength, maxLength));

	}		

	private void matrixToBitmap(Mat matrix, Bitmap bitmap) {
		Mat result = new Mat();
		Imgproc.cvtColor(matrix, result, Imgproc.COLOR_GRAY2BGRA);
		Utils.matToBitmap(result, bitmap);
	}

	public Bitmap getConvertedResult() {
		matrixToBitmap(intermediateMat, originalImg);
		return originalImg;
	}

}
