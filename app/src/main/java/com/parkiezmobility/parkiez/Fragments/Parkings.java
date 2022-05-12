package com.parkiezmobility.parkiez.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.android.SphericalUtil;
import com.parkiezmobility.parkiez.Database.DataHelp;
import com.parkiezmobility.parkiez.Database.MyOpenHelper;
import com.parkiezmobility.parkiez.Entities.AddressEntities;
import com.parkiezmobility.parkiez.Entities.ParkingEntities;
import com.parkiezmobility.parkiez.Entities.UserEntity;
import com.parkiezmobility.parkiez.GPSTracker;
import com.parkiezmobility.parkiez.Interfaces.APIResponseInterface;
import com.parkiezmobility.parkiez.MainActivity;
import com.parkiezmobility.parkiez.R;
import com.parkiezmobility.parkiez.Services.MasterService;
import com.parkiezmobility.parkiez.managers.ParkingManager;
import com.parkiezmobility.parkiez.utility.JSONReader;
import com.razorpay.Checkout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlaceAutocomplete;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlaceAutocomplete;

public class Parkings extends Fragment {
    private GoogleMap mMap;
    private final static int MY_PERMISSION_FINE_LOCATION = 101;
    private GPSTracker mGPS = null;
    private double latitude, longitude;
    private MasterService task;
    private ArrayList<AddressEntities> Parkings = new ArrayList<AddressEntities>();
    private SupportMapFragment mapFragment;
    private LinearLayout Info, CarParkingInfo, BikeParkingInfo, ThreeHrsInfo, OpenView;
    private FrameLayout Frames;
    private LinearLayout First, Second, Third, BookLater;
    private BottomNavigationView navigation;
    private int position;
    private int GPS_LOC = 0;
    private FrameLayout ParkingInfo;
    private AddressEntities parking;
    private Toolbar toolbar;
    private ArrayList<Marker> markers;
    private TextView ParkingName, ParkingDetails, ReviewCount, ParkingTime, CarCount, CarPrice, BikeCount, BikePrice, ThreeHrsCarPrice;
    private Button bookNow, bookLater, bookParking;
    private SlidingUpPanelLayout mLayout;
    private ImageView menuBtn, searchMenu;
    private UserEntity user;
    private MyOpenHelper db;
    private DataHelp dh;
    private LatLng currentLatLong;
    public AlertDialog PaymentMethodAlert;
    public Dialog PaymentMethodDialog;
    ParkingEntities parkingEntity;
    ArrayList<ParkingEntities> parkingEntities = new ArrayList<>();
    private ProgressDialog dialogProgress = null;
    View v = null;
    private Marker selectedMarker = null;
    private com.parkiezmobility.parkiez.Fragments.TimeDurationDialog timeDurationDialog;
    private FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    private Circle circle = null;

    public Parkings() {
    }

