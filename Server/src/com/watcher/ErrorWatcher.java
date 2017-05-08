package com.watcher;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import java.io.IOException;
import java.util.ArrayList;

public class ErrorWatcher {


    public static ArrayList<ErrorWs> list = new ArrayList<>();

    public static void push(String data) {
        for (ErrorWs ws : list) {
            try {
                if (ws.isOpen()) {
                    ws.send(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static class ErrorWs extends NanoWSD.WebSocket {

        public ErrorWs(NanoHTTPD.IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {
            list.add(this);
            //ErrorWatcher.push("test");

        }

        @Override
        protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
            close();
        }

        @Override
        protected void onMessage(NanoWSD.WebSocketFrame message) {

        }

        @Override
        protected void onPong(NanoWSD.WebSocketFrame pong) {

        }

        @Override
        protected void onException(IOException exception) {
            close();
        }


        public void close() {
            list.remove(this);
        }
    }

}
