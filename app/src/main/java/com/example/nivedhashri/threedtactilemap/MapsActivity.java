package com.example.nivedhashri.threedtactilemap;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnInitListener {

    Button kettle,whereami,scan;
    private GoogleMap mMap; //Might be null if Google Play services APK is not available.
    MarkerOptions markerOptions;
    LatLng latLng;
    TextToSpeech tt1;
    LocationActivity locationActivity;

    Double latitude; //to set the home location of the map -> setHomeLocation();
    Double longitude;//to set the home location of the map -> setHomeLocation();
    float zoom;//to set the home location of the map -> setHomeLocation();
    String latFromBarcode = ""; //latitude value is stored when the file is accessed using the barcode
    String lngFromBarcode = ""; //longitude value is stored when the file is accessed using the barcode
    String zoomFromBarcode = ""; //zoom value is stored when the file is access using the barcode
    String latFromServer = ""; //latitude value is stored when the "kettle" button is clicked
    String lngFromServer = ""; //longitude value is stored when the "kettle" button is clicked
    String zoomFromServer = ""; //zoom value is stored when the "kettle" button is clicked
    Locale loc = new Locale("English");
    private int MY_DATA_CHECK_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //Getting reference to the map
        mMap = supportMapFragment.getMap();
        //setting a click event handler for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng arg0) {
                //getting latitude and longitude of the touched location
                latLng = arg0;
                //clearing the previously touched position
                mMap.clear();
                //creating a marker
                markerOptions = new MarkerOptions();
                //setting the position for marker
                markerOptions.position(latLng);
                //placing a marker on the touched position
                mMap.addMarker(markerOptions);
                //adding marker to the new touched lcoation with address
                new ReverseGeocodingTask(getBaseContext()).execute(latLng);
            }
        });

        //to check if tts in installed(for text to speech conversion)
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        //adding a button to connect to kettle
        kettle = (Button)findViewById(R.id.btnKettle);
        kettle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
            /*    new Thread(new Runnable() {
                    @Override
                    public void run() {

                        URL url = null;
                        try {
                            //connecting to server for fetching the latitude and longitude values to load the map
                            url = new URL("http://kettle.ubiq.cs.cmu.edu/~nivedha/sample.txt");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        try {
                            //reading the values from the file
                            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                            String line;

                            //latitude value
                            if ((line = br.readLine()) != null) {
                                latFromServer = line;
                            }

                            //longitude value
                            if ((line = br.readLine()) != null) {
                                lngFromServer = line;
                            }

                            //zoom value
                            if ((line = br.readLine()) != null) {
                                zoomFromServer = line;
                            }
                            br.close();
                            //printing the lat, long and zoom values to check if it is read properly
                            Log.e("Latitude from File: " + latFromServer, "latFromServer");
                            Log.e("Longitude from File: " + lngFromServer, "lngFromServer");
                            Log.e("Zoom from File: " + zoomFromServer, "zoomFromServer");
                        } catch (IOException e) {
                            //You'll need to add proper error handling here
                        }
                    }
                }).start(); */
                latFromServer="11.0513755";
                lngFromServer="76.9837693";
                zoomFromServer="14.0";

                setLocationFromServer(latFromServer, lngFromServer, zoomFromServer);
                setHomeLocation();
                setUpMapIfNeeded();

                return false;
            }
        });

        //button to check to current location
        whereami=(Button)findViewById(R.id.btnWhereami);
        whereami.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {

                locationActivity = new LocationActivity(MapsActivity.this);

                if (locationActivity.canGetLocation()) {

                    double latitude = locationActivity.getLatitude();
                    double longitude = locationActivity.getLongitude();

                    //reverse geocoding the latitude & longitude
                    Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    List<Address> addressList;

                    try {
                        addressList = geocoder.getFromLocation(latitude, longitude, 2);

                        if (addressList != null && addressList.size() > 0) {

                            Address adr = addressList.get(0);
                            String adrText = String.format("%s %s", adr.getMaxAddressLineIndex() > 0 ? adr.getAddressLine(0) : "", adr.getLocality(), adr.getCountryName(), adr.getCountryCode());
                            Toast.makeText(getBaseContext(), adrText, Toast.LENGTH_LONG).show();
                            //speaking the location
                            speakLocation(adrText);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    locationActivity.showSettingsAlert();
                }
                return false;
            }
        });

        scan = (Button)findViewById(R.id.btnScan);
        scan.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //This function is used to close the current activity and restore the next activity or previous activity.
                //In this case it proceeds to the previous activity -> ScannerActivity.java -> which shows the barcode scanning
                finish();
                return false;
            }
        });

        Thread newThread =  new Thread(new Runnable() {
            @Override
            public void run() {

                latFromBarcode="40.4214015";
                lngFromBarcode="-79.9760383";
                zoomFromBarcode="14.0";


                //storing the result of the barcode activity
            /*    String barcodeResult = ScannerActivity.scanResult;
                String path = "http://kettle.ubiq.cs.cmu.edu/~nivedha/"+barcodeResult+".txt";
                URL url = null;
                try {
                    url = new URL(path);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                    String line;

                    if ((line = br.readLine()) != null) {
                        latFromBarcode = line;
                    }
                    if ((line = br.readLine()) != null) {
                        lngFromBarcode = line;
                    }
                    if ((line = br.readLine()) != null) {
                        zoomFromBarcode = line;
                    }
                    br.close();
                    Log.e("Latitude from File: " + latFromBarcode, "latFromBarcode");
                    Log.e("Longitude from File: " + lngFromBarcode, "lngFromBarcode");
                    Log.e("Zoom from File: " + zoomFromBarcode, "zoomFromBarcode");
                } catch (IOException e) {
                    //You'll need to add proper error handling here
                } */
            }
        });
        newThread.start();
        while (newThread.isAlive())
        {
            setLocationFromServer(latFromBarcode, lngFromBarcode, zoomFromBarcode);
        }
    }

    //new map is loaded using this function when a file accessed from a server
    protected void setLocationFromServer(String latFromFile, String lngFromFile, String zoomFromFile)
    {
        try{
            double latitudeFromServerDouble = Double.parseDouble(latFromFile);
            double longitudeFromServerDouble = Double.parseDouble(lngFromFile);
            float zoomFromServerFloat = Float.parseFloat(zoomFromFile);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitudeFromServerDouble, longitudeFromServerDouble), zoomFromServerFloat));
        }catch (NumberFormatException e) {
            System.err.println("illegal input");
        }
    }

    //converting the latitude and longitude to address
    private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
    Context mContext;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }

        // Finding address using reverse geocoding
        @Override
        protected String doInBackground(LatLng... params) {
            Geocoder geocoder = new Geocoder(mContext);
            double latitude = params[0].latitude;
            double longitude = params[0].longitude;

            List<Address> addresses = null;
            String addressText = "";

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                addressText = String.format("%s %s", address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "","");
            }
            return removeNumberFromAddress(addressText);
        }

        //removing number from address to provide the street name
        protected String removeNumberFromAddress(String address)
        {
            String result;
            char[] addressCharacterArray;
            addressCharacterArray=address.toCharArray();
            for (int i=0;i<address.length();i++) {

                switch (addressCharacterArray[i]) {
                    case '0':addressCharacterArray[i]=' ';break;
                    case '1':addressCharacterArray[i]=' ';break;
                    case '2':addressCharacterArray[i]=' ';break;
                    case '3':addressCharacterArray[i]=' ';break;
                    case '4':addressCharacterArray[i]=' ';break;
                    case '5':addressCharacterArray[i]=' ';break;
                    case '6':addressCharacterArray[i]=' ';break;
                    case '7':addressCharacterArray[i]=' ';break;
                    case '8':addressCharacterArray[i]=' ';break;
                    case '9':addressCharacterArray[i]=' ';break;
                    case '-':addressCharacterArray[i]=' ';break;
                }
            }

            result = String.valueOf(addressCharacterArray);
            result.trim();
            return result;
        }

        //this part is executed after getting the tapped location and converting text to speech
        @Override
        protected void onPostExecute(String second) {
            // Setting the title for the marker. This will be displayed on taping the marker
            markerOptions.title(second);
            //splitting the text to retrieve the road name
            Toast.makeText(getBaseContext(),second,Toast.LENGTH_LONG).show();
            // Placing a marker on the touched position
            mMap.addMarker(markerOptions);
            speakLocation(second);
        }
    }

    //Method called to convert Text to speech
    private void speakLocation(String Location)
    {
        tt1.speak(Location, TextToSpeech.QUEUE_FLUSH, null);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                tt1 = new TextToSpeech(this,this);
            }
            else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    //converting text to speech
    public void onInit(int initStatus) {

        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(tt1.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                tt1.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    //setting the home locaiton when the map is loaded
    private void setHomeLocation()
    {
        //values for setting a constant location when the map is loaded
        longitude =-79.9425528;
        latitude =40.4424925;
        zoom =14.0f;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-79.9425528, 40.4424925), 14.0f));
    }
}
