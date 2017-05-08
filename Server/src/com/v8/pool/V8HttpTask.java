package com.v8.pool;

import com.eclipsesource.v8.V8;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.ResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class V8HttpTask extends V8TaskBase {

    private IHTTPSession session;
    private String postData = "";

    public V8HttpTask(IHTTPSession session) {
        this.session = session;
        try {
            Map<String, String> mapData = new HashMap<>();
            session.parseBody(mapData);

            if (mapData.containsKey("postData")) {
                this.postData = mapData.get("postData");
            }
        } catch (IOException | ResponseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String run(V8 runtime) {
        String result = runtime.executeJSFunction("__serveHttpTask", postData).toString();
        return result;
    }

    @Override
    public void result(String result) {



        NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, result);
        response.addHeader("Content-Type", "application/json");
        response.addHeader("Access-Control-Allow-Origin", "*");



        session.release(response);
    }
}
