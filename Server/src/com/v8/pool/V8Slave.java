package com.v8.pool;

import com.filecache.FileCacheManager;
import com.eclipsesource.v8.ReferenceHandler;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Value;
import com.v8.pool.builtin.*;
import com.v8.pool.builtin.mysql.V8Mysql;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class V8Slave implements ReferenceHandler, Runnable {

    public enum Status {
        NONE, IDEAL, BUSY, RELEASE
    }

    public interface Listener {
        public void onUp(V8Slave slave);

        public void onDown(V8Slave slave);

        public void onCreate(V8Slave slave);

        public void onRelease(V8Slave slave);

        public void onTask(V8Slave slave);

        public void outTask(V8Slave slave);
    }

    public static Listener listener;

    public static void on(Listener listener) {
        V8Slave.listener = listener;
    }

    public Status status;
    private V8 runtime;
    private ArrayList<V8ValueRef> v8Values;
    private Thread lifeThread;

    private boolean down;
    private int downCount;
    private final int downCountLIMIT = 30; // *1000

    private Timer timer;
    private TimerTask timerTask;
    private int REALSE_TIME = 60 * 1000;

    public V8Slave() {
        status = Status.IDEAL;
        v8Values = new ArrayList<V8ValueRef>();
        down = true;
        lifeThread = null;

        runtime = V8.createV8Runtime();

        runtime.addReferenceHandler(this);

        runtime.executeVoidScript(FileCacheManager.readResWithCache("v8/lodash.min.js"));


        runtime.add("console", new V8Console(runtime));
        runtime.add("App", new V8App(runtime));
        //runtime.add("Mysql",new V8Mysql(runtime));

        V8Utils.addFunction(runtime, "newFile", V8File.class);
        V8Utils.addFunction(runtime, "newRes", V8Resource.class);
        V8Utils.addFunction(runtime, "newMysql", V8Mysql.class);


        runtime.executeVoidScript(FileCacheManager.readResWithCache("v8/slave.js"));
        runtime.getLocker().release();

        if (listener != null)
            listener.onCreate(this);
    }

    public void up() {
        if (down) {
            if (lifeThread != null && lifeThread.isAlive()) {
                try {
                    lifeThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            down = false;
            downCount = 0;
            lifeThread = new Thread(this);
            lifeThread.start();
            if (timer != null) {
                timer.cancel();
            }

            if (listener != null) {
                listener.onUp(this);
            }
        }
    }

    public void down() {
        if (!down) {
            down = true;
            timer = new Timer();
            timerTask = new TimerTask() {

                @Override
                public void run() {
                    release();
                }
            };
            timer.schedule(timerTask, REALSE_TIME);

            if (listener != null) {
                listener.onDown(this);
            }
        }
    }

    @Override
    public void run() {
        runtime.getLocker().acquire();
        while (!down) {
            V8TaskBase task = V8Pool.getTask();
            if (task == null) {
                if (downCount > downCountLIMIT) {
                    down();
                }
                downCount++;
            } else {
                downCount = 0;
                if (listener != null) {
                    listener.onTask(this);
                }

                String ans = task.run(runtime);
                task.result(ans);

                if (listener != null) {
                    listener.outTask(this);
                }
            }
        }
        runtime.getLocker().release();
    }

    public boolean isDown() {
        return down;
    }

    public void release() {

        runtime.getLocker().acquire();
        if (v8Values.size() > 0) {
            for (V8ValueRef v8Value : v8Values) {
                if (!v8Value.value.isReleased()) {
                    v8Value.value.release();
                }
            }
            v8Values.clear();
        }

        if (!runtime.isReleased()) {
            runtime.release(true);
        }

        if (listener != null) {
            listener.onRelease(this);
        }

    }

    @Override
    public void v8HandleCreated(V8Value obj) {
        v8Values.add(new V8ValueRef(obj));
    }

    @Override
    public void v8HandleDisposed(V8Value obj) {
        v8Values.remove(new V8ValueRef(obj));
    }

}
