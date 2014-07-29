package com.example.somethingtdo;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import com.example.somethingtdo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;

import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Geocoder;

public class MapActivity extends Activity {

	private String mLoc;
	private DatabaseHelper dh;
	
	class CustomInfoWindowAdapter implements InfoWindowAdapter {
		
		 
		@Override
		public View getInfoContents(Marker marker) {
			// TODO Auto-generated method stub
			View v = getLayoutInflater().inflate(R.layout.marker, null);
			
            SpannableString titleText = new SpannableString(marker.getTitle());
            titleText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, titleText.length(), 0);
            
            SpannableString snippetText = new SpannableString(marker.getSnippet());
            titleText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, titleText.length(), 0);
			
			TextView title = (TextView) v.findViewById(R.id.title);
            TextView info= (TextView) v.findViewById(R.id.info);

            title.setText(titleText);
            info.setText(snippetText);

            return v;
		}

		@Override
		public View getInfoWindow(Marker arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
		
	}

	private GoogleMap googleMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		
//		System.out.println(mEvents.getEvent(0).toString());
		
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
    		//new JSONEvent().execute("1 Columbus today music,comedy");

            googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
            
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            
            
	    	GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
//	    	String cityName = gpsTracker.getLocality(getApplicationContext());
	    	
			String user = LoginActivity.retrieveUsername();
			this.dh = new DatabaseHelper(this);
			
			String cityName;
			String time;
			
    		cityName = this.dh.searchAndGet(user).get(2);
    		time = this.dh.searchAndGet(user).get(1);
    		
    		Log.d("Map", cityName);
	    	
	    	gpsTracker.getLocation();
	    	LatLng latlng = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
	    	googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 11));
            
    		new JSONEvent().execute(cityName,time);

        }
	}
	
	private void createMarkers(double lat, double lon, String title, String snippet, float hue) {
	
		String snip;
	
		MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lon))
    			.title(title)
    			.snippet("\n" + snippet)
    			.icon(BitmapDescriptorFactory.defaultMarker(hue));
		
		googleMap.addMarker(marker);	
		
	}
	
	class JSONEvent extends AsyncTask<String, Integer, Events>  {
		
		private EditText mCityName;


		private static final String TAG = "JSONEventsTask";

		@Override
		protected Events doInBackground(String... params) {
			String pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("selectedInterests", null);
			System.out.println(pref);
	    	
	    	String cityname = params[0];
	    	String time = params[1];
	    	
	    	System.out.println(cityname);
			
			String data = null;
			try {
				data = ((new EventHttpClient()).getEventsData(1, cityname, time, pref));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Log.d("SUCCESS", data);
			

//			Log.d("Before Event Parser",null);
			System.out.println("Before EventParser");
			Events events = null;
			try {
				events = (new EventParser(data)).getEvents();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			System.out.println("After EventParser");
			
//			System.out.println(events.getEvent(0).toString());
						
			
			return events;
		}
		
		
/*		
		protected void onProgressUpdate(Integer ...progress) {
			setProgressPercent(progress[0]);
	    }
*/
	    protected void onPostExecute(Events mEvents) {
	        // TODO: check this.exception 
	        // TODO: do something with the feed
	    	
	    	//System.out.println("MapsActivity " + mEvents.getEvent(0).toString());
	    	

	    	
	    	if (mEvents.getSearchCount() > 1) {
	    		for (int i = 0; i < 10; i++){ 
	    			Event ev = mEvents.getEvent(i);
	    			createMarkers(ev.getLatitude(), ev.getLongitude(), ev.getTitle(), ev.getVenue(), BitmapDescriptorFactory.HUE_RED);
	    		}
	    	}
	    }

	}
	
}