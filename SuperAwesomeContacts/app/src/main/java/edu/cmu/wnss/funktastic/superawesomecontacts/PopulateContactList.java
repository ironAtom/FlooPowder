package edu.cmu.wnss.funktastic.superawesomecontacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Funky McAwesome on 9/4/2014.
 * Modified by some guy (who may or may be Funky McAwesome) on 9/2/2015
 *
 * SuperAwesomeContacts now returns phone numbers!!!!!  There is literally too much awesomeness
 * in this app!
 */

public class PopulateContactList implements Runnable {

    public static final String HANDLER_BUNDLE_KEY =
            "edu.cmu.wnss.funktastic.superawesomecontacts.populatecontactlist.contactKey";

    // Constructs for querying the contacts DB
    // The projection defines what columns we want returned from the query.  The _ID we use as a
    // primary key
    private final String[] QUERY_PROJECTION_CONTACT = {ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.HAS_PHONE_NUMBER};
    private final String QUERY_ORDERING_CONTACT = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY +" ASC";
    // The projection for fetching phone information from a contact.  Assuming the contact
    // has a phone number stored, the _ID will be used to fetch the appropriate mad info.
    // For those who are curious, there is a 1:1 mapping of _ID between contacts and their primary
    // phone numbers.  There is a one-many mapping between _ID and CONTACT_ID (this allow us to
    // fetch all phone numbers for a specific contact).
    private final String[] QUERY_PROJECTION_PHONE = {ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE};

    private ContentResolver mContentResolver; // provides the context from the content resolver
    private Handler mHandler;  // provides the handler to give the contact list to
    private String mQueryString = null; //query string
    private FindReturnable mFindActivity = null;

    // Some phat constructorZ right here yo
    // This one is for bulk list dump requests from the UI thread
    public PopulateContactList(ContentResolver contentResolver, Handler handler) {
        mContentResolver = contentResolver;
        mHandler = handler;
    }

    // This one is for search queries from a non UI-thread, hence why we don't need a handler
    // Note, FindReturnable is a generic callback interface, less overhead than using a Handler
    // here as the findActivity has no UI and does not update a UI thread...
    public PopulateContactList(ContentResolver contentResolver, FindReturnable findActivity, String queryString) {
        mContentResolver = contentResolver;
        mFindActivity = findActivity;
        mQueryString = queryString;
    }

    @Override
    public void run() {
        if(mFindActivity != null) {
            mFindActivity.setSearchResult(populateContacts());
        }
        else {
            Message msg = new Message();
            Bundle bundle = new Bundle();

            bundle.putString(PopulateContactList.HANDLER_BUNDLE_KEY, populateContacts());
            msg.setData(bundle);

            mHandler.sendMessage(msg);
        }
    }

    // Returns a string with the contact names
    private String populateContacts() {
        // fetch the additional information (currently phone numbers)
        Map<Integer, List<Pair<String, Integer>>> numberCache = getAdditionalInformation();

        Cursor cursor = mContentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, QUERY_PROJECTION_CONTACT, null, null, QUERY_ORDERING_CONTACT);

        int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
        int hasNumberIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

        StringBuilder concatList = new StringBuilder();

        // First thing we need to do is sanitize the queryString and split into tokens.  One
        // important purpose of doing this is to verify if we truly have tokens to apply to a
        // search process.  If we are left with an empty query, then we can simply return the
        // entire contact list.  The way the algorithm logic flows, there must be at least one
        // query token in the list, even if a blank placeholder.
        ArrayList<String> sanitizedQueryTokens = new ArrayList<String>();
        if (mQueryString != null) {
            // split up the query string into tokens, based on whitespace and asterisks
            String[] queryTokens = mQueryString.trim().split("[\\*\\s]+");
            // get rid of any empty string references
            for (String currentString : queryTokens) {
                if (!currentString.equals("")) {
                    sanitizedQueryTokens.add(currentString);
                }
            }
            // if the ArrayList is empty, then we assume match everything
            if (sanitizedQueryTokens.size() == 0) {
                mQueryString = null;
            }
        }

        // Now perform the search.  If the queryString is null, then the search reduces
        // to a dump of all contacts
        if ( mQueryString == null) {
            // The search algorithm is designed to work without search criteria (reduction to
            // entire list dumped).  However we need at least one query token to enter the main
            // search loop.
            sanitizedQueryTokens.add("");
        }

        StringBuilder concatListMatches = new StringBuilder();
        int numMatches = 0;

        while (cursor.moveToNext()) {
            String currentName = cursor.getString(nameIndex);

            if (currentName != null && !currentName.isEmpty()) {
                // If we are not actually performing a search, we still need to enter this loop
                // once (hence why we added a blank term above).
                for (String currentString : sanitizedQueryTokens) {
                    // Add this contact to the list if we are either not performing a search
                    // or if the current search criteria (token) matches
                    if (mQueryString == null || currentName.toLowerCase().contains(
                            currentString.toLowerCase())) {

                        concatListMatches.append(currentName).append("\n");

                        // If this contact has phone number(s), display them
                        if (cursor.getInt(hasNumberIndex) > 0) {
                            // grab the number(s) from the phat cache
                            List<Pair<String, Integer>> phNums;
                            if ((phNums = numberCache.get(cursor.getInt(idIndex))) != null) {
                                // Pair<Phone Number, Type of Number>
                                for (Pair<String, Integer> phNum : phNums) {
                                    concatListMatches.append("\t").append(phNum.first);
                                    switch(phNum.second) {
                                        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                            concatListMatches.append(" [HOME]");
                                            break;
                                        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                            concatListMatches.append(" [MOBILE]");
                                            break;
                                        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                            concatListMatches.append(" [WORK]");
                                            break;
                                    }
                                    concatListMatches.append("\n");
                                }
                            } else {
                                // This should not happen.  Means that there is a contact in which a
                                // phone number is associated but yet no such number actually can
                                // be found in the DB
                                concatList.append("\t<Phone number referenced, but not found>\n");
                            }
                        }

                        ++numMatches;
                        break;
                    }
                }
            }
        }
        concatList.append(numMatches).append(" Contacts Found\n\n").append(concatListMatches);

        return concatList.toString();
    }

    private Map<Integer, List<Pair<String, Integer>>> getAdditionalInformation() {
        // To lower the number of DB queries we have to perform, grab all the phone number info
        // up front in a single query and store in a lookup table to perform lookups (in memory)
        // as needed.  Note, this will happen each time the contact list is fetch (reloaded, searched on,
        // etc...)
        Cursor cursor = mContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, QUERY_PROJECTION_PHONE, null, null, null);

        int idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

        Map<Integer, List<Pair<String, Integer>>> numbers = new HashMap<Integer, List<Pair<String, Integer>>>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(idIndex);
            if (!numbers.containsKey(id)) {
                // create a new entry
                numbers.put(id, new LinkedList<Pair<String, Integer>>());
            }

            numbers.get(id).add(new Pair<String, Integer>(cursor.getString(phoneIndex), cursor.getInt(typeIndex)));
        }

        return numbers;
    }
}
