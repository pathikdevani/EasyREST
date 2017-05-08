package com.v8.pool.builtin;

import com.filecache.FileCacheManager;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;


public class V8Resource extends V8BaseFunction {


    private String path;

    public V8Resource(V8 runtime, V8Array args) {
        super(runtime, args);


        registerJavaMethod(this, "exits", "exits", null);
        registerJavaMethod(this, "readResWithCache", "readResWithCache", null);
    }

    public boolean exits() {
        return FileCacheManager.resExits(path);
    }

    public String readResWithCache() {
        return FileCacheManager.readResWithCache(path);
    }

}
