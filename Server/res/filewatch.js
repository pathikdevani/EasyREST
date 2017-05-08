(function () {
    document.addEventListener('DOMContentLoaded', function () {
        __ready();
    }, false);


    function __ready() {
        var host = window.location.host;
        var ws = new WebSocket("ws://" + host + "/watcher");
        ws.onopen = function () {
            ws.send(__send("list", __filelist));
        };

        ws.onmessage = function (e) {
            var data = e.data.split(":");
            switch (data[0]) {
                case "reload":
                    location.reload();
                    break;
            }

        };
    }


    function __send(cmd, data) {
        var send = "";
        send += cmd + ":";

        for (var i = 0; i < data.length; i++) {
            send += data[i] + ":"
        }
        return send.substring(0, send.length - 1);
    }
})();
