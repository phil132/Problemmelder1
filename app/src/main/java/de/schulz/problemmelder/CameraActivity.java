package de.schulz.problemmelder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * In der CameraActivity hat der User die M�glichkeit ein Foto aufzunehmen oder
 * aus der Galerie auszuw�hlen. Das Foto wird in einem Vorschaufenster
 * angezeigt. Der dazugeh�rige Pfad auf dem Ger�t wird darunter angezeigt.
 * 
 * @author Philipp Schulz
 * @version 10.07.2013
 * 
 */
public class CameraActivity extends Activity implements OnClickListener {

	private static final int SELECT_PICTURE = 0;
	private static final int CAMERA_REQUEST = 1000;
	// String f�r Logging
	private static final String TAG = "CameraActivity";

	private Button btnMain, btnLocation;
	private ImageButton btnCamera, btnGallery;
	private ImageView ivPic;
	private TextView tvPath;
	private String current_path;
	public Uri mFileUri = null;
	private String selectedImagePath;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);

		initializeGUI();
	}

	/**
	 * Initialisieren der grafischen Oberfl�che
	 */
	private void initializeGUI() {
		btnMain = (Button) findViewById(R.id.btnBackMain);
		btnLocation = (Button) findViewById(R.id.btnPos);
		btnCamera = (ImageButton) findViewById(R.id.ibtnCamera);
		btnGallery = (ImageButton) findViewById(R.id.ibtnGallery);
		ivPic = (ImageView) findViewById(R.id.ivPicture);
		tvPath = (TextView) findViewById(R.id.tvPhotoPath);

		btnMain.setOnClickListener(this);
		btnLocation.setOnClickListener(this);
		btnCamera.setOnClickListener(this);
		btnGallery.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// Auswahl gedr�ckter Button
		switch (v.getId()) {
		case R.id.btnBackMain:
			super.onBackPressed();
			break;
		case R.id.btnPos:
			if (current_path != null) {
				
				// Weitergabe des Bildpfades
				Bundle bundle = new Bundle();
				bundle.putString("image_path", current_path);
				
				// Starten der Karten Activity
				Intent i = new Intent(CameraActivity.this, MapOsm.class);
				
				//Senden des Bundles mit dem Pfad des Bildes
				i.putExtras(bundle);
				startActivity(i);

			} else {
				
				// Wenn kein Foto gew�hlt wurde 
				Toast toast = Toast.makeText(CameraActivity.this,
						"Bitte w�hlen Sie ein Foto", Toast.LENGTH_SHORT);
				toast.show();
			}
			break;
		case R.id.ibtnGallery:
			//open_gallery();

			break;
		case R.id.ibtnCamera:
			make_pic();

		}
	}

	private void make_pic() {

		//Kamera starten
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Verzeichnis erstellen falls nicht vorhanden
		File mediaStorageDir = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Problemmelder");
		try {
			mediaStorageDir.mkdir();
		} catch (Exception e) {
			Toast.makeText(this, "Fehler: Das Verzeichnis konnte nicht erstellt werden", Toast.LENGTH_LONG).show();
		}

		Log.d(TAG, mediaStorageDir.toString());
		
		// Datum f�r eindeutigen Bildnamen wird generiert
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		
		// Neue Datei erstellen
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "IMG_" + timeStamp + ".jpg");
		
		// URI ders Bildes 
		mFileUri = Uri.fromFile(mediaFile);
		
		// Hinzuf�gen der URI zum Intent
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
		
		// starten der Kamera Activity
		startActivityForResult(intent, CAMERA_REQUEST);

		// Bildpfad bestimmen und in TextView schreiben
		current_path = mediaFile.toString();
		tvPath.setText(current_path);
	}
	/**
	 * Methode zum �ffnen und Ausw�hlen von Bildern aus der Gallerie 
	 */
	private void open_gallery() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_PICTURE);
	}
	/**
	 * Methode nach R�ckkehr vom Intent
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Bei erfolgreicherr Aufnahme eines Bildes wird dieses gespeichert
		if (requestCode == CAMERA_REQUEST) {
			if (resultCode == RESULT_OK) {

				try {
					
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(
							getContentResolver(), mFileUri);
					//Meldung bei erfolgreichen Abspeichern + Gr��e des Bildes
					Toast.makeText(this,"Bild gespeichert (" + bitmap.getByteCount()+ " Bytes)", Toast.LENGTH_LONG).show();
					ivPic.setImageBitmap(bitmap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (resultCode == RESULT_CANCELED) {
				Log.d(TAG, "Kamera beendet");
			} else {
				Log.d(TAG, "Kamerafehler");
			}
		} else if (requestCode == SELECT_PICTURE) {
			// Auswahl des Bildpfades bei Bildern vm Ger�t
			if (resultCode == RESULT_OK) {

				Uri selectedImageUri = data.getData();
				selectedImagePath = getPath(selectedImageUri);
				System.out.println("Image Path : " + selectedImagePath);
				ivPic.setImageURI(selectedImageUri);
				current_path = selectedImagePath;
				tvPath.setText(current_path);
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	// ---- Men�
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Verarbeitet die Click Events im des Men�s
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			Intent iAbout = new Intent(CameraActivity.this, About.class);
			startActivity(iAbout);
			return true;

		case R.id.action_settings:
			Intent iSettings = new Intent(CameraActivity.this,
					UserSettingsActivity.class);
			startActivity(iSettings);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
		// ----
	}

}
