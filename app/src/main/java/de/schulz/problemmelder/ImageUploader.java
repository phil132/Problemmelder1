package de.schulz.problemmelder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Klasse zum Skalieren und Hochladen des Bildes
 * 
 * @author Philipp Schulz
 * @version 08.06.2013
 */
public class ImageUploader extends AsyncTask<String, Void, String> {

	HttpResponse response;

	protected String doInBackground(String... path) {

		String outPut = null;

		for (String sdPath : path) {

			Bitmap bitmap = BitmapFactory.decodeFile(sdPath);
			ByteArrayOutputStream bao = new ByteArrayOutputStream();

			// Die neue H�he wird mit Verh�ltnis von Breite zu neuer Breite bestimmt.
			double width = bitmap.getWidth();
			double height = bitmap.getHeight();
			int newwidth = 1000;  
			double ratio = newwidth / width;
			int newheight = (int) (ratio * height);

			//System.out.println("Breite: " + width);
			//System.out.println("H�he: " + height);

			// Skalieren des Bildes
			bitmap = Bitmap.createScaledBitmap(bitmap, newwidth, newheight,
					true);
			
			//System.out.println("neue H�he: " + newheight);
			
			// schreibt eine komprimierte Version des Bildes in den Outputstream
			bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bao);
			
			
			byte[] ba = bao.toByteArray();
			
			// Kodieren in Base64 Format
			String ba1 = Base64.encodeBytes(ba);
			
			//System.out.println("l�dt Bild hoch " + ba1);
			
			//String als Name/Wert Paar
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("image", ba1));

			/*
			 * Serververbindung zum PHP-Script
			 */
			try {
				// Client �bernimmt Aufgaben des Senden und Empfangens
				HttpClient httpclient = new DefaultHttpClient();

				// Http-Request-Methode POST
				HttpPost httppost = new HttpPost(
						"http://gistribution.de/smart-wedding/php/picture_upload.php");
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				/*
				 * Request wird ausgef�rt und an die URL geschickt Antwort ist
				 * ein Response-Objekt
				 */

				response = httpclient.execute(httppost);

				// Auslesen des Response-Objekts
				HttpEntity entity = response.getEntity();

				// Antwort in Log schreiben
				outPut = EntityUtils.toString(entity);
				Log.i("GET RESPONSE: ", outPut);

				Log.d("TAG", "Gute Verbindung");

				bitmap.recycle();

			} catch (Exception e) {
				Log.d("TAG", "Fehler bei der Http-Verbindung" + e.toString());
			}
		}
		return outPut;
	}
}

