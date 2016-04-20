package com.mikarney.jernil.checkin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static android.location.LocationManager.GPS_PROVIDER;

public class CheckinActivity extends AppCompatActivity {

    public int button_id = 0;
    private static final int CONTACT_PICKER_RESULT = 1001;
    static String DEBUG_TAG = "ShoutOut contact picker";
    String trigger="";
    String contact[]=new String[3];
    private Timer timer;
    String phone = "";
    String name = "";
    String mPhoneNumber="";
    String checkinno="";
    private TimerTask timerTask;
    String phone_no[]=new String[3];
    private CheckinTask checkinTask=null;
    private static String Imei = null;
    protected LocationManager locationManager;
    Location location;
    String save_line="";
    protected LocationListener locationListener;
    protected Context context;
    TextView txtLat;
    String lat;
    String provider;
    protected double latitude,longitude;
    protected boolean gps_enabled,network_enabled;
    String okay_tag="";
    String new_tag="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneNumber = tMgr.getLine1Number();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
       // final SharedPreferences.Editor editor = sharedpreferences.edit();
        boolean filled;

        // editor.putString("proj", "shoutout");
        //editor.commit();
        filled = sharedpreferences.getBoolean("filled", false);
        if (filled) {
            //    contact[1]=sharedpreferences.getString("name1",null);
            //    contact[2]=sharedpreferences.getString("name2",null);
            //    contact[3]=sharedpreferences.getString("name3",null);
            contact[0] = sharedpreferences.getString("name1", null);
            contact[1] = sharedpreferences.getString("name2", null);
            contact[2] = sharedpreferences.getString("name3", null);
            phone_no[0] = sharedpreferences.getString("phone1", null);
            phone_no[1] = sharedpreferences.getString("phone2", null);
            phone_no[2] = sharedpreferences.getString("phone3", null);
            EditText e1 = (EditText) findViewById(R.id.editText1);
            EditText e2 = (EditText) findViewById(R.id.editText2);
            EditText e3 = (EditText) findViewById(R.id.editText3);
            //e1.setText(contact[0] + " : " + phone_no[0]);
            //e2.setText(contact[1] + " : " + phone_no[1]);
            //e3.setText(contact[2] + " : " + phone_no[2]);
            e1.setText(contact[0]);
            e2.setText(contact[1]);
            e3.setText(contact[2]);
            //   break;
        }
        Button b2=(Button) findViewById(R.id.button12);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //SmsManager smsManager = SmsManager.getDefault();
                okay_tag="1";
                new_tag="2";
                Toast.makeText(getApplicationContext(),"We'll let them know you're okay! :)",Toast.LENGTH_SHORT).show();
            }
        });
        checkinno=sharedpreferences.getString("checkinno",null);
        new_tag="1";
        try {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {

                    checkinTask=new CheckinTask("1");
                    checkinTask.execute("");
                    //Download file here and refresh
                }
            };
            timer.schedule(timerTask, 10000, 10000);
        } catch (IllegalStateException e){
            android.util.Log.i("Damn", "resume error");
        }

    }
    public void contact_intent() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }


    public void doLaunchContactPicker(View view) {
        // Here, thisActivity is the current activity
        switch (view.getId()) {
            case R.id.button3:
                button_id = 1;
                break;
            case R.id.button4:
                button_id = 2;
                break;
            case R.id.button5:
                button_id = 3;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        123);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else
            contact_intent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    contact_intent();
                }
            }
            break;

        }
        // else {
        // permission denied, boo! Disable the
        // functionality that depends on this permission.
        // }
        return;
    }

    // other 'case' lines to check for other
    // permissions this app might request
    //     }
    //  }}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //final SharedPreferences sharedpreferences = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedpreferences.edit();
        EditText dispEntry = null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    Cursor cursor = null;
                    String email = "";
                    try {
                        Uri result = data.getData();
                        Log.v(DEBUG_TAG, "Got a contact result: "
                                + result.toString());

                        // get the contact id from the Uri
                        String id = result.getLastPathSegment();

                        // query for everything email
                        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id},
                                null);
                        int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
                        int phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1);


                        if (cursor.moveToFirst()) {
                            name = cursor.getString(nameIdx);
                            phone = cursor.getString(phoneIdx);
                            Log.v(DEBUG_TAG, "Got name: " + name);
                            Log.v(DEBUG_TAG, "Got phone: " + phone);
                        } else {
                            Log.w(DEBUG_TAG, "No results");
                        }
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "Failed to get name and number data", e);
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                        dispEntry = (EditText) findViewById(R.id.editText);
                        }
                        phone=phone.replaceAll("[()-]","");
                        phone=phone.replaceAll(" ","");
                        dispEntry.setText(name);
                        editor.putString("checkinname", name);
                        editor.putString("checkinno", phone);
                        //guardian[button_id - 1] = phone;
                        editor.commit();
                        if (name.length() == 0 && phone.length() == 0) {
                            Toast.makeText(this, "No display entries found for contact.", Toast.LENGTH_LONG).show();
                        }

                    }
                    editor.putBoolean("filled", true);
                    editor.commit();
            new_tag="3";
            checkinTask=new CheckinTask("3");
            checkinTask.execute("");

            }
            else {
            Log.w(DEBUG_TAG, "Warning: activity result not ok");
        }
    }
