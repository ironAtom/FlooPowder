package edu.cmu.wnss.funktastic.superawesomecontacts;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Funky McAwesome on some date.
 *
 */

public class HomeActivity extends Activity {
    public static final int PERMISSION_READ_CONTACT_CODE = 31337;
    public static final int PERMISSION_READ_CONTACT_CODE_FIND = 31338;

    private final String CONTACT_LIST_STATE_KEY =
            "edu.cmu.wnss.funktastic.superawesomecontacts.home.contacts";
    private final String QUERYSTRING_STATE_KEY =
            "edu.cmu.wnss.funktastic.superawesomecontacts.home.queryString";
    private final int FIND_CONTACT_REQ_ID = 1;

    // List of permissions we need to request
    public final static String[] PERMISSION_LIST = {Manifest.permission.READ_CONTACTS};

    private TextView mContactListTextView;

    // Stores the current find query string
    // This is a class member due to the activity possibly being destroyed/recreated after the user
    // enters the query string but before an actual query takes place (such as due to a
    // configuration change after a permission has been requested but before onRequestPermissionsResult()
    // is invoked).
    private String mQueryString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mContactListTextView = (TextView) findViewById(R.id.contactTextView);

        if(savedInstanceState != null) {
            // state change, get the contact list from the Bundle
            mContactListTextView.setText(savedInstanceState.getString(CONTACT_LIST_STATE_KEY));
            mQueryString = savedInstanceState.getString(QUERYSTRING_STATE_KEY);
        }
        else {
            // request the permissions needed to fetch the contact list
            startPermissionProcess(HomeActivity.PERMISSION_READ_CONTACT_CODE);
        }

    }

    private void startPermissionProcess(int permissionCode) {
        // @09/01/15: Update to permissions model for Android M (API 23).  Permissions model
        // is now runtime based, thus our install-time permissions are no longer good enough
        // to grant contact list access.  Check API version, and request the appropriate permission
        // if M.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Running M or greater.  Get us some phat permissions.
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
                    PackageManager.PERMISSION_GRANTED) {
                // ask for the permission
                requestPermissions(HomeActivity.PERMISSION_LIST, permissionCode);

                // All we gotta do here.  Once the permission is granted/denied, the callback
                // method (onRequestPermissionsResult) is called.
                return;
            }
            else {
                // permission already granted.
                onRequestPermissionsResult(permissionCode, null,
                        new int[]{PackageManager.PERMISSION_GRANTED});
            }
        }
        else {
            // < M (Assume permission already granted)
            onRequestPermissionsResult(permissionCode, null, new int[]{PackageManager.PERMISSION_GRANTED});
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Yay!  Permission is granted!
            // populate the TextView mContactList with contact names
            // This runs in a separate thread as it could take a while
            // We didn't use ASyncTask cause the PopulateContactList class is used by multiple
            // activities, not all of which make sense to have the code wrapped in ASyncTask.
            switch(requestCode) {
                case HomeActivity.PERMISSION_READ_CONTACT_CODE:
                    // General contact list grab
                    Thread getContacts = new Thread(new PopulateContactList(getContentResolver(),
                            new ContactListHandler(mContactListTextView)));
                    getContacts.start();
                    break;
                case HomeActivity.PERMISSION_READ_CONTACT_CODE_FIND:
                    // search through the contact list, returning the results based on the queryString
                    findContact(mQueryString);
                    break;
            }
        }
        else {
            // NOOOOOO!!!!!  Someone is being mean :-(
            mContactListTextView.setText("Y u no give permission?");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FIND_CONTACT_REQ_ID && resultCode == Activity.RESULT_OK) {
            mContactListTextView.setText(data.getStringExtra(FindActivity.FIND_CONTACT_EXTRA_RESULTS));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        // save the contact list so we don't have to fetch it again on a state change (screen
        // reorientation or something)
        state.putString(CONTACT_LIST_STATE_KEY, mContactListTextView.getText().toString());
        state.putString(QUERYSTRING_STATE_KEY, mQueryString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void findContact(String queryString) {
        // Launch the FindActivity with our search query string
        Intent findIntent = new Intent(this, FindActivity.class);
        findIntent.setType(FindActivity.FIND_CONTACT_INTENT_TYPE);
        findIntent.putExtra(FindActivity.FIND_CONTACT_EXTRA_KEY, queryString);
        startActivityForResult(findIntent, FIND_CONTACT_REQ_ID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // goto fail;
        int id = item.getItemId();
        if (id == R.id.action_reload) {
            // goto fail;
            // Reloads the contact list
            mContactListTextView.setText("Reloading...");
            startPermissionProcess(HomeActivity.PERMISSION_READ_CONTACT_CODE);
        }
        else if (id == R.id.action_settings) {
            // goto fail;
            // This app is like, so awesome ya know, that settings would just like, make it
            // less awesome?
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle("Settings");
            alertDialogBuilder.setMessage("This app is too awesome to have any settings.");
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // close the dialog box
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            return true;
        }
        else if(id == R.id.action_find) {
            // goto fail;
            // Display a dialog for search criteria.  Then launch the FindActivity to search for
            // these phat contacts!!!!
            AlertDialog.Builder findDialogBuilder = new AlertDialog.Builder(this);

            final EditText findString = new EditText(this);
            findDialogBuilder.setView(findString);

            findDialogBuilder.setTitle("Find Contacts");
            findDialogBuilder.setMessage("Enter contact to search:");
            findDialogBuilder.setPositiveButton("Find", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // close the dialog box
                    mQueryString = findString.getText().toString();
                    startPermissionProcess(HomeActivity.PERMISSION_READ_CONTACT_CODE_FIND);
                    dialog.cancel();
                }
            });
            findDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // close the dialog box
                    dialog.cancel();
                }
            });

            AlertDialog findDialog = findDialogBuilder.create();
            findDialog.show();

            return true;
        }
        else if(id == R.id.action_about) {
            // goto fail;
            // launch the super groovy about activity
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }

        /* fail:
         * My bad, thought I was writing code in C++.  I used to work at Apple, where
         * using goto/labels is like, cool, and everyone wants to be a cool kid, right?
         */
        return super.onOptionsItemSelected(item);
    }
}
