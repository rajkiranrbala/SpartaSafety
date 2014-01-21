package com.sjsu.spartasafety;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sjsu.spartasafety.util.SpartaSafetyData;

public class SpartaSafetyRegister extends Activity {

	boolean registration = true;

	private EditText username, email, eemail1, eemail2, eemail3, phone1,
			phone2, phone3;
	private Button btnDone, btnCancel;

	private boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	private void processResult(JSONObject result) {
		findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
		findViewById(R.id.waitText).setVisibility(View.INVISIBLE);
		findViewById(R.id.fields).setVisibility(View.VISIBLE);
		if (result == null) {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
			dlgAlert.setMessage("Unable to complete registration");
			dlgAlert.setTitle("Oops!");
			dlgAlert.setPositiveButton("OK", null);
			dlgAlert.setCancelable(false);
			dlgAlert.create().show();
		} else {
			boolean success;
			try {
				success = result.getBoolean("result");
				if (success) {
					SharedPreferences sharedPreferences = this
							.getSharedPreferences(
									"com.sjsu.spartasafety.RegistrationData",
									Context.MODE_PRIVATE);
					Editor editor = sharedPreferences.edit();
					editor.putString("username", username.getText().toString());
					editor.putString("email", email.getText().toString());
					editor.putString("eemail1", eemail1.getText().toString());
					editor.putString("eemail2", eemail2.getText().toString());
					editor.putString("eemail3", eemail3.getText().toString());
					editor.putString("phone1", phone1.getText().toString());
					editor.putString("phone2", phone2.getText().toString());
					editor.putString("phone3", phone3.getText().toString());
					editor.apply();
					if (registration) {
						Context context = getApplicationContext();
						CharSequence text = "Registration succeeded";
						int duration = Toast.LENGTH_LONG;
						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
						Intent i = new Intent(this, IncidentsView.class);
						startActivity(i);
					} else {
						activityCompleted(false);
					}
				} else {
					AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
					dlgAlert.setMessage(result.getString("error"));
					dlgAlert.setTitle("Oops!");
					dlgAlert.setPositiveButton("OK", null);
					dlgAlert.setCancelable(false);
					dlgAlert.create().show();
				}
			} catch (JSONException e) {
				AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
				dlgAlert.setMessage("Unable to complete registration");
				dlgAlert.setTitle("Oops!");
				dlgAlert.setPositiveButton("OK", null);
				dlgAlert.setCancelable(false);
				dlgAlert.create().show();
			}
		}
	}

	private void activityCompleted(boolean cancelled) {
		if (cancelled) {
			this.setResult(RESULT_CANCELED);
		} else {
			SpartaSafetyData.loadSharedPreferences(this);
			this.setResult(RESULT_OK);
		}
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SpartaSafetyData.loadSharedPreferences(this);
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				"com.sjsu.spartasafety.RegistrationData", Context.MODE_PRIVATE);

		setContentView(R.layout.activity_sparta_safety_register);

		findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
		findViewById(R.id.waitText).setVisibility(View.INVISIBLE);

		username = (EditText) findViewById(R.id.txtName);
		email = (EditText) findViewById(R.id.txtEmail);

		eemail1 = (EditText) findViewById(R.id.txtEmergencyEmail1);
		eemail2 = (EditText) findViewById(R.id.txtEmergencyEmail2);
		eemail3 = (EditText) findViewById(R.id.txtEmergencyEmail3);

		phone1 = (EditText) findViewById(R.id.txtEmergencyPhone1);
		phone2 = (EditText) findViewById(R.id.txtEmergencyPhone2);
		phone3 = (EditText) findViewById(R.id.txtEmergencyPhone3);

		btnDone = (Button) findViewById(R.id.btnDone);
		btnCancel = (Button) findViewById(R.id.btnCancel);

		if (!SpartaSafetyData.getUsername().equals("Guest")) {
			username.setText(sharedPreferences.getString("username", ""));
			username.setFocusable(false);
			email.setText(sharedPreferences.getString("email", ""));
			eemail1.setText(sharedPreferences.getString("eemail1", ""));
			eemail2.setText(sharedPreferences.getString("eemail2", ""));
			eemail3.setText(sharedPreferences.getString("eemail3", ""));
			phone1.setText(sharedPreferences.getString("phone1", ""));
			phone2.setText(sharedPreferences.getString("phone2", ""));
			phone3.setText(sharedPreferences.getString("phone3", ""));
		}
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activityCompleted(true);
			}
		});

		btnDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				validate();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(eemail1.getWindowToken(), 0);
				JSONObject request = new JSONObject();
				try {
					request.put("userName", username.getText());
					request.put("email", email.getText());
					JSONArray emailArray = new JSONArray();
					if (eemail1.getText().length() > 0) {
						emailArray.put(eemail1.getText().toString());
					}
					if (eemail2.getText().length() > 0) {
						emailArray.put(eemail2.getText().toString());
					}
					if (eemail3.getText().length() > 0) {
						emailArray.put(eemail3.getText().toString());
					}
					request.put("emergencyEmails", emailArray);
					JSONArray phoneArray = new JSONArray();

					if (phone1.getText().length() > 0) {
						phoneArray.put(phone1.getText().toString());
					}
					if (phone2.getText().length() > 0) {
						phoneArray.put(phone2.getText().toString());
					}
					if (phone3.getText().length() > 0) {
						phoneArray.put(phone3.getText().toString());
					}
					request.put("emergencyPhoneNumbers", phoneArray);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
				findViewById(R.id.waitText).setVisibility(View.VISIBLE);
				findViewById(R.id.fields).setVisibility(View.INVISIBLE);
				RegistrationTask task = new RegistrationTask();
				task.execute(request);

			}

			private void validate() {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sparta_safety_register, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuClear:
			username.setText("");
			email.setText("");
			eemail1.setText("");
			eemail2.setText("");
			eemail3.setText("");
			phone1.setText("");
			phone2.setText("");
			phone3.setText("");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class RegistrationTask extends
			AsyncTask<JSONObject, Integer, JSONObject> {

		@Override
		protected JSONObject doInBackground(JSONObject... object) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(getString(R.string.base_url)
					+ getString(R.string.register_url));
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
						new InputStreamReader(is, "iso-8859-1"), 8);
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
