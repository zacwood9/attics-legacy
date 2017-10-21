package me.zacwood.attics;

import java.sql.*;

public class Database {

    private final static Database instance = new Database();

    private Connection conn;

    private Database() {
        // connect to DB
        try {
            String url = "jdbc:sqlite::resource:archive.db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    public static Database getInstance() {
        return instance;
    }

    public static Item itemFromResult(ResultSet results) {
        try {
            int id = results.getInt("id");
            int showId = results.getInt("showId");
            String identifier = results.getString("identifier");
            int downloads = results.getInt("downloads");
            int numReviews = results.getInt("num_reviews");
            String avgRating = results.getString("avg_rating");
            String description = results.getString("description");
            String source = results.getString("source");

            return new Item(id, showId, identifier, downloads, avgRating, numReviews, description, source);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Show showFromResult(ResultSet results) {
        try {
            int id = results.getInt("id");
            int yearId = results.getInt("yearId");
            String date = results.getString("date");
            String venue = results.getString("venue");

            return new Show(id, yearId, date, venue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

    public Item getItemWithIdentifier(String identifier) {
        String sql = String.format("SELECT * FROM items WHERE identifier='%s'", identifier);
        ResultSet rs = rawSQL(sql);
        try {
            rs.next();
            return itemFromResult(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Show getShowWithId(int id) {
        String sql = String.format("SELECT * FROM shows where id = %d", id);
        ResultSet rs = rawSQL(sql);
        try {
            rs.next();
            return showFromResult(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
