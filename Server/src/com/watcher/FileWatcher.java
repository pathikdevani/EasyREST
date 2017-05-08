package com.watcher;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FileWatcher extends NanoWSD.WebSocket {


    private ArrayList<String> fileList = new ArrayList<>();
    private String www;
    private Path absWWW;

    public FileWatcher(NanoHTTPD.IHTTPSession handshakeRequest, String www) {
        super(handshakeRequest);
        this.www = www;
        try {
            absWWW = Paths.get(new File(www).getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onOpen() {

    }

    @Override
    protected void onException(IOException exception) {
        if (fileWatchThread != null) {
            fileWatchThread.kill();
        }
    }

    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        if (fileWatchThread != null) {
            fileWatchThread.kill();
        }
    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {

        try {
            String[] list = message.getTextPayload().split(":");
            String cmd = list[0];
            Map<String, File> dirsMap = new HashMap<>();
            ArrayList<File> dirs = new ArrayList<>();


            switch (cmd) {
                case "list":
                    for (int i = 1; i < list.length; i++) {
                        File file = new File(list[i]);
                        if (file.exists()) {
                            fileList.add(file.getCanonicalPath());
                            if (!dirsMap.containsKey(file.getParentFile().getCanonicalPath())) {
                                dirsMap.put(file.getParentFile().getCanonicalPath(), file.getParentFile());
                            }
                        }

                    }

                    for (Map.Entry<String, File> entry : dirsMap.entrySet()) {
                        dirs.add(entry.getValue());
                    }

                    startWatcher(dirs);
                    break;


                default:
                    break;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {

    }

    private WatchService watchService;
    private FileWatchThread fileWatchThread;

    private void startWatcher(ArrayList<File> dirs) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        Map<WatchKey, Path> list = new HashMap<>();
        for (int i = 0; i < dirs.size(); i++) {
            File dir = dirs.get(i);
            Path path = Paths.get(dir.getCanonicalPath());

            WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);
            list.put(key, path);
        }

        fileWatchThread = new FileWatchThread(watchService, list);
        fileWatchThread.setName("FileWatcher");
        fileWatchThread.start();
    }


    private class FileWatchThread extends Thread {
        private WatchService watcherService;
        private Map<WatchKey, Path> list;
        private boolean isRunning = true;


        public FileWatchThread(WatchService watcherService, Map<WatchKey, Path> list) {
            this.watcherService = watcherService;
            this.list = list;
        }

        public void kill() {
            isRunning = false;
        }

        public void run() {
            WatchKey key = null;
            Path dir = null;

            while (isRunning) {
                try {
                    key = watcherService.poll(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    key = null;
                    e.printStackTrace();
                }


                if (key != null) {
                    dir = list.get(key);
                    for (WatchEvent<?> event : key.pollEvents()) {
                        String path = dir.resolve((Path) event.context()).toString();
                        if (fileList.contains(path)) {
                            sendMsg("reload");
                            isRunning = false;
                            break;
                        }
                    }
                    if (isRunning)
                        isRunning = key.reset();
                }
            }
            closeFileWatcher();
        }

    }


    public void sendMsg(String msg) {
        try {
            if (FileWatcher.this.isOpen())
                send(msg);
        } catch (Exception e) {

        }


    }

    public void closeFileWatcher() {
        try {
            watchService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
