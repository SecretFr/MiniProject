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

public class MenuActivity extends AppCompatActivity {

    Button mapBtn;
    Button setBtn;
    Button regBtn;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mapBtn = (Button) findViewById(R.id.map_btn);
        setBtn = (Button) findViewById(R.id.setting_btn);
        regBtn = (Button) findViewById(R.id.reg_btn);

        mAuth = FirebaseAuth.getInstance();
        //initFirebaseAuth();
        //맵 버튼
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, MapsActivity.class);
                Intent intent2 = getIntent();
                intent.putExtra("userid", intent2.getStringExtra("userid"));
                intent.putExtra("useruid", intent2.getStringExtra("useruid"));
                startActivity(intent);
            }
        });

        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               currentUser = mAuth.getCurrentUser();
                updateProfile(currentUser);
            }
        });

    }

    //지금 여기 진행중, 현재 접속한 유저의 uid출력하기
    private void updateProfile(FirebaseUser user) {
        if (user != null) {
            //접속한사람들
            for (UserInfo profile : user.getProviderData()) {
                Toast.makeText(MenuActivity.this, "계정:"+profile,Toast.LENGTH_LONG).show();
            }
        } else {
            //로그아웃
            //Toast.makeText(MenuActivity.this, "계정:"+user,Toast.LENGTH_LONG).show();
        }
    }

    /*
    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth authData) {

            }
        };
    }
     */


    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        updateProfile(currentUser);
        //mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAuth.removeAuthStateListener(mAuthListener);
    }
}
