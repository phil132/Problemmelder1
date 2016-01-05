package de.schulz.problemmelder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Activity zum Beschreiben und Senden eines Problems.
 * 
 * Der Benutzer kann das Formular zur Beschreibung des Problems ausf�llen. Die
 * Adresse wird falls vorhanden von der MapOsm.java �bergeben und in die
 * Eingabefelder eingetragen. Beim best�tigen des Sende-Buttons wird
 * 
 * @author Philipp Schulz
 * @version 08.07.2013
 */
public class DescriptionActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {

	StringUploader stringUploader = new StringUploader();

	// String f�r das Logging
	private static final String TAG = "DescriptionActivity";

	// url zum Php-Scipt, welches einen Eintrag in die Datenbank generiert
	//private static String url_create_problem = "http://problemmelder.bplaced.net/php/create_poi.php";
	private static String url_create_problem = "http://gistribution.de/smart-wedding/php/create_poi.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";

	private Button btnBackMap, btnSend;
	private EditText etAddressline1, etAddressline2, etComment;
	public String addressline1 = " ", addressline2 = " ", imagePath = " ",
			postal_code = " ", city = " ", street = " ", streetnumber = " ",
			image_name=" ", problem_type = " ", comment = " ", user_name = " ",
			user_mail = " ";
	public Spinner problemSpinner;
	public float lon, lat;

	final Context context = this;
	
	
	
