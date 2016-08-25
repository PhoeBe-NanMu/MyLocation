package com.example.leiyang.mylocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView locationTextView;
    private TextView locationTextView1;
    private LocationManager locationManager;
    private String provider;
    private final int SHOW_LOCATION=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationTextView = (TextView) findViewById(R.id.location);
        locationTextView1 = (TextView) findViewById(R.id.location1);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(locationManager.NETWORK_PROVIDER)) {
            provider = locationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(locationManager.GPS_PROVIDER)) {
            provider = locationManager.GPS_PROVIDER;
        } else {
            Toast.makeText(this, "No location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            showLocation(location);
        }

        locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);

    }


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void showLocation(final Location location) {
        /*注意：Latitude：纬度
        *       Longitude：经度
        *       Altitude：海拔（不能获取）*/
        String currentPosition = "Latitude is " + location.getLatitude() + "\n" + "Longitude is " + location.getLongitude();
        locationTextView.setText(currentPosition);

        /*显示从服务器返回的位置信息*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                StringBuilder urlString = new StringBuilder();
                urlString.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
                urlString.append(location.getAltitude()+",");
                urlString.append(location.getLongitude());
                urlString.append("&sensor=false");
                Log.i("info",urlString.toString());
                try {
                    URL url = new URL(urlString.toString());
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = null;
                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    if (jsonArray.length() > 0) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                        String address = jsonObject1.getString("formatted_address");
                        Message message = new Message();
                        message.what = SHOW_LOCATION;
                        message.obj = address.toString();
                        handler.sendMessage(message);
                    }


                } catch (MalformedURLException e) {
                            e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();


    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SHOW_LOCATION) {
                String info = (String) msg.obj;
                locationTextView1.setText(info);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListener);
        }
    }
}
