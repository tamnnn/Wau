package com.tamatics.wau;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class ItemViewActivity extends Activity {

    String url;
    ImageLoader imageLoader = new ImageLoader(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from activity_item_view.xml
        setContentView(R.layout.activity_item_view);

        Intent i = getIntent();
        // Get the intent from ListViewAdapter
        url = i.getStringExtra("url");

        // Locate the ImageView in activity_item_view.xml
        ImageView imageView = (ImageView) findViewById(R.id.item);

        // Load image into the ImageView
        imageLoader.DisplayImage(url, imageView);
    }
}