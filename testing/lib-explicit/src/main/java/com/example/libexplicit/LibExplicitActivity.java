package com.example.libexplicit;

import android.app.Activity;
import android.os.Bundle;

import com.example.javalib.DummyClazz;

public class LibExplicitActivity extends Activity {
	protected DummyClazz dc = new DummyClazz();

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.activity_main );
	}
}
