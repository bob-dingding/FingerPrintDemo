package com.zjtt.fingerprintdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";


	TextView tvSixe, tvNine;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvSixe = findViewById(R.id.tv_fingerSix);
		tvNine = findViewById(R.id.tv_fingerNine);

		tvSixe.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sixIntent =new Intent(MainActivity.this,SixActivity.class);
				startActivity(sixIntent);
			}
		});
		tvNine.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent nineIntent =new Intent(MainActivity.this,NineActivity.class);
				startActivity(nineIntent);
			}
		});
	}


}
