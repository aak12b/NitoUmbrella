package com.cen.complit.nitoumbrella;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Andrew on 1/28/2015.
 */
public class clarifications_Fragment extends Fragment{

    public String purl = "http://hvz.sabaduy.com/api/" + ServiceHandler.APIKEY + "/user/profile";
    public String url = "http://hvz.sabaduy.com/api/" + ServiceHandler.APIKEY + "/user/cRequestView";
    //Accepts roleId, userId
    //-body-cRequests
    //cRequests in a JSONArray

    private static String TAG_SUCCESS = "success";
    private static String TAG_BODY = "body";
    private static String TAG_ROLE = "roleId";
    private static String TAG_CREQUESTS = "cRequests";
    private static String TAG_USERNAME = "userName";
    private static String TAG_SUBJECT = "subject";
    private boolean status = false;
    private String id,
            role,
            logindata;

    private JSONArray clarificatons;
    private ArrayList<HashMap<String, String>> clarList = new ArrayList<HashMap<String, String>>();;

    View rootview;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        rootview = inflater.inflate(R.layout.clarifications_layout, container, false);


        String filename = "session";
        File myFile = new File(getActivity().getFilesDir(), filename);
        if (myFile.exists()) {
            FileInputStream inputStream;
            try {
                inputStream = getActivity().openFileInput(filename);
                byte[] login_test = new byte[inputStream.available()];
                while (inputStream.read(login_test) != -1) {
                }
                logindata = new String(login_test);
                String[] separated = logindata.split(";");
                id = separated[2];

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        new GetRoleId().execute();

        return rootview;
    }

    private class GetRoleId extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //create service handler

            params.add(new BasicNameValuePair("userId", id));

            ServiceHandler sh = new ServiceHandler();

            //Make request to url
            String jsonStr = sh.makeServiceCall(purl, ServiceHandler.POST, params);
            Log.d("PROFILE", jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    status = jsonObject.getBoolean(TAG_SUCCESS);
                    if (status) {
                        JSONObject body = jsonObject.getJSONObject(TAG_BODY);
                        role = body.getString(TAG_ROLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get data from URL");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            new GetClarifications().execute();

        }
    }

    private class GetClarifications extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("roleId", role));
            params.add(new BasicNameValuePair("userId", id));

            ServiceHandler sh = new ServiceHandler();

            //Make request to url
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.POST, params);
            Log.d("CLARIFICATIONS", jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    status = jsonObject.getBoolean(TAG_SUCCESS);
                    if (status) {
                        JSONObject body = jsonObject.getJSONObject(TAG_BODY);
                        clarificatons = body.getJSONArray(TAG_CREQUESTS);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get data from URL");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            final ListView listview = (ListView) rootview.findViewById(R.id.clarificationlist);

            if (clarificatons != null) {

                // looping through all updates
                for (int i = 0; i < clarificatons.length(); i++) {
                    try {
                        JSONObject data = clarificatons.getJSONObject(i);
                        // tmp hashmap for single update
                        HashMap<String, String> clar = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        clar.put(TAG_SUBJECT, data.getString(TAG_SUBJECT));
                        clar.put(TAG_USERNAME, "Asked by: " + data.getString(TAG_USERNAME));

                        clarList.add(clar);

                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }


            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(getActivity(), clarList,
                    R.layout.list_view, new String[] { TAG_SUBJECT, TAG_USERNAME}, new int[] { R.id.firstLine, R.id.secondLine});

            listview.setAdapter(adapter);


        }

    }

}



