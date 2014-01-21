package com.sjsu.spartasafety;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class Application extends android.app.Application {

	public Application() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Initialize the Parse SDK.
		Parse.initialize(this, "1DTHSmVgssKMXjW9lAfAmvPys0tKIf91We470yiw",
				"Gs5xzLkyzuHZ5AFXj9Z1JBwi2sKM903FVeI9m1C2");

		// Specify a Activity to handle all pushes by default.
		PushService.setDefaultPushCallback(this, Safety.class);

		// Save the current installation.
		ParseInstallation.getCurrentInstallation().saveInBackground();
	}
}