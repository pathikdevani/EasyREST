package com.v8.pool.builtin;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.watcher.ErrorWatcher;

public class V8Console extends V8Object {


    public V8Console(V8 runtime) {
        super(runtime);

        registerJavaMethod(this, "_log", "_log", new Class<?>[]{Object.class});
    }

    public void _log(Object obj) {
        //System.out.println("[V8-LOG]:" + obj);
        ErrorWatcher.push(obj.toString());
    }

}
