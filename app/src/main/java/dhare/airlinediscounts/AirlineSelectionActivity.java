package dhare.airlinediscounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class AirlineSelectionActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private String mClosestAirport;
    private double mUserLatitude;
    private double mUserLongitude;

    public int screenWidth;
    public int portionOfScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().hide();
        super.onCreate(savedInstanceState);

        SharedPreferences screenSize = getPreferences(MODE_PRIVATE);
        screenWidth = screenSize.getInt("Width", 0);
        portionOfScreen = screenSize.getInt("Portion", 0);

        if (screenWidth == 0 || portionOfScreen == 0) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
            portionOfScreen = (size.x / 3) * 2;
            SharedPreferences.Editor editor = screenSize.edit();
            editor.putInt("Width", screenWidth);
            editor.putInt("Portion", portionOfScreen);
            editor.apply();
        }

        setContentView(R.layout.activity_airline_selection);
        ListView listView = (ListView) findViewById(R.id.list);
        //    listView.setBackgroundColor(Color.BLACK);
        int[] colors = {0, getResources().getColor(R.color.border_gray), getResources().getColor(R.color.border_gray), getResources().getColor(R.color.border_gray), 0};
        listView.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
        listView.setDividerHeight(3);

        View footerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer, null, false);
        listView.addFooterView(footerView);

        //Populate and initialise arraylist for all the airlines
        final ArrayList<Airline> ALL_AIRLINES = new ArrayList<Airline>() {{
            add(new Airline("Air Canada", "air_canada"));
            add(new Airline("American Airlines", "american_airlines"));
            add(new Airline("Alaska Airlines", "alaska_airlines"));
            add(new Airline("Delta Airlines", "delta_airlines"));
            add(new Airline("JetBlue Airways", "jetblue_airways"));
            add(new Airline("Southwest Airlines", "southwest_airlines"));
            add(new Airline("United Airlines", "united_airlines"));
            add(new Airline("WestJet", "westjet"));
        }};
        // Create the adapter to convert the array to views
        AirlinesAdapter adapter = new AirlinesAdapter(this, ALL_AIRLINES, screenWidth, portionOfScreen);

        // Attach the adapter to a ListView
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                Intent main = new Intent(AirlineSelectionActivity.this, WebActivity.class);
                main.putExtra("AIRPORT NAME", mClosestAirport);
                Airline a = (Airline) adapter.getItemAtPosition(position);
                main.putExtra("AIRLINE NAME", a.name);
                startActivity(main);
            }
        });


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLocation == null) {
            LocationRequest mLocationRequest = LocationRequest.create()
                    .setInterval(10000)
                    .setFastestInterval(5000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(mLocation);
        }
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        if (!prefs.contains(key))
            return defaultValue;
        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    private void handleNewLocation(Location location) {
        SharedPreferences lastLocationAndAirport = getPreferences(MODE_PRIVATE);
        double lastLatitude = getDouble(lastLocationAndAirport, "Latitude", 0);
        double lastLongitude = getDouble(lastLocationAndAirport, "Longitude", 0);

        if (location.getLatitude() < lastLatitude + 0.5 && location.getLatitude() > lastLatitude - 0.5 &&
                location.getLongitude() < lastLongitude + 0.5 && location.getLongitude() > lastLongitude - 0.5) {
            mClosestAirport = lastLocationAndAirport.getString("Airport", "");
        } else {
            String placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
                    "json?location=" + location.getLatitude() + "," + location.getLongitude() +
                    //"&rankby=distance" +
                    "&radius=50000" +
                    "&types=airport" +
                    "&key=AIzaSyC3Cs3jkSUQjg1lpAPV-viGXeQBTANC2PI";

            new GetPlaces().execute(placesSearchStr);
            mUserLatitude = location.getLatitude();
            mUserLongitude = location.getLongitude();
        }

        /*Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            String cityName = addresses.get(0).getLocality();
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
            tAirportFound = true;
        }*/
    }

    private class GetPlaces extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... placesURL) {
            //fetch places

            //build result as string
            StringBuilder placesBuilder = new StringBuilder();
            //process search parameter string(s)
            for (String placeSearchURL : placesURL) {
                HttpClient placesClient = new DefaultHttpClient();
                try {
                    //try to fetch the data

                    //HTTP Get receives URL string
                    HttpGet placesGet = new HttpGet(placeSearchURL);
                    //execute GET with Client - return response
                    HttpResponse placesResponse = placesClient.execute(placesGet);
                    //check response status
                    StatusLine placeSearchStatus = placesResponse.getStatusLine();
                    //only carry on if response is OK
                    if (placeSearchStatus.getStatusCode() == 200) {
                        //get response entity
                        HttpEntity placesEntity = placesResponse.getEntity();
                        //get input stream setup
                        InputStream placesContent = placesEntity.getContent();
                        //create reader
                        InputStreamReader placesInput = new InputStreamReader(placesContent);
                        //use buffered reader to process
                        BufferedReader placesReader = new BufferedReader(placesInput);
                        //read a line at a time, append to string builder
                        String lineIn;
                        while ((lineIn = placesReader.readLine()) != null) {
                            placesBuilder.append(lineIn);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return placesBuilder.toString();
        }

        //process data retrieved from doInBackground
        protected void onPostExecute(String result) {
            //parse place data returned from Google Places
            try {
                //create JSONObject, pass string returned from doInBackground
                JSONObject resultObject = new JSONObject(result);
                //get "results" array
                JSONArray placesArray = resultObject.getJSONArray("results");
                mClosestAirport = placesArray.getJSONObject(0).getString("name");

                SharedPreferences lastLocationAndAirport = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = lastLocationAndAirport.edit();
                editor.putString("Airport", mClosestAirport);
                putDouble(editor, "Latitude", mUserLatitude);
                putDouble(editor, "Longitude", mUserLongitude);
                editor.apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        handleNewLocation(location);
    }

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }
}
