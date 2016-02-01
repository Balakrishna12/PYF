package com.pyt.postyourfun.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.pyt.postyourfun.R;
import com.pyt.postyourfun.constants.Constants;
import com.pyt.postyourfun.dynamoDBClass.ImageMapper;
import com.pyt.postyourfun.dynamoDBClass.ImageQueryMapper;
import com.pyt.postyourfun.dynamoDBManager.tableTasks.ImageDBManager;
import com.pyt.postyourfun.dynamoDBManager.tableTasks.ImageQueryDBManager;

/**
 * Created by r8tin on 2/1/16.
 */
public class ShowImageActivity extends Activity {

	private String full_image_url = "";
	private String thumbnail_image_url = "";
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_all_image);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Loading data...");
		progressDialog.setCancelable(false);
		progressDialog.show();




	}

	protected class GetImageQuery extends AsyncTask<String, Void, ImageQueryMapper> {
		@Override
		protected ImageQueryMapper doInBackground(String... params) {

			ImageQueryDBManager.sharedInstance(ShowImageActivity.this);
			ImageQueryMapper result_image = ImageQueryDBManager.getImage(params[0], params[1]);
			return result_image;
		}

		@Override
		protected void onPostExecute(ImageQueryMapper imageMapper) {
			super.onPostExecute(imageMapper);
			if (imageMapper != null) {
				new GetImage().execute(imageMapper.getImage_id());
			}
		}
	}

	protected class GetImage extends AsyncTask<String, Void, ImageMapper> {

		@Override
		protected ImageMapper doInBackground(String... params) {
			ImageDBManager.sharedInstance(ShowImageActivity.this);
			ImageMapper result = ImageDBManager.getImage(params[0]);
			return result;
		}

		@Override
		protected void onPostExecute(ImageMapper imageMapper) {
			super.onPostExecute(imageMapper);
			if (imageMapper != null) {
				thumbnail_image_url = Constants.IMAGE_CONSTANT_URL + imageMapper.getRegion() + ".thumbs/tn_" + imageMapper.getImageName();
				Log.d("Image URL:", Constants.IMAGE_CONSTANT_URL + imageMapper.getRegion() + ".thumbs/tn_" + imageMapper.getImageName());
				//TODO
//				image_thumb.setImageUrl(thumbnail_image_url);
				full_image_url = Constants.IMAGE_CONSTANT_URL + imageMapper.getRegion() + ".pictures/" + imageMapper.getImageName();
			}
		}
	}
}
