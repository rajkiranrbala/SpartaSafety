package com.sjsu.spartasafety;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.sjsu.spartasafety.util.SpartaSafetyData;

public class IncidentsView extends Activity {

	private GoogleMap mMap;
	private String dataUrl;
	private int mode;
	private Map<Marker, String> markerMap = new HashMap<Marker, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_incidents_view);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		mode = R.id.menuByLocations;
		LatLng loc = new LatLng(37.335148, -121.881081);
		mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0F));
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				if (mode == R.id.menuByLocations) {
					Intent i = new Intent(IncidentsView.this,
							IncidentsList.class);
					i.putExtra("url", dataUrl + markerMap.get(marker));
					startActivityForResult(i, R.id.lstIncidents);
				} else {
					Intent i = new Intent(IncidentsView.this,
							IncidentsList.class);
					i.putExtra("url", dataUrl);
					startActivityForResult(i, R.id.lstIncidents);
				}
				return true;
			}
		});
		refresh();
		findViewById(R.id.btnReport).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(IncidentsView.this, ReportIncident.class);
				startActivityForResult(i, R.id.btnReport);
			}
		});
		findViewById(R.id.btnHelpMe).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(IncidentsView.this, Safety.class);
				startActivityForResult(i, R.id.btnHelpMe);
			}
		});
	}

	private void refresh() {
		String url = getString(R.string.base_url)
				+ SpartaSafetyData.getUsername() + "/"
				+ getString(R.string.groupbylocations_url);
		IncidentsFetchTask task = new IncidentsFetchTask();
		mode = R.id.menuByLocations;
		dataUrl = getString(R.string.base_url) + SpartaSafetyData.getUsername()
				+ "/" + getString(R.string.getincidentsbylocation_url) + "/";
		task.execute(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.incidents_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.menuByCategories:
			i = new Intent(this, CategoriesChooser.class);
			startActivityForResult(i, R.id.menuByCategories);
			return true;
		case R.id.menuByLocations:
			IncidentsFetchTask task = new IncidentsFetchTask();
			String url = getString(R.string.base_url)
					+ SpartaSafetyData.getUsername() + "/"
					+ getString(R.string.groupbylocations_url);
			mode = R.id.menuByLocations;
			dataUrl = getString(R.string.base_url)
					+ SpartaSafetyData.getUsername() + "/"
					+ getString(R.string.getincidentsbylocation_url) + "/";
			task.execute(url);
			return true;
		case R.id.menuByMonth:
			i = new Intent(this, MonthChooser.class);
			startActivityForResult(i, R.id.menuByMonth);
			return true;
		case R.id.menuByTime:
			i = new Intent(this, TimeChooser.class);
			startActivityForResult(i, R.id.menuByTime);
			return true;
		case R.id.menuEditProfile:
			i = new Intent(this, SpartaSafetyRegister.class);
			i.putExtra("RegistrationMode", false);
			startActivityForResult(i, R.id.menuEditProfile);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IncidentsFetchTask task = new IncidentsFetchTask();
		String url = "";
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case R.id.menuByCategories:
				String category = data.getStringExtra("Category");
				url = getString(R.string.base_url)
						+ SpartaSafetyData.getUsername() + "/"
						+ getString(R.string.groupbycateogry_url) + "/"
						+ category;
				task.execute(url);
				mode = R.id.menuByCategories;
				dataUrl = getString(R.string.base_url)
						+ SpartaSafetyData.getUsername() + "/"
						+ getString(R.string.getincidentsbycategory_url) + "/"
						+ category;
				break;
			case R.id.menuByMonth:
				int month = data.getIntExtra("Month", 0);
				int year = data.getIntExtra("Year", 0);
				url = getString(R.string.base_url)
						+ SpartaSafetyData.getUsername() + "/"
						+ getString(R.string.groupbymonth_url) + "/" + month
						+ "/" + year;
				task.execute(url);
				mode = R.id.menuByMonth;
				dataUrl = getString(R.string.base_url)
						+ SpartaSafetyData.getUsername() + "/"
						+ getString(R.string.getincidentsbymonth_url) + "/"
						+ month + "/" + year;
				break;
			case R.id.menuByTime:
				int hour = data.getIntExtra("Hour", -1);
				url = getString(R.string.base_url)
						+ SpartaSafetyData.getUsername() + "/"
						+ getString(R.string.groupbytime_url) + "/" + hour;
				task.execute(url);
				mode = R.id.menuByTime;
				dataUrl = getString(R.string.base_url)
						+ SpartaSafetyData.getUsername() + "/"
						+ getString(R.string.getincidentsbytime_url) + "/"
						+ hour;
				break;
			case R.id.menuEditProfile:
				break;
			default:
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void processIncidentsResult(JSONObject result) {
		if (result != null) {
			try {
				if (result.getBoolean("result")) {
					JSONArray array = result.getJSONArray("loc");
					markerMap.clear();
					mMap.clear();
					IconGenerator generator = new IconGenerator(
							IncidentsView.this);
					generator.setStyle(IconGenerator.STYLE_RED);
					for (int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);

						Bitmap bmp = generator.makeIcon(obj.getInt("count")
								+ "");
						MarkerOptions options = new MarkerOptions();
						options.position(new LatLng(obj.getDouble("lat"), obj
								.getDouble("lon")));
						options.icon(BitmapDescriptorFactory.fromBitmap(bmp));
						Marker m = mMap.addMarker(options);
						markerMap.put(m, obj.getString("_id"));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public class IncidentsFetchTask extends AsyncTask<String, Long, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... args) {

			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 300000;
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					timeoutConnection);
			int timeoutSocket = 300000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			String url = args[0];
			url = url.replace(" ", "%20");
			HttpGet httpGet = new HttpGet(url);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpResponse response;
			try {
				response = httpclient.execute(httpGet);
				HttpEntity httpEntity = response.getEntity();
				InputStream is = httpEntity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				String json = sb.toString();
				JSONObject result = new JSONObject(json);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			processIncidentsResult(result);
		}

	}

}
