package me.zacwood.attics;

import javafx.scene.control.TreeItem;

import java.util.ArrayList;

public class Show implements Comparable<Show> {

    private int id;
    private int yearId;
    private String date;

    public Show(int id, int yearId, String date) {
        this.id = id;
        this.yearId = yearId;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public int getYearId() {
        return yearId;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setYearId(int yearId) {
        this.yearId = yearId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int compareTo(Show o) {
        return getDate().compareTo(o.getDate());
    }
}
