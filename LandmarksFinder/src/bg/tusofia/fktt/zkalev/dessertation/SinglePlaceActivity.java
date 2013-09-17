package bg.tusofia.fktt.zkalev.dessertation;

import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.DaoMaster;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.DaoMaster.DevOpenHelper;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.DaoSession;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.PhotosDao;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.PlaceDao;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.PlaceDao.Properties;
import bg.tusofia.fktt.zkalev.dessertation.jsondatamodel.Place;
import bg.tusofia.fktt.zkalev.dessertation.jsondatamodel.PlaceDetails;

public class SinglePlaceActivity extends Activity {
	// flag for Internet connection status
	Boolean isInternetPresent = false;
	AlertDialogManager alert = new AlertDialogManager();
	GooglePlaces googlePlaces;
	PlaceDetails placeDetails;

	// Progress dialog
	ProgressDialog pDialog;
	private SQLiteDatabase db;
	private DevOpenHelper helper;
	private DaoMaster master;
	Place place;
	Button showPhotos;
	String[] photoRefList;
	private static final String DATABASE_NAME = "PLACE";
	// KEY Strings
	public static String KEY_REFERENCE = "reference";
	public static String PHOTO_REFERENCE = "photo_reference";
	private TextView placaName;
	private TextView placeAddress;
	private TextView placePhone;
	private TextView placeLocation;
	private DevOpenHelper helperPhotos;
	private SQLiteDatabase dbPhotos;
	private DaoMaster masterPhotos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_place);

		showPhotos = (Button) findViewById(R.id.showPhotosBtn);
		helper = new DaoMaster.DevOpenHelper(this, DATABASE_NAME, null);
		placaName = (TextView) findViewById(R.id.name);
		placeAddress = (TextView) findViewById(R.id.address);
		placePhone = (TextView) findViewById(R.id.phone);
		placeLocation = (TextView) findViewById(R.id.location);
		Intent i = getIntent();
		final String reference = i.getStringExtra(KEY_REFERENCE);
		helperPhotos = new DaoMaster.DevOpenHelper(this, "PHOTOS", null);
		String name = i.getStringExtra("name");
		isInternetPresent = i.getBooleanExtra("isInternetPresent", false);
		if (!isInternetPresent) {
			new LoadSingleDBPlaceDetails().execute(name, reference);

		} else {
			new LoadSinglePlaceDetails().execute(reference);
		}
		showPhotos.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(),
						PicturePager.class);
				i.putExtra("photo_references", photoRefList);
				i.putExtra("isInternetPresent", isInternetPresent);
				i.putExtra("reference", reference);
				startActivity(i);

			}

		});

	}

	private class LoadSinglePlaceDetails extends
			AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(SinglePlaceActivity.this);
			pDialog.setMessage("Зареждам информация за обекта...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Profile JSON
		 * */
		protected String doInBackground(String... args) {
			String reference = args[0];
			
			googlePlaces = new GooglePlaces();

			try {
				placeDetails = googlePlaces.getPlaceDetails(reference);
				photoRefList = Utilities
						.takePhotoRefs(placeDetails.result.photos);
			} catch (IOException e) {
				
				e.printStackTrace();
			} catch (Exception e) {
				
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			if (photoRefList == null || photoRefList.length == 0)
				showPhotos.setVisibility(TRIM_MEMORY_UI_HIDDEN);

			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 * */
					if (placeDetails != null) {
						String status = placeDetails.status;

						// Check for all possible status
						if (status.equals("OK")) {
							if (placeDetails.result != null) {
								String name = placeDetails.result.name;
								String address = placeDetails.result.formatted_address;
								String phone = placeDetails.result.formatted_phone_number;
								String latitude = Double
										.toString(placeDetails.result.geometry.location.lat);
								String longitude = Double
										.toString(placeDetails.result.geometry.location.lng);
								// Displaying all the details in the view
								// single_place.xml
								name = name == null ? "Не е предтавено" : name; 
								
								address = address == null ? "Не е предтавен"
										: address;
								phone = phone == null ? "Не е предтавен"
										: phone;
								latitude = latitude == null ? "Не е предтавен"
										: latitude;
								longitude = longitude == null ? "" : longitude;
								placaName.setText(name);
								placeAddress.setText(address);
								placePhone.setText(Html
										.fromHtml("<b>Teлефон:</b> " + phone));
								placeLocation.setText(Html
										.fromHtml("<b>Геолокация:</b> "
												+ latitude + ":" + longitude));

							}
						} else if (status.equals("ZERO_RESULTS")) {
							alert.showAlertDialog(SinglePlaceActivity.this,
									"Near Places", "Sorry no place found.",
									false);
						} else if (status.equals("UNKNOWN_ERROR")) {
							alert.showAlertDialog(SinglePlaceActivity.this,
									"Places Error",
									"Sorry unknown error occured.", false);
						} else if (status.equals("OVER_QUERY_LIMIT")) {
							alert.showAlertDialog(
									SinglePlaceActivity.this,
									"Places Error",
									"Sorry query limit to google places is reached",
									false);
						} else if (status.equals("REQUEST_DENIED")) {
							alert.showAlertDialog(SinglePlaceActivity.this,
									"Places Error",
									"Sorry error occured. Request is denied",
									false);
						} else if (status.equals("INVALID_REQUEST")) {
							alert.showAlertDialog(SinglePlaceActivity.this,
									"Places Error",
									"Sorry error occured. Invalid Request",
									false);
						} else {
							alert.showAlertDialog(SinglePlaceActivity.this,
									"Places Error", "Sorry error occured.",
									false);
						}
					} else {
						alert.showAlertDialog(SinglePlaceActivity.this,
								"Places Error", "Sorry error occured.", false);
					}
				}
			});
		}
	}

	private class LoadSingleDBPlaceDetails
			extends
			AsyncTask<String, String, bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place> {
		List<bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Photos> lpd;

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(SinglePlaceActivity.this);
			pDialog.setMessage("Зареждам информация за обекта...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place doInBackground(
				String... name) {
			bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place place = null;
			try {
				db = helper.getWritableDatabase();
				master = new DaoMaster(db);
				DaoSession ds = master.newSession();
				dbPhotos = helperPhotos.getReadableDatabase();
				masterPhotos = new DaoMaster(dbPhotos);
				DaoSession session = masterPhotos.newSession();
				PhotosDao photo = session.getPhotosDao();

				PlaceDao ph = ds.getPlaceDao();
				List<bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place> listPlace = ph
						.queryBuilder().where(Properties.Name.eq(name[0]))
						.list();
				place = listPlace.get(0);

				lpd = photo
						.queryBuilder()
						.where(bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.PhotosDao.Properties.Photo_reference
								.eq(name[1])).list();

			} finally {
				if (helper != null)
					helper.close();
				if (db != null)
					db.close();
				if (helperPhotos != null)
					helperPhotos.close();
				if (dbPhotos != null)
					dbPhotos.close();
			}
			return place;
		}

		@Override
		protected void onPostExecute(
				final bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Place result) {
			if (lpd == null || lpd.size() == 0)
				showPhotos.setVisibility(TRIM_MEMORY_UI_HIDDEN);
			pDialog.dismiss();
			try {
				runOnUiThread(new Runnable() {
					public void run() {
						String name = result.getName();
						String address = result.getAddres();
						String phone = result.getPhone();
						String location = result.getLocation();
						placaName.setText(name);
						placeAddress.setText(address);
						placePhone.setText(Html.fromHtml("<b>Teлефон:</b> "
								+ phone));
						placeLocation.setText(Html
								.fromHtml("<b>Геолокация: </b> " + location));

					}
				});
			} finally {
				if (helper != null)
					helper.close();
				if (db != null)
					db.close();
			}
		}

	}
}