    public Parkings(Toolbar toolbar, ImageView menuBtn, ImageView searchMenu) {
        this.toolbar = toolbar;
        this.menuBtn = menuBtn;
        this.searchMenu = searchMenu;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (v != null) {
            ViewGroup parent = (ViewGroup) v.getParent();
            if (parent != null) {
                parent.removeView(v);
            }
        }
        try {
            v = inflater.inflate(R.layout.fragment_map, container, false);
            menuBtn.setVisibility(View.VISIBLE);
            searchMenu.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.GONE);

            DeclareVariables(v);

            mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {
                    Log.i("Parkings", "onPanelSlide, offset " + slideOffset);
                }

                @Override
                public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                    if (newState.toString().equals("COLLAPSED")) {
                        BookLater.setVisibility(View.VISIBLE);
                        menuBtn.setVisibility(View.VISIBLE);
                        searchMenu.setVisibility(View.VISIBLE);
                    } else if (newState.toString().equals("EXPANDED")) {
                        BookLater.setVisibility(View.GONE);
                        menuBtn.setVisibility(View.GONE);
                        searchMenu.setVisibility(View.GONE);
                    }
                }
            });

            mLayout.setFadeOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            });
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            bookNow.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View view) {
                    if (parkingEntity!= null && parkingEntity.getNo_of_rows() == 0 &&
                            parkingEntity.getNo_of_columns() == 0) {
                        Toast.makeText(getActivity(), "This parking is full", Toast.LENGTH_SHORT).show();
                    } else {
                        timeDurationDialog = new TimeDurationDialog(Parkings.this, parkingEntity);
                        timeDurationDialog.display();
                    }
                }
            });

            bookParking.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View view) {
                    if (parkingEntity.getNo_of_rows() == 0 &&
                            parkingEntity.getNo_of_columns() == 0) {
                        Toast.makeText(getActivity(), "This parking is full", Toast.LENGTH_SHORT).show();
                    } else {
                        timeDurationDialog = new TimeDurationDialog(Parkings.this, parkingEntity);
                        timeDurationDialog.display();
                    }
                }
            });

            bookLater.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "In Process...", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (InflateException e) {

        }

        return v;
    }

    public void DeclareVariables(View v) {
        mLayout = (SlidingUpPanelLayout) v.findViewById(R.id.sliding_layout);
        ParkingName = (TextView) v.findViewById(R.id.parking_name);
        ParkingDetails = (TextView) v.findViewById(R.id.parking_detail);
        ReviewCount = (TextView) v.findViewById(R.id.review_count);
        ParkingTime = (TextView) v.findViewById(R.id.parking_time);
        CarCount = (TextView) v.findViewById(R.id.car_count);
        CarPrice = (TextView) v.findViewById(R.id.carPrice);
        BikeCount = (TextView) v.findViewById(R.id.bike_count);
        BikePrice = (TextView) v.findViewById(R.id.bikePrice);
        ThreeHrsCarPrice = (TextView) v.findViewById(R.id.three_hrs_car_price);

        navigation = (BottomNavigationView) v.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setItemIconTintList(null);

        First = (LinearLayout) v.findViewById(R.id.first);
        Second = (LinearLayout) v.findViewById(R.id.second);
        Third = (LinearLayout) v.findViewById(R.id.third);
        BookLater = (LinearLayout) v.findViewById(R.id.book_later_layout);

        bookNow = (Button) v.findViewById(R.id.book_now);
        bookLater = (Button) v.findViewById(R.id.book_later);
        bookParking = (Button) v.findViewById(R.id.bookParking);

        db = new MyOpenHelper(getActivity());
        dh = new DataHelp(getActivity());
        user = db.getUser();


        dialogProgress = new ProgressDialog(getContext());
        dialogProgress.setMessage("Please Wait...");
        dialogProgress.setCancelable(false);

        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            onLocationFound(new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    }
                });
            } else {
                Toast.makeText(this.getContext(), "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            onLocationFound(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this.getActivity(), new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void onLocationFound(LatLng location){
        currentLatLong = location;
        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title("Current Location"));
        marker.setTag("Current Loc");

        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        if(circle!= null)
            circle.remove();

        circle = mMap.addCircle(new CircleOptions()
                .center(currentLatLong)
                .radius(10000)
                .strokeWidth(1)
                .strokeColor(Color.argb(100, 71, 149, 255))
                .fillColor(Color.argb(100, 71, 149, 255)));


        getLocationData(location.latitude, location.longitude);
    }
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initMap();
    }

    private void initMap(){
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                try {
                    mMap = googleMap;
                    mMap.clear();;
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_strings));
                    getLastLocation();
                }
                catch (Exception ex){

                }
            }
        });
    }
    // A place has been received; use requestCode to track the request.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("MainActivity", "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
                SearchResult(place);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("MainActivity", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if (requestCode == 2) {
            if (resultCode == GPS_LOC) {
                switch (requestCode) {
                    case 2:
                        super.onResume();
                        break;
                }
            }
        }
    }


    private void SearchResult(Place place) {
        mMap.clear();
        latitude = place.getLatLng().latitude;
        longitude = place.getLatLng().longitude;

        LatLng searchedLocation = new LatLng(latitude, longitude);
        //Marker marker = mMap.addMarker(new MarkerOptions().position(searchedLocation).title("Current Location"));
        //marker.setTag("Current Loc");

        mMap.moveCamera(CameraUpdateFactory.newLatLng(searchedLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        if(circle!= null)
            circle.remove();

        circle = mMap.addCircle(new CircleOptions()
                .center(searchedLocation)
                .radius(10000)
                .strokeWidth(1)
                .strokeColor(Color.argb(100, 71, 149, 255))
                .fillColor(Color.argb(100, 71, 149, 255)));

        getLocationData(latitude, longitude);
    }

    private void getLocationData(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String city = obj.getLocality();
            String state = obj.getAdminArea();
            String zipcode = obj.getPostalCode();

            getParkings(city, state, zipcode, String.valueOf(lat), String.valueOf(lng));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getParkings(String city, String state, String zipcode, String latitude, String longitude) {
        JSONObject latlong = new JSONObject();
        try {
            latlong.put("latitude", latitude);
            latlong.put("longitude", longitude);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JSONObject info = new JSONObject();
        try {
            info.put("city", city);
            info.put("state", state);
            info.put("zipcode", zipcode);
            info.put("currentLocation", latlong);
            info.put("radius", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("location", info.toString());
        ParkingManager.getInstance().getAllParkings(info, new APIResponseInterface<JSONObject>() {
            @Override
            public <T> void OnSuccess(T response) {
                try {
                    JSONObject obj = (JSONObject) response;
                    if (parkingEntities !=null)
                        parkingEntities.clear();
                    if (markers !=null)
                        markers.clear();
                    parkingEntities.addAll(JSONReader.getInstance().parseParkingEntitiesResponse(obj.toString()));
                    setMarkers(parkingEntities);
                }
                catch (Exception ex){

                }
            }

            @Override
            public void OnFailed(String response) {

            }

            @Override
            public void OnError(VolleyError error) {
                ((MainActivity)Parkings.this.getActivity()).onHandleNetworkResponse(error);
            }
        });
    }

    private void setMarkers(ArrayList<ParkingEntities> parkingEntities) {
        if (parkingEntities != null && parkingEntities.size() > 0) {
            markers = new ArrayList<>();
            for (int i = 0; i < parkingEntities.size(); i++) {
                if (!parkingEntities.get(i).getLatitude().isEmpty() && !parkingEntities.get(i).getLongitude().isEmpty()
                        && !parkingEntities.get(i).getTitle().isEmpty() && !parkingEntities.get(i).getAddress().isEmpty()) {
                    createMarker(i, parkingEntities.get(i).getLatitude(), parkingEntities.get(i).getLongitude(),
                            parkingEntities.get(i).getTitle(), parkingEntities.get(i).getAddress(),
                            "Dummy");
                }
            }
        } else {
            Toast.makeText(getContext(), "Sorry no vaccant parkings available in this area", Toast.LENGTH_SHORT).show();
        }
    }

    protected Marker createMarker(int pos, String latitude, String longitude, String parkingName, String address, String mobileNo) {
        double newLat = Double.parseDouble(latitude);
        double newLong = Double.parseDouble(longitude);
        Marker marker = null;

        try {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(newLat, newLong))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_green_icon_round)));

            marker.setTag(String.valueOf(pos));

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    try {
                        if (selectedMarker != null)
                            selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_green_icon_round));
                        selectedMarker = marker;
                        String str = (String) marker.getTag();
                        int pos = Integer.parseInt(str);

                        if (!str.equals("Current Loc")) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_blue_icon_round));
                            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            parkingEntity = parkingEntities.get(pos);

                            ParkingName.setText(parkingEntity.getTitle());
                            CarCount.setText(String.valueOf(parkingEntity.getNo_of_columns()));
                            CarPrice.setText(String.valueOf(parkingEntity.getVehicle_types().get(0).getCost()));
                            BikeCount.setText(String.valueOf(parkingEntity.getNo_of_rows()));
                            BikePrice.setText(String.valueOf(parkingEntity.getVehicle_types().get(1).getCost()));
                            ThreeHrsCarPrice.setText("Total Price \n Rs. " + String.valueOf(3 * Double.valueOf(parkingEntity.getVehicle_types().get(0).getCost())));

                            if (main_polygon != null)
                                main_polygon.remove();
                            LatLng latlong2 = new LatLng(Double.parseDouble(parkingEntity.getLatitude()), Double.parseDouble(parkingEntity.getLongitude()));
                            drawCurveOnMap(currentLatLong, latlong2);
                        }
                    } catch (Exception e) {
                        e.getMessage();
                    }
                    return false;
                }
            });
            markers.add(marker);
