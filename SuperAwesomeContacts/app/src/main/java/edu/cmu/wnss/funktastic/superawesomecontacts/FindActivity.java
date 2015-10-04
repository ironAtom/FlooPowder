package edu.cmu.wnss.funktastic.superawesomecontacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Funky McAwesome on some date.
 *
 */

public class FindActivity extends Activity implements FindReturnable {
    public static final String FIND_CONTACT_INTENT_TYPE =
            "edu.cmu.wnss.funktastic.superawesomecontacts.findcontact.intent.type";
    public static final String FIND_CONTACT_EXTRA_KEY =
            "edu.cmu.wnss.funktastic.superawesomecontacts.findcontact.findQueryString";
    public static final String FIND_CONTACT_EXTRA_RESULTS =
            "edu.cmu.wnss.funktastic.superawesomecontacts.findcontact.queryResults";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        setFinishOnTouchOutside(false);

        // Get the queryString from the intent and find some phat contacts yo!
        // get the intent which started this activity
        Intent sourceIntent = getIntent();

        if(sourceIntent.getType() != null && sourceIntent.getType().equals(
                FindActivity.FIND_CONTACT_INTENT_TYPE) && getCallingActivity().getShortClassName().equals(
                getPackageManager().getLaunchIntentForPackage(getPackageName()).getComponent().getShortClassName())) {
            String queryString = sourceIntent.getStringExtra(FindActivity.FIND_CONTACT_EXTRA_KEY);
            if(queryString != null) {
                Thread getContacts = new Thread(new PopulateContactList(getContentResolver(), this, queryString));
                getContacts.start();
            }
        }
    }

    @Override
    public void setSearchResult(String results) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(FindActivity.FIND_CONTACT_EXTRA_RESULTS, "SEARCH RESULTS\n" + results);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}