package com.example.tom.lolhelper;

import android.util.Log;

/**
 * Created by Brian on 10/2/2014.
 */
public class SendEmailTask implements Runnable {

    private static String LOG_TAG = "SendEmailTask";

    private String mSMTPUser;
    private String mSMTPPass;
    private String mSenderEmailAddress;

    private String mEmailRecipient = "";
    private String mEmailSubject = "";
    private String mEmailBody = "";

    // [README] - In EmailSender, the default SMTP server is gmail with auth.
    // Therefore, use a google account to send your email with (it can be
    // the same one you want to use to receive emails).  Note, you do not
    // need to use a gmail account to send or receive emails, but, if you want
    // to use a different SMTP server, then you need to modify EmailSender
    // appropriately.
    public SendEmailTask(String smtp_username, String smtp_password, String sender_email_address) {
        mSMTPUser = smtp_username;
        mSMTPPass = smtp_password;
        mSenderEmailAddress = sender_email_address;
    }

    // [README] - This is what you call to set the body of the email message.  Call this before
    // executing the thread!
    public void setEmailRecipient(String email_recipient){
        mEmailRecipient = email_recipient;
    }

    public void setEmailSubject(String email_subject){
        mEmailSubject = email_subject;
    }

    public void setEmailBody(String emailBody) {
        mEmailBody = emailBody;
    }

    @Override
    public void run() {
        Log.i(LOG_TAG, "Attempting to send email [" + mEmailSubject + "] to " + mEmailRecipient);
        EmailSender m = new EmailSender(mSMTPUser, mSMTPPass); //user and pass
        String[] recipient = {mEmailRecipient};
        m.setTo(recipient);
        m.setFrom(mSenderEmailAddress);
        m.setSubject(mEmailSubject);
        m.setBody(mEmailBody);

        try {
            if(m.send()) {
                Log.i(LOG_TAG, "Email sent successfully");
            } else {
                Log.e(LOG_TAG, "Email did not send");
            }
        } catch(Exception e) {
            Log.e(LOG_TAG, "Email did not send due to Exception", e);
        }
    }
}
