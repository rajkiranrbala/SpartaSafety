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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class TimeChooser extends Activity {

	TimePicker timePicker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_chooser);
		timePicker = (TimePicker) findViewById(R.id.timePicker1);
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

	}

	private void cancelActivity() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	private void completeActivity() {
		Intent result = new Intent();
		result.putExtra("Hour", timePicker.getCurrentHour());
		setResult(Activity.RESULT_OK, result);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
}
