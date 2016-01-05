package de.schulz.problemmelder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;



import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Die Activity dient zur Bestimmung des Standortes des Problems.
 * 
 * Zun�chst wird die Position des Benutzers bestimmt und angezeigt. 
 * Dieser kann dann mi einem Fadenkreuz die Position des Problems festlegen
 * 
 *	@author Philipp Schulz
 *	@version 03.07.2013
 */
public class MapOsm extends Activity  implements View.OnClickListener, MapListener, LocationListener{
	
	private static final String TAG = "MapOsm";
	private MapView mapView;
	private IMapController controller;
	LocationManager locationManager;
	ArrayList<OverlayItem> overlayItemArray;
	private DrawOnTop mDraw;
	private TextView tvActualPos;	
	String locationProvider, postal_code, city, street, streetnumber;
	String imagePath;
	private Button btnBackCamera, btnDescription;
	private ImageButton btn_ZoomToPosition;
	public boolean contfirmCoordinate;
	float x,y;
	int pos_lat =0,  pos_longi =0;
	float selection_lat, selection_longi;
	GeoPoint position_point, currentCenter;
	Location lastLocation;
	Timer myTimer;
	
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.osm_map);
	        initializeGui();
		    initializeMap();
		    getBundle();
		    drawCrosOnTop();  
		    setupLocation();
		    crateToasty();
		    zoomToPosition();
	  }
	 

private void crateToasty() {
		
	Toast.makeText(MapOsm.this,"Bitte w�hlen Sie den Ort an dem das Problem aufgetreten ist.", Toast.LENGTH_LONG).show();
	}

/**
 * 	Holt sich die gesendeten Variablenwerte aus der vorherigen Activity
 */
	 private void getBundle() {
			Bundle gotValues = getIntent().getExtras();
			imagePath = gotValues.getString("image_path");
		
	}

	 /**
	 * Zecihnet Fadenkreuz zum ausw�hlen des Punktes
	 */
	private void drawCrosOnTop() {
		   
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int sHeight = metrics.heightPixels;
			int sWidth = metrics.widthPixels;
			
			//Log.d(TAG, "centerPixel=[" + sWidth + "," + sHeight+ "]");
	
		   mDraw = new DrawOnTop(this, sHeight, sWidth); 
			addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	 
	  public void onConfigurationChanged(Configuration newConfig) {
          super.onConfigurationChanged(newConfig);
          drawCrosOnTop();
	  }

	/*
	  *  location Manager such Position mittels Wlan oder GPS
	  */
	private void setupLocation() {
		 locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		 Criteria criteria = new Criteria();
		 locationProvider = locationManager.getBestProvider(criteria, false);
		 
	     lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	     
	     if (lastLocation != null){
                 updateLoc(lastLocation);
	        }
	     
	     locationManager.requestLocationUpdates(locationProvider, 500, 1, this);
	}

	/*
	 *  Initialisieren der Karte 
	 */
	private void initializeMap() {

		position_point = new GeoPoint(13,52);

		mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(false);
	    mapView.setMultiTouchControls(true);
	    mapView.setClickable(true);
		mapView.setTileSource(TileSourceFactory.MAPNIK);
	    //mapView.setMapListener(this);

	    controller = mapView.getController();
	    controller.setZoom(18);
	    
	    //--- erstelle Overlay
        overlayItemArray = new ArrayList<OverlayItem>();
        DefaultResourceProxyImpl defaultResourceProxyImpl
         = new DefaultResourceProxyImpl(this);
        MyItemizedIconOverlay myItemizedIconOverlay 
         = new MyItemizedIconOverlay(
           overlayItemArray, null, defaultResourceProxyImpl);
        mapView.getOverlays().add(myItemizedIconOverlay);
        //---
	}
	  
	/*
	 *	Initialisiert die grafische Oberflaeche und setzt die OnClicklistener
	 */
	private void initializeGui() {
	 
	   	tvActualPos = (TextView)findViewById(R.id.tvPosition);
		btn_ZoomToPosition =(ImageButton) findViewById(R.id.bZoomToPosition);
		btnBackCamera = (Button) findViewById(R.id.btnBackCamera);
		btnDescription = (Button) findViewById(R.id.btnDescription);
		btn_ZoomToPosition.setOnClickListener(this);
		btnBackCamera.setOnClickListener(this);
		btnDescription.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			// Zoom to current position
		case R.id.bZoomToPosition:
            setupLocation();
			zoomToPosition();
			 locationManager.requestLocationUpdates(locationProvider, 500, 1, this);
			break;
			
		case R.id.btnBackCamera:
			super.onBackPressed();
			break;
			
		case R.id.btnDescription:
			setCoordinate();
			getAdress();
			start_descriptionActivity();
		}
	}
	
	private void zoomToPosition() {
		controller.animateTo(position_point);
		mapView.invalidate();
		new CountDownTimer(1000, 1000) {

		     public void onTick(long millisUntilFinished) {
		     }

		     public void onFinish() {
		    	controller.animateTo(position_point);
		    	mapView.invalidate();
				controller.setZoom(20);
		     }
		  }.start();	
	}
	/*
	 * Bestimmt die Koordinate des von Benutzer ausgew�hlten Punktes
	 */
	private void setCoordinate() {
		currentCenter = (GeoPoint) mapView.getMapCenter();
		selection_lat = (int) (currentCenter.getLatitudeE6());
    	selection_longi = (int) (currentCenter.getLongitudeE6());
		}
		
