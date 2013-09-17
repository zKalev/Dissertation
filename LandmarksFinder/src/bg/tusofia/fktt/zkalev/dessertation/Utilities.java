package bg.tusofia.fktt.zkalev.dessertation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import bg.tusofia.fktt.zkalev.dessertation.jsondatamodel.Place.Photos;

public class Utilities {
	public static List<Bitmap> takePhotos(GooglePlaces googlePlaces,
			String... photoRefArr) {

		List<Bitmap> bmL = new ArrayList<Bitmap>();
		if (photoRefArr == null || photoRefArr.length == 0)
			return bmL;
		for (String phRef : photoRefArr) {
			try {
				bmL.add(googlePlaces.getPlacePhotos(phRef));
			} catch (IOException e) {

				Log.e("Грешка", "Проблем със зареждането на снимки");
			}
		}
		return bmL;
	}

	public static String[] takePhotoRefs(Photos[] photos) {
		ArrayList<String> photoRefs = new ArrayList<String>();
		for (Photos photo : photos) {
			photoRefs.add(photo.photo_reference);
		}
		String[] arrStr = new String[photoRefs.size()];
		arrStr = photoRefs.toArray(arrStr);
		return arrStr;
	}

	public static List<String> SavePhotoOnFilesSystem(Photos[] photos,
			GooglePlaces gp, String storageFolderName, String fileName,
			boolean isSDMounted) throws IOException {
		List<String> pathToImages = new ArrayList<String>();
		if (photos == null || photos.length == 0) {
			return pathToImages;
		} else {
			try {
				String[] list = takePhotoRefs(photos);
				List<Bitmap> imageList = takePhotos(gp, list);

				File photoDir = null;
				if (isSDMounted) {
					photoDir = new File(
							Environment.getExternalStorageDirectory()
									+ File.separator + storageFolderName);
				} else {
					photoDir = new File(Environment.getDataDirectory()
							+ File.separator + storageFolderName);
				}
				if (!photoDir.exists()) {
					photoDir.mkdirs();
				}
				if (photoDir.exists()) {
					int count = 0;
					File file = null;
					for (Bitmap b : imageList) {
						FileOutputStream fo = null;
						ByteArrayOutputStream bytes = null;
						try {
							bytes = new ByteArrayOutputStream();
							b.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
							file = new File(photoDir.getAbsolutePath()
									+ File.separator + fileName + count
									+ ".jpg");
							file.createNewFile();
							fo = new FileOutputStream(file);
							fo.write(bytes.toByteArray());
							fo.flush();
							pathToImages.add(file.getAbsolutePath());
							count++;
						} finally {
							if (fo != null)
								fo.close();
							if (bytes != null)
								bytes.close();
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("UTILY error", e.getMessage());
			}
		}
		return pathToImages;
	}

	public static Bitmap[] loadImageFromLocal(
			List<bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Photos> imageList) {
		List<Bitmap> bitmapList = new ArrayList<Bitmap>();
		for (bg.tusofia.fktt.zkalev.dessertation.db.datamodel.Photos ph : imageList) {
			Bitmap bitmap = BitmapFactory.decodeFile(ph.getPhoto());
			bitmapList.add(bitmap);
		}
		Bitmap[] bitArr = new Bitmap[bitmapList.size()];
		return bitmapList.toArray(bitArr);

	}

	public static void deleteGaleryDir(File dir) {
		if (dir != null) {
			if (dir.isDirectory()) {
				String[] children = dir.list();
				for (String file : children) {
					File child = new File(dir, file);
					if (child.isDirectory()) {
						deleteGaleryDir(child);
						child.delete();
					} else {
						child.delete();

					}
				}
				dir.delete();
			}
		} else {
			Log.d("Utilities.deleteGaleryDir", "Folder or file doas not exist");
		}
	}

}
