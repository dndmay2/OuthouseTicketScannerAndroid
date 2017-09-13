package com.amayzingapps.outhouseticketscannerandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.amayzingapps.outhouseticketscannerandroid.Data.DataService;
import com.amayzingapps.outhouseticketscannerandroid.Data.ResultsListener;
import com.amayzingapps.outhouseticketscannerandroid.Data.UpcomingEvent;

import java.util.ArrayList;

/**
 * Comment
 * AppCompatActivity is necessary to see the title bar. Simply extending Activity or
 * listActivity would not show the title bar.
 */

public class EventListActivity extends AppCompatActivity implements ResultsListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    private ListView eventListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        this.setTitle(getResources().getString(R.string.title_event_list));

        final Context context = this;

        getUpcomingEventsWithDate();
        getUpcomingEventsForVenue(DataService.TEST_VENUE_ID);

        eventListView = (ListView) findViewById(R.id.event_list);
        setupListView(eventListView);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                UpcomingEvent event = DataService.UPCOMING_EVENTS_FOR_VENUE.get(position);
                Intent detailIntent = new Intent(context, EventDetailsActivity.class);
                detailIntent.putExtra("id", event.eventId);
                detailIntent.putExtra("name", event.eventName);
                detailIntent.putExtra("date", event.eventDate);

                startActivity(detailIntent);
            }
        });
    }

    public void onResultsSucceeded(String result) {
        Log.d("onResultsSucceeded", "I made it " + result);
        Log.v("DataService", "I made it to the listener on activity");
        eventListView = (ListView) findViewById(R.id.event_list);
        setupListView(eventListView);

    }

    private void setupListView(@NonNull ListView eventListView) {
//        ArrayList<UpcomingEvent> mValues = new ArrayList<>();
//        mValues.add(new UpcomingEvent("1", "my event name", "1/2/3"));
//        EventListViewAdapter adapter = new EventListViewAdapter(this, mValues);
        EventListViewAdapter adapter = new EventListViewAdapter(this, DataService.UPCOMING_EVENTS_FOR_VENUE);
        eventListView.setAdapter(adapter);
    }


    public class EventListViewAdapter extends BaseAdapter {
        private final ArrayList<UpcomingEvent> mValues;
        private final LayoutInflater mInflater;
        private final Context mContext;

        public EventListViewAdapter(Context context, ArrayList<UpcomingEvent> items) {
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

        public class ViewHolder {
            public TextView eventNameView;
            public TextView eventDateView;
        }

    }

    public void getUpcomingEventsForVenue(String venueId){
        String cmd = "GetUpcommingEvents_Custom";
        DataService.CallWebService task = new DataService.CallWebService();
        /*
         * Set this Activity as the listener
         * on the AsyncTask. The AsyncTask will now
         * have a reference to this Activity and will
         * call onResultsSucceeded() in its
         * onPostExecute method.
         */
        task.setOnResultsListener(this);
        task.execute(cmd, venueId);
    }

    public void getUpcomingEventsWithDate(){
        String cmd = "GetUpcommingEvents";
        DataService.CallWebService task = new DataService.CallWebService();
        /*
         * Set this Activity as the listener
         * on the AsyncTask. The AsyncTask will now
         * have a reference to this Activity and will
         * call onResultsSucceeded() in its
         * onPostExecute method.
         */
        task.setOnResultsListener(this);
        task.execute(cmd);
    }

}