	JSONObject json;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.description);

		initializeGui();
		getBundle();
		Toast toast = Toast.makeText(DescriptionActivity.this,
				"Bitte beschreiben Sie welches Problem aufgetreten ist.", Toast.LENGTH_SHORT);
		toast.show();
	}

	private void initializeGui() {
		// initialisieren der grafischen Oberfl�che
		btnBackMap = (Button) findViewById(R.id.btnBackMap);
		btnSend = (Button) findViewById(R.id.btnSend);

		btnBackMap.setOnClickListener(this);
		btnSend.setOnClickListener(this);

		// -- Dropdown Auswahlliste initialisieren
		problemSpinner = (Spinner) findViewById(R.id.sp_type_of_damage);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.type_of_problem_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		problemSpinner.setAdapter(adapter);

		problemSpinner.setOnItemSelectedListener(this);
		// --

		etAddressline1 = (EditText) findViewById(R.id.etPostcodeCity);
		etAddressline2 = (EditText) findViewById(R.id.etStreetStreetnumber);
		etComment = (EditText) findViewById(R.id.etComment);

	}

	/**
	 * Ausgew�hlter Eintrag des Dropdown-Menus wird in String umgewandelt
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		problem_type = parent.getItemAtPosition(pos).toString();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Another interface callback
	}

	/**
	 * Holt sich die gesendeten Variablenwerte aus der vorherigen Activity
	 */
	private void getBundle() {
		Bundle gotValues = getIntent().getExtras();
		imagePath = gotValues.getString("image_path");
		postal_code = gotValues.getString("postal_code");
		city = gotValues.getString("city");
		street = gotValues.getString("street");
		streetnumber = gotValues.getString("streetnumber");
		lat = (float) (gotValues.getFloat("lat") / 1E6);
		lon = (float) (gotValues.getFloat("lon") / 1E6);
		if (city != null) {
			etAddressline1.setText(city);
			if (postal_code != null) {
				etAddressline1.append(" , " + postal_code);
			}
		}
		if (street != null) {
			etAddressline2.setText(street);
			if (streetnumber != null) {
				etAddressline2.append("  " + streetnumber);
			}
		}

	}

	/**
	 * Senden und
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnBackMap:
			super.onBackPressed();
			break;

		case R.id.btnSend:

			getStrings();
			progressDialogWhileUploading();
		}
	}

	/**
	 * Startet einen ProgressDialog f�r die Dauer des Uploadvorganges
	 */
	private void progressDialogWhileUploading() {

		final ProgressDialog myPd_ring = ProgressDialog.show(this,
				"Bitte warten", "l�dt..", true);
		myPd_ring.setCancelable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					
					upload_pic();
					new CreateNewProblem().execute();

				} catch (Exception e) {
					Log.d(TAG, "Fehler beim Hochladen");
				}
				myPd_ring.dismiss();
			}
		}).start();
	}

	/**
	 * holt sich die Strings der Eingabefelder und die der Einstellungen
	 */
	private void getStrings() {

		// Holt der Stirng der ersten Adresszeile, trennt ihn in Stadt und Plz
		addressline1 = etAddressline1.getText().toString();
		try {
			StringTokenizer tokens = new StringTokenizer(addressline1, ",");
			city = tokens.nextToken();
			postal_code = tokens.nextToken();
		} catch (Exception e) {
			e.printStackTrace();
			new CreateNewProblem().showAlertDialogFailure();
		}

		// Holt der Stirng der zweiten Adresszeile
		street = etAddressline2.getText().toString();

		comment = etComment.getText().toString();
		if (comment.equals("")) {
			comment = "kein Kommentar";
		}
		//
		load_sharedPrefs();
	}

	/**
	 * Holt sich die in den Settings gepeicherten Werte
	 */
	private void load_sharedPrefs() {
		// TODO Auto-generated method stub
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		user_name = sp.getString("USERNAME", "default User");
		user_mail = sp.getString("USERMAIL", "kein Angabe");
	}

	/**
	 * Instanz der Klasse HttpUploader mit der das gew�hlte Bild hochgeladen
	 * wird
	 */
	private void upload_pic() {
		ImageUploader uploader = new ImageUploader();
		try {
			image_name = uploader.execute(imagePath).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			new CreateNewProblem().showAlertDialogFailure();

		} catch (ExecutionException e) {
			e.printStackTrace();
			new CreateNewProblem().showAlertDialogFailure();

		}
	}

	/**
	 * Hintergrund AsyncTask der mittels Http POST die Parameter and den Server
	 * sendet.
	 **/
	class CreateNewProblem extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		/**
		 * Wird nach dem Hintergrundtask ausgef�hrt. Bei Erfolg oder Misserfolg
		 * der �bertagung der Parameter an den Sever, wird ein Dialogfenster
		 * erstellt.
		 */
		protected void onPostExecute(String result) {
			if (result.equalsIgnoreCase("Fehler")) {
				showAlertDialogFailure();
			} else {
				showAlertDialogSuccess();
			}
		}

		/**
		 * Meldung bei fehlerhafter �bertragung. Bei best�tigung wird die
		 * MainActivity gestartet.
		 */
		public void showAlertDialogFailure() {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);

			alertDialogBuilder.setTitle("Fehler!");

			alertDialogBuilder
					.setMessage("Das Problem konnte nicht gemeldet werden.")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent i = new Intent(
											getApplicationContext(),
											MainActivity.class);
									startActivity(i);
								}
							});

			AlertDialog alertDialog = alertDialogBuilder.create();

			alertDialog.show();
		}

		/**
		 * Meldung bei erfolgreicher �bertragung. Bei best�tigung wird die
		 * MainActivity gestartet.
		 */
		public void showAlertDialogSuccess() {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);

			alertDialogBuilder.setTitle("Vielen Dank!");

			alertDialogBuilder
					.setMessage("Das Problem wurde erfolgreich gemeldet")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Intent der die Main Activity startet
									Intent i = new Intent(
											getApplicationContext(),
											MainActivity.class);
									startActivity(i);
								}
							});

			AlertDialog alertDialog = alertDialogBuilder.create();

			alertDialog.show();
		}

		/**
		 * Problem erstellen
		 * */
		
		protected String doInBackground(String... args) {

			
			// Koordinaten Kommawerte in Strings umwandeln
			String StrLat = Float.toString(lat);
			String StrLon = Float.toString(lon);

			// Erstellen der Liste mit Schl�ssel-Wert-Paaren
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("city", city));
			params.add(new BasicNameValuePair("postal_code", postal_code));
			params.add(new BasicNameValuePair("street", street));
			params.add(new BasicNameValuePair("image_name", image_name));
			params.add(new BasicNameValuePair("lat", StrLat));
			params.add(new BasicNameValuePair("lon", StrLon));
			params.add(new BasicNameValuePair("problem_type", problem_type));
			params.add(new BasicNameValuePair("comment", comment));
			params.add(new BasicNameValuePair("user_name", user_name));
			params.add(new BasicNameValuePair("user_mail", user_mail));
			
		
			/*
			 * Die Liste wird �ber ein Objekt der Klasse StringUploader an den Server gesendet
			 * Z�ruck wird ein JSON-Objekt mit der HTTP-Response gegeben.
			 */
	
				json = stringUploader.makeHttpRequest(
						url_create_problem, params);

				Log.d("R�ckmeldung: ", json.toString());

				// auf erfolgreiche R�ckmeldung pr�fen
				try {
				int success = json.getInt(TAG_SUCCESS);
				
				if (success == 1) {
					// Problem erfolgreich �bertragen
					return "Erfolg";
				} else {
					return "Fehler";
				}
			} catch (JSONException e) {
				e.printStackTrace();
				showAlertDialogFailure();
			}

			return null;
		}
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
			Intent iAbout = new Intent(DescriptionActivity.this, About.class);
			startActivity(iAbout);
			return true;

		case R.id.action_settings:
			Intent iSettings = new Intent(DescriptionActivity.this,
					UserSettingsActivity.class);
			startActivity(iSettings);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	// ----
}
