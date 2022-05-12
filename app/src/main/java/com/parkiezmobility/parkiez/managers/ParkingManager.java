package com.parkiezmobility.parkiez.managers;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.parkiezmobility.parkiez.Interfaces.APIResponseInterface;
import com.parkiezmobility.parkiez.URLs;
import com.parkiezmobility.parkiez.networkhandler.JsonObjectRequestHandler;
import com.parkiezmobility.parkiez.networkhandler.SingletonRequestQueue;

import org.json.JSONObject;

public class ParkingManager {
    private static ParkingManager manager = null;

    public static ParkingManager getInstance(){
        if (manager == null){
            manager = new ParkingManager();
        }
        return manager;
    }

    public void getAllParkings(final JSONObject body, final APIResponseInterface handler){
        Log.e("data" ,body.toString());
        JsonObjectRequestHandler jsonObjectRequest = new JsonObjectRequestHandler(Request.Method.GET, URLs.getAllParkings, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                handler.OnSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.OnError(error);
            }
        });
        RequestQueue queue = SingletonRequestQueue.getInstance(ApplicationManager.getInstance().getContext()).getRequestQueue();
        queue.add(jsonObjectRequest);
    }

    public void getPaymentMerchantId(final APIResponseInterface handler){
        JsonObjectRequestHandler jsonObjectRequest = new JsonObjectRequestHandler(Request.Method.GET, URLs.getMerchantId, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("success")) {
                    handler.OnSuccess(response);
                } else {
                    handler.OnFailed(response.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.OnError(error);
            }
        });
        RequestQueue queue = SingletonRequestQueue.getInstance(ApplicationManager.getInstance().getContext()).getRequestQueue();
        queue.add(jsonObjectRequest);
    }

    public void generateParkingOrder(final JSONObject body, final APIResponseInterface handler){
        JsonObjectRequestHandler jsonObjectRequest = new JsonObjectRequestHandler(Request.Method.POST, URLs.generateOrder, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("success")) {
                    handler.OnSuccess(response);
                } else {
                    handler.OnFailed(response.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.OnError(error);
            }
        });
        RequestQueue queue = SingletonRequestQueue.getInstance(ApplicationManager.getInstance().getContext()).getRequestQueue();
        queue.add(jsonObjectRequest);
    }

    public void onSaveOrderCancelStatus(final JSONObject body, final APIResponseInterface handler){
        JsonObjectRequestHandler jsonObjectRequest = new JsonObjectRequestHandler(Request.Method.POST, URLs.savePaymentFailure, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("success")) {
                    handler.OnSuccess(response);
                } else {
                    handler.OnFailed(response.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.OnError(error);
            }
        });
        RequestQueue queue = SingletonRequestQueue.getInstance(ApplicationManager.getInstance().getContext()).getRequestQueue();
        queue.add(jsonObjectRequest);
    }

    public void onSaveOrderSuccessStatus(final JSONObject body, final APIResponseInterface handler){
        JsonObjectRequestHandler jsonObjectRequest = new JsonObjectRequestHandler(Request.Method.POST, URLs.savePaymentSuccess, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("success")) {
                    handler.OnSuccess(response);
                } else {
                    handler.OnFailed(response.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.OnError(error);
            }
        });
        RequestQueue queue = SingletonRequestQueue.getInstance(ApplicationManager.getInstance().getContext()).getRequestQueue();
        queue.add(jsonObjectRequest);
    }
}
