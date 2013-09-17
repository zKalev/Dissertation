/**
 * 
 */
package bg.tusofia.fktt.zkalev.dessertation;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.DaoMaster;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.DaoMaster.DevOpenHelper;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.DaoSession;
import bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.PhotosDao;

/**
 * @author zkalev
 * 
 */
public class PicturePager extends Activity {
	private static final String TABLE_PHOTO_NAME = "PHOTOS";
	private static final String PHOTO_REFERENCE = "photo_references";
	private static final String REFERENCE = "reference";
	private GooglePlaces googlePlace;
	private ProgressDialog pd;
	private SQLiteDatabase dbPhotos;
	private DevOpenHelper helperPhotos;
	private DaoMaster masterPhotos;
	ViewPager viewPager;
	ImageAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picture_pager);
		Intent i = getIntent();

		boolean isInternetpresent = i.getBooleanExtra("isInternetPresent",
				false);

		if (!isInternetpresent) {
			helperPhotos = new DaoMaster.DevOpenHelper(this, TABLE_PHOTO_NAME,
					null);
			DbImageLoader dbLoader = new DbImageLoader();
			String reference = i.getStringExtra(REFERENCE);
			dbLoader.execute(reference);

		} else {
			String[] photoreference = i.getStringArrayExtra(PHOTO_REFERENCE);
			ImagesLoader il = new ImagesLoader();
			il.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, photoreference);
		}
		viewPager = (ViewPager) findViewById(R.id.view_pager);

	}

	class ImagesLoader extends AsyncTask<String, String, Bitmap[]> {

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(PicturePager.this);
			pd.setMessage("Зареждане на снимки...");
			pd.setIndeterminate(false);
			pd.setCancelable(true);
			pd.show();
		}

		@Override
		protected Bitmap[] doInBackground(String... params) {
			String[] photoreference = params;
			Bitmap[] bitmaps = new Bitmap[photoreference.length];
			googlePlace = new GooglePlaces();
			for (int i = 0; i < photoreference.length; i++) {
				try {
					bitmaps[i] = googlePlace.getPlacePhotos(photoreference[i]);
				} catch (IOException e) {
					Log.e("Грешка", "Проблем със зареждането на снимки");
				}
			}
			adapter = new ImageAdapter(PicturePager.this, bitmaps);

			return bitmaps;
		}

		@Override
		protected void onPostExecute(Bitmap[] result) {
			pd.dismiss();
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					viewPager.setAdapter(adapter);
				}
			});
		}
	}

	class DbImageLoader extends AsyncTask<String, String, Bitmap[]> {

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(PicturePager.this);
			pd.setMessage("Зареждане на снимки...");
			pd.setIndeterminate(false);
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected Bitmap[] doInBackground(String... ref) {
			try {
				dbPhotos = helperPhotos.getWritableDatabase();
				masterPhotos = new DaoMaster(dbPhotos);
				DaoSession session = masterPhotos.newSession();
				PhotosDao pd = session.getPhotosDao();
				List<bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Photos> lpd = pd
						.queryBuilder()
						.where(bg.tusofia.fktt.zkalev.dessertation.db.datamodel.dao.PhotosDao.Properties.Photo_reference
								.eq(ref[0])).list();
				Bitmap[] bitmaps = Utilities.loadImageFromLocal(lpd);
				adapter = new ImageAdapter(PicturePager.this, bitmaps);
				return bitmaps;
			} finally {
				if (helperPhotos != null)
					helperPhotos.close();
				if (dbPhotos != null)
					dbPhotos.close();
			}
		}

		@Override
		protected void onPostExecute(Bitmap[] imageList) {
			pd.dismiss();
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					viewPager.setAdapter(adapter);
				}
			});
		}
	}
}
