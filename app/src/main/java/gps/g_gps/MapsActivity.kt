package gps.g_gps


import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException

data class UserItem(
    val name: String = "",
    val latitude: String = "",
    val longitude: String = ""
)


@Suppress("DEPRECATION")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    //데이터베이스var
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var myRef: DatabaseReference = database.reference


    var test2 : Intent = getIntent()


    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    //private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        var test3 = test2.getStringExtra("usertest")

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

        //var lati = myRef1.database.getReference("latitude") as Double
        //var longi = myRef2.database.getReference("longitude") as Double

        //Toast.makeText(this@MapsActivity, "위도: $lati, 경도: $longi", Toast.LENGTH_LONG).show()

        //val pos = LatLng(lati, longi)
        //val pos = LatLng(36.4473078035503, 128.323783017259)

        //mMap.addMarker(MarkerOptions().position(pos).title("test Marker"))
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(database: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //var user = myRef.database.getReference("name")
                //var lat = myRef.database.getReference("latitude")
                //var lon = myRef.database.getReference("longitude")
                var tname = dataSnapshot.getValue(UserItem::class.java)
                //Toast.makeText(this@MapsActivity, tname?.latitude, Toast.LENGTH_LONG).show()
                var posx = tname?.latitude
                var posy = tname?.longitude
                var pos = LatLng(posx!!.toDouble(), posy!!.toDouble())
                //Toast.makeText(this@MapsActivity, "$pos", Toast.LENGTH_LONG).show()

                //var intent2 : Intent = getIntent()
                //var test3 = intent2.getStringExtra("usertest")
                mMap.addMarker(MarkerOptions().position(pos).title("${tname?.name}"))
            }
         })
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)


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

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))


                //Log.d("MapsActivity", "위도: ${location.latitude}, 경도: ${location.longitude}")

                //Toast.makeText(this@MapsActivity, "위도: ${location.latitude}, 경도: ${location.longitude}",
                //    Toast.LENGTH_LONG).show()

                //myName.setValue(test3)
                //myRef1.setValue("${location.latitude}")
                //myRef2.setValue("${location.longitude}")
                //var test2 : Intent =
                //var test3  = test2.getStringExtra("usertest")

                var user = UserItem("${location.latitude}", "${location.longitude}")
                //myRef.setValue(user)
                myRef.child("$test3").setValue(user)
            }
        }






    }

    private fun placeMarkerOnMap(location: LatLng) {
        // 1
        val markerOptions = MarkerOptions().position(location)
        // 2

        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources,R.mipmap.ic_user_location)
        ))

        val titleStr = getAddress(location)  // add these two lines
        markerOptions.title(titleStr)
        mMap.addMarker(markerOptions)
    }

    private fun getAddress(latLng: LatLng): String {
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




}
/*
    원의 방정식
    원의 중심이 (x,y)고, 임의의 좌표가 (a,b)이고, r이 반지름이면,
    (x-a)^2 + (y-b)^2 <= r2 이면
    (a,b)는 원안에 속하는 좌표인것이다.
 */