//            mMap.setInfoWindowAdapter(this);
        } catch (Exception e) {
            e.getMessage();
        }

        return marker;
    }

    Polygon main_polygon;
    void drawCurveOnMap(LatLng latLng1, LatLng latLng2) {
        double k = 0.5; //curve radius
        double h = SphericalUtil.computeHeading(latLng1, latLng2);
        double d = 0.0;
        LatLng pe;

        //The if..else block is for swapping the heading, offset and distance
        //to draw curve always in the upward direction
        if (h < 0) {
            d = SphericalUtil.computeDistanceBetween(latLng2, latLng1);
            h = SphericalUtil.computeHeading(latLng2, latLng1);
            //Midpoint position
            pe = SphericalUtil.computeOffset(latLng2, d * 0.5, h);
        } else {
            d = SphericalUtil.computeDistanceBetween(latLng1, latLng2);

            //Midpoint position
            pe = SphericalUtil.computeOffset(latLng1, d * 0.5, h);
        }

        //Apply some mathematics to calculate position of the circle center
        double x = (1 - k * k) * d * 0.5 / (2 * k);
        double r = (1 + k * k) * d * 0.5 / (2 * k);

        LatLng c = SphericalUtil.computeOffset(pe, x, h + 90.0);

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(c, latLng1);
        double h2 = SphericalUtil.computeHeading(c, latLng2);

        //Calculate positions of points on circle border and add them to polyline options
        int numberOfPoints = 1000;//more numberOfPoints more smooth curve you will get
        double step = (h2 - h1) / numberOfPoints;

        //Create PolygonOptions object to draw on map
        PolygonOptions polygon = new PolygonOptions();

        //Create a temporary list of LatLng to store the points that's being drawn on map for curve
        ArrayList<LatLng> temp = new ArrayList<LatLng>();

        //iterate the numberOfPoints and add the LatLng to PolygonOptions to draw curve
        //and save in temp list to add again reversely in PolygonOptions
        for (int i = 0; i < numberOfPoints; i++) {
            LatLng latlng = SphericalUtil.computeOffset(c, r, h1 + i * step);
            polygon.add(latlng); //Adding in PolygonOptions
            temp.add(latlng);   //Storing in temp list to add again in reverse order
        }

        for (int i = temp.size() - 1; i >= 0; i--) {

            // Append the elements in reverse order
            polygon.add(temp.get(i));
        }

//        //iterate the temp list in reverse order and add in PolygonOptions
//        for (int i = (temp.size()); i<=1; i--) {
//            polygon.add(temp.get(i));
//        }

        polygon.strokeColor(getResources().getColor(R.color.colorAccent));
        polygon.strokeWidth(12f);
        //polygon.strokePattern(listOf(Dash(30f), Gap(50f))) //Skip if you want solid line
        main_polygon = mMap.addPolygon(polygon);

        temp.clear();//clear the temp list
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_details:
                    First.setVisibility(View.VISIBLE);
                    Second.setVisibility(View.GONE);
                    Third.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_images:
                    First.setVisibility(View.GONE);
                    Second.setVisibility(View.VISIBLE);
                    Third.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_review:
                    Toast.makeText(getActivity(), "In Process...", Toast.LENGTH_SHORT).show();
//                    First.setVisibility(View.GONE);
//                    Second.setVisibility(View.GONE);
//                    Third.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    public void generateOrder(final String ParkingAmount, String dateStr, String timeStr, int parkingHrs, String VehicleNo) {
        String in_time = null, out_time = null;
        try {
            in_time = dateStr + " " + timeStr + ":00";
            final long millisToAdd = parkingHrs * 60 * 60 * 1000; //two hours

            DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date d = format.parse(in_time);
            Date inDate = format.parse(in_time);
            d.setTime(d.getTime() + millisToAdd);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            in_time = dateFormat.format(inDate);
            out_time = dateFormat.format(d);
        } catch (Exception e) {
            e.getMessage();
        }

        JSONObject info = new JSONObject();
        try {
            info.put("parking_id", String.valueOf(parkingEntity.getParkingID()));
            info.put("user_id", String.valueOf(user.getUserID()));
            info.put("car_number", VehicleNo);
            info.put("phone_number", user.getMobileNo());
            info.put("cost", String.valueOf(ParkingAmount).replace("Rs. ", ""));
            info.put("unit", "1");
            info.put("in_time", in_time);
            info.put("out_time", out_time);
            info.put("order_placed_from", "app");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParkingManager.getInstance().generateParkingOrder(info, new APIResponseInterface<JSONObject>() {
            @Override
            public <T> void OnSuccess(T response) {
                try {
                    JSONObject obj = (JSONObject) response;
                    getMerchantID(obj.getJSONObject("success").getString("order_id"), ParkingAmount);
                }
                catch (Exception ex){

                }
            }

            @Override
            public void OnFailed(String response) {

            }

            @Override
            public void OnError(VolleyError error) {
                ((MainActivity)Parkings.this.getActivity()).onHandleNetworkResponse(error);
            }
        });
    }

    private void getMerchantID(final String orderId, final String ParkingAmount) {

        ParkingManager.getInstance().getPaymentMerchantId(new APIResponseInterface<JSONObject>() {
            @Override
            public <T> void OnSuccess(T response) {
                try {
                    JSONObject obj = (JSONObject) response;
                    startPayment(obj.getJSONObject("success").getString("RAZORPAY_MERCHANT_ID"), orderId, ParkingAmount);
                }
                catch (Exception ex){

                }
            }

            @Override
            public void OnFailed(String response) {

            }

            @Override
            public void OnError(VolleyError error) {
                ((MainActivity)Parkings.this.getActivity()).onHandleNetworkResponse(error);
            }
        });
    }

    public void startPayment(String MerchantId, String orderId, String ParkingAmount) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("OrderId",orderId);
        editor.commit();

        String PA = ParkingAmount.substring(4);

        /**   * Instantiate Checkout   */
        Checkout checkout = new Checkout();
//        checkout.setKeyID("rzp_test_8ohg46iMdD6Cly");
//        checkout.setKeyID("rzp_live_QQA4JgwAVhNQAG");
        checkout.setKeyID(MerchantId);
        /**   * Set your logo here   */
        checkout.setImage(R.drawable.logo);
        /**   * Reference to current activity   */
        final Activity activity = getActivity();
        /**   * Pass your payment options to the Razorpay Checkout as a JSONObject   */
        try {
            JSONObject options = new JSONObject();
            options.put("name", "Parkiez");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("order_id", orderId);//from response of step 3.
            options.put("theme.color", "#69BD45");
            options.put("currency", "INR");
            options.put("amount", String.valueOf(Double.parseDouble("1")*100));//pass amount in currency subunits
            options.put("prefill.email", "connect@parkiez.com");
            options.put("prefill.contact",user.getMobileNo());
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);
            checkout.open(activity, options);
            checkout.setFullScreenDisable(true);
        } catch(Exception e) {
            Log.e("Parkiez", "Error in starting Razorpay Checkout", e);
        }
    }

}
