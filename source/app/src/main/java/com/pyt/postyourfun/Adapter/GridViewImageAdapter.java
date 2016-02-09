package com.pyt.postyourfun.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.pyt.postyourfun.R;
import com.pyt.postyourfun.Utils.Image.BitmapImage;
import com.pyt.postyourfun.Utils.Image.SmartImageView;
import com.pyt.postyourfun.Utils.UsersImageModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simon on 7/18/2015.
 */
public class GridViewImageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<UsersImageModel> image_grid;
    private GridViewImageInterface callback;
    private List<Integer> selectedPosition;

    public GridViewImageAdapter(Context context, ArrayList<UsersImageModel> images, GridViewImageInterface callback) {
        this.context = context;
        this.image_grid = images;
        this.callback = callback;
        selectedPosition = new ArrayList<>();
    }

    public List<Integer> getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(List<Integer> selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Override
    public int getCount() {
        return this.image_grid.size();
    }

    @Override
    public UsersImageModel getItem(int position) {
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

        imageCheck.setOnCheckedChangeListener(null);
        imageCheck.setChecked(selectedPosition.contains(position) ? true : false);
        imageCheck.setTag(position);
        imageCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectedPosition.clear();
                if (isChecked) selectedPosition.add((Integer) buttonView.getTag());
                notifyDataSetChanged();
            }
        });

        File imageFile = new File(getItem(position).getLocalPath());
        if (imageFile.exists())
            imageView.setImage(new BitmapImage(imageFile, 500, 500));
        else
            imageView.setImageUrl(getItem(position).getThumbImageUrl());

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
