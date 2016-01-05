package de.schulz.problemmelder;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import de.schulz.problemmelder.R;


/**
 * Activity für die Einstellungen des Benutzers
 * 
 * Der Benutzer kann seinen Namen und E-Mailadresse in den Optionen Speichern. 
 * Die Variablen werden für die Dauer der Installation der Applikation, intern auf dem Gerät geschpeichert.
 * Bei einer Meldung, werden die Werte in die Zieldatenbank gespeichert.
 * 
 * @author Philipp Schulz
 * @version 08.07.2013
 */
public class UserSettingsActivity extends Activity implements OnClickListener {
	
	private EditText etUserName, etMail;
	private Button bSave;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        etUserName = (EditText)findViewById(R.id.etUserName);
        etMail = (EditText)findViewById(R.id.etUserMail);
        bSave = (Button)findViewById(R.id.bSaveSettings);
        bSave.setOnClickListener(this);
        loadPrefs();

    }
	
	private void loadPrefs() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		 String name = sp.getString("USERNAME", "default User");
		 etUserName.setText(name);
		String mail = sp.getString("USERMAIL", "kein Angabe");
		etMail.setText(mail);

	}
	
	
    private void savePrefs(String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		savePrefs("USERNAME", etUserName.getText().toString());
		savePrefs("USERMAIL", etMail.getText().toString());
		finish();

		
	}




}
