package com.example.nivedhashri.threedtactilemap;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnInitListener {

    Button kettle,whereami,file2;
    InputStream is = null;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    MarkerOptions markerOptions;
    LatLng latLng;
    TextToSpeech tt1;
            Double latitude;
            Double longitude;
            float zoom;
            Locale loc = new Locale("English");
            private int MY_DATA_CHECK_CODE = 0;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_maps);

                //adding a button to connect to kettle
                kettle = (Button)findViewById(R.id.button);
                kettle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = "http://kettle.ubiq.cs.cmu.edu:8080/greeting";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });

                whereami=(Button)findViewById(R.id.button2);
                whereami.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mMap.setMyLocationEnabled(true);
                    }
                });

                file2=(Button)findViewById(R.id.button3);
                file2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        setHomeLocation();
        setUpMapIfNeeded();

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
                //animating to the touched position
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
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

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
            }
        });
        //to check if tts in installed
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    //specking the location
    private void speakLocation(String Location)
    {
        tt1.speak(Location, TextToSpeech.QUEUE_FLUSH, null);
    }

    //setting the home locaiton when the map is loaded
    private void setHomeLocation()
    {
        //values for setting a constant location when the map is loaded
        longitude =-79.9425528;
        latitude =40.4424925;
        zoom =14.0f;
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
                //splitAdress(addressText);
            }
            return removeNumberFromAddress(addressText);
        }

        //removing number from address to provide the street name
        protected String removeNumberFromAddress(String address)
        {
            String result;
            int MAXADDRSIZE=150;
            int incrementor=0;
            char tempCharacter;
            char[] addressCharacterArray;
            char[] resultCharacterArray = new char[MAXADDRSIZE];
            addressCharacterArray=address.toCharArray();
            for (int i=0;i<address.length();i++)
            {
                switch (addressCharacterArray[i])
                {
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
//      mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
//      mMap.
    }
}
