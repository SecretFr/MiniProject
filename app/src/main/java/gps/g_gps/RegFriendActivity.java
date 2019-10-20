package gps.g_gps;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegFriendActivity extends AppCompatActivity {

    EditText regId;
    Button regBtn;
    Intent intent2;
    String userid;

    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_friend);

        regId = (EditText) findViewById(R.id.editText2);
        regBtn = (Button) findViewById(R.id.regBtn);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        intent2 = getIntent();
        userid = intent2.getStringExtra("userid");


        regId.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                regId.setText("");
                return false;
            }
        });


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String changeId = regId.getText().toString().replace("@","_").replace(".","_");
                myRef.child(userid).child("friends").setValue(changeId);
                Toast.makeText(RegFriendActivity.this, "등록완료", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
