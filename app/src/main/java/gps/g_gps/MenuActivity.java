package gps.g_gps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MenuActivity extends AppCompatActivity {

    Button mapBtn;
    Button setBtn;
    Button regBtn;

    FirebaseDatabase database;
    DatabaseReference myRef;
    /*
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser currentUser;

     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mapBtn = (Button) findViewById(R.id.map_btn);
        setBtn = (Button) findViewById(R.id.setting_btn);
        regBtn = (Button) findViewById(R.id.reg_btn);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        //mAuth = FirebaseAuth.getInstance();
        //initFirebaseAuth();
        //맵 버튼
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, MapsActivity.class);
                Intent intent2 = getIntent();
                intent.putExtra("userid", intent2.getStringExtra("userid"));
                //intent.putExtra("useruid", intent2.getStringExtra("useruid"));
                startActivity(intent);
            }
        });



        //레드존 셋팅버튼
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String test1 = "ok-naver_com";
                String test2 = test1.replace("-","@").replace("_",".");

                //String test = myRef.child(test2);
                Toast.makeText(MenuActivity.this,test2, Toast.LENGTH_LONG).show();

            }
        });



        //친구 등록
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }//onCreate

}
