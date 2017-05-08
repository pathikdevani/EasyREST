package com.server;

import com.v8.pool.V8Pool;

import java.io.IOException;


public class TestApp {

    private static int PORT = 8080;
    public static Boolean DEBUG = true;


    public static void main(String args[]) throws IOException {
        V8Pool.Init();
        Server server = new Server(PORT, "./www/");
        server.start(0);
        System.out.println("Server ruining on port:" + PORT);

    }
}
