package me.zacwood.attics;

import javafx.scene.control.TreeItem;

import java.util.ArrayList;

public class Show extends TreeItem<String> implements Comparable<Show> {

    private String date;
    private ArrayList<Item> items;

    public Show(String date) {
        super(date);
        this.date = date;
    }

    public Show(String date, ArrayList<Item> items) {
        super(date);
        this.date = date;
        this.items = items;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    @Override
    public int compareTo(Show o) {
        return getDate().compareTo(o.getDate());
    }
}
