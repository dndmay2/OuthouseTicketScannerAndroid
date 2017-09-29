package com.amayzingapps.outhouseticketscannerandroid;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.amayzingapps.outhouseticketscannerandroid.Data.DataService;

public class EventDetailsActivity extends AppCompatActivity {

    static int GET_SCAN_CODE = 1;
    private String eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("OHEventDetailsActivity", "entering onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventId = this.getIntent().getExtras().getString("id");
        eventName = this.getIntent().getExtras().getString("name");
//        String eventDate = this.getIntent().getExtras().getString("date");

        setupTitleBar();

        TextView eventNameText = findViewById(R.id.eventName);
        eventNameText.setText(eventName);

        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scanIntent = new Intent(EventDetailsActivity.this, ScanActivity.class);
                startActivityForResult(scanIntent, GET_SCAN_CODE);
            }
        });

        final DataService model = ViewModelProviders.of(this).get(DataService.class);
        model.getTicketCountForEvent_Live(eventId);
        model.getTicketCountLive().observe(this, setupObserver(R.id.totalTickets));

        model.getScannedTicketCountForEvent_Live(eventId);
        model.getScannedTicketCountLive().observe(this, setupObserver(R.id.ticketsScanned));

        model.getTicketCodeLive().observe(this, setupObserver(R.id.msgText));

    }

    private Observer<String> setupObserver(final int idName) {
        return new Observer<String>() {
            @Override
            public void onChanged(@Nullable String data) {
                TextView tv = findViewById(idName);
                tv.setText(data);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_SCAN_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String ticketCode = data.getStringExtra("TICKET_CODE");
                Log.d("OHEventDetailsActivity", "onActivityResult is " + ticketCode);
                DataService model = ViewModelProviders.of(this).get(DataService.class);
                model.setTicketCodeLive(ticketCode);
                processTicketCode(eventId, ticketCode);
            }
        }
    }

    public void processTicketCode(String eventId, String codeNumber) {
        Log.d("OHEventDetailsActivity", "eventId = " + eventId + " codeNumber = " + codeNumber);
        final DataService model = ViewModelProviders.of(this).get(DataService.class);
        model.processTicketCode_Live(eventId, codeNumber);
        model.getTicketMessageLive().observe(this, setupObserver(R.id.ticketMessage));
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
        assert abar != null;
        abar.setCustomView(viewActionBar, params);
        abar.setDisplayShowCustomEnabled(true);
        abar.setDisplayShowTitleEnabled(false);
//        abar.setDisplayHomeAsUpEnabled(true);
//        abar.setIcon(R.color.transparent);
        abar.setHomeButtonEnabled(true);

    }

//    public void getTicketCountForEvent(String eventId){
//        String cmd = "GetTicketCountForEvent";
//        DataService.CallWebService task = new DataService.CallWebService();
//        /*
//         * Set this Activity as the listener
//         * on the AsyncTask. The AsyncTask will now
//         * have a reference to this Activity and will
//         * call onResultsSucceeded() in its
//         * onPostExecute method.
//         */
//        task.setOnResultsListener(this);
//        task.execute(cmd, eventId);
//    }
//
//    public void getScannedTicketCountForEvent(String eventId){
//        String cmd = "GetScannedTicketCountForEvent";
//        DataService.CallWebService task = new DataService.CallWebService();
//        /*
//         * Set this Activity as the listener
//         * on the AsyncTask. The AsyncTask will now
//         * have a reference to this Activity and will
//         * call onResultsSucceeded() in its
//         * onPostExecute method.
//         */
//        task.setOnResultsListener(this);
//        task.execute(cmd, eventId);
//    }
//
//    public void processTicketCode(String eventId, String codeNumber){
//        String cmd = "ProcessTicketCode";
//        DataService.CallWebService task = new DataService.CallWebService();
//        /*
//         * Set this Activity as the listener
//         * on the AsyncTask. The AsyncTask will now
//         * have a reference to this Activity and will
//         * call onResultsSucceeded() in its
//         * onPostExecute method.
//         */
//        task.setOnResultsListener(this);
//        task.execute(cmd, eventId, codeNumber);
//    }

}

