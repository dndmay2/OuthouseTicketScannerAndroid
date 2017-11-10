package com.amayzingapps.outhouseticketscannerandroid.Data;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;
import android.os.AsyncTask;
import android.util.Log;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static com.amayzingapps.outhouseticketscannerandroid.Data.Password.OUTHOUSE_ADMIN;
import static com.amayzingapps.outhouseticketscannerandroid.Data.Password.OUTHOUSE_PASSWORD;

/**
 * Created by derekmay on 6/28/17.
 */

public class DataService extends ViewModel {
    public static final String TEST_EVENT_ID = "8532";
    public static final String TEST_VENUE_ID = "191";
    //private static String TEST_TICKET_CODE = "06L0575520";
    //private static String TEST_TICKET_CODE = "06L0213152";
    public static String TEST_TICKET_CODE = "06L0660550";
    public static boolean LED_BACK_LIGHT_SETTING = false;
    public MutableLiveData <String> TicketCountLive = new MutableLiveData<>();
    public MutableLiveData <String> ScannedTicketCountLive = new MutableLiveData<>();
    public MutableLiveData <String> VenueEventsLive = new MutableLiveData<>();
    public MutableLiveData <String> UpcomingEventsWithDateLive = new MutableLiveData<>();
    public MutableLiveData <String> TicketCodeLive = new MutableLiveData<>();
    public MutableLiveData <String> TicketMessageLive = new MutableLiveData<>();


    public static String NUM_TICKETS = "0";
    public static String NUM_SCANNED_TICKETS = "0";
    public final ObservableField<String> TICKET_CODE = new ObservableField<>();
    public static String TICKET_STATUS = "None";
    public static String TICKET_STATUS_MESSAGE = "";
    public static ArrayList<UpcomingEvent> UPCOMING_EVENTS_FOR_VENUE = new ArrayList<>();
    public static final Map<String, String> UPCOMING_EVENTS_WITH_DATE = new HashMap<>();
    public static final Map<String, UpcomingEvent> EVENTS_MAP = new HashMap<>();

    private static final String URL = "http://www.outhousetickets.com/webservice/barcodescanner.asmx";
    private static final String NAMESPACE = "http://www.outhousetickets.com/";
    private static final String SOAP_ACTION = "http://www.outhousetickets.com/GetUpcommingEvents_Custom";
    private static Map EventListMap = new HashMap();

    {
        Log.v("OHDataService", "DataService class is being executed");
    }

    public DataService() {
        super();
    }

    public void getTicketCountForEvent_Live(String eventId){
        String cmd = "GetTicketCountForEvent";
        DataService.CallWebService task = new DataService.CallWebService();
        task.execute(cmd, eventId);
    }

    public MutableLiveData<String> getTicketCountLive() {
        return TicketCountLive;
    }

    public void getScannedTicketCountForEvent_Live(String eventId){
        String cmd = "GetScannedTicketCountForEvent";
        DataService.CallWebService task = new DataService.CallWebService();
        task.execute(cmd, eventId);
    }

    public MutableLiveData<String> getScannedTicketCountLive() {
        return ScannedTicketCountLive;
    }

    public void getUpcomingEventsForVenue_live(String venueId){
        String cmd = "GetUpcommingEvents_Custom";
        DataService.CallWebService task = new DataService.CallWebService();
        task.execute(cmd, venueId);
    }

    public MutableLiveData<String> getVenueEventsLive() {
        if(VenueEventsLive == null){
            VenueEventsLive.postValue("null");
        }
        return VenueEventsLive;
    }

    public void getUpcomingEventsWithDate_live(){
        String cmd = "GetUpcommingEvents";
        DataService.CallWebService task = new DataService.CallWebService();
        task.execute(cmd);
    }

    public MutableLiveData<String> getUpcomingEventsWithDateLive() {
        Log.d("OHLiveData", "i made it");
        if(UpcomingEventsWithDateLive == null){
            UpcomingEventsWithDateLive.postValue("null");
        }
        return UpcomingEventsWithDateLive;
    }

