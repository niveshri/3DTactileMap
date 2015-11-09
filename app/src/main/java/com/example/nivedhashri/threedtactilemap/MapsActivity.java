package com.example.nivedhashri.threedtactilemap;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnInitListener {

    Button kettle,whereami,scan;
    private GoogleMap mMap; //Might be null if Google Play services APK is not available.
    MarkerOptions markerOptions;
    LatLng latLng;
    TextToSpeech tt1;
    LocationActivity locationActivity;
    int MAX_BUTTONS = 10;
    long mapNumber = 1;
    final static long ONE = 1;
    final static long TWO = 2;
    final static long THREE = 3;

    Double latitude = 0.0; //to set the home location of the map -> setHomeLocation();
    Double longitude = 0.0;//to set the home location of the map -> setHomeLocation();
    float zoom;//to set the home location of the map -> setHomeLocation();
    String latFromBarcode = ""; //latitude value is stored when the file is accessed using the barcode
    String lngFromBarcode = ""; //longitude value is stored when the file is accessed using the barcode
    String zoomFromBarcode = ""; //zoom value is stored when the file is access using the barcode
    String latFromServer = ""; //latitude value is stored when the "kettle" button is clicked
    String lngFromServer = ""; //longitude value is stored when the "kettle" button is clicked
    String zoomFromServer = ""; //zoom value is stored when the "kettle" button is clicked
    Locale loc = new Locale("English");
    private int MY_DATA_CHECK_CODE = 0;
    private final float ZERO = 0;

    private final String URL_SERVER_BUTTON_DETAILS_PREFIX = "http://kettle.ubiq.cs.cmu.edu/~nivedha/";
    private String URL_SERVER_BUTTON_DETAILS_FILENAME = "ip.txt";
    private List<Button> buttonList;
    private int buttonCount = 0;
    float[] buttonXCoordinates = new float[50];
    float[] buttonYCoordinates = new float[50];
    String[] buttonValue = new String[50];
    private static final int[] buttonIDs = {
            R.id.button1,
            R.id.button2,
            R.id.button3,
            R.id.button4,
            R.id.button5,
            R.id.button6,
            R.id.button7,
            R.id.button8,
            R.id.button9,
            R.id.button10
    };

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

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
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

                latFromServer = "38.7257";
                lngFromServer = "-9.14855";
                zoomFromServer = "14.9";
                setLocationFromServer(latFromServer, lngFromServer, zoomFromServer);

                setUpMapIfNeeded();
                int temp = 1;
                new ButtonDataRetrievalTask(temp).execute();
                return false;
            }
        });

        //button to check the current location
        whereami=(Button)findViewById(R.id.btnWhereami);
        whereami.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {

                /*
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
                */
                latFromServer = "38.7275";
                lngFromServer = "-9.1504";
                zoomFromServer = "15.6";

                setLocationFromServer(latFromServer, lngFromServer, zoomFromServer);
                setUpMapIfNeeded();

                int temp = 2;
                new ButtonDataRetrievalTask(temp).execute();
                return false;
            }
        });

        //button to scan another barcode to change the displayed map
        scan = (Button)findViewById(R.id.btnScan);
        scan.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //This function is used to close the current activity and restore the next activity or previous activity.
                //In this case it proceeds to the previous activity -> ScannerActivity.java -> which shows the barcode scanning
                /*
                finish();
                 */

                latFromServer = "40.45165";
                lngFromServer = "-79.9327";
                zoomFromServer = "15.5";

                setLocationFromServer(latFromServer, lngFromServer, zoomFromServer);
                setUpMapIfNeeded();
                int temp = 3;
                new ButtonDataRetrievalTask(temp).execute();
                return false;
            }
        });

        Thread newThread =  new Thread(new Runnable() {
            @Override
            public void run() {

                Long barcodeResult = Long.parseLong(ScannerActivity.scanResult);
                //We could make a switch case out of the barcodeResult to make sure different values are taken for different scan results.
                //If we also update the URL_BUTTON_DETAILS_FILENAME with the barcodeResult then we can access the buttons of the specific file.
                if(barcodeResult==ONE) {
                    latFromBarcode = "38.7257";
                    lngFromBarcode = "-9.14855";
                    zoomFromBarcode = "14.9";
                }
                else if(barcodeResult==TWO) {
                    latFromBarcode = "38.7275";
                    lngFromBarcode = "-9.1504";
                    zoomFromBarcode = "15.6";
                }
                else if(barcodeResult==THREE) {
                    latFromBarcode = "40.45165";
                    lngFromBarcode = "-79.9327";
                    zoomFromBarcode = "15.5";
                }
                else {
                    latFromBarcode = "0.0";
                    lngFromBarcode = "0.0";
                    zoomFromBarcode = "1";
                }

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

        //Starts to get the button details from the server
        new ButtonDataRetrievalTask(Long.parseLong(ScannerActivity.scanResult)).execute();
        //new ButtonDataRetrievalTask(4).execute();

        /*
        Button tempButton = (Button) findViewById(R.id.button1);
        float buttonX = 160;
        float buttonY = 160;
        tempButton.setX((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, buttonX, getResources().getDisplayMetrics()));
        tempButton.setY((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, buttonY, getResources().getDisplayMetrics()));
        tempButton.setVisibility(View.VISIBLE);
        tempButton.setEnabled(true);
        tempButton.setOnClickListener(buttonHandler);
        */
    }

    //This class is used to run the Async Task of getting the details of the buttons to be displayed and after it is done, it does the setting up of buttons
    private class ButtonDataRetrievalTask extends AsyncTask<Void, Void, String>
    {
        public ButtonDataRetrievalTask(long mapNumberParam)
        {
            super();
            mapNumber = mapNumberParam;


            //Resetting buttons
            for (int i = 0; i < MAX_BUTTONS; i++) {
                Button tempButton = (Button) findViewById(buttonIDs[i]);
                tempButton.setX((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()));
                tempButton.setY((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()));
                tempButton.setText("");
                tempButton.setVisibility(View.INVISIBLE);
                tempButton.setEnabled(false);
            }


            if(mapNumber==ONE) {
                buttonValue[0] = "San Liboa Hotel";
                buttonXCoordinates[0] = (float) 35.3708609272;
                buttonYCoordinates[0] = (float) 46.2222222222;
                buttonValue[1] = "Hospital Miguel Bombarda";
                buttonXCoordinates[1] = (float) 65.1927152318;
                buttonYCoordinates[1] = (float) 37.9;
                buttonValue[2] = "Parque Eduardo VII";
                buttonXCoordinates[2] = (float) 16.7026490066;
                buttonYCoordinates[2] = (float) 50.6388888889;
                buttonValue[3] = "Subway - Parque (Blue)";
                buttonXCoordinates[3] = (float) 26.7993377483;
                buttonYCoordinates[3] = (float) 60.4055555555;
                buttonValue[4] = "Subway - Picoas";
                buttonXCoordinates[4] = (float) 44.9529801325;
                buttonYCoordinates[4] = (float) 62.6333333333;
                buttonValue[5] = "Subway - Avenida";
                buttonXCoordinates[5] = (float) 47.6880794702;
                buttonYCoordinates[5] = (float) 3.76666666667;
                buttonValue[6] = "Subway - Rato";
                buttonXCoordinates[6] = (float) 9.58675496688;
                buttonYCoordinates[6] = (float) 6.40555555557;
                buttonValue[7] = "Subway - Marques de Pombal";
                buttonXCoordinates[7] = (float) 24.9079470199;
                buttonYCoordinates[7] = (float) 32.9666666666;
                buttonCount = 8;
            }
            else if(mapNumber==TWO) {
                buttonValue[0] = "San Liboa Hotel";
                buttonXCoordinates[0] = (float) 49.850877193;
                buttonYCoordinates[0] = (float) 35.1627906977;
                buttonValue[1] = "Subway - Marques de Pombal";
                buttonXCoordinates[1] = (float) 32.9921052632;
                buttonYCoordinates[1] = (float) 11.7418604651;
                buttonValue[2] = "Parque Eduardo VII";
                buttonXCoordinates[2] = (float) 20.1236842105;
                buttonYCoordinates[2] = (float) 43.6337209303;
                buttonValue[3] = "Subway - Parque (Blue)";
                buttonXCoordinates[3] = (float) 35.4973684211;
                buttonYCoordinates[3] = (float) 61.9430232558;
                buttonValue[4] = "Subway - Picoas";
                buttonXCoordinates[4] = (float) 65.5429824561;
                buttonYCoordinates[4] = (float) 65.2069767442;
                buttonCount = 5;
            }
            else if(mapNumber==THREE) {
                buttonValue[0] = "5th and Aiken";
                buttonXCoordinates[0] = (float) 24.5;
                buttonYCoordinates[0] = (float) 4.7;
                buttonValue[1] = "5th and Negley";
                buttonXCoordinates[1] = (float) 56.1;
                buttonYCoordinates[1] = (float) 18.1;
                buttonValue[2] = "Walnut and Aiken";
                buttonXCoordinates[2] = (float) 15.7;
                buttonYCoordinates[2] = (float) 25.6;
                buttonValue[3] = "Walnut and Negley";
                buttonXCoordinates[3] = (float) 46.9;
                buttonYCoordinates[3] = (float) 38.9;
                buttonValue[4] = "Ellsworth and Aiken";
                buttonXCoordinates[4] = (float) 7.5;
                buttonYCoordinates[4] = (float) 45.3;
                buttonValue[5] = "Ellsworth and Negley";
                buttonXCoordinates[5] = (float) 35.9;
                buttonYCoordinates[5] = (float) 63.9;
                buttonCount = 6;
            }
            else{
                buttonCount=0;
            }
        }

        @Override
        protected String doInBackground(Void... voids) {

            /*
            //This code calls kettle and gets the file for Interested Places
            buttonCount = 0;
            Thread buttonSetupThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    URL url = null;
                    try {
                        //connecting to server for fetching the Interested Places Coordinates to load the map
                        url = new URL(URL_SERVER_BUTTON_DETAILS_PREFIX + URL_SERVER_BUTTON_DETAILS_FILENAME);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    try {
                        //reading the values from the file
                        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                        String line;
                        String[] parts = new String[3];
                        int iterator;


                        //Read the line and loop only if a line is present.
                        for (iterator = 0; (line = br.readLine()) != null ; iterator++) {
                            Log.e("Line from Server:",line);
                            //Check if the line contains a comma ','
                            if (line.contains(",")) {
                                //Split the line and store it in a String Array
                                parts = line.split("[,]");
                                //Check if there are 3 strings after splitting
                                if(parts.length==3) {
                                    //Assign the Value, X coordinate and Y coordinate respectively using iterator
                                    buttonValue[iterator] = parts[0];
                                    buttonXCoordinates[iterator] = Float.parseFloat(parts[1]);
                                    buttonYCoordinates[iterator] = Float.parseFloat(parts[2]);
                                }
                            }
                        }
                        //Now the iterator value should be the number of buttons to be set
                        buttonCount=iterator;

                        br.close();
                        //printing the last values to check if it is read properly
                        Log.e("Name: ", ""+buttonValue[buttonCount-1]);
                        Log.e("X Coordinate: ", String.valueOf(buttonXCoordinates[buttonCount - 1]));
                        Log.e("Y Coordinate: ", String.valueOf(buttonYCoordinates[buttonCount - 1]));
                    } catch (IOException e) {
                        //You'll need to add proper error handling here
                    }
                }
            });
            buttonSetupThread.start();
            */
            return null;
        }

        protected void onPostExecute(String second) {
            if (buttonCount > 0 && buttonIDs.length >= buttonCount) {
                for (int i = 0; i < buttonCount; i++) {
                    Button tempButton = (Button) findViewById(buttonIDs[i]);
                    buttonXCoordinates[i] = (buttonXCoordinates[i] / 70) * 320;
                    buttonYCoordinates[i] = (buttonYCoordinates[i] / 70) * 320;
                    tempButton.setX((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, buttonXCoordinates[i], getResources().getDisplayMetrics()));
                    tempButton.setY((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 340 - buttonYCoordinates[i], getResources().getDisplayMetrics()));
                    tempButton.setText(buttonValue[i]);
                    tempButton.setTextSize(ZERO);
                    tempButton.setVisibility(View.VISIBLE);
                    tempButton.setEnabled(true);
                    //Comment the background color to see the buttons and make changes to their position
                    //tempButton.setBackgroundColor(Color.TRANSPARENT);
                    tempButton.setOnClickListener(buttonHandler);
                    Log.e("Name: ", "" + buttonValue[i]);
                    Log.e("X Coordinate: ", String.valueOf(buttonXCoordinates[i]));
                    Log.e("Y Coordinate: ", String.valueOf(buttonYCoordinates[i]));
                }
            } else {
                if(buttonCount == 0)
                {
                    Log.e("Number of buttons", String.valueOf(buttonCount));
                }
                else
                {
                    Log.e("Places count exceeds", "Interested Places count exceeds number of buttons present in screen");
                }
            }
        }
    }

    private View.OnClickListener buttonHandler = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            Button temporaryButton = (Button)findViewById(v.getId());
                    speakLocation((String) temporaryButton.getText());
        }
    };

    //new map is loaded using this function when a file accessed from a server
    protected void setLocationFromServer(String latFromFile, String lngFromFile, String zoomFromFile)
    {
        try{
            double latitudeFromServerDouble = Double.parseDouble(latFromFile);
            double longitudeFromServerDouble = Double.parseDouble(lngFromFile);
            latitude=latitudeFromServerDouble;
            longitude=longitudeFromServerDouble;
            float zoomFromServerFloat = Float.parseFloat(zoomFromFile);
            zoom=zoomFromServerFloat;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
        }catch (NumberFormatException e) {
            System.err.println("Illegal Latitude or Longitude Value ");
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
            if (tt1.isLanguageAvailable(Locale.US) ==TextToSpeech.LANG_AVAILABLE)
                tt1.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    //setting the home locaiton when the map is loaded
   /* private void setHomeLocation()
    {
        //values for setting a constant location when the map is loaded
        longitude =-79.9425528;
        latitude =40.4424925;
        zoom =14.0f;
    } */

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