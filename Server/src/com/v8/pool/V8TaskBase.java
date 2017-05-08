package com.v8.pool;

import com.eclipsesource.v8.V8;

public abstract class V8TaskBase {
	public abstract String run(V8 runtime);
	public abstract void result (String result);
}
