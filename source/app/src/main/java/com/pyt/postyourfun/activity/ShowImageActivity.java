package com.pyt.postyourfun.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.pyt.postyourfun.Adapter.GridViewShowAllAdapter;
import com.pyt.postyourfun.Image.ImageDownloadMangerInterface;
import com.pyt.postyourfun.Payment.PaymentController;
import com.pyt.postyourfun.R;
import com.pyt.postyourfun.Utils.UserImageSQLiteHelper;
import com.pyt.postyourfun.Utils.UsersImageModel;
import com.pyt.postyourfun.constants.Constants;
import com.pyt.postyourfun.dynamoDBClass.ImageMapper;
import com.pyt.postyourfun.dynamoDBClass.ImageQueryMapper;
import com.pyt.postyourfun.dynamoDBManager.tableTasks.ImageDBManager;
import com.pyt.postyourfun.dynamoDBManager.tableTasks.ImageQueryDBManager;
import com.pyt.postyourfun.dynamoDBManager.tableTasks.UserImageDBmanager;

import java.util.ArrayList;
import java.util.List;

import static com.pyt.postyourfun.constants.PostYourFunApp.createGUID;
import static com.pyt.postyourfun.constants.PostYourFunApp.getCurrentTimDate;

/**
 * Created by r8tin on 2/1/16.
 */
public class ShowImageActivity extends Activity implements ImageDownloadMangerInterface {

	public static final String EXTRA_DEVICE_ID = "extra_device_id";

