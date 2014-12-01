package com.example;

import android.test.ActivityInstrumentationTestCase2;

public class Test extends ActivityInstrumentationTestCase2<AppActivity> {
	public Test() {
		super( AppActivity.class );
	}

	public void testDummy() {
		assertTrue( true );
	}
}