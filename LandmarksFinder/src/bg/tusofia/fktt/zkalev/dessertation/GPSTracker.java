package bg.tusofia.fktt.zkalev.dessertation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {

	private final Context mContext;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetExist = false;

	// flag for GPS status
	boolean canGetLocation = false;

	Location location = null;
	double latitude;
	double longitude;

	private static final long DISTANCE_CHANGES_FOR_UPDATE = 10;

	private static final long TIME_PERIOD_FOR_UPDATE = 1000 * 60 * 1; // 1
																		// minute

	protected LocationManager locManager;

	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}

	public Location getLocation() {
		try {
			locManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetExist = locManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetExist) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (isNetExist) {
					locManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							TIME_PERIOD_FOR_UPDATE,
							DISTANCE_CHANGES_FOR_UPDATE, this);
					Log.d("Network", "Network Enabled");
					if (locManager != null) {
						location = locManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								TIME_PERIOD_FOR_UPDATE,
								DISTANCE_CHANGES_FOR_UPDATE, this);
						Log.d("GPS", "GPS Enabled");
						if (locManager != null) {
							location = locManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
