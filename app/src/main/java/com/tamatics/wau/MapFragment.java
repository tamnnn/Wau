package com.tamatics.wau;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapFragment extends Fragment implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = MapFragment.class.getSimpleName();

    private ParseUser mCurrentUser;
    private GoogleApiClient mGoogleApiClient;
    private ParseGeoPoint mLastGeoPoint;
    private LocationManager mLocationManager;
    private List<ParseObject> mLocationRequests;
    private String mLocationProvider;
    private GoogleMap mMap;
    private MapView mMapView;
    private HashMap<String, Marker> mFriendMarkers;

    // milliseconds
    private static final long MIN_TIME = 30000;     // 30seconds
    private static final long MAX_TIME = 180000;    // 3min
    // meters
    private static final float MIN_DISTANCE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFriendMarkers = new HashMap<String, Marker>();
        mCurrentUser = ParseUser.getCurrentUser();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // create LocationManager based on GPS or Network availability; for location updates
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationProvider = LocationManager.GPS_PROVIDER;
        } else {
            mLocationProvider = LocationManager.NETWORK_PROVIDER;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        MapsInitializer.initialize(getActivity());

        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // display user location
                mMap = googleMap;
                mMap.setMyLocationEnabled(true);
                // display friend locations
                queryParseAndUpdateFriendLocations();
            }
        });

        return view;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.

        // find the user location
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // save user location in Parse
        mLastGeoPoint = new ParseGeoPoint(latLng.latitude, latLng.longitude);
        mCurrentUser.put(ParseConstants.KEY_LAST_LOCATION, mLastGeoPoint);
        mCurrentUser.saveInBackground();

        // zoom to occupied vicinity
        findMidPointAndZoom();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        // save user location in Parse
        mLastGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        mCurrentUser.put(ParseConstants.KEY_LAST_LOCATION, mLastGeoPoint);
        mCurrentUser.saveInBackground();

        queryParseAndUpdateFriendLocations();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // if GPS is turned on, switch to it
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            mLocationProvider = LocationManager.GPS_PROVIDER;
            mLocationManager.requestLocationUpdates(mLocationProvider, MIN_TIME, MIN_DISTANCE, this);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // if GPS is turned off, switch to network provider
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            mLocationProvider = LocationManager.NETWORK_PROVIDER;
            mLocationManager.requestLocationUpdates(mLocationProvider, MIN_TIME, MIN_DISTANCE, this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
        // reduce rate of location update
        mLocationManager.requestLocationUpdates(mLocationProvider, MAX_TIME, MIN_DISTANCE, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mGoogleApiClient.connect();
        mLocationManager.requestLocationUpdates(mLocationProvider, MIN_TIME, MIN_DISTANCE, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    // Updates accepted friend requests and corresponding Map markers
    private void updateFriendLocations() {
        // clear all markers off Map
        mMap.clear();

        for (ParseObject locationRequest : mLocationRequests) {
            final String friendId;

            if (locationRequest.get(ParseConstants.KEY_SENDER).equals(mCurrentUser.getObjectId())) {
                friendId = locationRequest.getString(ParseConstants.KEY_RECIPIENT);
            } else {
                friendId = locationRequest.getString(ParseConstants.KEY_SENDER);
            }

            // Check TTL
            long timeElapsed = System.currentTimeMillis() -
                    locationRequest.getLong(ParseConstants.KEY_TIME_ACCEPTED);
            if (timeElapsed > locationRequest.getInt(ParseConstants.KEY_TTL)) {
                locationRequest.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        mFriendMarkers.remove(friendId);
                    }
                });
            } else {
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo(ParseConstants.KEY_ID, friendId);
                query.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser friend, ParseException e) {
                        addFriendMarker(friend);
                    }
                });
            }
        }
    }

    // Populates mLocationRequests with all ACCEPTED location requests corresponding to mCurrentUser
    private void queryParseAndUpdateFriendLocations() {
        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

        // Query where the sender is Current User
        ParseQuery<ParseObject> queryCurUserSender =
                ParseQuery.getQuery(ParseConstants.CLASS_LOCATION_REQUEST);
        queryCurUserSender.whereEqualTo(ParseConstants.KEY_SENDER, mCurrentUser.getObjectId());
        queryCurUserSender.whereEqualTo(ParseConstants.KEY_ACCEPTED, true);

        // Query where the recipient is Current User
        ParseQuery<ParseObject> queryCurUserRecipient =
                ParseQuery.getQuery(ParseConstants.CLASS_LOCATION_REQUEST);
        queryCurUserRecipient.whereEqualTo(ParseConstants.KEY_RECIPIENT,
                mCurrentUser.getObjectId());
        queryCurUserRecipient.whereEqualTo(ParseConstants.KEY_ACCEPTED, true);

        queries.add(queryCurUserSender);
        queries.add(queryCurUserRecipient);

        // Find BOTH queries
        ParseQuery<ParseObject> queryLocationRequests = ParseQuery.or(queries);
        queryLocationRequests.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> locationRequests, ParseException e) {
                if (e == null) {
                    mLocationRequests = locationRequests;
                    updateFriendLocations();
                } else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    private void addFriendMarker(ParseUser friend) {
        ParseGeoPoint geoPoint = friend.getParseGeoPoint(ParseConstants.KEY_LAST_LOCATION);
        String name = friend.getString(ParseConstants.KEY_NAME);

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()))
                .title(name));
        mFriendMarkers.put(friend.getObjectId(), marker);
    }


    private void findMidPointAndZoom(){
        LatLng loc = new LatLng(mLastGeoPoint.getLatitude(), mLastGeoPoint.getLongitude());

        if(mFriendMarkers.size() == 0) {
            loc = new LatLng(mLastGeoPoint.getLatitude(), mLastGeoPoint.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
        }
        else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : mFriendMarkers.values()) {
                builder.include(marker.getPosition());
            }
            // Add yourself to the midpoint calculation
            builder.include(loc);
            LatLngBounds bounds = builder.build();

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }
}