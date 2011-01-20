package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.internal.Instrument;

@Instrument
public class Foo {
    private String name;

    public Foo(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }
}
