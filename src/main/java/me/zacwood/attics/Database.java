package me.zacwood.attics;

import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    private ResultSet rawSQL(String sql) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery(sql);
            return results;
        } catch (SQLException e) {
            System.err.println(e.toString());
        }
        return null;
    }

    private static Item itemFromResult(ResultSet results) {
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

    private static Show showFromResult(ResultSet results) {
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

    private static Year yearFromResult(ResultSet results) {
        try {
            int id = results.getInt("id");
            String year = results.getString("year");
            String collection = results.getString("collection");

            return new Year(id, year, collection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /////////// Year query methods ///////////

    private Year getYearFromQuery(String sql) {
        ResultSet rs = rawSQL(sql);
        try {
            if (rs.next()) {
                Year year = yearFromResult(rs);
                rs.close();
                return year;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Year getYearWithId(int id) {
        String sql = String.format("SELECT * FROM years WHERE id=%d", id);
        return getYearFromQuery(sql);
    }

    public List<Year> getYearsWithCollection(String collection) {
        ResultSet results = rawSQL(String.format("SELECT id FROM years WHERE collection = '%s'", collection));
        List<Year> yearList = new LinkedList<>();
        try {
            // iterate through every show
            while (results.next()) {
                // add it to the list
                int id = results.getInt("id");
                yearList.add(getYearWithId(id));
            }
            // sort the list of shows
            Collections.sort(yearList);

            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return yearList;
    }

    //////////////////////////////////////////

    /////////// Show query methods ///////////

    private Show getShowFromQuery(String sql) {
        ResultSet rs = rawSQL(sql);
        try {
            if (rs.next()) {
                Show show = showFromResult(rs);
                rs.close();
                return show;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Show getShowWithId(int id) {
        String sql = String.format("SELECT * FROM shows where id = %d", id);
        return getShowFromQuery(sql);
    }

    public Show getShowWithDate(String date) {
        String sql = String.format("SELECT * FROM shows where date = '%s'", date);
        return getShowFromQuery(sql);
    }

    public List<Show> getShowsWithYearId(int yearId) {
        ResultSet results = rawSQL("SELECT id FROM shows WHERE yearId=" + yearId);
        List<Show> showList = new LinkedList<>();
        try {
            // iterate through every show
            while (results.next()) {
                // add it to the list
                int id = results.getInt("id");
                showList.add(getShowWithId(id));
            }
            // sort the list of shows
            Collections.sort(showList);

            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showList;
    }

    public int getShowCountForYear(int yearId) {
        int numberOfShows = 0;
        String sql = String.format("SELECT COUNT(*) AS total FROM shows where yearId=%d", yearId);
        ResultSet result = Database.getInstance().rawSQL(sql);
        try {
            result.next();
            numberOfShows = result.getInt("total");
            result.close();
        } catch (SQLException e) {
            System.err.println(e.toString());
        }
        return numberOfShows;
    }

    //////////////////////////////////////////

    /////////// Item query methods ///////////

    private Item getItemFromQuery(String sql) {
        ResultSet rs = rawSQL(sql);
        try {
            if(rs.next()) {
                Item item = itemFromResult(rs);
                rs.close();
                return item;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Item getItemWithId(int id) {
        String sql = String.format("SELECT * FROM items WHERE id=%d", id);
        return getItemFromQuery(sql);
    }

    public Item getItemWithIdentifier(String identifier) {
        String sql = String.format("SELECT * FROM items WHERE identifier='%s'", identifier);
        return getItemFromQuery(sql);
    }

    public List<Item> getItemsWithShowId(int showId) {
        ResultSet results = rawSQL("SELECT id FROM items WHERE showId=" + showId);
        List<Item> itemList = new LinkedList<>();
        try {
            // iterate through every show
            while (results.next()) {
                // add it to the list
                int id = results.getInt("id");
                itemList.add(getItemWithId(id));
            }
            // sort the list of shows
            Collections.sort(itemList);

            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemList;
    }

    public int getItemCountForShow(int showId) {
        int numberOfItems = 0;
        String sql = String.format("SELECT COUNT(*) AS total FROM items where showId=%d", showId);
        ResultSet result = Database.getInstance().rawSQL(sql);
        try {
            result.next();
            numberOfItems = result.getInt("total");
            result.close();
        } catch (SQLException e) {
            System.err.println(e.toString());
        }
        return numberOfItems;
    }

}
