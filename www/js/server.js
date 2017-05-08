/**
 * Created by Pathik on 3/3/2017.
 */

var server = server || {};

(function ($) {
    var url = window.location.origin;
    server.ajax0 = function (path, name, data, callback) {
        $.ajax({
            type: "post",
            url: url,
            headers: {
                "_worker": "_v8"
            },
            data: JSON.stringify({
                path: path,
                data: data,
                name: name
            }),
            contentType: "text/plain",
            success: function (data) {
                if (callback) {
                    callback(data);
                }
            }
        });
    };
    server.ajax = function (args) {
        $.ajax({
            type: "post",
            url: url,
            headers: {
                "_worker": "_v8"
            },
            data: JSON.stringify({
                path: args.path,
                data: args.data,
                name: args.name
            }),
            contentType: "text/plain",
            success: function (data) {
                if (args.done) {
                    args.done(data);
                }
            }
        });
    };
})(jQuery);


(function ($) {
    var ws = new WebSocket("ws://" + window.location.host + "/error");
    ws.onmessage = function (e) {
        console.warn(e.data);
    };
})(jQuery);