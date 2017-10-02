package me.zacwood.attics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class UIController {

    @FXML
    ListView<String> itemList;

    @FXML
    ListView<String> songList;

    @FXML
    private Label itemInfo;

    @FXML
    private Label selectedItemLabel;

    @FXML
    private Label currentSongLabel;

    @FXML
    private Label currentShowLabel;

    @FXML
    private TextField date;

    @FXML
    private Label selectedShowLabel;

    @FXML
    private Label currentAction;

    @FXML
    private Button serach;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Slider seekSlider;

    @FXML
    private TreeView<String> dateList;

    // TODO: make selectable in UI
    private String currentCollection = "GratefulDead";

    private TreeItem<String> root;
    private SortedMap<Integer, List<Show>> years;
    private Item selectedItem;
    private Song selectedSong;

    private MediaController mediaController;

    private Connection conn;

    public UIController() {
        // connect to DB
        try {
            String url = "jdbc:sqlite:archive.db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    public void initialize() {
        mediaController = new MediaController(this);


        // initialize root item
        root = new TreeItem<>(currentCollection);
        root.setExpanded(true);

        years = new TreeMap<>();

        try {
            Statement statement = conn.createStatement();
            String sql = String.format("SELECT DISTINCT date FROM %s;", currentCollection);
            ResultSet rs = statement.executeQuery(sql);

            // iterate through every date that an item exists for
            while (rs.next()) {

                // get the year of the show
                int year = Integer.parseInt(rs.getString("date").substring(0, 4));

                // if the year hasn't been added already, add an empty list
                years.computeIfAbsent(year, k -> years.put(year, new ArrayList<>()));

                String date = rs.getString("date");

                Show show = new Show(date);

                // add show to its year
                years.get(year).add(show);
            }

            // iterate through all the years in the set
            for (int year : years.keySet()) {
                // create the node for the year
                TreeItem<String> yearNode = new TreeItem<>(Integer.toString(year));

                // get the list of shows for the year
                List<Show> showsInYear = years.get(year);

                // sort the shows from the year
                Collections.sort(showsInYear);

                // add all shows from that year to the year node
                for (Show show : showsInYear) {
                    yearNode.getChildren().add(show);
                }

                root.getChildren().add(yearNode);
            }

            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        dateList.setRoot(root);
        dateList.getSelectionModel().selectAll();
        dateList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // when a date is clicked
        dateList.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            // if the clicked element is not a year
            if (newValue.getValue().length() > 4) {
                // show items for the date
                Show show = findShowWithDate(newValue.getValue());
                showItems(show);
            } else { //TODO: show all shows for the year

            }
        }));

        // when an item is clicked
        itemList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                // get the item with the clicked identifier
                Statement statement = conn.createStatement();
                String sql = String.format("SELECT * FROM %s WHERE identifier='%s';", currentCollection, newValue);
                ResultSet rs = statement.executeQuery(sql);

                rs.next();

                // create an item with the result
                selectedItem = new Item(rs.getString("identifier"), rs.getString("date"), rs.getInt("downloads"),
                        rs.getString("avg_rating"), rs.getString("description"));

                // fill the info text with item's info
                itemInfo.setText(selectedItem.toString());

                rs.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        });


        // when the song list is clicked
        songList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                // find the song that was clicked, set it as the selected song
                for (Song song : selectedItem.getSongs()) {
                    if (song.getTitle().equals(observable.getValue())) {
                        selectedSong = song;
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        });

        // volume slider listener
        volumeSlider.valueProperty().addListener(observable -> mediaController.setVolume(volumeSlider.getValue() / 100));

        seekSlider.setOnMousePressed(event -> {
            mediaController.pause();
        });
        // seek slider listener
        seekSlider.setOnMouseReleased(event -> {
            System.out.println(seekSlider.getValue() / 100);
            mediaController.seekTo(seekSlider.getValue() / 100);
            mediaController.resume();

        });

        // double click listener for items
        itemList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                try {
                    showSongs();
                } catch (Exception e) {
                    System.err.println(e.toString());
                }
            }
        });


        // double click listener for songs
        songList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                try {
                    mediaController.play(selectedSong);
                } catch (Exception e) {
                    System.err.println(e.toString());
                }

            }
        });
    }

    /**
     * Sets seeker bar
     * @param dur percentage of silder
     */
    public void setSeeker(double dur) {
        seekSlider.setValue(dur * 100);
    }

    /**
     * Method to display all items for a given show
     *
     * @param show
     */
    private void showItems(Show show) {

        List<Item> items = new LinkedList<>();

        try {
            // select all items from db with the given date
            Statement statement = conn.createStatement();
            String sql = String.format("SELECT * FROM %s WHERE date='%s'", currentCollection, show.getDate());
            ResultSet rs = statement.executeQuery(sql);

            // add all items to the linked list
            while (rs.next()) {
                Item item = new Item(rs.getString("identifier"), rs.getString("date"), rs.getInt("downloads"),
                        rs.getString("avg_rating"), rs.getString("description"));
                items.add(item);
            }

            // sort the list of items by number of downloads
            Collections.sort(items);

            // store the list in an OberserableList
            ObservableList<String> observableList = FXCollections.observableArrayList();
            for (Item item : items) {
                observableList.add(item.getIdentifier());
            }

            // display the list
            itemList.setItems(observableList);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

    }

    /**
     * Method to display all songs for the current item
     *
     * @throws Exception
     */
    @FXML
    private void showSongs() throws Exception {

        ObservableList<String> observableSongList = FXCollections.observableArrayList();
        for (Song song : selectedItem.getSongs()) {
            observableSongList.add(song.getTitle());
        }

        songList.setItems(observableSongList);

        mediaController.setItem(selectedItem);
        selectedItemLabel.setText(selectedItem.getIdentifier());
    }

    /**
     * Method to play the selected song
     *
     * @throws Exception
     */
    @FXML
    private void playSong() throws Exception {
        mediaController.play(selectedSong);
    }

    @FXML
    private void pauseSong() throws Exception {
        mediaController.pause();
    }

    @FXML
    private void downloadItem() throws Exception {
        for (Song song : selectedItem.getSongs()) {
            song.downloadAsync();
        }
    }

    /**
     * Method to return a show that has the passed in date
     *
     * @param date
     * @return
     */
    private Show findShowWithDate(String date) {
        for (int year : years.keySet()) {
            for (Show show : years.get(year)) {
                if (show.getDate().equals(date)) {
                    return show;
                }
            }
        }
        return null;
    }

    public void setCurrentSongLabel(String text) {
        this.currentSongLabel.setText(text);
    }

    public void setCurrentShowLabel(String text) {
        this.currentShowLabel.setText(text);
    }

    public void setCurrentAction(String text) {
        this.currentAction.setText(text);
    }

    @FXML
    public void searchShows() {
        System.out.println(date.getText());
    }
}



