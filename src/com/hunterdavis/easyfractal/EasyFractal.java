package com.hunterdavis.easyfractal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyFractal extends Activity {

	// global fractal bitmap
	Bitmap staticBitmap = null;

	// The width and height of the bitmap.
	private static final int WIDTH = 300;
	private static final int HEIGHT = 400;

	int color1 = Color.WHITE;
	int color2 = Color.BLACK;

	// The ComplexNumber to base the Julia Set off of.
	private static double c1 = -0.223;
	private static double c2 = 0.745;

	// Array of booleans stating which pixels make up the fractal.
	int rgbSize = 300 * 400;
	int[] rgbValues = new int[rgbSize];

	// The bounds of the Complex Plane to graph.
	private double ominX = -1.5;
	private double omaxX = -.75;
	private double ominY = 1.0;
	private double omaxY = 1.35;
	private double minX = ominX;
	private double maxX = omaxX;
	private double minY = ominY;
	private double maxY = omaxY;

	// The maximum Magnitude of the ComplexNumer to be allowed in the Set.
	private double threshold = 1;
	// The number of times that the algorithm recurses.
	private int iterations = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		OnClickListener saveButtonListner = new OnClickListener() {
			public void onClick(View v) { 
				// do something when the button is clicked
				saveImage(v.getContext());
			} 
		};

		Button saveButton = (Button) findViewById(R.id.savebutton);
		saveButton.setOnClickListener(saveButtonListner);

		OnClickListener regenButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				final Random myRandom = new Random();
				c1 = -0.223 * (1 + (.3 * myRandom.nextFloat()));
				c2 = 0.745 * (1 + (.3 * myRandom.nextFloat()));

				iterations = 1;
				color1 = myRandom.nextInt();
				color2 = myRandom.nextInt();

				minX = ominX;
				maxX = omaxX;
				minY = ominY;
				maxY = omaxY;
				
				
				regenFractal();
			}
		};

		Button regenButton = (Button) findViewById(R.id.newbutton);
		regenButton.setOnClickListener(regenButtonListner);

		OnClickListener colorsListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				newColorsSameFractal();
			}
		};

		Button colorsButton = (Button) findViewById(R.id.colorsbutton);
		colorsButton.setOnClickListener(colorsListner);

		// grab a view to the image and load blank png
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);

		// photo on click listener
		imgView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				// zoom in a little
				// The bounds of the Complex Plane to graph.
				minX = minX * .99;
				maxX = maxX * .99;
				minY = minY * .99;
				maxY = maxY * .99;
				
				
				iterations = iterations + 2;
				regenFractal();
			}

		});

		final Random myRandom = new Random();
		color1 = myRandom.nextInt();
		color2 = myRandom.nextInt();
		
		c1 = -0.223 * (1 + (.1 * myRandom.nextFloat()));
		c2 = 0.745 * (1 + (.1 * myRandom.nextFloat()));


		regenFractal();

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

	}// end of oncreate

	public void regenFractal() {
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		Julia(imgView);

	}

	public void newColorsSameFractal() {
		final Random myRandom = new Random();
		int color3 = myRandom.nextInt();
		int color4 = myRandom.nextInt();
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);

		for (int k = 0; k < rgbSize; k++) {
			int xLocation = (int) k % WIDTH;
			int yLocation = (int) Math.floor(k / WIDTH);

			if (rgbValues[k] == color1) {
				rgbValues[k] = color3;
			} else {
				rgbValues[k] = color4;
			}
		}
		
		color1 = color3;
		color2 = color4;

		staticBitmap = Bitmap.createBitmap(rgbValues, 300, 400,
				Bitmap.Config.RGB_565);
		imgView.setImageBitmap(staticBitmap);

	}

	public Boolean saveImage(Context context) {
		String extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		// actually save the file

		OutputStream outStream = null;
		String newFileName = null;
		Calendar c = Calendar.getInstance();

		String sDate = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-"
				+ (c.get(Calendar.DAY_OF_MONTH) + 1) + "_"
				+ c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE);
		newFileName = "fractal-" + sDate + "-.png";

		if (newFileName != null) {
			{
				File file = new File(extStorageDirectory, newFileName);
				try {
					outStream = new FileOutputStream(file);
					staticBitmap.compress(Bitmap.CompressFormat.PNG, 100,
							outStream);
					try {
						outStream.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
					try {
						outStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}

					Toast.makeText(context, "Saved " + newFileName,
							Toast.LENGTH_LONG).show();
					new SingleMediaScanner(context, file);

				} catch (FileNotFoundException e) {
					// do something if errors out?
					return false;
				}
			}

			return true;

		}
		return false;
	} // end of save function

	/**
	 * The Julia Fractal family is my personal favorite. This class is to show
	 * and explain the math behind it. It can be very complicated, so I will try
	 * to explain it as best as I can. This is NOT meant to be a comrehensive
	 * guide to Fractals, or the Julia Set. This is only to help you understand
	 * how it works. This is a VERY simple example, and is for educational
	 * purposes only. Please do not claim this as your own, or submit it as your
	 * own work.
	 * 
	 * Please excuse any spelling errors you may find. I'm a programmer, not a
	 * speller.
	 * 
	 * Complex Numbers are the key to fractals. A complex number is in the form:
	 * a+bi, where a & b are real numbers, and i = sqrt(-1).
	 * 
	 * Generating fractals involves doing math in the Complex Plane. You can
	 * think of the Complex Plane in the following way: i-values | |* (1+6i) | |
	 * * (2+4i) | | *(x+yi) |_ _ _ _ _ _ real values.
	 * 
	 * The i-values (imaginary values) go up & down the "y" axis, and the real
	 * numbers go out along the "x" axis. (Note that I only drew the one
	 * quadrant, all four are used).
	 * 
	 * The Julia Set is created by putting each point through an algorithm, and
	 * adding it to the set by determining how close it is to infinity. If the
	 * threshold^2 is less than the Magnitude of the Complex Number (where
	 * magnitude = a^2 + b^2), the point is added to the set. The iteration
	 * value determins how many times the point is put through the algorithm.
	 * Putting it through more times, and making the threshold much smaller,
	 * leads to cleaner images, but they take longer to create.
	 * 
	 * The values in this example give a good depiction of a Julia Set.
	 * 
	 * @author Neil
	 */
	public void Julia(ImageView imgView) {
		// Create a BufferedImage to paint on.
		int rgbSize = 300 * 400;
		double xDiff = maxX - minX;
		double yDiff = maxY - minY;
		double wM = WIDTH + minX;
		double hM = HEIGHT + minY;
		double xRat = xDiff / wM;
		double yRat = yDiff / hM;
		double threshholdsquared = threshold * threshold;
		double a = 0;
		double b = 0;
		double e = 0;
		double f = 0;

		// Go through the array and set the pixel color on the BufferedImage
		// according to the values in the array.
		for (int k = 0; k < rgbSize; k++) {
			int xLocation = (int) k % WIDTH;
			int yLocation = (int) Math.floor(k / WIDTH);
			a = (xLocation * xRat);
			b = (yLocation * yRat);

			for (int i = 0; i < iterations; i++) {
				// The basic Julia Set Algorithm.
				// Other Algorithms can be found online.
				// cn = cn.square().add(c);

				// first square complex number featuring a and b
				e = a * a - b * b;
				f = 2 * a * b;

				// now add it to the big guy upstairs
				a = e + c1;
				b = f + c2;

			}
			// If the threshold^2 is larger than the magnitude, return true.
			double magnitude = a * a + b * b;

			if (magnitude < threshholdsquared) {
				rgbValues[k] = color1;
			} else
				rgbValues[k] = color2;
		}

		staticBitmap = Bitmap.createBitmap(rgbValues, 300, 400,
				Bitmap.Config.RGB_565);

		// actually draw the image
		imgView.setImageBitmap(staticBitmap);
		return;

	}

}// end of file

