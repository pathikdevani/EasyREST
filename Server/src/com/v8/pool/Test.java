package com.v8.pool;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;

public class Test extends V8Object {
	private int i;

	public Test(V8 runtime) {
		super(runtime);
		registerJavaMethod(this, "inc", "inc", new Class<?>[] {});
	}

	public int inc() {
		i++;
		return i;
	}
}
