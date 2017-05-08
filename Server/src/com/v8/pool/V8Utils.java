package com.v8.pool;

import com.filecache.FileCacheManager;
import com.eclipsesource.v8.*;
import com.v8.pool.builtin.V8BaseFunction;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class V8Utils {

	public static void executeJsFile(V8 runtime, File file) {
		if (file.exists() && file.isFile()) {
			runtime.executeScript(FileCacheManager.readFileWithCache(file));
		} else {
			//System.out.println("file not foundd:executeJsFile");
		}
	}

	public static void addFunction(V8 runtime, String name, Class<? extends V8BaseFunction> clazz) {
		runtime.add(name, new V8Function(runtime, new JavaCallback() {

			@Override
			public Object invoke(V8Object arg0, V8Array arg1) {
				try {
					return clazz.getConstructor(V8.class, V8Array.class).newInstance(runtime, arg1);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				return null;
			}
		}));
	}

}
