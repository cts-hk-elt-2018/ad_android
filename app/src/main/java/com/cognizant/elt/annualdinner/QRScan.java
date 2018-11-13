package com.cognizant.elt.annualdinner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRScan extends AppCompatActivity {
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String textData = "";
    Switch checkin;
    String token;
    int cam_wid;
    int cam_hei;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);

        initViews();

        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");

        checkin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkin.isChecked()){
                    //check in again
                    CheckInHTTP();
                } else {
                    //delete check-in record
                    DelCheckInHTTP();
                }
            }
        });
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        checkin = findViewById(R.id.checkinswitch);
        surfaceView = findViewById(R.id.surfaceView);

        cam_wid = getResources().getDisplayMetrics().widthPixels*3/4;
        cam_hei=getResources().getDisplayMetrics().heightPixels*3/4;
//        surfaceView.getHolder().setFixedSize(cam_wid,cam_hei);


    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(cam_hei, cam_wid)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(QRScan.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(QRScan.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(final Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {


                    txtBarcodeValue.post(new Runnable() {

                        @Override
                        public void run() {

                            if (barcodes.valueAt(0).displayValue != null) {
                                txtBarcodeValue.removeCallbacks(null);
                                textData = barcodes.valueAt(0).displayValue;
                                txtBarcodeValue.setText(textData);
                                //cameraSource.stop();

                                String display = CheckInHTTP();
                                txtBarcodeValue.setText(display);
                                checkin.setChecked(true);
                                checkin.setVisibility(View.VISIBLE);
                            } else {
                                //barcode value is null
                            }
                        }
                    });

                }
            }
        });
    }

    private String CheckInHTTP() {
        final String[] result = new String[1];

        RequestQueue queue = Volley.newRequestQueue(QRScan.this);
        String url = "http://ad-backend.fqs3taypzi.ap-southeast-1.elasticbeanstalk.com/api/checkin/1/"+textData;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);
                            String isSuccess = reader.getString("success");
                            if (isSuccess.equals("true")) {
                                String name = reader.getString("name");
                                String username = reader.getString("username");
                                result[0] = new String(name+"\n"+username);
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Incorrect username or password",
                                        Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Could not get server response",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", token);

                return params;
            }
        };
        queue.add(postRequest);
        return result[0];
    }

    private String DelCheckInHTTP() {
        final String[] result = new String[1];

        RequestQueue queue = Volley.newRequestQueue(QRScan.this);
        String url = "http://ad-backend.fqs3taypzi.ap-southeast-1.elasticbeanstalk.com/api/checkin/1/"+textData;
        StringRequest postRequest = new StringRequest(Request.Method.DELETE, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);
                            String isSuccess = reader.getString("success");
                            if (isSuccess.equals("true")) {
                                String name = reader.getString("name");
                                String username = reader.getString("username");
                                result[0] = new String(name+"\n"+username);
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Incorrect username or password",
                                        Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Could not get server response",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", token);

                return params;
            }
        };
        queue.add(postRequest);
        return result[0];
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}
