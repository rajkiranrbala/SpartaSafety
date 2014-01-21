package com.sjsu.spartasafety;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import com.sjsu.spartasafety.util.SpartaSafetyData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CategoriesChooser extends Activity {

	ListView lstCategories;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_categories_chooser);
		lstCategories = (ListView) findViewById(R.id.lstCategories);
		findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
		findViewById(R.id.waitText).setVisibility(View.INVISIBLE);
		if (SpartaSafetyData.getCategories() != null) {
			CategoriesArrayAdapter adapter = new CategoriesArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					SpartaSafetyData.getCategories());
			lstCategories.setAdapter(adapter);
			lstCategories.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, final View view,
						int position, long id) {
					final String item = (String) parent
							.getItemAtPosition(position);
					Intent result = new Intent();
					result.putExtra("Category", item);
					setResult(Activity.RESULT_OK, result);
					finish();

				}
			});
		} else {
			refresh();
		}
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
		findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
		findViewById(R.id.waitText).setVisibility(View.VISIBLE);
		CategoriesFetchTask task = new CategoriesFetchTask();
		task.execute();
	}

	private void processResult(JSONObject result) {
		findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
		findViewById(R.id.waitText).setVisibility(View.INVISIBLE);
		findViewById(R.id.lstCategories).setVisibility(View.VISIBLE);
		String errorMessage = "";
		try {
			if (result != null && result.has("result")) {
				if (result.getBoolean("result")) {
					JSONArray array = result.getJSONArray("categories");
					if (array.length() == 0) {
						return;
					}
					ArrayList<String> categoriesAdapter = new ArrayList<String>();
					for (int i = 0; i < array.length(); i++) {
						categoriesAdapter.add(array.getString(i));
					}
					SpartaSafetyData.setCategories(categoriesAdapter);
					CategoriesArrayAdapter adapter = new CategoriesArrayAdapter(
							this, android.R.layout.simple_list_item_1,
							categoriesAdapter);
					lstCategories.setAdapter(adapter);
					lstCategories
							.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										final View view, int position, long id) {
									final String item = (String) parent
											.getItemAtPosition(position);
									Intent result = new Intent();
									result.putExtra("Category", item);
									setResult(Activity.RESULT_OK, result);
									finish();

								}
							});
				} else {
					errorMessage = result.getString("error");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		lstCategories.refreshDrawableState();
	}

	public class CategoriesFetchTask extends
			AsyncTask<String, Long, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... arg0) {

			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					timeoutConnection);
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpGet httpGet = new HttpGet(getString(R.string.base_url)
					+ SpartaSafetyData.getUsername() + "/"
					+ getString(R.string.categories_url));
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
			processResult(result);
		}

	}

	private class CategoriesArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public CategoriesArrayAdapter(Context context, int textViewResourceId,
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
