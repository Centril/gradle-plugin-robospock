package com.example.app;

import android.app.Activity;
import android.os.Bundle;

import com.example.libexplicit.LibExplicitActivity;
import com.example.javalib.DummyClazz;

import static java.lang.System.out;

public class AppActivity extends LibExplicitActivity {
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.activity_main );

		// From LibExplicitActivity, testing transitive javalib dependency.
		DummyClazz mydc = dc.inc();
		out.println( mydc.get() );
	}
}
