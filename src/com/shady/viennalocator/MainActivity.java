package com.shady.viennalocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shady.viennalocator.geoData.GeoDataManager;
import com.shady.viennalocator.jsonSchemas.offline.Feature;
import com.shady.viennalocator.jsonSchemas.offline.Geometry;
import com.shady.viennalocator.jsonSchemas.offline.ObjectFeature;
import com.shady.viennalocator.transportationData.TransportationDataManager;

public class MainActivity extends FragmentActivity {

	private GoogleMap _map = null;
	private TransportationDataManager transManager;
	private GeoDataManager _geoManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Navigation drawer ---------
		final ListView listview = (ListView) findViewById(R.id.left_drawer);
		String[] values = new String[] { "Subway", "Tram", "Bus" };
		final ArrayList<String> list = new ArrayList<String>();
		for (String item : values) {
			list.add(item);
		}
		final StableArrayAdapter adapter = new StableArrayAdapter(this,
				android.R.layout.simple_list_item_multiple_choice, list);

		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				final String item = (String) parent.getItemAtPosition(position);

				ObjectFeature objectFeature = transManager.getOfflineDataJSON();

				view.animate().setDuration(2000).alpha(0)
						.withEndAction(new Runnable() {

							@Override
							public void run() {
								adapter.notifyDataSetChanged();
								view.animate().alpha(1);
							}
						});
			}

		});
		SparseBooleanArray checkedArray = listview.getCheckedItemPositions();
		String checked = "";

		for (int i = 0; i < listview.getCount(); i++) {
			if (checkedArray.get(i) == true) {
				checked += listview.getItemAtPosition(i).toString();
			}
		}
		// END Navigation drawer stuff --------------

		// Map---------------
		setUpMapIfNeeded();

		// END Map stuff--------

		_geoManager = GeoDataManager.getInstance(this);
		// END GeoDataManager---------

		// Transportation Data initialization -----------
		transManager = TransportationDataManager.getInstance();

		// Button
		Button loadButton = (Button) findViewById(R.id.button1);
		loadButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ObjectFeature objFeat = transManager.getOfflineDataJSON();
				markStopsOnMap(objFeat);
			}
		});
	}

	// Extract geometry data from every feature (=stop) and put a marker on the
	// map
	protected void markStopsOnMap(ObjectFeature objFeat) {
		for (Feature feature : objFeat.getFeatures()) {
			Double lat = (double) feature.getGeometry().getCoordinates()[1];
			Double lng = (double) feature.getGeometry().getCoordinates()[0];

			LatLng position = new LatLng(lat, lng);
			_map.addMarker(new MarkerOptions().position(position)
					.snippet(feature.getProperties().getHLINIEN())
					.title(feature.getProperties().getHTXTK()));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Map stuff ----------
	private void setUpMapIfNeeded() {
		// null check
		if (_map == null) {
			// Try to obtain the map from the SupportMapFragment.
			_map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
		}

		if (_map == null) {
			// map succesfully instantiated
			Toast.makeText(this, "Map cannot be displayed on this device",
					Toast.LENGTH_SHORT).show();
		}
		// Configure map
		_map.setMyLocationEnabled(true);

		_map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				Location location = new Location("Shady");
				location.setLongitude(marker.getPosition().longitude);
				location.setLatitude(marker.getPosition().latitude);
				Address address = _geoManager.getAddressFromLocation(location);
				marker.setSnippet(marker.getSnippet() + "\n\n"
						+ address.getAddressLine(0));
				return false;
			}
		});
	}

	// Navigation Drawer stuff ----------
	public class StableArrayAdapter extends ArrayAdapter<String> {
		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}
}