    public void setTicketCodeLive(String ticketCode) {
        Log.d("OHsetTicketCodeLive", "setting the ticketCodeLive variable = " + ticketCode);
        TicketCodeLive.setValue(ticketCode);
    }

    public MutableLiveData<String> getTicketCodeLive() {
        return TicketCodeLive;
    }

    public void processTicketCode_Live(String eventId, String ticketCode) {
        String cmd = "ProcessTicketCode";
        DataService.CallWebService task = new DataService.CallWebService();
        task.execute(cmd, eventId, ticketCode);
    }

    public MutableLiveData<String> getTicketMessageLive() {
        return TicketMessageLive;
    }

    private class CallWebService extends AsyncTask<String, Void, String> {
        private SoapObject soapObject;
        private SoapObject soapObjectResult;
        private SoapPrimitive soapPrimitiveResult;
        private boolean ticketResult;
        private Vector<SoapPrimitive> soapVectorPrimitiveResult;
        private String currentCommand;
        private ArrayList<String> properties = new ArrayList<>();

        @Override
        protected void onPostExecute(String s) {
            Log.v("OHDataService", "onPostExecute:" + s + ", " + this.currentCommand);
            switch (this.currentCommand) {
                case "GetTicketCountForEvent":
                    NUM_TICKETS = this.soapPrimitiveResult.toString();
                    TicketCountLive.setValue(NUM_TICKETS);
                    break;
                case "GetScannedTicketCountForEvent":
                    NUM_SCANNED_TICKETS = this.soapPrimitiveResult.toString();
                    ScannedTicketCountLive.setValue(NUM_SCANNED_TICKETS);
                    break;
                case "ProcessTicketCode":
                    if (this.ticketResult) {
                        String result = this.soapPrimitiveResult.toString();
                        String message = "Ticket scanned successfully";
                        Log.d("OHDataService", "testing processTicketCode " + result + " " + message);
                        TICKET_STATUS = result;
                        TicketMessageLive.postValue(message);
                    }
                    else {
                        String result = this.soapVectorPrimitiveResult.get(0).toString();
                        String message = this.soapVectorPrimitiveResult.get(1).toString();
                        Log.d("OHDataService", "testing processTicketCode " + result + " " + message);
                        TICKET_STATUS = result;
                        TicketMessageLive.postValue(message);
                    }
                    break;
                case "GetUpcommingEvents_Custom":
                case "GetUpcommingEvents":
                    UPCOMING_EVENTS_FOR_VENUE = new ArrayList<>();
                    // We have a hierarchical soap object
                    for(int i = 0; i < this.properties.size(); i+=2) {
                        String [] eventIdList = this.properties.get(i).split("::");
                        String [] eventNameList = this.properties.get(i+1).split("::");
                        String eventId = eventIdList[1];
                        String eventName = eventNameList[1];
                        if(this.currentCommand.equals("GetUpcommingEvents_Custom")) {
//                            if (eventName.equals("Bar Code Test")) {
//                                eventName = "This is a very long event name that should ";
//                            }
//                            if (eventName.equals("Bar Code Test 2")) {
//                                eventName = "This is a very long event name that should span more than one line ";
//                            }
                            UpcomingEvent event = new UpcomingEvent(eventId, eventName, UPCOMING_EVENTS_WITH_DATE.get(eventId));
                            UPCOMING_EVENTS_FOR_VENUE.add(event);
                            EVENTS_MAP.put(eventId, event);
                            Log.d("OHDataService", "prop:" + event.eventId + "," + event.eventName + "," + event.eventDate);
                        }
                        else {
                            String [] eventNamePartsList = eventName.split(" - ");
                            String eventDate;
                            if (eventNamePartsList.length >= 2) {
                                eventDate = eventNamePartsList[eventNamePartsList.length - 1];
                                UPCOMING_EVENTS_WITH_DATE.put(eventId, eventDate);
                            }
                        }
                    }
                    if(this.currentCommand.equals("GetUpcommingEvents_Custom")) {
                        VenueEventsLive.setValue("Done");
                    }
                    else {
                        UpcomingEventsWithDateLive.setValue("Done");
                    }
                default:
                    break;
            }
        }

