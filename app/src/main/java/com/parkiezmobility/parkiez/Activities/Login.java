package com.parkiezmobility.parkiez.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.parkiezmobility.parkiez.Database.DataHelp;
import com.parkiezmobility.parkiez.Database.MyOpenHelper;
import com.parkiezmobility.parkiez.Entities.UserEntity;
import com.parkiezmobility.parkiez.Interfaces.APIResponseInterface;
import com.parkiezmobility.parkiez.MainActivity;
import com.parkiezmobility.parkiez.R;
import com.parkiezmobility.parkiez.Services.MasterService;
import com.parkiezmobility.parkiez.URLs;
import com.parkiezmobility.parkiez.managers.UserManager;
import com.parkiezmobility.parkiez.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends BaseActivity {
    private EditText MobileNo;
    private Button GetOTP;
    private String mobile_no;
    private MasterService task;
    private DataHelp dh;
    private MyOpenHelper db;
    private UserEntity user;
    private ProgressDialog dialogProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DeclareVariables();

        if (user != null) {
            Intent i = new Intent(Login.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        GetOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidate()) {
                    ;
                    sendOTP(view, MobileNo.getText().toString().trim());
                }
            }
        });
        findViewById(R.id.link_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Login.this, Registration.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void DeclareVariables() {
        getWindow().setBackgroundDrawableResource(R.drawable.login_bg);
        dh = new DataHelp(getApplicationContext());
        db = new MyOpenHelper(getApplicationContext());

        dialogProgress = new ProgressDialog(Login.this);
        dialogProgress.setMessage("Please Wait...");
        dialogProgress.setCancelable(false);

        MobileNo = (EditText) findViewById(R.id.mobile_no);
        GetOTP = (Button) findViewById(R.id.get_otp);

        user = db.getUser();
    }

    public boolean isValidate() {
        boolean valid = true;
        mobile_no = MobileNo.getText().toString();

        if (mobile_no.isEmpty()) {
            MobileNo.setError("Please enter a mobile number!");
            valid = false;
        } else if (mobile_no.length() < 10) {
            MobileNo.setError("Please enter 10 digit mobile number!");
            valid = false;
        }

        return valid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_skip) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendOTP(final View v, final String mobile){
        Utility.CloseKeyBoard(this, v);
        try {
            JSONObject jobject = new JSONObject();
            jobject.put("mobile_no", mobile.trim());
            jobject.put("type", "login");

            UserManager.getInstance().generateOTP(jobject, new APIResponseInterface<JSONObject>() {
                @Override
                public <T> void OnSuccess(T response) {
                    try {
                        JSONObject obj = (JSONObject)response;
                        if (obj.has("success")) {
                            String otp = obj.getJSONObject("success").getString("otp");
                            JSONObject jobject = new JSONObject();
                            jobject.put("mobile_no", mobile.trim());
                            String requestBody = jobject.toString();
                            Intent i = new Intent(Login.this, OTPVerification.class);
                            i.putExtra("OTP", otp);
                            i.putExtra("FROM", "LOGIN");
                            i.putExtra("USERINFO", requestBody);
                            startActivity(i);
                        }
                    }
                    catch (Exception ex){

                    }
                }

                @Override
                public void OnFailed(String response) {

                }

                @Override
                public void OnError(VolleyError error) {
                    Login.this.onHandleNetworkResponse(error);
                }
            });
        }
        catch (Exception ex){

        }
    }
    private void sendOTP() {
        dialogProgress.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.sendOTP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialogProgress.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.has("success")) {
                                String otp = obj.getJSONObject("success").getString("otp");
                                String token = obj.getJSONObject("success").getString("token");
                                int id = obj.getJSONObject("success").getInt("user_id");
                                Log.d("Login", "OTP: " +otp);
                                Intent i = new Intent(com.parkiezmobility.parkiez.Activities.Login.this, OTPVerification.class);
                                i.putExtra("mobile_no", mobile_no);
                                i.putExtra("otp", otp);
                                i.putExtra("token", token);
                                i.putExtra("user_id", id);
                                startActivity(i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialogProgress.dismiss();
                        if (error instanceof TimeoutError) {
                            Toast.makeText(getApplicationContext(), "Session Time out, Please Try Again...", Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(getApplicationContext(), "Server Authorization Failed", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(), "Server Error, please try after some time later", Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(), "Network not Available", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(getApplicationContext(), "JSONArray Problem", Toast.LENGTH_LONG).show();
                        }
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("mobile_no", mobile_no);
                params.put("type", "login");
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

}
