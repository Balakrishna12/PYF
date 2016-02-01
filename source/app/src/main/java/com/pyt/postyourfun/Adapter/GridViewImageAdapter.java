package com.pyt.postyourfun.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.google.android.gms.plus.model.people.Person;
import com.pyt.postyourfun.R;
import com.pyt.postyourfun.Utils.Image.SmartImage;
import com.pyt.postyourfun.Utils.Image.SmartImageView;

import java.util.ArrayList;

/**
 * Created by Simon on 7/18/2015.
 */
public class GridViewImageAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<String> image_grid;
	private GridViewImageInterface callback;

	public GridViewImageAdapter(Context context, ArrayList<String> images, GridViewImageInterface callback) {
		this.context = context;
		this.image_grid = images;
		this.callback = callback;
	}

	@Override
	public int getCount() {
		return this.image_grid.size();
	}

	@Override
	public String getItem(int position) {
		return this.image_grid.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (view == null) {
			view = inflater.inflate(R.layout.grid_cell, null);
		}

		SmartImageView imageView = (SmartImageView) view.findViewById(R.id.image_element);
		CheckBox imageCheck = (CheckBox) view.findViewById(R.id.image_check);

		final String imageUrl = image_grid.get(position);
		imageView.setImageUrl(imageUrl);

		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (callback != null) {
					callback.onClickedImage(v, position);
				}
			}
		});

		return view;
	}
}
