package bg.tusofia.fktt.zkalev.dessertation;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

public class ImageAdapter extends PagerAdapter {

	Context context;
	Bitmap[] images;
	ProgressDialog pd;

	public ImageAdapter(Context context, Bitmap[] images) {
		this.context = context;
		this.images = images;
	}

	@Override
	public int getCount() {

		return images.length;
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view == (ImageView) obj;
	}

	@Override
	public Object instantiateItem(View container, int position) {
		ImageView imageView = new ImageView(context);
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		imageView.setImageBitmap(images[position]);
		((ViewPager) container).addView(imageView, 0);
		return imageView;

	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((ImageView) object);
	}

}
