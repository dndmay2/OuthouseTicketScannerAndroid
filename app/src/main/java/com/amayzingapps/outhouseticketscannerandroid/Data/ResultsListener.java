package com.amayzingapps.outhouseticketscannerandroid.Data;

/**
 * Activites that wish to be notified about results
 * in onPostExecute of an AsyncTask must implement
 * this interface.
 *
 * This is the basic Observer pattern.
 *
 * From https://github.com/levinotik/ReusableAsyncTask
 */
public interface ResultsListener {
    public void onResultsSucceeded(String result);
}