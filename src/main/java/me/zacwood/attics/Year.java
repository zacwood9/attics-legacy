package me.zacwood.attics;

public class Year implements Comparable<Year> {
    private int id;
    private String year;
    private String collection;

    public Year(int id, String year, String collection) {
        this.id = id;
        this.year = year;
        this.collection = collection;
    }

    public int getId() {
        return id;
    }

    public String getYear() {
        return year;
    }

    public int compareTo(Year y) {
        return year.compareTo(y.getYear());
    }
}