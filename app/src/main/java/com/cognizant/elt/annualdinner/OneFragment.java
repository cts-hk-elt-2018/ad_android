package com.cognizant.elt.annualdinner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OneFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OneFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RelativeLayout mainbox;
    EditText searchID;
    ImageView searchBtn;
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String textData = "";
    Switch checkin;
    Button contin;
    String token;

    private OnFragmentInteractionListener mListener;

    public OneFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment OneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OneFragment newInstance(String param1) {
        OneFragment fragment = new OneFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString("token");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragView = inflater.inflate(R.layout.fragment_one, container, false);
        mainbox = fragView.findViewById(R.id.mainbox);

        searchID = fragView.findViewById(R.id.search_id);

        contin = fragView.findViewById(R.id.contin);
        contin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    cameraSource.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        txtBarcodeValue = fragView.findViewById(R.id.txtBarcodeValue);
        checkin = fragView.findViewById(R.id.checkinswitch);
        surfaceView = fragView.findViewById(R.id.surfaceView);
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

        searchBtn = mainbox.findViewById(R.id.imageButton2);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textData = String.valueOf(searchID.getText());
                CheckInHTTP();
                checkin.setVisibility(View.VISIBLE);
            }
        });
        return fragView;
    }

    private void initialiseDetectorsAndSources() {

//        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(getContext(), barcodeDetector)
                .setRequestedPreviewSize(1280, 960)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new
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
//                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
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
//                                txtBarcodeValue.setText(textData);
                                cameraSource.stop();

                                CheckInHTTP();
//                                txtBarcodeValue.setText(result);
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

    private void CheckInHTTP() {
//        final String[] result = new String[1];

        RequestQueue queue = Volley.newRequestQueue(getContext());
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
//                                result = new String(name+"\n"+username);
                                txtBarcodeValue.setText(name+"\n"+username);
                            } else {
                                Toast toast = Toast.makeText(getContext(),
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
                        Toast toast = Toast.makeText(getContext(),
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
//        return result[0];
    }

    private void DelCheckInHTTP() {
//        final String[] result = new String[1];

        RequestQueue queue = Volley.newRequestQueue(getContext());
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
//                                result[0] = new String(name+"\n"+username);
                                txtBarcodeValue.setText(name+"\n"+username);
                            } else {
                                Toast toast = Toast.makeText(getContext(),
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
                        Toast toast = Toast.makeText(getContext(),
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
//        return result[0];
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    public void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
