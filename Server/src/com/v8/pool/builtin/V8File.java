package com.v8.pool.builtin;

import com.filecache.FileCacheManager;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;

import java.io.File;

public class V8File extends V8BaseFunction {

    private File file;

    public V8File(V8 runtime, V8Array args) {
        super(runtime, args);


        if (args.length() > 0) {
            file = new File(args.getString(0));

            registerJavaMethod(this, "exits", "exits", null);
            registerJavaMethod(this, "isFile", "isFile", null);
            registerJavaMethod(this, "extension", "extension", null);
            registerJavaMethod(this, "readWithCache", "readWithCache", null);
            registerJavaMethod(this, "read", "read", null);
            registerJavaMethod(this, "lastModified", "lastModified", null);
            registerJavaMethod(this, "getPath", "getPath", null);
        }
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    public boolean exits() {
        return file.exists();
    }

    public boolean isFile() {
        return file.isFile();
    }

    public String extension() {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public String readWithCache() {
        return FileCacheManager.readFileWithCache(file);
    }

    public String read() {
        return FileCacheManager.readFile(file);
    }

    public String lastModified() {
        return file.lastModified() + "";
    }
}
