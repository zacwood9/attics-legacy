package me.zacwood.attics;

import java.sql.*;

public class Database {

    private final static Database instance = new Database();

    private Connection conn;

    private Database() {
        // connect to DB
        try {
            String url = "jdbc:sqlite:archive.db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    public static Database getInstance() {
        return instance;
    }

    public ResultSet rawSQL(String sql) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery(sql);
            return results;
        } catch (SQLException e) {
            System.err.println(e.toString());
        }
        return null;
    }
}
