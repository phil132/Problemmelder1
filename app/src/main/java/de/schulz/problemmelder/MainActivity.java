package de.schulz.problemmelder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/** 
 * Acitvity mit dem Hauptmenü. Der Benutzer hat die Möglichkeit ein Problem zu melden, 
 *  oder die Karte mit bereits gemeldeten Problemen aufzurufen.
 * 
 * @author Philipp Schulz
 * @version 08.07.2013
 *
 */
public class MainActivity extends Activity implements OnClickListener {

	private ImageButton IbtnNewProblem, IbtnWebMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		

		// Initialisieren
		IbtnNewProblem = (ImageButton) findViewById(R.id.iBtnNewProblem);
		IbtnWebMap = (ImageButton) findViewById(R.id.iBtnProblemMap);
		IbtnWebMap.setOnClickListener(this);
		IbtnNewProblem.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iBtnNewProblem:
			Intent iCam = new Intent(MainActivity.this, CameraActivity.class);
			startActivity(iCam);
			break;

		case R.id.iBtnProblemMap:
			Intent iMap = new Intent(MainActivity.this, WebMap.class);
			startActivity(iMap);
		}
	}

	// -----Menü
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			Intent iAbout = new Intent(MainActivity.this, About.class);
			startActivity(iAbout);
			return true;
			
		case R.id.action_settings:
			Intent iSettings = new Intent(MainActivity.this, UserSettingsActivity.class );
			startActivity(iSettings);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
		//-----
		
	}

}
