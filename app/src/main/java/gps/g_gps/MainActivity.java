package gps.g_gps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.google.firebase.auth.FirebaseAuth.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    FirebaseAuth auth;                      //회원가입 데이터베이스
    FirebaseDatabase database;              //메시지전송 데이터베이스
    DatabaseReference myRef1;
    DatabaseReference myRef2;

    FirebaseAuth.AuthStateListener mAuthListener;

    EditText loginId;
    EditText loginPw;
    Button   loginBtn;
    TextView loginTxt;
    Button   createAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = getInstance();
        database = FirebaseDatabase.getInstance();
        myRef1 = database.getReference("id");
        myRef2 = database.getReference("password");


        loginId = (EditText) findViewById(R.id.login_id);
        loginPw = (EditText) findViewById(R.id.login_pw);
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginTxt = (TextView) findViewById(R.id.textView);
        createAcc = (Button) findViewById(R.id.create_Acc);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth authData) {
                if (authData != null) {
                    //접속한사람들
                    Toast.makeText(MainActivity.this, "계정:"+authData,Toast.LENGTH_LONG).show();
                } else {
                    //로그아웃
                }
            }
        };

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginId.getText().toString().length() == 0 || loginPw.getText().toString().length() == 0) {
                    loginTxt.setText("제대로 입력해주세요");
                } else {
                    auth.signInWithEmailAndPassword(loginId.getText().toString(), loginPw.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        /*      메시지 전송
                                        String idMessage = loginId.getText().toString();
                                        String pwMessage = loginPw.getText().toString();
                                        myRef1.setValue(idMessage);
                                        myRef2.setValue(pwMessage);
                                        */
                                        auth.signInWithEmailAndPassword(loginId.getText().toString(), loginPw.getText().toString());
                                        Toast.makeText(MainActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                                        String uid = auth.getCurrentUser().getUid();
                                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                                        intent.putExtra("userid", loginId.getText().toString());        //메일값넘길때
                                        intent.putExtra("useruid", uid);  //uid넘길때testuser
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                                        auth.signOut();     //로그아웃
                                    }
                                }
                            });
                }
            }
        });     //로그인

        //회원가입 폼으로
        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });
    }
}


