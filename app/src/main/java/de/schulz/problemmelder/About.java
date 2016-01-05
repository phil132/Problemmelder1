package de.schulz.problemmelder;

import android.app.Activity;
import android.os.Bundle;

/**
 * In der Activity wird das Layout in welchem sich die Informationen zur Applikation befinden angezeigt.
 * 
 * @author Philipp Schulz
 * @version 02.08.2013
 *
 */

public class About extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}
	
}
