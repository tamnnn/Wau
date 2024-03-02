package com.tamatics.wau;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.ArrayList;

public class GridViewAdapter extends BaseAdapter {
    public static final String TAG = GridViewAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<ParseFile> mImages;

    public GridViewAdapter(Context c, ArrayList<ParseFile> images) {
        mContext = c;
        mImages = images;
    }

    public int getCount() {
        return mImages.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        // Set thumbnail
        final ParseFile imageFile = mImages.get(position);
        imageFile.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e == null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = false;
                    options.inDither = false;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        // Click handler -> expand image
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send single item click data to ItemViewActivity class
                Intent intent = new Intent(mContext, ItemViewActivity.class);
                // Pass all file data
                intent.putExtra("url", imageFile.getUrl());
                mContext.startActivity(intent);
            }
        });

        return imageView;
    }
}