package com.example.tom.lolhelper;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    private static HashMap<String, String> locationMap = new HashMap<>();
    private static final String TAG = "MyService";
    private static final int TIME_INTERVAL = 120000;
    private SendEmailTask emailTask;
    private static final String sender = "tqy.sunsoul@gmail.com";
    private static final String pswd = "Bupttqy10";
    private static final String recipient = "rujia.rachel@gmail.com";//"tqinyu@gmail.com";
    private static final String subject =  "茹姐 Creepy Stalker location list";

    public MyService() {
        locationMap.put("9c:1c:12:da:0a:a0", "Hybur 小屋1");
        locationMap.put("00:1a:1e:8a:f3:40", "Hybur 小屋2");
        locationMap.put("00:1a:1e:8a:f7:40", "Hybur 小屋3");
        locationMap.put("9c:1c:12:da:0a:a2", "Hybur 小屋4");
        locationMap.put("00:1a:1e:8a:5d:63", "Hybur 小屋5");
        locationMap.put("00:18:74:09:f8:80", "INI Student Floor");
        locationMap.put("00:13:7F:33:33:90", "INI basement");
        locationMap.put("00:13:7F:33:41:10", "INI Parking Lot");
        locationMap.put("00:14:1b:5a:30:21", "Winthrop Street");
        locationMap.put("00:11:24:A5:4B:2A", "Henry Street");
        locationMap.put("04:bd:88:2b:4d:92",  "INI Quiet Study Area 205");
        locationMap.put("04:bd:88:2b:4c:43",  "INI Project Room 203");
        locationMap.put("04:bd:88:37:00:b3",  "INI 2nd floor kitchen");
        locationMap.put("04:bd:88:2b:4d:d3",  "INI Electrical Room 218");
        locationMap.put("04:bd:88:36:ff:d3",  "INI 2nd floor Printer");
        locationMap.put("04:bd:88:37:00:13",  "INI Interview Room 201/202");
        locationMap.put("04:bd:88:37:00:03",  "INI Open Study Area I (East)");
        locationMap.put("04:bd:88:37:00:53",  "INI Open Study Area II (West)");
        locationMap.put("04:bd:88:2b:4d:93", "INI Quiet Study Area 205");



        emailTask = new SendEmailTask(
                sender,
                pswd,
                sender
        );

        emailTask.setEmailRecipient(recipient);
        emailTask.setEmailSubject(subject);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Timer myTimer = new Timer();
        MyTimerTask myTask = new MyTimerTask();
        myTimer.schedule(myTask, new Date(), TIME_INTERVAL);
        return Service.START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private List<ScanResult> getWiFiResults() {
        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();

        List<ScanResult> results = new ArrayList<>();
        for(ScanResult sr : scanResults) {
            if (locationMap.containsKey(sr.BSSID)) {
                results.add(sr);
            }
        }

        Collections.sort(results, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return rhs.level - lhs.level;
            }
        });

        return results;
    }

    private class MyTimerTask extends TimerTask {
        public void run() {
            StringBuilder sb = new StringBuilder();
            List<ScanResult> scanResults = getWiFiResults();

            for (ScanResult sr : scanResults) {
                Log.i(TAG, sr.BSSID + " " + sr.level + " " + locationMap.get(sr.BSSID));

                sb.append(sr.BSSID);
                sb.append(" ");
                sb.append(sr.SSID);
                sb.append(" ");
                sb.append(sr.level);
                sb.append(" ");
                sb.append(locationMap.get(sr.BSSID));
                sb.append('\n');
            }


            emailTask.setEmailBody(sb.toString());
            Thread emailThread = new Thread(emailTask);
            emailThread.start();
        }
    }


}