        @Override
        protected String doInBackground(String... params) {
            //params[1] == venueId
            String commandName = params[0];
            String soapAction = NAMESPACE + commandName;
            Log.d("OHDataService", "doInBackground: " + commandName);

            SoapObject soapObject = new SoapObject(NAMESPACE, commandName);

            this.soapObject = soapObject;
            this.currentCommand = commandName;

            addSoapProperty("AdminUserName", OUTHOUSE_ADMIN);
            addSoapProperty("AdminPassword", OUTHOUSE_PASSWORD);
            switch (commandName) {
                case "GetUpcommingEvents_Custom":
                    addSoapProperty("VenueID", params[1]);
                    break;
                case "GetTicketCountForEvent":
                    Log.d("OHDataService", "BG - GetTicketCountForEvent" );
                    addSoapProperty("EventID", params[1]);
                    break;
                case "GetScannedTicketCountForEvent":
                    Log.d("OHDataService", "BG - GetScannedTicketCountForEvent" );
                    addSoapProperty("EventID", params[1]);
                    break;
                case "ProcessTicketCode":
                    Log.d("OHDataService", "BG - ProcessTicketCode" );
                    addSoapProperty("EventID", params[1]);
                    addSoapProperty("TicketCode", params[2]);
                    break;
                default:
                    break;
            }

            SoapSerializationEnvelope envelope =  new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(soapObject);

            HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
            httpTransportSE.debug = true;

            try {
                httpTransportSE.call(soapAction, envelope);
                switch (commandName) {
                    case "GetUpcommingEvents":
                    case "GetUpcommingEvents_Custom":
                    case "GetTicketDBForEvent": {
                        // "org.ksoap2.serialization.SoapObject"
                        this.soapObjectResult = (SoapObject)envelope.getResponse();
                        ScanSoapObject(this.soapObjectResult);
                        break;
                    }
                    case "ProcessTicketCode": {
                        // When it fails there are two return values - false, msg
                        if(envelope.getResponse().getClass().getName().equals("java.util.Vector")) {
                            this.soapVectorPrimitiveResult = (Vector<SoapPrimitive>) envelope.getResponse();
                            this.ticketResult = false;
                        }
                        // When it is true, it simply returns true
                        else {
                            // "org.ksoap2.serialization.SoapPrimitive"
                            this.soapPrimitiveResult = (SoapPrimitive) envelope.getResponse();
                            this.ticketResult = true;
                        }
                        break;
                    }
                    default: {
                        // "org.ksoap2.serialization.SoapPrimitive"
                        this.soapPrimitiveResult = (SoapPrimitive) envelope.getResponse();
                        break;
                    }

                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return commandName;
        }

        private void addSoapProperty(String propName, String propValue) {
            PropertyInfo pi = new PropertyInfo();
            pi.setNamespace(NAMESPACE);
            pi.setType(PropertyInfo.STRING_CLASS);
            pi.setName(propName);
            pi.setValue(propValue);
            this.soapObject.addProperty(pi);
        }

        private void ScanSoapObject(SoapObject result)
        {
            for(int i=0;i<result.getPropertyCount();i++)
            {
                if(result.getProperty(i) instanceof SoapObject)
                {
                    ScanSoapObject((SoapObject)result.getProperty(i));
                }
                else
                {
                    //do something with the current property

                    //get the current property name:
                    PropertyInfo pi = new PropertyInfo();
                    result.getPropertyInfo(i,pi);
                    String name = pi.getName();
                    String value = String.valueOf(pi.getValue());
                    this.properties.add(name + "::" + value);
                }
            }
        }

    }

}

