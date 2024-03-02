package com.tamatics.wau;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FriendsFragment extends ListFragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected List<ParseObject> mLocationRequests;
    protected String[] mSecondaryTexts;
    // milliseconds
    protected int mLocationTTL = 300000; // 5min

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setProgressBarIndeterminateVisibility(true);

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        queryParseAndInitializeFriendList();
    }

    // Populates mLocationRequests with all location requests corresponding to mCurrentUser,
    // and then proceeds to initialize the friends list.
    private void queryParseAndInitializeFriendList() {
        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

        // Query where the Sender is current user
        ParseQuery<ParseObject> queryCurUserSender =
                ParseQuery.getQuery(ParseConstants.CLASS_LOCATION_REQUEST);
        queryCurUserSender.whereEqualTo(ParseConstants.KEY_SENDER, mCurrentUser.getObjectId());

        // Query where the Recipient is current user
        ParseQuery<ParseObject> queryCurUserRecipient =
                ParseQuery.getQuery(ParseConstants.CLASS_LOCATION_REQUEST);
        queryCurUserRecipient.whereEqualTo(ParseConstants.KEY_RECIPIENT,
                mCurrentUser.getObjectId());

        queries.add(queryCurUserSender);
        queries.add(queryCurUserRecipient);

        // OR both queries
        ParseQuery<ParseObject> queryLocationRequests = ParseQuery.or(queries);
        queryLocationRequests.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> locationRequests, ParseException e) {
                if (e == null) {
                    mLocationRequests = locationRequests;
                    initializeFriendList();
                } else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    private void initializeFriendList() {
        ParseQuery<ParseUser> queryFriendsRelation = mFriendsRelation.getQuery();
        queryFriendsRelation.addAscendingOrder(ParseConstants.KEY_NAME);
        queryFriendsRelation.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e == null) {
                    mFriends = friends;

                    assignSecondaryTexts();

                    ArrayAdapter<ParseUser> adapter = new ArrayAdapter<ParseUser>(
                            getListView().getContext(),
                            R.layout.simple_list_item_2_custom,
                            android.R.id.text1,
                            mFriends) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                            text1.setText(mFriends.get(position).get(ParseConstants.KEY_NAME)
                                    .toString());
                            text2.setText(mSecondaryTexts[position]);

                            return view;
                        }
                    };
                    setListAdapter(adapter);
                } else {
                    Toast.makeText(getListView().getContext(),
                            "Woops! Retrieving friends list failed...",
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    // Assign string values in mSecondaryTexts for every mFriends
    private void assignSecondaryTexts() {
        mSecondaryTexts = new String[mFriends.size()];
        int i = 0;
        for (ParseUser friend : mFriends) {
            mSecondaryTexts[i] = getResources().getString(R.string.text_where_are_u);

            // Skip if there are no location requests
            if (mLocationRequests != null) {
                Iterator<ParseObject> itr = mLocationRequests.iterator();
                while(itr.hasNext()) {
                    ParseObject locationRequest = itr.next();
                    if (locationRequest.get(ParseConstants.KEY_ACCEPTED).equals(true) &&
                            (locationRequest.get(ParseConstants.KEY_RECIPIENT)
                                    .equals(friend.getObjectId()) ||
                            locationRequest.get(ParseConstants.KEY_SENDER)
                                    .equals(friend.getObjectId()))) {
                        // sharing location
                        mSecondaryTexts[i] = getResources()
                                .getString(R.string.text_sharing_location);
                        itr.remove();
                        break;
                    } else if (locationRequest.get(ParseConstants.KEY_RECIPIENT)
                            .equals(friend.getObjectId())) {
                        // pending location request TO friend
                        mSecondaryTexts[i] = getResources()
                                .getString(R.string.text_request_pending);
                        itr.remove();
                        break;
                    } else if (locationRequest.get(ParseConstants.KEY_SENDER)
                            .equals(friend.getObjectId())) {
                        // pending location request FROM friend
                        mSecondaryTexts[i] = getResources()
                                .getString(R.string.text_asking_location);
                        itr.remove();
                        break;
                    }
                }
            }
            i++;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseUser friend = mFriends.get(position);
        String status = mSecondaryTexts[position];

        if (status.equals(getResources().getString(R.string.text_where_are_u))) {
            mSecondaryTexts[position] = getResources().getString(R.string.text_request_pending);

            ParseObject locationRequest = new ParseObject(ParseConstants.CLASS_LOCATION_REQUEST);
            // Set custom ACL; enable public read/write access
            ParseACL locationRequestACL = new ParseACL();
            locationRequestACL.setPublicReadAccess(true);
            locationRequestACL.setPublicWriteAccess(true);
            locationRequest.setACL(locationRequestACL);
            // Set fields
            locationRequest.put(ParseConstants.KEY_SENDER, mCurrentUser.getObjectId());
            locationRequest.put(ParseConstants.KEY_RECIPIENT, friend.getObjectId());
            locationRequest.put(ParseConstants.KEY_ACCEPTED, false);
            locationRequest.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    // Location request failed
                    if (e != null) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        } else if (status.equals(getResources().getString(R.string.text_asking_location))) {
            mSecondaryTexts[position] = getResources().getString(R.string.text_sharing_location);

            ParseQuery<ParseObject> queryLocationRequests =
                    ParseQuery.getQuery(ParseConstants.CLASS_LOCATION_REQUEST);
            queryLocationRequests.whereEqualTo(ParseConstants.KEY_SENDER, friend.getObjectId());
            queryLocationRequests.whereEqualTo(ParseConstants.KEY_RECIPIENT,
                    mCurrentUser.getObjectId());
            queryLocationRequests.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject locationRequest, ParseException e) {
                    locationRequest.put(ParseConstants.KEY_ACCEPTED, true);
                    locationRequest.put(ParseConstants.KEY_TTL, mLocationTTL);
                    locationRequest.put(ParseConstants.KEY_TIME_ACCEPTED,
                            System.currentTimeMillis());
                    locationRequest.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            // Location sharing failed
                            if (e != null) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });
                }
            });
        } else {
            mSecondaryTexts[position] = getResources().getString(R.string.text_where_are_u);
            List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

            // Query where the sender is Current User
            ParseQuery<ParseObject> queryCurUserSender =
                    ParseQuery.getQuery(ParseConstants.CLASS_LOCATION_REQUEST);
            queryCurUserSender.whereEqualTo(ParseConstants.KEY_SENDER, mCurrentUser.getObjectId());
            queryCurUserSender.whereEqualTo(ParseConstants.KEY_RECIPIENT,
                    friend.getObjectId());

            // Query where the recipient is Current User
            ParseQuery<ParseObject> queryCurUserRecipient =
                    ParseQuery.getQuery(ParseConstants.CLASS_LOCATION_REQUEST);
            queryCurUserRecipient.whereEqualTo(ParseConstants.KEY_SENDER, friend.getObjectId());
            queryCurUserRecipient.whereEqualTo(ParseConstants.KEY_RECIPIENT,
                    mCurrentUser.getObjectId());

            queries.add(queryCurUserSender);
            queries.add(queryCurUserRecipient);

            // Find BOTH queries
            ParseQuery<ParseObject> queryLocationRequests = ParseQuery.or(queries);
            queryLocationRequests.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject locationRequest, ParseException e) {
                    locationRequest.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                // Location cancel failed
                                if (e != null) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        }
                    });
                }
            });
        }

        // Change secondary text
        TextView text2 = (TextView) v.findViewById(android.R.id.text2);
        text2.setText(mSecondaryTexts[position]);
    }
}
