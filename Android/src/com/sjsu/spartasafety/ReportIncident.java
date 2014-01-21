package com.sjsu.spartasafety;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParsePush;
import com.sjsu.spartasafety.util.SpartaSafetyData;

public class ReportIncident extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	public static final SimpleDateFormat jsonDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private String[] severityStrings = new String[] { "Information", "Low",
			"Medium", "High", "Critical" };
	private String[] categories = new String[] { "ACCIDENT", "ALCOHOL",
			"ASSAULT", "BURGLARY", "DEADLY WEAPON", "DISTURBANCE", "DRUGS",
			"FIRE", "FORGERY", "MISSING PERSON", "PETTY THEFT", "ROBBERY",
			"SEXUAL OFFENSE", "SUSPICIOUS", "THEFT", "UNAUTHORIZED",
			"VANDALISM", "WARRANT" };
	private int[] severityIcons = new int[] { R.drawable.severity1,
			R.drawable.severity2, R.drawable.severity3, R.drawable.severity4,
			R.drawable.severity5 };

	private int[] categoryIcons = new int[] { R.drawable.severity1,
			R.drawable.severity2, R.drawable.severity3, R.drawable.severity4,
			R.drawable.severity5, R.drawable.severity1, R.drawable.severity3,
			R.drawable.severity5, R.drawable.severity1, R.drawable.severity5,
			R.drawable.severity3, R.drawable.severity5, R.drawable.severity5,
			R.drawable.severity1, R.drawable.severity4, R.drawable.severity1,
			R.drawable.severity2, R.drawable.severity1, };
	MapFragment mapFragment;
	private LocationRequest lr;
	private LocationClient lc;
	private GoogleMap map;

	private Spinner severitySpinner;

	private Spinner categorySpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_incident);
		mapFragment = ((MapFragment) this.getFragmentManager()
				.findFragmentById(R.id.map));
		map = mapFragment.getMap();
		LatLng loc = new LatLng(37.335148, -121.881081);
		map.moveCamera(CameraUpdateFactory.newLatLng(loc));
		map.animateCamera(CameraUpdateFactory.zoomTo(17.0F));
		map.getUiSettings().setAllGesturesEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);
		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		// Location l = map.getMyLocation();
		lr = LocationRequest.create();
		lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		lc = new LocationClient(getApplicationContext(), this, this);
		lc.connect();
		severitySpinner = (Spinner) findViewById(R.id.lstSeverity);
		severitySpinner
				.setAdapter(new SeverityAdapter(ReportIncident.this,
						R.layout.severity_spinner_item, severityStrings,
						severityIcons));
		categorySpinner = (Spinner) findViewById(R.id.lstCategories);
		categorySpinner.setAdapter(new SeverityAdapter(ReportIncident.this,
				R.layout.severity_spinner_item, categories, categoryIcons));
		findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		findViewById(R.id.btnReport).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					int cat = categorySpinner.getSelectedItemPosition();
					int sev = severitySpinner.getSelectedItemPosition();
					String sString = severityStrings[severitySpinner
							.getSelectedItemPosition()];
					JSONObject severityObject = new JSONObject();
					severityObject.put("sev", sString);
					Location l = map.getMyLocation();
					JSONObject object = new JSONObject();
					JSONObject locationObject = new JSONObject();
					locationObject.accumulate("lon", l.getLongitude());
					locationObject.accumulate("lat", l.getLatitude());
					object.accumulate("loc", locationObject);
					object.accumulate("Severity", sev + 1);
					object.accumulate("Threat", categories[cat]);
					object.accumulate("flag", 1);
					object.accumulate("Date", jsonDateFormat.format(Calendar
							.getInstance().getTime()));
					findViewById(R.id.btnReport).setEnabled(false);
					ReportTask task = new ReportTask();
					task.execute(object, severityObject);
				} catch (Exception ex) {
					AlertDialog.Builder dlgAlert = new AlertDialog.Builder(
							ReportIncident.this);
					dlgAlert.setMessage(ex.getMessage());
					dlgAlert.setTitle("Oops!");
					dlgAlert.setPositiveButton("OK", null);
					dlgAlert.setCancelable(false);
					dlgAlert.create().show();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	public class SeverityAdapter extends ArrayAdapter<String> {

		private int[] myIcons;
		private String[] myObjects;

		public SeverityAdapter(Context context, int textViewResourceId,
				String[] objects, int[] icons) {
			super(context, textViewResourceId, objects);
			myObjects = objects;
			myIcons = icons;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		public View getCustomView(int position, View convertView,
				ViewGroup parent) {

			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.severity_spinner_item, parent,
					false);
			TextView label = (TextView) row.findViewById(R.id.txtSeverity);
			label.setText(myObjects[position]);

			ImageView icon = (ImageView) row.findViewById(R.id.imageSeverity);
			icon.setImageResource(myIcons[position]);

			return row;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onLocationChanged(Location l2) {
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
				new LatLng(l2.getLatitude(), l2.getLongitude()), 17.0F);
		map.animateCamera(cameraUpdate);

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		lc.requestLocationUpdates(lr, this);

	}
		
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	private class ReportTask extends AsyncTask<JSONObject, Integer, JSONObject> {

		@Override
		protected JSONObject doInBackground(JSONObject... object) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(getString(R.string.base_url)
					+ SpartaSafetyData.getUsername() + "/"
					+ getString(R.string.report_url));

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("details", object[0]
						.toString()));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
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
				try {
					if (result.getBoolean("result")) {

						ParsePush androidPush = new ParsePush();
						androidPush.setChannel("Alerts");
						androidPush.setPushToAndroid(true);
						androidPush
								.setMessage(object[0].getString("Threat")
										+ " with severity level "
										+ object[1].getString("sev")
										// object[0].getInt("Severity")
										+ " has been reported near San Jose State University");
						androidPush.sendInBackground();
					}
				} catch (Exception ex) {
				}
				return result;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			processResult(result);
		}

		private void processResult(JSONObject result) {
			findViewById(R.id.btnReport).setEnabled(true);
			String errorMessage = "Unable to report incident!";
			if (result != null) {
				try {
					if (result.getBoolean("result")) {
						Context context = getApplicationContext();
						CharSequence text = "Incident Reported";
						int duration = Toast.LENGTH_LONG;
						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
						setResult(RESULT_OK);
						finish();
						return;
					} else {
						errorMessage = result.getString("error");
					}
				} catch (Exception ex) {

				}
			}

			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(
					ReportIncident.this);
			dlgAlert.setMessage(errorMessage);
			dlgAlert.setTitle("Oops!");
			dlgAlert.setPositiveButton("OK", null);
			dlgAlert.setCancelable(false);
			dlgAlert.create().show();
		}
	}

}
