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
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlin.math.*




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
    GoogleMap.OnMarkerClickListener, GoogleMap.OnCircleClickListener{

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
    private lateinit var markerLocation: LatLng
    private var circleList = mutableListOf<Circle>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        database = FirebaseDatabase.getInstance()
        myRef = database.reference
        intent1 = getIntent()
        userid = intent1.getStringExtra("userid")
        //useruid = intent1.getStringExtra("useruid")

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
                        markerLocation = LatLng(get_pos2?.latitude!!.toDouble(), get_pos2?.longitude!!.toDouble())
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
        mMap.setOnCircleClickListener{it
            onCircleClick(it)
        }
        mMap.setOnMapClickListener {it
            placeCircleOnMap(it)
            //여기에 레드존 좌표 데이터베이스에 저장하기
            //Toast.makeText(this@MapsActivity, "서클 중심 : $it", Toast.LENGTH_LONG).show()
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
        val markerOptions = MarkerOptions().position(location)

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources,R.mipmap.ic_user_location)
        ))
        markerOptions.position(location)
        //markerLocation = markerOptions.position
        mMap.addMarker(markerOptions)
    }

    override fun onMarkerClick(marker:Marker): Boolean {
        //Toast.makeText(this, "marker1 : "+marker.position, Toast.LENGTH_LONG).show()
        return true
    }

    //서클 그리기
    private fun placeCircleOnMap(location: LatLng){
        val circleOptions = CircleOptions()
            .center(location)
            .radius(1000.0)
            .fillColor(0x220000FF)
            .strokeColor(Color.RED)
            .clickable(true)
        mMap.addCircle(circleOptions)

        circleList.add(mMap.addCircle(circleOptions.center(location)))
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()

        editor.putInt("listSize", circleList.size)


        //원을 저장해야되는데 type이 맞지 않아서 잘 안됨..
        for(i in circleList){
            editor.putFloat("lat$i", circleList[0].center.latitude.toFloat());
            editor.putFloat("long$i", circleList[0].center.longitude.toFloat());
            //Toast.makeText(this, "lati test : "+i+circleList.get(0).getCenter().latitude.toFloat(), Toast.LENGTH_LONG).show()
            //Toast.makeText(this, "long test : "+i+circleList.get(0).getCenter().longitude.toFloat(), Toast.LENGTH_LONG).show()
        }

        editor.apply()

        val CirX: Double = location.latitude
        val CirY: Double = location.longitude
        val CirRad: Double = 1000.0

        CheckOnMarker(CirX, CirY, CirRad, markerLocation)
    }

    override fun onCircleClick(circle: Circle) {
        circle.remove()
    }

    private fun CheckOnMarker(x: Double, y:Double, rad: Double, location: LatLng){
        if(abs(x-location.latitude).pow(2) + abs(y-location.longitude).pow(2) <= (0.0004597*2)){
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
}