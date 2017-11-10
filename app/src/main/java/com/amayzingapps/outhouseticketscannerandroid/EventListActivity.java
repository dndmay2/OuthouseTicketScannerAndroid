package com.amayzingapps.outhouseticketscannerandroid;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.amayzingapps.outhouseticketscannerandroid.Data.DataService;
import com.amayzingapps.outhouseticketscannerandroid.Data.UpcomingEvent;

import java.util.ArrayList;

/**
 * Comment
 * AppCompatActivity is necessary to see the title bar. Simply extending Activity or
 * listActivity would not show the title bar.
 */

public class EventListActivity extends AppCompatActivity  {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    private ListView eventListView;
    private String venueId;
    private Boolean soundEffects;
    private SharedPreferences sharedPref;
    private DataService model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        this.setTitle(getResources().getString(R.string.title_event_list));

        final Context context = this;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        venueId = sharedPref.getString("venueId", "");
        soundEffects = sharedPref.getBoolean("soundEffects", false);

        Log.d("OHEventListActivity", "venueId = " + venueId);

        eventListView = (ListView) findViewById(R.id.event_list);
        model = ViewModelProviders.of(this).get(DataService.class);
        model.getUpcomingEventsWithDate_live();
        model.getUpcomingEventsWithDateLive().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String data) {
                Log.d("OHEventListActivity", "upcoming events: " + data);
                model.getUpcomingEventsForVenue_live(venueId);
            }
        });

        model.getVenueEventsLive().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String data) {
                Log.d("OHEventListActivity", "venue events: " + data);
                setupListViewWithEvents(eventListView);
            }
        });

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                UpcomingEvent event = DataService.UPCOMING_EVENTS_FOR_VENUE.get(position);
                Intent detailIntent = new Intent(context, EventDetailsActivity.class);
                detailIntent.putExtra("id", event.eventId);
                detailIntent.putExtra("name", event.eventName);
                detailIntent.putExtra("date", event.eventDate);
                detailIntent.putExtra("venue", venueId);
                detailIntent.putExtra("soundEffects", soundEffects);

                startActivity(detailIntent);
            }
        });
    }

    private void setupListViewWithEvents(@NonNull ListView eventListView) {
        EventListViewAdapter adapter = new EventListViewAdapter(this, DataService.UPCOMING_EVENTS_FOR_VENUE);
        eventListView.setAdapter(adapter);
        Log.d("OHsetupListView", "Done");
    }

    @Override
    public void onResume() {
        super.onResume();
        String currentVenueId = sharedPref.getString("venueId", "");
        Log.d("OHEventListActivity", "currentVenueId=" + currentVenueId + "  venueId="+venueId);
        if(!venueId.equals(currentVenueId)) {
            venueId = currentVenueId;
            model.getUpcomingEventsWithDate_live();
        }
        Boolean currentSoundEffects = sharedPref.getBoolean("soundEffects", false);
        if(!soundEffects.equals(currentSoundEffects)) {
            soundEffects = currentSoundEffects;
        }
    }

    public class EventListViewAdapter extends BaseAdapter {
        private final ArrayList<UpcomingEvent> mValues;
        private final LayoutInflater mInflater;
        private final Context mContext;

        EventListViewAdapter(Context context, ArrayList<UpcomingEvent> items) {
            mValues = items;
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mValues.size();
        }

        @Override
        public Object getItem(int position) {
            return mValues.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            // Get view for row item
            UpcomingEvent mItem = (UpcomingEvent) getItem(position);

            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.event_list_content, parent, false);

                holder = new ViewHolder();
                holder.eventDateView = convertView.findViewById(R.id.event_date_tv);
                holder.eventNameView = convertView.findViewById(R.id.event_name_tv);

                convertView.setTag((holder));

            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            TextView eventNameView = holder.eventNameView;
            TextView eventDateView = holder.eventDateView;
            eventNameView.setText(mItem.eventName);
            eventDateView.setText(mItem.eventDate);
            return convertView;
        }

        class ViewHolder {
            TextView eventNameView;
            TextView eventDateView;
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_list, menu);
        return true;
    }

    /**
     * react to the user tapping/selecting an options menu item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                //Toast.makeText(this, "ADD!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MyPreferenceActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



}
