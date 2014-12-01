package com.example;

import android.app.Activity;
import android.os.Bundle;

public class AppActivity extends Activity {
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.activity_main );
	}
}
