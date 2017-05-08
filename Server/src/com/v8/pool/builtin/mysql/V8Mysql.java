package com.v8.pool.builtin.mysql;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.v8.pool.builtin.V8BaseFunction;
import com.watcher.ErrorWatcher;

import java.sql.*;
import java.util.ArrayList;

public class V8Mysql extends V8BaseFunction {


    private String query;
    //private PreparedStatement statement;
    private NamedParameterStatement statement;

    private ResultSet result;
    private Connection connection;
    private V8 runtime;

    public V8Mysql(V8 runtime, V8Array args) {
        super(runtime, args);
        this.runtime = runtime;

        if (args.length() > 0) {
            query = args.getString(0);
            try {
                connection = DatabasePool.getConnection();
                statement = new NamedParameterStatement(connection, query);
            } catch (SQLException e) {
                ErrorWatcher.push(e.toString());
            }

            registerJavaMethod(this, "select", "select", null);
            registerJavaMethod(this, "insert", "insert", null);
            registerJavaMethod(this, "update", "update", null);

            registerJavaMethod(this, "set", "set", new Class<?>[]{String.class, String.class});
            registerJavaMethod(this, "toString", "toString", null);
        }
    }


    public String toString() {
        if (statement != null) {
            return statement.getStatement().toString();
        }
        return query;
    }

    public void set(String name, String data) {
        try {
            statement.setString(name, data);
        } catch (SQLException e) {
            ErrorWatcher.push(e.toString());
        }
    }


    public V8Object select() {
        try {
            result = statement.executeQuery();
            V8Object ans = parseResult();
            close();
            return ans;
        } catch (SQLException e) {
            ErrorWatcher.push(e.toString());
        }
        close();
        return null;
    }



    public int insert() {
        try {
            statement.executeUpdate();
            ResultSet rs = statement.getStatement().getGeneratedKeys();
            if (rs.next()) {
                int ans = rs.getInt(1);
                close();
                return ans;
            }

        } catch (SQLException e) {
            ErrorWatcher.push(e.toString());
        }
        close();
        return -1;
    }

    public int update() {
        try {
            int ans = statement.executeUpdate();
            close();
            return ans;
        } catch (SQLException e) {
            ErrorWatcher.push(e.toString());
        }
        close();
        return -1;
    }


    private V8Object parseResult() {
        V8Object ansV8 = new V8Object(runtime);


        try {
            ArrayList<String> columns = new ArrayList<>();
            ResultSetMetaData meta = result.getMetaData();
            V8Array columnsV8 = new V8Array(runtime);
            int columnsCount = meta.getColumnCount();

            for (int i = 1; i <= columnsCount; i++) {
                String name = meta.getColumnName(i);
                columns.add(name);
                columnsV8.push(name);
            }

            V8Array rawsV8 = new V8Array(runtime);
            int rawCount = 0;
            while (result.next()) {
                V8Object rawV8 = new V8Object(runtime);
                for (int i = 1; i <= columnsCount; i++) {
                    rawV8.add(columns.get(i - 1), result.getString(i));
                }
                rawsV8.push(rawV8);
                rawV8.release();
                rawCount++;
            }


            ansV8.add("fieldCount", columnsCount);
            ansV8.add("dataCount", rawCount);

            ansV8.add("field", columnsV8);
            ansV8.add("data", rawsV8);

            columnsV8.release();
            rawsV8.release();

        } catch (SQLException e) {
            ErrorWatcher.push(e.toString());
        }


        return ansV8;
    }

    private void close() {
        if (result != null) {
            try {
                result.close();
            } catch (SQLException e) {
                ErrorWatcher.push(e.toString());
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                ErrorWatcher.push(e.toString());
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                ErrorWatcher.push(e.toString());
            }
        }
    }
}
