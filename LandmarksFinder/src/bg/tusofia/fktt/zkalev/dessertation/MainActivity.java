package bg.tusofia.fktt.zkalev.dessertation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Photos;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.DaoMaster;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.DaoMaster.DevOpenHelper;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.DaoSession;
import bg.tusofia.fktt.zkalev.dessertation.jsondatamodel.Place;
import bg.tusofia.fktt.zkalev.dessertation.jsondatamodel.PlaceDetails;
import bg.tusofia.fktt.zkalev.dessertation.jsondatamodel.PlacesList;

/**
 * @author zkalev
 * 
 */

public class MainActivity extends Activity {
	private Boolean isInternetPresent;
	private ConnectionDetector cd;
	private AlertDialogManager alert = new AlertDialogManager();
	private GooglePlaces googlePlaces;
	private PlacesList nearPlaces;
	private GPSTracker gps;
	private ProgressDialog pDialog;
	private PlaceDetails placeDetails;
	private SQLiteDatabase dbPlace;
	private DevOpenHelper helperPlace;
	private DaoMaster masterPlace;
	private SQLiteDatabase dbPhotos;
	private DevOpenHelper helperPhotos;
	private DaoMaster masterPhotos;
	private ListView lv;
	ArrayList<Map<String, String>> placesListItems;
	public static String KEY_REFERENCE = "reference";
	public static String KEY_NAME = "name";
	private static final double RADIUS = 5 * 1000;
	private static final String PLACE_TYPE = "museum|art_gallery|zoo|church|aquarium|amusement_park";
	private static final String TABLE_PLACE_NAME = "PLACE";
	private static final String TABLE_PHOTO_NAME = "PHOTOS";
	private static final String PHOTOS_STORAGE_FOLDER_NAME = "LandmarksFinderGalery";
	private Boolean isSDPresent;
	TextView tv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		tv = (TextView) findViewById(R.id.currentLocation);
		cd = new ConnectionDetector(getApplicationContext());
		gps = new GPSTracker(this);
		helperPlace = new DaoMaster.DevOpenHelper(this, TABLE_PLACE_NAME, null);
		helperPhotos = new DaoMaster.DevOpenHelper(this, TABLE_PHOTO_NAME, null);
		dbPlace = helperPlace.getWritableDatabase();
		masterPlace = new DaoMaster(dbPlace);
		dbPhotos = helperPhotos.getWritableDatabase();
		masterPhotos = new DaoMaster(dbPhotos);
		// Check if Internet present
		isInternetPresent = cd.isConnectingToInternet();
		isSDPresent = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (!isInternetPresent) {
			new DBPlaceShower().execute();
		} else {
			File galeryDir = new File(Environment.getDataDirectory()
					+ File.separator + PHOTOS_STORAGE_FOLDER_NAME);
			Utilities.deleteGaleryDir(galeryDir);
			helperPlace.onUpgrade(dbPlace, 1, 2);
			helperPhotos.onUpgrade(dbPhotos, 1, 2);
		    new DBLoader().execute();
			new LoadPlaces().execute();

			if (gps.canGetLocation()) {
				Log.d("Your Location", "latitude:" + gps.getLatitude()
						+ ", longitude: " + gps.getLongitude());
				tv.setText("" + String.format("%.6f", gps.getLatitude()) + ":"
						+ String.format("%.6f", gps.getLongitude()));

			} else {
				// Can't get user's current location
				alert.showAlertDialog(MainActivity.this, "GPS Status",
						"Couldn't get location information. Please enable GPS",
						false);
				// stop executing code by return
				return;
			}
		}
		lv = (ListView) findViewById(R.id.list);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				String reference = ((TextView) view
						.findViewById(R.id.reference)).getText().toString();
				String name = ((TextView) view.findViewById(R.id.name))
						.getText().toString();
				Intent i = new Intent(getApplicationContext(),
						SinglePlaceActivity.class);
				i.putExtra(KEY_REFERENCE, reference);
				i.putExtra(KEY_NAME, name);
				i.putExtra("isInternetPresent", isInternetPresent);
				startActivity(i);

			}
		});

	}

	private class LoadPlaces extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage(Html
					.fromHtml("<b>Търсене</b><br/>Зареждане на околните забележителности..."));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		@Override
		protected String doInBackground(String... args) {
			// creating Places class object
			googlePlaces = new GooglePlaces();

			try {
				// get nearest places
				nearPlaces = googlePlaces.search(gps.getLatitude(),
						gps.getLongitude(), RADIUS, PLACE_TYPE);
			} catch (Exception e) {
				Log.e("Erorr while searching nearest places ", e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();

			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 * */
					// Get json response status
					String status = nearPlaces.status;
					placesListItems = new ArrayList<Map<String, String>>();
					// Check for all possible status
					if (status.equals("OK")) {
						// Successfully got places details
						if (nearPlaces.results != null) {
							// loop through each place
							for (Place p : nearPlaces.results) {
								HashMap<String, String> map = new HashMap<String, String>();
								map.put(KEY_REFERENCE, p.reference);

								map.put(KEY_NAME, p.name);

								placesListItems.add(map);
							}
							ListAdapter adapter = new SimpleAdapter(
									MainActivity.this, placesListItems,
									R.layout.list_item, new String[] {
											KEY_REFERENCE, KEY_NAME },
									new int[] { R.id.reference, R.id.name });

							lv.setAdapter(adapter);
						}
					} else if (status.equals("ZERO_RESULTS")) {

						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Няма намерени забележителности", false);
					} else if (status.equals("UNKNOWN_ERROR")) {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Непозната грешка", false);
					} else if (status.equals("OVER_QUERY_LIMIT")) {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Лимитът на заявки е достигнат", false);
					} else if (status.equals("REQUEST_DENIED")) {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Заявката отказана", false);
					} else if (status.equals("INVALID_REQUEST")) {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Невалидна заявка", false);
					} else {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Грешка", false);
					}
				}
			});
		}
	}

	private class DBLoader extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			googlePlaces = new GooglePlaces();
			try {
				nearPlaces = googlePlaces.search(gps.getLatitude(),
						gps.getLongitude(), RADIUS, PLACE_TYPE);

				DaoSession session = masterPlace.newSession();
				DaoSession photoSession = masterPhotos.newSession();
				String status = nearPlaces.status;
				if (status.equals("OK")) {
					// Successfully got places details
					if (nearPlaces.results != null) {
						try {
							for (Place p : nearPlaces.results) {
								bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place pls = new bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place();
								placeDetails = googlePlaces
										.getPlaceDetails(p.reference);
								pls.setName(placeDetails.result.name);
								pls.setAddres(placeDetails.result.formatted_address);
								pls.setPhone(placeDetails.result.formatted_phone_number);
								pls.setLocation(placeDetails.result.geometry.location.lat
										+ ":"
										+ placeDetails.result.geometry.location.lng);
								pls.setReference(p.reference);

								session.insert(pls);
								StringBuilder sbName = new StringBuilder();
								sbName.append(pls.getName().replaceAll(" ", "")
										.trim());
								List<String> imagesPath = null;
								try {
									imagesPath = Utilities
											.SavePhotoOnFilesSystem(
													placeDetails.result.photos,
													googlePlaces,
													PHOTOS_STORAGE_FOLDER_NAME,
													sbName.toString(),
													isSDPresent);
									System.gc();
									Runtime.getRuntime().gc();
								} catch (Exception e) {
									Log.d("IMAGE PATH", e.getLocalizedMessage());
									e.printStackTrace();
								}
								Photos phs = null;
								for (String path : imagesPath) {
									phs = new Photos();
									phs.setPhoto(path);
									phs.setPhoto_reference(pls.getReference());
									photoSession.insert(phs);
								}

							}
						} finally {
							if (helperPlace != null)
								helperPlace.close();
							if (dbPlace != null)
								dbPlace.close();
							if (helperPhotos != null)
								helperPhotos.close();
							if (dbPhotos != null)
								dbPhotos.close();
						}
					} else if (status.equals("ZERO_RESULTS")) {
						// Zero results found
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Няма намерени забележителности", false);
					} else if (status.equals("UNKNOWN_ERROR")) {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Непозната грешка", false);
					} else if (status.equals("OVER_QUERY_LIMIT")) {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Лимитът на заявки е достигнат", false);
					} else if (status.equals("REQUEST_DENIED")) {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Заявката отказана", false);
					} else if (status.equals("INVALID_REQUEST")) {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Невалидна заявка", false);
					} else {
						alert.showAlertDialog(MainActivity.this, "Грешка",
								"Грешка", false);
					}

				}
			} catch (NullPointerException ex) {
				ex.printStackTrace();
			} catch (Exception e) {
				Log.e("Error while loading data ", e.getMessage());
			}
			return null;
		}
	}

	private class DBPlaceShower
			extends
			AsyncTask<String, String, List<bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage(Html
					.fromHtml("<b>Търсене</b><br/>Зареждане на околните забележителности..."));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected List<bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place> doInBackground(
				String... params) {
			dbPlace = helperPlace.getWritableDatabase();
			masterPlace = new DaoMaster(dbPlace);
			DaoSession ds = masterPlace.newSession();
			List<bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place> pdL;
			pdL = ds.queryBuilder(
					bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place.class)
					.list();
			return pdL;
		}

		@Override
		protected void onPostExecute(
				final List<bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place> result) {
			try {
				runOnUiThread(new Runnable() {
					public void run() {
						pDialog.dismiss();
						placesListItems = new ArrayList<Map<String, String>>();

						for (bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place place : result) {
							Map<String, String> map = new HashMap<String, String>();
							map.put(KEY_REFERENCE, place.getReference());
							map.put(KEY_NAME, place.getName());
							placesListItems.add(map);

						}
						ListAdapter adapter = new SimpleAdapter(
								MainActivity.this, placesListItems,
								R.layout.list_item, new String[] {
										KEY_REFERENCE, KEY_NAME }, new int[] {
										R.id.reference, R.id.name });
						lv.setAdapter(adapter);
					}
				});
			} finally {
				if (helperPlace != null)
					helperPlace.close();
				if (dbPlace != null)
					dbPlace.close();
			}
		}
	}
}
