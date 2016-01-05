package de.schulz.problemmelder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
 
/**
 * In der Klasse werden die Daten zu Beschreibung des Problems über HTTP-Request an den Server gesendet
 * Zurück wird ein JSON-Objekt mit dem Inhalt der HTTP-Response gegeben. 
 * 
 * @author Philipp Schulz
 * @version 03.07.2013
 */
public class StringUploader {
 
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
 
    // Konstruktor
    public StringUploader() {
 
    }
 
    // function get json from url
    // by making HTTP POST mehtod
    public JSONObject makeHttpRequest(String url, List<NameValuePair> params) {
 
        // Durchführen eines HTTP-Request mit POST Methode
        try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();     
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            //Log.e("Buffer Error", "Fehler beim Konvertieren" + e.toString());
        }
 
        // Versucht den String in ein JSON Objekt umzuwandeln
        try {
            jObj = new JSONObject(json);
            //Log.e("JSON Parser", "zurüchgegebenes JSON-Objekt" + jObj);
        } catch (JSONException e) {
            //Log.e("JSON Parser", "Fehler beim Parsen" + e.toString());
 
        }
        // Zurückgeben des JSON Strings
        return jObj;
    }
}
