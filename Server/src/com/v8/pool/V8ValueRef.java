package com.v8.pool;

import com.eclipsesource.v8.V8Value;

public class V8ValueRef {
	V8Value value;

	public V8ValueRef(V8Value value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null) {
			return false;
		}
		if (!(that instanceof V8Value)) {
			return false;
		}
		if (((V8ValueRef) that).value == value) {
			return true;
		}
		return false;
	}
}
