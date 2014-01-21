package com.sjsu.spartasafety.util;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SpartaSafetyData {

	private static String username;

	private static long startDate, endDate;

	private static ArrayList<String> categories;

	public static long getStartDate() {
		return startDate;
	}

	public static void setStartDate(long startDate) {
		SpartaSafetyData.startDate = startDate;
	}

	public static long getEndDate() {
		return endDate;
	}

	public static void setEndDate(long endDate) {
		SpartaSafetyData.endDate = endDate;
	}

	public static String getUsername() {
		if (username == null) {
			username = "Guest";
		}
		return username;
	}

	public static void loadSharedPreferences(Activity activity) {
		SharedPreferences sharedPreferences = activity.getSharedPreferences(
				"com.sjsu.spartasafety.RegistrationData", Context.MODE_PRIVATE);
		username = sharedPreferences.getString("username", null);
	}

	public static ArrayList<String> getCategories() {
		return categories;
	}

	public static void setCategories(ArrayList<String> categories) {
		SpartaSafetyData.categories = categories;
	}

}
