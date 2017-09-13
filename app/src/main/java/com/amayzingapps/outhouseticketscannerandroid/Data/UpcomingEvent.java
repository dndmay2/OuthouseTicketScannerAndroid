package com.amayzingapps.outhouseticketscannerandroid.Data;

/**
 * Essentially a structure that makes the properties eventId, eventName and eventDate
 * immutable, but accessible without getters and setters. See the following web page:
 * https://stackoverflow.com/questions/36701/struct-like-objects-in-java
 * It is like the 4th answer on the page. Making the properties final prevents change
 * from an outside agent.
 */
public class UpcomingEvent  {
    public final String eventId;
    public final String eventName;
    public final String eventDate;

    public UpcomingEvent(String eventId, String eventName, String eventDate) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
    }
}
