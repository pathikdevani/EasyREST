package com.filecache;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FileCacheManager {

    private static Map<String, CacheFile> cacheFiles = new HashMap<>();
    private static Map<String, String> cacheRes = new HashMap<>();

    private static final Object lock = new Object();

    public static String readFileWithCache(File file) {
        if (file != null) {
            if (file.exists() && file.isFile()) {
                CacheFile cacheFile = cacheFiles.get(file.getAbsolutePath());
                if (cacheFile != null && cacheFile.lastModified == file.lastModified()) {
                    //System.out.println("load from cache");
                    return cacheFile.data;
                } else {
                    cacheFile = new CacheFile();
                    cacheFile.file = file;
                    cacheFile.lastModified = file.lastModified();
                    cacheFile.data = readFile(file);

                    addCache(file.getAbsolutePath(), cacheFile);
                    return cacheFile.data;
                }
            } else {
                removeCache(file);
            }
        }
        return "";
    }

    public static void clearCache() {
        synchronized (lock) {
            cacheFiles.clear();
        }
    }

    private static void addCache(String absolutePath, CacheFile cacheFile) {
        synchronized (lock) {
            cacheFiles.put(absolutePath, cacheFile);
        }
    }

    private static void removeCache(File file) {
        synchronized (lock) {
            cacheFiles.remove(file.getAbsolutePath());
        }
    }

    public static String readFile(File file) {
        StringBuilder everything = new StringBuilder();

        BufferedReader buffIn = null;
        try {
            buffIn = new BufferedReader(new FileReader(file));
            String line;
            while ((line = buffIn.readLine()) != null) {
                everything.append(line);
                everything.append(System.lineSeparator());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (buffIn != null) {
                try {
                    buffIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return everything.toString();
    }

    public static String readRes(String path) {

        try {
            ClassLoader classLoader = FileCacheManager.class.getClassLoader();
            InputStream is = classLoader.getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(System.lineSeparator());
            }
            reader.close();
            return out.toString();
        } catch (Exception e) {

        }
        return "";
    }

    public static String readResWithCache(String path) {
        String data = cacheRes.get(path);
        if (data == null) {
            data = readRes(path);
            cacheRes.put(path, data);
        }
        return data;
    }

    public static boolean resExits(String path) {
        if (cacheRes.containsKey(path)) {
            return true;
        } else {
            URL url = FileCacheManager.class.getClassLoader().getResource(path);
            return url != null;
        }
    }

}