/**
 * bestimmt die Adresse des gesetzten Punktes 
 */
	private void getAdress() {

			Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
			try{
				List<Address> address = geocoder.getFromLocation(currentCenter.getLatitudeE6()/ 1E6, currentCenter.getLongitudeE6()/ 1E6, 1);
				if (address.size()>0){
									
					Address addr = address.get(0);
					postal_code = addr.getPostalCode();
					city = addr.getLocality();
					streetnumber = addr.getFeatureName();
					street = addr.getThoroughfare();
					Log.d(TAG, "Adresse: " + postal_code + " " + city + " " + street + " "  + streetnumber);	 
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				
			}					
		}

	/**
	 * Startet die Activity Description
	 */
	private void start_descriptionActivity() {
		
		Bundle bundle = new Bundle();
		bundle.putString("image_path", imagePath);
		bundle.putString("postal_code", postal_code);
		bundle.putString("city", city);
		bundle.putString("street", street);
		bundle.putString("streetnumber", streetnumber);
		bundle.putFloat("lat", selection_lat);
		bundle.putFloat("lon", selection_longi);
		
		// Starten der Activity zum Beschreiben des Problems
		Intent i = new Intent(MapOsm.this, DescriptionActivity.class);
		i.putExtras(bundle);
		startActivity(i);
	}

	@Override
	public boolean onScroll(ScrollEvent arg0) {
		return false;
	}

	@Override
	public boolean onZoom(ZoomEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		  locationManager.removeUpdates(myLocationListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(locationProvider, 500, 1, this);
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
		  
	}
	
	 private void updateLoc(Location loc){
	    // GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
	     //controller.setCenter(locGeoPoint);
	     try {
             setOverlayLoc(loc);
             mapView.invalidate();
             makeUseOfNewLocation(loc);
         }catch (Exception e){

         }

	    }

	 private void makeUseOfNewLocation(Location location) {
		 int lon = (int) (location.getLongitude()*1E6);
		    int lat = (int) (location.getLatitude()*1E6);

		    int lontitue = (int)lon;
		    int latitute = (int)lat;

		    tvActualPos.setText(latitute + " ; " + lontitue );
		    position_point = new GeoPoint(latitute, lontitue);    
			}
		
	private void setOverlayLoc(Location overlayloc){
		  GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
		  //---
		     overlayItemArray.clear();
		     
		     OverlayItem newMyLocationItem = new OverlayItem(
		       "My Location", "My Location", overlocGeoPoint);
		     overlayItemArray.add(newMyLocationItem);
		     //---
		 }
	 
	 private LocationListener myLocationListener
	    = new LocationListener(){

	  @Override
	  public void onLocationChanged(Location location) {
	   // TODO Auto-generated method stub
	   updateLoc(location);
	  }

	  @Override
	  public void onProviderDisabled(String provider) {
	   // TODO Auto-generated method stub
	   
	  }

	  @Override
	  public void onProviderEnabled(String provider) {
	   // TODO Auto-generated method stub
	   
	  }

	  @Override
	  public void onStatusChanged(String provider, int status, Bundle extras) {
	   // TODO Auto-generated method stub

		}
	  };
	  
	  private class MyItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem> {

		  public MyItemizedIconOverlay(
		    List<OverlayItem> pList,
		    org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener,
		    ResourceProxy pResourceProxy) {
		   super(pList, pOnItemGestureListener, pResourceProxy);
		  }

		  @Override
		  public void draw(Canvas canvas, MapView mapview, boolean arg2) {
		   // TODO Auto-generated method stub
		   super.draw(canvas, mapview, arg2);
		   
		   if(!overlayItemArray.isEmpty()){
		    
		    //hohlt das erste Element aus dem Array
		    IGeoPoint in = overlayItemArray.get(0).getPoint();
		    
		    Point out = new Point();
		    mapview.getProjection().toPixels(in, out);
		    
		    Bitmap bm = BitmapFactory.decodeResource(getResources(), 
		      R.drawable.position);
		    /*
		     * Zeichnet Das Bild auf den Punkt out. Um die Mitte des Bildes zu erhalten, wird 
		     * die h�lfte der Breite bei der X-Koordinate subtrahiert und die H�lfte der H�he bei 
		     *  der Y-Koordinate.
		     */
		    canvas.drawBitmap(bm,
		      out.x - bm.getWidth()/2,  
		      out.y - bm.getHeight()/2,  
		      null);
		   }
		  }

		  @Override
		  public boolean onSingleTapUp(MotionEvent event, MapView mapView) {
		   // TODO Auto-generated method stub
		   //return super.onSingleTapUp(event, mapView);
		   return true;
		  }
		 }
	  /*
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
					Intent iAbout = new Intent(MapOsm.this, About.class);
					startActivity(iAbout);
					return true;
					
				case R.id.action_settings:
					Intent iSettings = new Intent(MapOsm.this, UserSettingsActivity.class);
					startActivity(iSettings);
					return true;

				default:
					return super.onOptionsItemSelected(item);
				}
		
			}
	//----	

*/
			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				
			}


			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}


			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}


			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}
		 }



	
	
	

