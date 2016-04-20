package com.mikarney.jernil.checkin;

import android.os.AsyncTask;

/**
 * Created by Jernil on 4/7/2016.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CloudRetrieve extends AppCompatActivity {
    Activity context;
    TextView txtview;
    ProgressDialog pd;
    String user,password;
    boolean trigger;
    Intent i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_decision_factor_2);
        context=this;
        trigger=false;
    }
    public void onStart(){
        super.onStart();
        BackTask bt=new BackTask();
        Intent in = getIntent();
        i=new Intent(CloudRetrieve.this,Main2Activity.class);
        user = in.getStringExtra("user");
        password = in.getStringExtra("password");
        //if(uname.contains(" "))
        //{
        //uname=user.replaceAll(" ","%20");
        //System.out.println("uuuuu"+uname);
        //ucity=ucity.replaceAll(" ","%20");
        //int index=uname.indexOf(" ");
        //}
        bt.execute("http://checkin-mikarney.rhcloud.com/login.php?user=" + user + "&password=" + password );
        /*i.putExtra("ucity",ucity);
        i.putExtra("uemail", uemail);
        i.putExtra("ubloodgroup",ubloodgroup);*/
        //bt.execute("http://maps.googleapis.com/maps/api/geocode/json?address=" + ucity );
        //System.out.println("http://main-trusttransit.rhcloud.com/Update_login.jsp?enterName=" + uname + "&password=" + upass);

    }

    //background process to download the file from internet
    private class BackTask extends AsyncTask<String,Integer,Void> {
        String text="";
        protected void onPreExecute(){
            super.onPreExecute();
            //display progress dialog
            pd = new ProgressDialog(context);
            pd.setTitle("Loading....");
            pd.setMessage("Please wait....");
            pd.setCancelable(true);
            pd.setIndeterminate(false);
            pd.show();

        }



        protected Void doInBackground(String...params){
            URL url;
            try {
                //create url object to point to the file location on internet
                url = new URL(params[0]);
                System.out.println("Helloo"+" "+params[0]);
                //System.out.println("Helloo"+" "+params[1]);
                //make a request to server
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                //get InputStream instance
                InputStream is=con.getInputStream();
                //create BufferedReader object
                BufferedReader br=new BufferedReader(new InputStreamReader(is));
                String line;
                //read content of the file line by line
                while((line=br.readLine())!=null){
                    if (line.contains("true")) {
                        trigger = true;
                        break;
                    }
                }

                br.close();

            }catch (Exception e) {
                e.printStackTrace();
                //close dialog if error occurs
                if(pd!=null) pd.dismiss();
            }

            return null;

        }


        protected void onPostExecute(Void result){
            //close dialog
            if(pd!=null)
                pd.dismiss();
            if(trigger==true)
                startActivity(i);
            else if(trigger==false)
            {
                Toast.makeText(getApplicationContext(), "Sign up Not Successfull - Password Doesn't Match or Some Error! - Press Back",
                        Toast.LENGTH_LONG).show();
            }

            //display read text in TextView
        }


    }
}
