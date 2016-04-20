package com.mikarney.jernil.checkin;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;

import com.mikarney.jernil.checkin.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.location.LocationManager.GPS_PROVIDER;


public class Main2Activity extends AppCompatActivity {
    private static Main2Activity instance;

    public int button_id = 0;
    private static final int CONTACT_PICKER_RESULT = 1001;
    static String DEBUG_TAG = "ShoutOut contact picker";
    String contact[] = new String[3];
    String phone_no[] = new String[3];
    String[] guardian = new String[3];
    String phone = "";
    String name = "";
    boolean filled;
    String proj_name = "";
    private SetupTask setupTask;
    String trigger="false";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Button b1 = (Button) findViewById(R.id.button);

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedpreferences.edit();
        // editor.putString("proj", "shoutout");
        //editor.commit();
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String mPhoneNumber = tMgr.getLine1Number();
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
            e1.setText(contact[0] + " : " + phone_no[0]);
            e2.setText(contact[1] + " : " + phone_no[1]);
            e3.setText(contact[2] + " : " + phone_no[2]);
            //   break;
        }
        proj_name = sharedpreferences.getString("proj", null);
        Log.v(DEBUG_TAG, "proj name:" + proj_name);
        //Log.v(DEBUG_TAG,"ennada");
        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                setupTask = new SetupTask(mPhoneNumber, phone_no);
                setupTask.execute("");

            }

        });
    }
    //@Override
    /*public void onBackPressed() {
        // your code.
        this.finishActivity(123);
    }*/


    public void contact_intent() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                Contacts.CONTENT_URI);
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
                        cursor = getContentResolver().query(Phone.CONTENT_URI,
                                null, Phone.CONTACT_ID + "=?", new String[]{id},
                                null);
                        int nameIdx = cursor.getColumnIndex(Phone.SORT_KEY_PRIMARY);
                        int phoneIdx = cursor.getColumnIndex(Phone.DATA1);


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
                        EditText dispEntry = null;
                        switch (button_id) {
                            case 1:
                                dispEntry = (EditText) findViewById(R.id.editText1);
                                break;
                            case 2:
                                dispEntry = (EditText) findViewById(R.id.editText2);
                                break;
                            case 3:
                                dispEntry = (EditText) findViewById(R.id.editText3);
                        }
                        phone=phone.replaceAll("[()-]","");
                        phone=phone.replaceAll(" ","");
                        dispEntry.setText(name + " : " + phone);
                        editor.putString("name" + button_id, name);
                        editor.putString("phone" + button_id, phone);
                        guardian[button_id - 1] = phone;
                        editor.commit();
                        if (name.length() == 0 && phone.length() == 0) {
                            Toast.makeText(this, "No display entries found for contact.", Toast.LENGTH_LONG).show();
                        }

                    }
                    editor.putBoolean("filled", true);
                    editor.commit();
                    break;
            }

        } else {
            Log.w(DEBUG_TAG, "Warning: activity result not ok");
        }
    }

    public class SetupTask extends AsyncTask<String, Integer, Boolean> {

        private final String g1, g2, g3;
        private final String mPhoneNumber;

        SetupTask(String phone, String[] guardian) {
            g1 = guardian[0];
            g2 = guardian[1];
            g3 = guardian[2];
            Log.v("SetupTask","g1:"+g1+"g2:"+g2+"g3"+g3);
            mPhoneNumber = phone;

        }


        @Override
        protected Boolean doInBackground(String... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            try {
                //create url object to point to the file location on internet
                url = new URL("http://checkin-mikarney.rhcloud.com/setup.php?phone=" + mPhoneNumber +"&gone=" + g1 + "&gtwo=" + g2 + "&gthree=" + g3);
                //Log.v("Test Checkin","Helloo"+" "+" pass:"+mPassword);
                //System.out.println("Helloo"+" "+params[1]);
                //make a request to server
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Log.v("test app", "response:" + con.getResponseCode());
                //get InputStream instance
            InputStream is=con.getInputStream();
            //create BufferedReader object
            BufferedReader br=new BufferedReader(new InputStreamReader(is));
            String line;
            //read content of the file line by line
            while((line=br.readLine())!=null){
                if (line.contains("true")) {
                    Log.v("test app_cloud", line);
                    trigger="true";
                    break;
                }
                else
                {
                    Log.v("inside false loop",trigger);
                    trigger="false";}
            }


                //br.close();

            } catch (Exception e) {
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
            setupTask = null;
            Log.v("Inside post execute",trigger);
            Intent i = new Intent(Main2Activity.this, CheckinActivity.class);
            if (trigger == "true") {
                Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                startActivity(i);
            } else if (trigger == "false") {
                Toast.makeText(getApplicationContext(), "Sorry..Something went wrong!", Toast.LENGTH_SHORT).show();
            }
            //finish();
            else {

            }
        }

        @Override
        protected void onCancelled() {
            setupTask = null;
        }
    }
}
