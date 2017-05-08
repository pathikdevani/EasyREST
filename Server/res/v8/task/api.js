this.test = function (args) {
    return "Hello " + args.name;
};

this.mysqlDemo = function (args) {
    var tables = mysql.fs("getTableList.sql").execute(); // get all table list
    var data = mysql.s("SELECT * FROM dms_cuts_transactiontype").execute(); // get all row from table

    return {
        "tables": tables,
        "data": data,
        temp: "from server"
    };
};