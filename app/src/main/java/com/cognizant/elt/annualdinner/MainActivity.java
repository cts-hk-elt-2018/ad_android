package com.cognizant.elt.annualdinner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    ImageView Logo;
    ImageView smallPotato;
    EditText username;
    EditText password;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logo = (ImageView) findViewById(R.id.logo);
        smallPotato = (ImageView) findViewById(R.id.small_potato);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String url ="http://ad-backend.fqs3taypzi.ap-southeast-1.elasticbeanstalk.com/api/auth/signin";
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new com.android.volley.Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d("Response", response);
                                JSONObject reader = null;
                                try {
                                    reader = new JSONObject(response);
                                    String isSuccess = reader.getString("success");
//                                    String isSuccess = "true";
                                    if (isSuccess.equals("true")){
//                                    if (true){
                                        String token = reader.getString("token");
                                        Intent i = new Intent(MainActivity.this, QRScan.class);
                                        i.putExtra("token", token);
                                        startActivity(i);
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
                        new com.android.volley.Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", error.toString());
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Login Fail",
                                        Toast.LENGTH_SHORT);
                                toast.show();
                                Intent i = new Intent(MainActivity.this, TabActivity.class);
                                startActivity(i);
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("username", username.getText().toString());
                        params.put("password", password.getText().toString());

                        return params;
                    }
                };
                queue.add(postRequest);
            }
        });
    }
}
