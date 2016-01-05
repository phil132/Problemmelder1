package de.schulz.problemmelder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Zeigt die Karte von der entsprechenden URL an
 * 
 * @author Philipp Schulz
 * @version 06.08.2013
 *
 */

public class WebMap extends Activity {

	private WebView webView;
	String url = "http://www.gistribution.de/smart-wedding";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.problem_map);

		webView = (WebView) findViewById(R.id.webView1);
		webView.setWebViewClient(new MyWebViewClient());

		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(url);



		if(validateUrl(url)){
		    webView.getSettings().setJavaScriptEnabled(true);
		    //webView.loadUrl(url);
	}
	}

	private boolean validateUrl(String url) {
		//fehlt noch
		return true;
	}

	//---- Men�
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
				Intent iAbout = new Intent(WebMap.this, About.class);
				startActivity(iAbout);
				return true;

			case R.id.action_settings:
				Intent iSettings = new Intent(WebMap.this, UserSettingsActivity.class);
				startActivity(iSettings);
				return true;


			default:
				return super.onOptionsItemSelected(item);
		}
		//----
	}

	private class MyWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
			}

		}
	}




