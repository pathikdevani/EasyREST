package com.v8.pool.builtin;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.v8.pool.V8Pool;

public class V8App extends V8Object {
    public V8App(V8 runtime) {
        super(runtime);


        //only use in salve
        add("rootTask", V8Pool.ROOT_TASK);
        add("rootSql", V8Pool.ROOT_SQL);
    }
}
