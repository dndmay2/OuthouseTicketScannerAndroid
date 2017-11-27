package com.amayzingapps.outhouseticketscannerandroid;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.amayzingapps.outhouseticketscannerandroid.Data.DataService;

public class EventDetailsActivity extends AppCompatActivity {

    static int GET_SCAN_CODE = 1;
    private String eventId;
    private String eventName;
    private String venueId;
    private Boolean soundEffects;
    private MediaPlayer mp = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("OHEventDetailsActivity", "entering onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventId = this.getIntent().getExtras().getString("id");
        eventName = this.getIntent().getExtras().getString("name");
        venueId = this.getIntent().getExtras().getString("venue");
        soundEffects = this.getIntent().getExtras().getBoolean("soundEffects");
//        String eventDate = this.getIntent().getExtras().getString("date");
        Log.d("OHEventDetailsActivity", "soundEffects = " + soundEffects);

        setupTitleBar();

        TextView eventNameText = (TextView) findViewById(R.id.eventName);
        eventNameText.setText(eventName);

        Button scanButton = (Button) findViewById(R.id.scanButton);
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

        model.getTicketMessageLive().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                switch (model.TICKET_STATUS) {
                    case "true":
                        setImgDisplay("valid", s);
                        model.getScannedTicketCountForEvent_Live(eventId);
                        break;
                    case "false":
                        setImgDisplay("invalid", s);
                        break;
                    default:
                        setImgDisplay("cancelled", s);
                        break;
                }
                // Update total ticket count between each scan in case more
                // tickets are purchased
                model.getTicketCountForEvent_Live(eventId);
            }
        });
    }

    private Observer<String> setupObserver(final int idName) {
        return new Observer<String>() {
            @Override
            public void onChanged(@Nullable String data) {
                TextView tv = (TextView) findViewById(idName);
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
                if(ticketCode.equals("Cancelled")){
                    setImgDisplay("cancelled", "Press Scan Ticket");
                }
                else {
                    model.setTicketCodeLive(ticketCode);
                    processTicketCode(eventId, ticketCode);
                }
            }
        }
    }

    public void processTicketCode(String eventId, String codeNumber) {
        Log.d("OHEventDetailsActivity", "eventId = " + eventId + " codeNumber = " + codeNumber);
        final DataService model = ViewModelProviders.of(this).get(DataService.class);
        model.processTicketCode_Live(eventId, codeNumber);
    }

    private void setImgDisplay(String value, String s) {
        Log.d("OHEventDetailsActivity", "setImgDisplay - " + value + ":" + s);
        TextView tv = (TextView) findViewById(R.id.ticketMessage);
        TextView code = (TextView) findViewById(R.id.msgText);
        ImageView stopSignImg = (ImageView) findViewById(R.id.stopSignImg);
        ImageView checkMarkImg = (ImageView) findViewById(R.id.checkMarkImg);
        switch(value) {
            case "valid":
                tv.setText(s);
                stopSignImg.setVisibility(View.INVISIBLE);
                checkMarkImg.setVisibility(View.VISIBLE);
                if(this.soundEffects) {
                    playSound(R.raw.what_another_message);
                }
                break;
            case "invalid":
                tv.setText(s);
                stopSignImg.setVisibility(View.VISIBLE);
                checkMarkImg.setVisibility(View.INVISIBLE);
                if(this.soundEffects) {
                    playSound(R.raw.nasty_error_long);
                }
                break;
            default:
                tv.setText("");
                code.setText(s);
                stopSignImg.setVisibility(View.INVISIBLE);
                checkMarkImg.setVisibility(View.INVISIBLE);
                break;

        }
    }

    private void playSound(int resource) {
        if(mp != null){
            try {
                if (mp.isPlaying()) {
                    mp.stop();
                }
                mp.reset();
                mp.release();
            } catch (Exception e) {
                Log.d("Exception", "playSound error");
            }
            mp = null;
        }
        mp = MediaPlayer.create(this, resource);
        mp.start();

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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_event_detail, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                final Dialog dialog = new Dialog(this, R.style.CustomDialog);
                dialog.setContentView(R.layout.info_dialog);
                dialog.setTitle("Outhouse Tickets");
                Button dialogButton = (Button) dialog.findViewById(R.id.closeButton);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                Button dialogWebButton = (Button) dialog.findViewById(R.id.visitWebPageButton);
                dialogWebButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.outhousetickets.com"));
                       startActivity(browserIntent);
                   }
                });
                TextView dlgEventName = (TextView) dialog.findViewById(R.id.dialog_eventName);
                dlgEventName.setText(String.format("Event Name: %s", this.eventName));
                TextView dlgEventId = (TextView) dialog.findViewById(R.id.dialog_eventId);
                dlgEventId.setText(String.format("Event ID: %s", this.eventId));
                TextView dlgVenueId = (TextView) dialog.findViewById(R.id.dialog_venueId);
                dlgVenueId.setText(String.format("Venue ID: %s", this.venueId));
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

