package com.rs.tzfe;

import java.util.HashMap;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class MyApplication extends Application {

	// The following line should be changed to include the correct property id.
	private static final String PROPERTY_ID = "UX-XXXXXXXX-X";

	// Logging TAG
	private static final String TAG = "MyApp";

	public static int GENERAL_TRACKER = 0;

	public enum TrackerName {
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
						// roll-up tracking.
		ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a
							// company.
	}

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	public MyApplication() {
		super();
	}

	synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics
					.newTracker(R.xml.app_tracker)
					: (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics
							.newTracker(PROPERTY_ID) : null;
			mTrackers.put(trackerId, t);

		}
		return mTrackers.get(trackerId);
	}
}