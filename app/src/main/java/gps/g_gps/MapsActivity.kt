package gps.g_gps

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.GoogleMap
import kotlin.math.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnCircleClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private lateinit var markerLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                //placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
        createLocationRequest()


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnCircleClickListener{it
            onCircleClick(it)
        }
        mMap.setOnMapClickListener {it
            placeCircleOnMap(it)
            Toast.makeText(this@MapsActivity, "서클 중심 : $it", Toast.LENGTH_LONG).show()
        }
        setUpMap()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this){location ->
            if(location != null){
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                markerLocation = currentLatLng
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

                Log.d("MapsActivity", "위도: ${location.latitude}, 경도: ${location.longitude}")

                //Toast.makeText(this@MapsActivity, "위도: ${location.latitude}, 경도: ${location.longitude}",
                  //  Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources,R.mipmap.ic_user_location)
        ))
        markerOptions.position(location)
        mMap.addMarker(markerOptions)
    }

    override fun onMarkerClick(marker:Marker): Boolean {
        Toast.makeText(this, "marker1 : "+marker.position, Toast.LENGTH_LONG).show()
        return true
    }

    private fun placeCircleOnMap(location: LatLng){
        val circleOptions = CircleOptions()
            .center(location)
            .radius(1000.0)
            .strokeColor(Color.BLACK)
            .fillColor(Color.RED)
            .clickable(true)
        mMap.addCircle(circleOptions)

        val CirX: Double = location.latitude
        val CirY: Double = location.longitude
        val CirRad: Double = 1000.0
        /*Toast.makeText(this, "marker pos : "+markerLocation, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "X pos : "+CirX, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "Y pos"+CirY, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "CirRad : "+CirRad, Toast.LENGTH_SHORT).show()*/
        CheckOnMarker(CirX, CirY, CirRad, markerLocation)
    }

    override fun onCircleClick(circle: Circle) {
        circle.remove()
    }

    private fun CheckOnMarker(x: Double, y:Double, rad: Double, location: LatLng){
        
        if((x-location.latitude).pow(2) + (y-location.longitude).pow(2) <= (0.0004597 *2)){
            Toast.makeText(this, "Red Zone!", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Safe!", Toast.LENGTH_SHORT).show()
        }

    }





    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    // 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    /*override fun onMapClick(location: LatLng){

    }*/

    /*private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }*/


}

