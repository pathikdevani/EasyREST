package com.v8.pool;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class V8Pool {

    public static final String ROOT_V8 ="./Server/res/v8/";
    public static final String ROOT_TASK = "./Server/res/v8/task/";
    public static final String ROOT_SQL = "./Server/res/v8/sql/";

    private static int MAX_POOL_SIZE = 1;
    private static int LIVE_SLAVE = 0;
    private static int UP_SLAVE = 0;
    private static int ON_TASK_SLAVE = 0;
    private static final Object lock = new Object();

    private static BlockingQueue<V8TaskBase> TASKS;
    private static List<V8Slave> WORKERS;

    private static boolean initDone = false;
    private static final String HEADER = "_worker";
    private static final String HEADER_VALUE = "_v8";

    public static void Init() {
        if (!initDone) {
            TASKS = new LinkedBlockingQueue<>();
            WORKERS = Collections.synchronizedList(new ArrayList<>());

            V8Slave.on(new V8Slave.Listener() {

                @Override
                public void onRelease(V8Slave slave) {

                    synchronized (lock) {
                        WORKERS.remove(slave);
                        LIVE_SLAVE--;
                        print();
                    }

                }

                @Override
                public void onUp(V8Slave slave) {
                    synchronized (lock) {
                        UP_SLAVE++;
                        print();
                    }

                }

                @Override
                public void onCreate(V8Slave slave) {
                    synchronized (lock) {
                        WORKERS.add(slave);
                        LIVE_SLAVE++;
                        print();
                    }

                }

                @Override
                public void onDown(V8Slave slave) {
                    synchronized (lock) {
                        UP_SLAVE--;
                        print();
                    }

                }

                @Override
                public void onTask(V8Slave slave) {

                    synchronized (lock) {
                        ON_TASK_SLAVE++;
                        print();
                    }
                }

                @Override
                public void outTask(V8Slave slave) {
                    synchronized (lock) {
                        ON_TASK_SLAVE--;
                        print();
                    }
                }
            });
        } else {
            System.out.println("Multipal Init!!!");
        }
    }

    public static void print() {
        // System.out.println( "LIVE:" + LIVE_SLAVE + " UP:" + UP_SLAVE +
        // " CTASK:" + ON_TASK_SLAVE + " TASK:" + TASKS.size());
    }

    public static void serve(IHTTPSession session) {
        String header = session.getHeaders().get(HEADER);
        if (header != null && header.equals(HEADER_VALUE)) {
            session.hijack(true);
            addTask(new V8HttpTask(session));
        }
    }

    public static void addTask(V8TaskBase task) {
        synchronized (lock) {
            TASKS.add(task);
            if (ON_TASK_SLAVE < UP_SLAVE) {
                // up n free
            } else if (UP_SLAVE < LIVE_SLAVE) {
                for (V8Slave slave : WORKERS) {
                    if (slave.isDown()) {
                        slave.up();
                        break;
                    }
                }
            } else if (LIVE_SLAVE < MAX_POOL_SIZE) {
                new V8Slave().up();
            }
        }
    }

    public static V8TaskBase getTask() {
        try {
            return TASKS.poll(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
