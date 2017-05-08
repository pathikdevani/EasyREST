package com.v8.pool.builtin.mysql;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabasePool {
    private static final HikariDataSource dataSource = new HikariDataSource();

    static {
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/app");
        dataSource.setUsername("root");
        dataSource.setPassword("");
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource.setConnectionTestQuery("show tables");
        dataSource.setMaximumPoolSize(100);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
