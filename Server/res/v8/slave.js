var __serveHttpTask; //call from java
var __serveJavaTask; //call from java
(function () {
    var JsManager = function () {
        this.js = [];
    };

    JsManager.prototype = {
        loadJS: function (path) {

            var file = newFile(path);
            if (file.exits() && file.isFile() && file.extension() == "js") {
                var lastModified = file.lastModified();
                var js = this.js[path];

                if (js != undefined && js.lastModified == lastModified) {
                    //console.log("file is already loaded");
                } else {
                    this.js[path] = {
                        lastModified: lastModified,
                        path: path,
                        script: new Script(file.readWithCache())
                    }
                }
                return true;
            } else {
                //console.log("file can not be load");
            }
            return false;
        },
        "callJs": function (path, fname, args) {
            if (this.loadJS(path)) {
                return this.js[path].script.call(fname, args);
            }
            return "file can not be load";
        }
    };

    var Script = function (_str) {
        this.script = new (function () {
            eval(_str);
        });
    };

    Script.prototype = {
        "call": function (name, args) {
            var fun = this.script[name];
            if (fun) {
                var ans = fun.apply(this.script, [args]);
                return ans != undefined ? JSON.stringify(ans) : "";
            } else {
                console.log("function not exits");
            }
            return "function not exits";
        }
    };
    var jsManager = new JsManager();

    function serveTask(path, fnname, data) {
        return jsManager.callJs(path, fnname, data);
    }


    __serveHttpTask = function (task) {
        try {
            task = JSON.parse(task);
            return serveTask(App.rootTask + task.path, task.name, task.data);
        } catch (e) {
            console.log("e:" + e.toString());
            console.log("json parse error ..!! and other");
        }
        return "json parse error ..!! and other or any resone";
    };
    __serveJavaTask = function (path, fnname, data) {
        return serveTask(path, fnname, data);
    };
})();

var mysql;
(function () {

    var mysqlSelect = function (query) {
        var _mysql = newMysql(query);

        this.set = function (name, val) {
            _mysql.set(name, val + "");
        };

        this.execute = function () {
            return _mysql.select();
        };

        this.toString = function () {
            return _mysql.toString();
        };

    };
    var mysqlInsert = function (data) {
        var query = "INSERT INTO `" + data.table + "` ";
        var _mysql;

        if (data.data) {


            var cols = "";
            var vals = "";

            for (var field in data.data) {
                if (data.data.hasOwnProperty(field)) {
                    cols += "`" + field + "`,";
                    vals += "'" + data.data[field] + "',";
                }
            }

            cols = cols.substring(0, cols.length - 1);
            vals = vals.substring(0, vals.length - 1);


            query = query + "(" + cols + ") VALUES (" + vals + ")";
        }

        _mysql = newMysql(query);

        this.execute = function () {
            return _mysql.insert();
        };

        this.toString = function () {
            return query;
        };
    };
    var mysqlUpdate = function (data) {
        var query = "UPDATE `" + data.table + "` SET";
        var _mysql;

        if (data.data) {


            var vals = "";
            for (var field in data.data) {
                if (data.data.hasOwnProperty(field)) {
                    vals += "`" + field + "` = '" + data.data[field] + "',"
                }
            }
            vals = vals.substring(0, vals.length - 1);
            query += vals + " WHERE " + data.where;
        }

        _mysql = newMysql(query);

        this.execute = function () {
            return _mysql.update();
        };

        this.toString = function () {
            return query;
        };
    };
    var mysqlDelete = function (data) {
        var query = "DELETE FROM `" + data.table + "` WHERE " + data.where;
        var _mysql;

        _mysql = newMysql(query);

        this.execute = function () {
            return _mysql.update();
        };

        this.toString = function () {
            return query;
        };
    };

    mysql = {
        s: function (query) {
            return new mysqlSelect(query);
        },
        i: function (data) {
            return new mysqlInsert(data);
        },
        u: function (data) {
            return new mysqlUpdate(data);
        },
        d: function (data) {
            return new mysqlDelete(data);
        },

        fs: function (fname) {
            var file = newFile(App.rootSql + fname);
            if (file.exits()) {
                var fdata = file.readWithCache();
                return mysql.s(fdata);
            } else {
                console.log("mysql: " + fname + " not found");
            }
        }
    }
})();

//console
(function () {
    console.log = function (obj) {
        console._log(JSON.stringify(obj));
    };
})();











