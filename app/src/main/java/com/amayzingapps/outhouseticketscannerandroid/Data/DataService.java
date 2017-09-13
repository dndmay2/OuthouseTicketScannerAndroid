package com.amayzingapps.outhouseticketscannerandroid.Data;

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
import static com.amayzingapps.outhouseticketscannerandroid.Data.Password.OUTHOUSE_ADMIN;
import static com.amayzingapps.outhouseticketscannerandroid.Data.Password.OUTHOUSE_PASSWORD;

/**
 * Created by derekmay on 6/28/17.
 */

public class DataService {
    public static final String TEST_EVENT_ID = "8532";
    public static final String TEST_VENUE_ID = "191";
    //private static String TEST_TICKET_CODE = "06L0575520";
    //private static String TEST_TICKET_CODE = "06L0213152";
    public static String TEST_TICKET_CODE = "06L0660550";
    public static boolean LED_BACK_LIGHT_SETTING = false;


    public static String NUM_TICKETS = "0";
    public static String NUM_SCANNED_TICKETS = "0";
    public static String TICKET_CODE = "Press Scan Ticket";
    public static String TICKET_STATUS = "None";
    public static String TICKET_STATUS_MESSAGE = "";
    public static final ArrayList<UpcomingEvent> UPCOMING_EVENTS_FOR_VENUE = new ArrayList<>();
    public static final Map<String, String> UPCOMING_EVENTS_WITH_DATE = new HashMap<>();
    public static final Map<String, UpcomingEvent> EVENTS_MAP = new HashMap<>();

    private static final String URL = "http://www.outhousetickets.com/webservice/barcodescanner.asmx";
    private static final String NAMESPACE = "http://www.outhousetickets.com/";
    private static final String SOAP_ACTION = "http://www.outhousetickets.com/GetUpcommingEvents_Custom";
    private static Map EventListMap = new HashMap();

    {
        Log.d("DataService", "DataService class is being executed");
    }

    public static class CallWebService extends AsyncTask<String, Void, String> {
        ResultsListener listener;
        private SoapObject soapObject;
        private SoapObject soapObjectResult;
        private SoapPrimitive soapPrimitiveResult;
        private String currentCommand;
        private ArrayList<String> properties = new ArrayList<>();

        public void setOnResultsListener(ResultsListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v("DataService", "onPostExecute:" + s + ", " + this.currentCommand);
            if(this.properties.size() > 0) {
                // We have a hierarchical soap object
                for(int i = 0; i < this.properties.size(); i+=2) {
                    String [] eventIdList = this.properties.get(i).split("::");
                    String [] eventNameList = this.properties.get(i+1).split("::");
                    String eventId = eventIdList[1];
                    String eventName = eventNameList[1];
                    String [] eventNamePartsList = eventName.split(" - ");
                    String eventDate = "no date";
                    switch (this.currentCommand) {
                        case "GetUpcommingEvents_Custom":
                            UpcomingEvent event = new UpcomingEvent(eventId, eventName, UPCOMING_EVENTS_WITH_DATE.get(eventId));
                            UPCOMING_EVENTS_FOR_VENUE.add(event);
                            EVENTS_MAP.put(eventId, event);
                            Log.v("DataService", "prop:" + event.eventId + "," + event.eventName + "," + event.eventDate);
                            break;
                        case "GetUpcommingEvents":
                            if(eventNamePartsList.length >= 2) {
                                eventDate = eventNamePartsList[eventNamePartsList.length - 1];
                                UPCOMING_EVENTS_WITH_DATE.put(eventId, eventDate);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            else {
                switch (this.currentCommand) {
                    case "GetTicketCountForEvent":
                        NUM_TICKETS = this.soapPrimitiveResult.toString();
                        break;
                    case "GetScannedTicketCountForEvent":
                        NUM_SCANNED_TICKETS = this.soapPrimitiveResult.toString();
                        break;
                    default:
                        break;
                }

            }
            if(this.currentCommand != "GetUpcommingEvents") {
                Log.v("DataService", "Done");
                listener.onResultsSucceeded("second call");
            }
        }

        @Override
        protected String doInBackground(String... params) {
            //params[1] == venueId
            String commandName = params[0];
            String soapAction = NAMESPACE + commandName;

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
                    Log.v("DataService", "BG - GetTicketCountForEvent" );
                    addSoapProperty("EventID", params[1]);
                    break;
                case "GetScannedTicketCountForEvent":
                    Log.v("DataService", "BG - GetScannedTicketCountForEvent" );
                    addSoapProperty("EventID", params[1]);
                    break;
                case "ProcessTicketCode":
                    Log.v("DataService", "BG - ProcessTicketCode" );
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
                if(commandName == "GetUpcommingEvents" || commandName == "GetUpcommingEvents_Custom" || commandName == "GetTicketDBForEvent") {
                    this.soapObjectResult = (SoapObject)envelope.getResponse();
                    ScanSoapObject(this.soapObjectResult);
                }
                else {
                    this.soapPrimitiveResult = (SoapPrimitive) envelope.getResponse();
                }
            } catch (Exception e) {
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

