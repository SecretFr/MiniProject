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
import android.widget.Toast
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
import com.google.firebase.database.*
import java.io.IOException

data class UserItem(
    var id: String = "",
    var latitude: String = "",
    var longitude: String = "",
    var redzone: String = "",
    var friends: String = ""
)

data class Pos(
    var latitude: String = "",
    var longitude: String = ""
)


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    //데이터베이스var
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference


    private lateinit var intent1 : Intent
    var userid :String = ""
    var getSearch : String = ""
    //var useruid :String = ""


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

        database = FirebaseDatabase.getInstance()
        myRef = database.reference
        intent1 = getIntent()
        userid = intent1.getStringExtra("userid")
        //useruid = intent1.getStringExtra("useruid")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
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
    } // onCreate

    //데이터베이스에 값들 등록함
    private fun writeNewUser(id: String, latitude: String, longitude: String, redzone: String, friends: String) {
        val user = UserItem(id, latitude, longitude, redzone, friends)
       // val pos = Pos(latitude, longitude)
        myRef.child("users").child(id).setValue(user)
        //Toast.makeText(this@MapsActivity, id, Toast.LENGTH_LONG).show()
    }

    private fun readFriends() {
        //일단 자신의 데이터베이스에서 friend값을 불러옴
        myRef.child("users").child("$userid").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(database: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var get_pos1 = dataSnapshot.getValue(UserItem::class.java)
                //var pos = LatLng(get_pos?.latitude!!.toDouble(), get_pos?.longitude!!.toDouble())
                //Toast.makeText(this@MapsActivity, "$get_pos1"+1, Toast.LENGTH_LONG).show()

                if("${get_pos1?.friends}" == "")
                {
                    Toast.makeText(this@MapsActivity, "상대id를 등록해주세요", Toast.LENGTH_LONG).show()
                    finish()
                }
                //상대방 위치주소 가져오기
                myRef.child("users").child("${get_pos1?.friends}").addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(database: DatabaseError) {
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var get_pos2 = dataSnapshot.getValue(UserItem::class.java)
                        var pos = LatLng(get_pos2?.latitude!!.toDouble(), get_pos2?.longitude!!.toDouble())
                        //Toast.makeText(this@MapsActivity, "$get_pos2"+2, Toast.LENGTH_LONG).show()

                        //var nick = get_pos?.id.replace("_",".")
                        mMap.addMarker(MarkerOptions().position(pos).title(get_pos2?.id))
                    }
                })
            }
        })
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        readFriends()

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



                myRef.child("users").child("$userid").addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(database: DatabaseError) {
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var get_friend1 = dataSnapshot.getValue(UserItem::class.java)
                        //var pos = LatLng(get_pos?.latitude!!.toDouble(), get_pos?.longitude!!.toDouble())
                        //데이터베이스에 uid, 좌표값저장
                        writeNewUser("${userid}","${location.latitude}", "${location.longitude}", "","${get_friend1?.friends}")
                        //Toast.makeText(this@MapsActivity, "${get_friend1?.friends}" + "friend", Toast.LENGTH_LONG).show()
                    }
                })

            }
        }
    }

    //자신의 마커찍기
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