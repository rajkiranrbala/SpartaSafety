package com.sjsu.spartasafety;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.sjsu.spartasafety.util.SpartaSafetyData;

public class Safety extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	private MapFragment mapFragment;
	private GoogleMap map;
	private LocationRequest lr;
	private LocationClient lc;
	private TextView txtMessage;
	private TextView txtError;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_safety);
		txtError = (TextView) findViewById(R.id.txtErrorMessage);
		txtMessage = (TextView) findViewById(R.id.txtMessage);
		txtMessage.setVisibility(View.INVISIBLE);
		mapFragment = ((MapFragment) this.getFragmentManager()
				.findFragmentById(R.id.map));
		map = mapFragment.getMap();
		LatLng loc = new LatLng(37.335148, -121.881081);
		map.moveCamera(CameraUpdateFactory.newLatLng(loc));
		map.moveCamera(CameraUpdateFactory.zoomTo(18.0F));
		map.getUiSettings().setAllGesturesEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);
		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		lr = LocationRequest.create();
		lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		lr.setInterval(3000);
		lr.setSmallestDisplacement(5.0F);
		lc = new LocationClient(getApplicationContext(), this, this);
		lc.connect();
	}

	private Location lastLocation;
	private boolean isRunning = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public void onLocationChanged(Location l2) {
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
				new LatLng(l2.getLatitude(), l2.getLongitude()),
				map.getCameraPosition().zoom);
		map.animateCamera(cameraUpdate);
		if (!isRunning) {
			isRunning = true;
			SafetyFetchTask task = new SafetyFetchTask();
			task.execute(l2);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// Timer t = new Timer();
		// t.schedule(new TimerTask() {
		//
		// @Override
		// public void run() {
		lc.requestLocationUpdates(lr, Safety.this);
		// }
		// }, 5000);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		lc.disconnect();
	}

	public class SafetyFetchTask extends AsyncTask<Location, Long, JSONObject> {

		@Override
		protected JSONObject doInBackground(Location... args) {

			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					timeoutConnection);
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpGet httpGet = new HttpGet(getString(R.string.base_url)
					+ SpartaSafetyData.getUsername() + "/"
					+ getString(R.string.help_url) + "/"
					+ args[0].getLatitude() + "/" + args[0].getLongitude());
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
				lastLocation = args[0];
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			processSafety(result);
		}

	}

	public void processSafety(JSONObject result) {
		isRunning = false;
		txtError.setText("");
		txtMessage.setText("");
		txtMessage.setVisibility(View.INVISIBLE);
		if (result != null) {
			try {
				if (result.getBoolean("result")) {
					DecimalFormat df = new DecimalFormat("#.##");
					txtMessage
							.setText("You are "
									+ df.format(result.getDouble("percent"))
									+ "% safe");
					txtMessage.setVisibility(View.VISIBLE);
					JSONArray array = result.getJSONArray("loc");
					map.clear();
					IconGenerator generator = new IconGenerator(Safety.this);
					generator.setStyle(IconGenerator.STYLE_RED);
					for (int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);
						Bitmap bmp = generator.makeIcon(obj.getInt("Severity")
								+ "");
						MarkerOptions options = new MarkerOptions();
						options.position(new LatLng(obj.getDouble("lat"), obj
								.getDouble("lon")));
						options.icon(BitmapDescriptorFactory.fromBitmap(bmp));
						Marker m = map.addMarker(options);
					}

				} else {
					txtError.setText(result.getString("error"));
				}

			} catch (Exception ex) {

			}
		} else {

		}
	}

}
