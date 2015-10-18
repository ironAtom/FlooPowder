package com.example.tom.lolgamehelper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    public static final String TARGET_PACKAGE_NAME =
            "edu.cmu.wnss.funktastic.superawesomecontacts";
    public static final String TARGET_ACTIVITY_NAME =
            "edu.cmu.wnss.funktastic.superawesomecontacts.FindActivity";
    public static final String FIND_CONTACT_INTENT_TYPE =
            "edu.cmu.wnss.funktastic.superawesomecontacts.findcontact.intent.type";
    public static final String FIND_CONTACT_EXTRA_KEY =
            "edu.cmu.wnss.funktastic.superawesomecontacts.findcontact.findQueryString";
    public static final String FIND_CONTACT_EXTRA_RESULTS =
            "edu.cmu.wnss.funktastic.superawesomecontacts.findcontact.queryResults";
    private final int FIND_CONTACT_REQ_ID = 1;

    private TextView mContactListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContactListTextView = (TextView) findViewById(R.id.textBox);
        findContact("*");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FIND_CONTACT_REQ_ID && resultCode == Activity.RESULT_OK) {
            mContactListTextView.setText(data.getStringExtra(FIND_CONTACT_EXTRA_RESULTS));
        }
    }

    private void findContact(String queryString) {
        // Launch the FindActivity with our search query string
        Intent findIntent = new Intent();
        ComponentName compName = new ComponentName(TARGET_PACKAGE_NAME, TARGET_ACTIVITY_NAME);
        findIntent.setComponent(compName);
        findIntent.setType(FIND_CONTACT_INTENT_TYPE);
        findIntent.putExtra(FIND_CONTACT_EXTRA_KEY, queryString);
        startActivityForResult(findIntent, FIND_CONTACT_REQ_ID);
    }
}
