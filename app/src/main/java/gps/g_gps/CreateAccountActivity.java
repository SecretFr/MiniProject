package gps.g_gps;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static android.widget.Toast.LENGTH_SHORT;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "CreateAccountActivity";

    FirebaseAuth auth;

    EditText accId;
    EditText accPw;
    EditText accPw2;
    Button createBtn;
    Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        auth = FirebaseAuth.getInstance();

        accId = (EditText) findViewById(R.id.create_Id);
        accPw = (EditText) findViewById(R.id.create_Pw);
        accPw2 = (EditText) findViewById(R.id.create_Pw2);
        createBtn = (Button) findViewById(R.id.map_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_Btn);


        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accId.getText().toString().length() == 0 || accPw.getText().toString().length() == 0 || accPw2.getText().toString().length() == 0) {
                    Toast.makeText(CreateAccountActivity.this, "빈칸이 있습니다.", LENGTH_SHORT).show();
                } else if (!accPw.getText().toString().equals(accPw2.getText().toString())) {
                    Toast.makeText(CreateAccountActivity.this, "패스워드가 같지 않습니다.", LENGTH_SHORT).show();
                    accPw2.requestFocus();
                } else {
                    auth.createUserWithEmailAndPassword(accId.getText().toString(), accPw.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                finish();
                                Toast.makeText(CreateAccountActivity.this, "성공!", LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CreateAccountActivity.this, "실패", LENGTH_SHORT).show();
                                accId.setText("");
                                accPw.setText("");
                                accPw2.setText("");
                                accId.requestFocus();
                            }
                        }
                    }); //auth
                }
            }
        });     //회원가입버튼


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