	private ProgressDialog progressDialog;
	private GridView listView;
	private ImageView imageView;
	private Button buyButton;
	private UserImageSQLiteHelper dbHelper;
	private String userId;
	private SharedPreferences _sharedPreference;
	private String thumbnailSelect = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_all_image);

		_sharedPreference = getSharedPreferences("user_info", 0);
		userId = _sharedPreference.getString("user_id", "");
		dbHelper = new UserImageSQLiteHelper(this);

		listView = (GridView) findViewById(R.id.grid);
		imageView = (ImageView) findViewById(R.id.show_image);
		buyButton = (Button) findViewById(R.id.buy_button);

		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				imageView.setVisibility(View.GONE);
				buyButton.setVisibility(View.GONE);
			}
		});

		buyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageWrapper wrapper = (ImageWrapper) v.getTag();
				if (wrapper != null && !TextUtils.isEmpty(wrapper.getFull_image_url())) {
					PaymentController.sharedInstance().buyImage(ShowImageActivity.this, 8.0f, "EUR", wrapper.getFull_image_url(), ShowImageActivity.this);
				} else {
					Toast.makeText(ShowImageActivity.this, "Please get image first.", Toast.LENGTH_SHORT).show();
				}
			}
		});

		initImageLoader(getApplicationContext());
		new GetImageQuery().execute(getIntent().getStringExtra(EXTRA_DEVICE_ID));
	}

	@Override
	public void onSuccessImageDownload(Boolean isSuccess, String image_Url) {
		String transactionId = createGUID();
		String imageId = image_Url.substring(image_Url.lastIndexOf("/") + 1, image_Url.length() - 4);
		String dateTime = getCurrentTimDate(System.currentTimeMillis(), "dd.MM.yyyy");

		new InsertTransaction().execute(transactionId, userId, imageId, image_Url, dateTime);

		UsersImageModel imageModel = new UsersImageModel();
		imageModel.setTransactionId(transactionId);
		imageModel.setUserId(userId);
		imageModel.setImageId(imageId);
		imageModel.setImageUrl(image_Url);
		imageModel.setDateTime(dateTime);
		imageModel.setThumbImageUrl(thumbnailSelect);
		dbHelper.addImage(imageModel);

		ArrayList<UsersImageModel> result = new ArrayList<>();
		result = dbHelper.getAllImages();
		Log.d("SQLite Confirm: ", String.valueOf(result.size()));
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
		config.diskCache(new UnlimitedDiskCache(context.getCacheDir()));
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
		ImageLoader.getInstance().init(config.build());
	}

	private void showProgressBur() {
		try {
			if (progressDialog == null) progressDialog = new ProgressDialog(ShowImageActivity.this);
			progressDialog.setMessage("Loading data...");
			progressDialog.setCancelable(false);
			progressDialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void hideProgressBur() {
		try {
			if (progressDialog != null) progressDialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected class GetImageQuery extends AsyncTask<String, Void, List<ImageQueryMapper>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressBur();
		}

		@Override
		protected List<ImageQueryMapper> doInBackground(String... params) {

			ImageQueryDBManager.sharedInstance(ShowImageActivity.this);
			return ImageQueryDBManager.getImage(params[0]);
		}

		@Override
		protected void onPostExecute(List<ImageQueryMapper> mappers) {
			super.onPostExecute(mappers);
			if (mappers != null && !mappers.isEmpty()) {
				new GetImage().execute(mappers);
			} else hideProgressBur();
		}
	}

	protected class GetImage extends AsyncTask<List<ImageQueryMapper>, Void, List<ImageMapper>> {

		@SafeVarargs
		@Override
		protected final List<ImageMapper> doInBackground(List<ImageQueryMapper>... params) {
			ImageDBManager.sharedInstance(ShowImageActivity.this);
			List<ImageMapper> imageMappers = new ArrayList<>();
			for (ImageQueryMapper imageQueryMapper : params[0])
				imageMappers.add(ImageDBManager.getImage(imageQueryMapper.getImage_id()));
			return imageMappers;
		}

		@Override
		protected void onPostExecute(List<ImageMapper> imageMappers) {
			super.onPostExecute(imageMappers);
			hideProgressBur();
			if (imageMappers != null && !imageMappers.isEmpty()) {
				List<ImageWrapper> imageWrappers = new ArrayList<>();
				for (ImageMapper imageMapper : imageMappers)
					imageWrappers.add(new ImageWrapper(imageMapper));
				GridViewShowAllAdapter adapter = new GridViewShowAllAdapter(ShowImageActivity.this, imageWrappers);
				adapter.setOnItemClickListener(new GridViewShowAllAdapter.OnItemClickListener() {
					@Override
					public void itemClick(ImageWrapper item) {
						imageView.setVisibility(View.VISIBLE);
						buyButton.setVisibility(View.VISIBLE);
						ImageLoader.getInstance().displayImage(item.getThumbnail_image_url(), imageView);
						buyButton.setTag(item);
						thumbnailSelect = item.getThumbnail_image_url();
					}
				});
				listView.setAdapter(adapter);
			}
		}
	}

	public static class ImageWrapper {
		private String thumbnail_image_url;
		private String full_image_url;

		public ImageWrapper(ImageMapper imageMapper) {
			thumbnail_image_url = Constants.IMAGE_CONSTANT_URL + imageMapper.getRegion() + ".thumbs/tn_" + imageMapper.getImageName();
			full_image_url = Constants.IMAGE_CONSTANT_URL + imageMapper.getRegion() + ".pictures/" + imageMapper.getImageName();
		}

		public String getThumbnail_image_url() {
			return thumbnail_image_url;
		}

		public String getFull_image_url() {
			return full_image_url;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ImageWrapper that = (ImageWrapper) o;

			if (thumbnail_image_url != null ? !thumbnail_image_url.equals(that.thumbnail_image_url) : that.thumbnail_image_url != null) return false;
			return !(full_image_url != null ? !full_image_url.equals(that.full_image_url) : that.full_image_url != null);
		}

		@Override
		public int hashCode() {
			int result = thumbnail_image_url != null ? thumbnail_image_url.hashCode() : 0;
			result = 31 * result + (full_image_url != null ? full_image_url.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "ImageWrapper{" +
					"thumbnail_image_url='" + thumbnail_image_url + '\'' +
					", full_image_url='" + full_image_url + '\'' +
					'}';
		}
	}

	protected class InsertTransaction extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			UserImageDBmanager.sharedInstance(ShowImageActivity.this);
			UserImageDBmanager.insertUserImage(params[0], params[1], params[2], params[3], params[4]);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
		}
	}
}
