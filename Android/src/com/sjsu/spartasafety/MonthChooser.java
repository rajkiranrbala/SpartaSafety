package com.sjsu.spartasafety;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.sjsu.spartasafety.util.SpartaSafetyData;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

public class MonthChooser extends Activity {

	DatePicker datePicker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_month_chooser);
		datePicker = (DatePicker) findViewById(R.id.datePicker1);
		findViewById(R.id.btnDone).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				completeActivity();
			}
		});

		findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelActivity();
			}
		});
		if (SpartaSafetyData.getStartDate() != 0) {
			datePicker.setMinDate(SpartaSafetyData.getStartDate());
			datePicker.setMaxDate(SpartaSafetyData.getEndDate());
		} else {
			refresh();
		}

	}

	private void cancelActivity() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	private void completeActivity() {
		Intent result = new Intent();
		result.putExtra("Month", datePicker.getMonth());
		result.putExtra("Year", datePicker.getYear());
		setResult(Activity.RESULT_OK, result);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chooser, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuRefresh:
			refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refresh() {
		MonthFetchTask task = new MonthFetchTask();
		task.execute();
	}

	private void processResult(JSONObject result) {
		try {
			if (result != null && result.has("result")) {
				if (result.getBoolean("result")) {
					String startDate = result.getString("startDate");
					String endDate = result.getString("endDate");
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					Date sDate = sdf.parse(startDate);
					Date eDate = sdf.parse(endDate);
					SpartaSafetyData.setStartDate(sDate.getTime());
					SpartaSafetyData.setEndDate(eDate.getTime());
					datePicker.setMaxDate(eDate.getTime());
					datePicker.setMinDate(sDate.getTime());
				}
			}
		} catch (Exception ex) {

		}
	}

	public class MonthFetchTask extends AsyncTask<String, Long, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... arg0) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(getString(R.string.base_url)
					+ SpartaSafetyData.getUsername() + "/"
					+ getString(R.string.daterange_url));
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
			processResult(result);
		}

	}

}
