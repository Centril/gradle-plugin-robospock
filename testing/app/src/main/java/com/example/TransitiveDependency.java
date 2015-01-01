package com.example;

import timber.log.Timber;

public final class TransitiveDependency {
    public TransitiveDependency() {
        Class<?> c = Timber.class;
    }
}
