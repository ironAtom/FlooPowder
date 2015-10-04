package edu.cmu.wnss.funktastic.superawesomecontacts;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

/**
 * Created by Funky McAwesome on 9/4/2014.
 */

public class ContactListHandler extends Handler {

    private TextView mContext;

    public ContactListHandler(TextView context) {
        mContext = context;
    }

    @Override
    public void handleMessage(Message msg) {
        mContext.setText(msg.getData().getString(PopulateContactList.HANDLER_BUNDLE_KEY));
    }
}
