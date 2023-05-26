package com.dh.reggie.common;

public class BaseContext {
    private static ThreadLocal<Long> tl = new ThreadLocal<>();

    public static void set(Long id) {
        tl.set(id);
    }

    public static Long get() {
        return tl.get();
    }

    public static void remove() {
        tl.remove();
    }
}
