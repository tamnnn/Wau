package com.tamatics.wau;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class NearbyMediaFragment extends Fragment {

    public static final String TAG = NearbyMediaFragment.class.getSimpleName();
    // miles
    public static final double MAX_DISTANCE = 0.005;    // 26.4ft

    protected ParseUser mCurrentUser;
    protected GridView mGridView;
    protected List<ParseObject> mMedias;
    protected ArrayList<ParseFile> mFiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFiles = new ArrayList<ParseFile>();
        mCurrentUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MULTIMEDIA);
        query.whereWithinMiles(ParseConstants.KEY_UPLOAD_LOCATION,
                mCurrentUser.getParseGeoPoint(ParseConstants.KEY_LAST_LOCATION),
                MAX_DISTANCE);
        query.orderByDescending(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    mMedias = parseObjects;
                    for (ParseObject media : mMedias) {
                        ParseFile file = (ParseFile) media.get(ParseConstants.KEY_PHOTO);
                        mFiles.add(file);
                    }
                } else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_media, container, false);
        mGridView = (GridView) view.findViewById(R.id.gridview);
        mGridView.setAdapter(new GridViewAdapter(view.getContext(), mFiles));

        return view;
    }
}
