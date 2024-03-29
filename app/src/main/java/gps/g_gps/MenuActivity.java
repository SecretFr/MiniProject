package gps.g_gps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    Button mapBtn;
    Button setBtn;
    Button regBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mapBtn = (Button) findViewById(R.id.map_btn);
        setBtn = (Button) findViewById(R.id.setting_btn);
        regBtn = (Button) findViewById(R.id.reg_btn);


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

    }
}
