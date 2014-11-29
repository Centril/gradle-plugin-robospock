package com.example.javalib;

public class DummyClazz {
	int a = 1;

	public DummyClazz inc() {
		++a;
		return this;
	}

	public int get() {
		return this.a;
	}
}
