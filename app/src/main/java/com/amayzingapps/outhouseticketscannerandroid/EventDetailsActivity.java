package com.amayzingapps.outhouseticketscannerandroid;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.amayzingapps.outhouseticketscannerandroid.Data.DataService;
import com.amayzingapps.outhouseticketscannerandroid.Data.ResultsListener;
import com.amayzingapps.outhouseticketscannerandroid.Data.UpcomingEvent;

public class EventDetailsActivity extends AppCompatActivity implements ResultsListener {

    Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        String eventId = this.getIntent().getExtras().getString("id");
        String eventName = this.getIntent().getExtras().getString("name");
        String eventDate = this.getIntent().getExtras().getString("date");

        setupTitleBar();

        TextView eventNameText = (TextView) findViewById(R.id.eventName);
        eventNameText.setText(eventName);

        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scanIntent = new Intent(EventDetailsActivity.this, ScanActivity.class);
                startActivity(scanIntent);
            }
        });

        getTicketCountForEvent(eventId);
        getScannedTicketCountForEvent(eventId);

    }

    public void onResultsSucceeded(String result) {
        Log.d("onResultsSucceeded", "I made it " + result);
        Log.v("DataService", "I made it to the listener on activity");
        TextView ticketsScanned = (TextView) findViewById(R.id.ticketsScanned);
        TextView totalTickets = (TextView) findViewById(R.id.totalTickets);
        ticketsScanned.setText(DataService.NUM_SCANNED_TICKETS);
        totalTickets.setText(DataService.NUM_TICKETS);
    }

    private void setupTitleBar() {
        final ActionBar abar = getSupportActionBar();
        View viewActionBar = getLayoutInflater().inflate(R.layout.action_bar_layout, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams( //Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        TextView textviewTitle = viewActionBar.findViewById(R.id.actionbar_textview);
        textviewTitle.setText("Ticket Details");
        abar.setCustomView(viewActionBar, params);
        abar.setDisplayShowCustomEnabled(true);
        abar.setDisplayShowTitleEnabled(false);
//        abar.setDisplayHomeAsUpEnabled(true);
//        abar.setIcon(R.color.transparent);
        abar.setHomeButtonEnabled(true);

    }

    public void getTicketCountForEvent(String eventId){
        String cmd = "GetTicketCountForEvent";
        DataService.CallWebService task = new DataService.CallWebService();
        /*
         * Set this Activity as the listener
         * on the AsyncTask. The AsyncTask will now
         * have a reference to this Activity and will
         * call onResultsSucceeded() in its
         * onPostExecute method.
         */
        task.setOnResultsListener(this);
        task.execute(cmd, eventId);
    }

    public void getScannedTicketCountForEvent(String eventId){
        String cmd = "GetScannedTicketCountForEvent";
        DataService.CallWebService task = new DataService.CallWebService();
        /*
         * Set this Activity as the listener
         * on the AsyncTask. The AsyncTask will now
         * have a reference to this Activity and will
         * call onResultsSucceeded() in its
         * onPostExecute method.
         */
        task.setOnResultsListener(this);
        task.execute(cmd, eventId);
    }

    public void processTicketCode(String eventId, String codeNumber){
        String cmd = "ProcessTicketCode";
        DataService.CallWebService task = new DataService.CallWebService();
        /*
         * Set this Activity as the listener
         * on the AsyncTask. The AsyncTask will now
         * have a reference to this Activity and will
         * call onResultsSucceeded() in its
         * onPostExecute method.
         */
        task.setOnResultsListener(this);
        task.execute(cmd, eventId, codeNumber);
    }

}