public class CheckinTask extends AsyncTask<String, Integer, Boolean> implements LocationListener{

    //private final String g1,g2,g3;
    public String Tag;

    CheckinTask(String tag) {

        //Tag=tag;
        Tag=new_tag;

    }


    public void send_sms(String p,String okay){


        SmsManager smsManager = SmsManager.getDefault();
        TelephonyManager tManager= (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Imei = tManager.getDeviceId();
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(GPS_PROVIDER,0,0,this);

        if (lm != null) {
            location = lm.getLastKnownLocation(GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {

                Log.e("Printf", "Location returned null");

            }
        } else {
            Log.e("printf", "Location Manager returned null");
        }

        try{
            if(okay=="1")
            { smsManager.sendTextMessage(p, null, "Sent from Check-in app: The user wants to let you know that he's okay", null, null);}
            else
            {smsManager.sendTextMessage(p, null, "Sent from Check-in app:Imei:"  + Imei + " http://maps.google.com/maps?z=12&t=m&q=loc:"+ Double.toString(latitude) + "+" + Double.toString(longitude), null, null);}

            //Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();

            Log.v("debug_tag", "inside sms intent");
        }
        catch (Exception e) {
            // Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    public void onLocationChanged(Location location) {

        txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
    }


    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }


    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }


    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }
    @Override
    protected Boolean doInBackground(String... params) {
        // TODO: attempt authentication against a network service.
        //String trigger="";
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(CheckinActivity.this);
        SharedPreferences.Editor editor=pref.edit();
        URL url;
        try {
            if(Tag=="1") {
                //create url object to point to the file location on internet
                url = new URL("http://checkin-mikarney.rhcloud.com/check.php?phone=" + mPhoneNumber);
                //Log.v("Test Checkin", "Helloo" + " " + " pass:" + mPassword);
                //System.out.println("Helloo"+" "+params[1]);
                //make a request to server
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Log.v("test app", "response:" + con.getResponseCode());
                //get InputStream instance
                InputStream is = con.getInputStream();
                //create BufferedReader object
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                //read content of the file line by line
                editor.putString("g1", "false");
                editor.putString("g2", "false");
                editor.putString("g3", "false");
                while ((line = br.readLine()) != null) {
                    if (line.contains("guardian1")) {
                        Log.v("test app", line);
                        trigger = "g1";
                        editor.putString("g1", "true");
                        //break;
                    }
                    if (line.contains("guardian2"))
                    {trigger = "g2";
                        editor.putString("g2","true");
                       // break;
                    }
                    if (line.contains("guardian3"))
                    {trigger = "g3";
                        editor.putString("g3","true");}
                   // save_line=line;
                }

                editor.commit();

                Log.v("test app", "read!!"+trigger+pref.getString("g1","false"));

            }else if(Tag=="2"){
                String phone_no[]={pref.getString("phone1",null),pref.getString("phone2",null),pref.getString("phone3",null)};
                url = new URL("http://checkin-mikarney.rhcloud.com/cancel.php?phone=" + mPhoneNumber+"&gone="+phone_no[0]+"&gtwo="+phone_no[1]+"&gthree="+phone_no[2]);
                Log.v("bgtask cancel debug",url.toString());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Log.v("test app", "response:" + con.getResponseCode());
                //get InputStream instance
                InputStream is = con.getInputStream();
                //create BufferedReader object
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                String save_line;
                //read content of the file line by line
                while ((line = br.readLine()) != null) {
                    if (line.contains("true")) {
                        trigger = "true";
                        Log.v("checkin cancel block", trigger);
                        editor.putString("g1", "false");
                        editor.putString("g2", "false");
                        editor.putString("g3", "false");
                        editor.commit();
                        break;
                    } else {
                        trigger = "false";
                    }
                }
                new_tag="1";
            }
            else {
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(CheckinActivity.this);
                URL u2 = new URL("http://checkin-mikarney.rhcloud.com/checkin.php?phone=" + mPhoneNumber+"&contact="+sharedpreferences.getString("checkinno",null));
                Log.v("bgtask debug",u2.toString());
                HttpURLConnection con = (HttpURLConnection) u2.openConnection();
                Log.v("test app", "response:" + con.getResponseCode());
                //get InputStream instance
                InputStream is = con.getInputStream();
                //create BufferedReader object
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                String save_line;
                //read content of the file line by line
                while ((line = br.readLine()) != null) {
                    if (line.contains("checkin")) {
                        trigger = "checkin_sent";
                        Log.v("checkin block",trigger);
                        break;
                        }
                        else if(line.contains("false")){
                        trigger = "checkin_notsent";
                        Log.v("checkin block",trigger);
                        break;
                        }
                        else
                        {
                        trigger=line;}
                    }
                }
                //br.close();
        }catch (Exception e) {
            e.printStackTrace();
            //close dialog if error occurs
            //  if(pd!=null) pd.dismiss();
        }
        return null;
        //return null;

        // TODO: register the new account here.
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(CheckinActivity.this);
        Log.v("test app", "inside post execute!!"+trigger);
        checkinTask = null;
        String cond1=pref.getString("g1", "false");
        String cond2=pref.getString("g2", "false");
        String cond3=pref.getString("g3", "false");
        Log.v("test app", "inside post execute!!"+trigger+cond1);
        if (cond1.equals("true"))
        {
            Log.v("test app", "inside g1!!" + trigger);
            TextView t=(TextView)findViewById(R.id.textView7);
            t.setTextColor(Color.RED);
            t.setText("has checked in on you!!");
            send_sms(pref.getString("phone1",null),okay_tag);
            new_tag="2";
        }
        else if (cond1.equals("false"))
        {
            TextView t=(TextView)findViewById(R.id.textView7);
            t.setTextColor(Color.LTGRAY);
            t.setText("no checkins");
        }
        if(cond2.equals("true"))
        {
            Log.v("test app", "inside g2!!"+trigger);
            TextView t=(TextView)findViewById(R.id.textView8);
            //EditText e=(EditText) findViewById(R.id.editText);
            t.setTextColor(Color.RED);
            t.setText("has checked in on you!!");
            Log.v("app", "g2 has checked in!");
            send_sms(pref.getString("phone2", null),okay_tag);
            new_tag="2";
        }
        else if (cond2.equals("false"))
        {
            TextView t=(TextView)findViewById(R.id.textView8);
            t.setTextColor(Color.LTGRAY);
            t.setText("no checkins");
        }
        if(cond3.equals("true"))
        {
            TextView t=(TextView)findViewById(R.id.textView9);
            t.setTextColor(Color.RED);
            t.setText("has checked in on you!!");
            send_sms(pref.getString("phone3",null),okay_tag);
            new_tag="2";
            //finish();
        }
        else if (cond3.equals("false"))
        {
            TextView t=(TextView)findViewById(R.id.textView9);
            t.setTextColor(Color.LTGRAY);
            t.setText("no checkins");
        }
        if(trigger=="checkin_sent")
        {
            Toast.makeText(getApplicationContext(),"Checkin request sent!",Toast.LENGTH_SHORT).show();
            TextView t=(TextView)findViewById(R.id.textView5);
            t.setText("Checkin sent!");
        }
        //finish();
        else if(trigger=="checkin_notsent"){
            Toast.makeText(getApplicationContext(),"Checkin request could not be sent!",Toast.LENGTH_SHORT).show();
            TextView t=(TextView)findViewById(R.id.textView5);
            t.setText("Checkin not sent!");
            //mPasswordView.setError(getString(R.string.error_incorrect_password));
           // mPasswordView.requestFocus();
        }
        else{

        }
    }

    @Override
    protected void onCancelled() {
        checkinTask = null;
    }
}
}