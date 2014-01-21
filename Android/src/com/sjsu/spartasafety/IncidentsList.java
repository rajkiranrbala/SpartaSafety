package com.sjsu.spartasafety;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class IncidentsList extends Activity {

	public static final SimpleDateFormat jsonDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_incidents_list);
		IncidentsFetchTask task = new IncidentsFetchTask();
		String url = this.getIntent().getStringExtra("url");
		task.execute(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
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

	private void processIncidentsResult(JSONObject result) {
		if (result != null) {
			try {
				if (result.getBoolean("result")) {
					JSONArray array = result.getJSONArray("incidents");
					ListView view = (ListView) findViewById(R.id.lstIncidents);
					view.setAdapter(new IncidentsArrayAdapter(array));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Context context = getApplicationContext();
			CharSequence text = "Error fetching incidents";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	private class IncidentsArrayAdapter extends BaseAdapter {

		private JSONArray items;
		private LayoutInflater mInflater;

		public IncidentsArrayAdapter(JSONArray array) {
			this.items = array;
			mInflater = (LayoutInflater) IncidentsList.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return items.length();
		}

		@Override
		public Object getItem(int position) {
			try {
				return items.getJSONObject(position);
			} catch (JSONException e) {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
			if (vi == null)
				vi = mInflater.inflate(R.layout.incident_row_item, null);
			TextView incident = (TextView) vi.findViewById(R.id.txtIncident);
			TextView time = (TextView) vi.findViewById(R.id.txtTIme);
			TextView location = (TextView) vi.findViewById(R.id.txtLocation);
			ImageView imageView = (ImageView) vi
					.findViewById(R.id.imageSeverity);
			JSONObject data = (JSONObject) getItem(position);
			try {
				incident.setText(data.getString("Threat"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				location.setText(data.getString("Location"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {

				try {
					time.setText(jsonDateFormat.parse(data.getString("Date"))
							.toString());
				} catch (ParseException e) {
					time.setText(data.getString("Date"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			int severity;
			try {
				severity = data.getInt("Severity");
				switch (severity) {
				case 1:
					imageView.setImageResource(R.drawable.severity1);
					break;
				case 2:
					imageView.setImageResource(R.drawable.severity2);
					break;
				case 3:
					imageView.setImageResource(R.drawable.severity3);
					break;
				case 4:
					imageView.setImageResource(R.drawable.severity4);
					break;
				case 5:
					imageView.setImageResource(R.drawable.severity5);
					break;
				default:
					break;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return vi;
		}
	}

}
