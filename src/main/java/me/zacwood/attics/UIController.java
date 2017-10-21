package me.zacwood.attics;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.MediaPlayer;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * UI controller class
 *
 * @author Zachary Wood
 */
public class UIController {

    @FXML
    ListView<Year> yearsListView;

    @FXML
    ListView<Show> showsListView;

    @FXML
    ListView<Item> itemsListView;

    @FXML
    ListView<Song> songsListView;

    @FXML
    Slider seekSlider;

    @FXML
    Slider volumeSlider;

    @FXML
    Label songLabel;

    @FXML
    Label showLabel;

    @FXML
    Label seekerText;

    @FXML
    Label status;

    @FXML
    Label itemsSongsLabel;

    @FXML
    TextField searchBar;

    @FXML
    Button playPauseButton;

    @FXML
    Button otherSources;

    Show selectedShow;

    // TODO: make selectable in UI
    private String collection = "GratefulDead";

    private MediaController mediaController;

    public UIController() {
        mediaController = new MediaController(this);
    }

    public void initialize() {
        // populate year list
        List<Year> years = Database.getInstance().getYearsWithCollection(collection);

        ObservableList<Year> yearList = FXCollections.observableArrayList();
        yearList.addAll(years);

        // add the set to the list view
        yearsListView.setItems(yearList);
        yearsListView.setCellFactory(param -> new YearListViewCell());

        initializeListeners();

    }

    /**
     * Big ugly method to setup the various change listeners.
     * There's probably a cleaner, more readable way to do this.
     */
    public void initializeListeners() {

        // when a year is clicked
        yearsListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null)
                displayShowList(newValue);
        }));

        // when a show is clicked
        showsListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            //itemsListView.setItems(FXCollections.observableArrayList());
            if (newValue != null) {
                displayItemList(newValue);
            }
        }));

        // when an item is clicked
        itemsListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            //itemsListView.setItems(FXCollections.observableArrayList());
            if (newValue != null) {
                displaySongList(newValue);
            }
        }));

        // volume slider listener
        volumeSlider.valueProperty().addListener(observable -> mediaController.setVolume(volumeSlider.getValue() / 100));

        // when the mouse is pressed on the slider, pause the song
        seekSlider.setOnMousePressed(event -> {
            mediaController.pause();
        });

        // seek slider listener
        seekSlider.setOnMouseReleased(event -> {
            mediaController.seekTo(seekSlider.getValue() / 100);
            mediaController.play(mediaController.getPlayingSong());
        });

        // double click listener for songs
        songsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                try {
                    mediaController.play(songsListView.getSelectionModel().getSelectedItem());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // when the play/pause button is clicked
        playPauseButton.setOnAction(event -> {
            if (mediaController.getPlayingSong().getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                mediaController.pause();
                playPauseButton.setText("Play");
            } else {
                mediaController.play(mediaController.getPlayingSong());
                playPauseButton.setText("Pause");
            }
        });

        // when the show or song labels are clicked
        // maybe these should do different things?
        showLabel.setOnMouseClicked(event -> goToPlayingSong());
        songLabel.setOnMouseClicked(event -> goToPlayingSong());

    }

    /**
     * If the playing item is already selected, just scroll to the playing song.
     * Else, display the song list of the playing item, then scroll to the playing song.
     */
    private void goToPlayingSong() {
        if (mediaController.getPlayingItem() != null) {
            if(mediaController.getPlayingItem().equals(itemsListView.getSelectionModel().getSelectedItem())) {
                songsListView.getSelectionModel().select(mediaController.getPlayingSong());
                songsListView.scrollTo(mediaController.getPlayingSong());
            } else {
                displaySongList(mediaController.getPlayingItem());
                selectedShow = Database.getInstance().getShowWithId(mediaController.getPlayingItem().getShowId());
                songsListView.scrollTo(mediaController.getPlayingSong());
            }
        }
    }

    /**
     * Displays the list of items for a given show.
     * @param show
     */
    private void displayItemList(Show show) {
        int showId = show.getId();

        // get all items that belong to the given show
        List<Item> items = Database.getInstance().getItemsWithShowId(showId);

        ObservableList<Item> itemList = FXCollections.observableArrayList();
        itemList.addAll(items);

        // clear selection so the selection model doesn't
        // automatically load the song list for the previously
        // selected item.
        itemsListView.getSelectionModel().clearSelection();

        // add the list of items to the list view
        itemsListView.setItems(itemList);
        itemsListView.setCellFactory(param -> new ItemListViewCell());

        // set the item list to be visible
        if (!itemsListView.isVisible()) {
            songsListView.setVisible(false);
            otherSources.setVisible(false);
            itemsListView.setVisible(true);
        }
        itemsSongsLabel.setText("Items");
        selectedShow = show;
    }


    /**
     * Displays the list of songs for a given item.
     * @param item
     */
    private void displaySongList(Item item) {
        // hide item list, show loading text
        status.setText("Loading " + item.getIdentifier() + "...");

        ObservableList<Song> songObservableList = FXCollections.observableArrayList();

        // in order to properly update the UI and that the window remains
        // responsive, create a task to perform the (possibly) blocking
        // call to getSongs().
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Song> songs = item.getSongs();

                // add the songs to an observable list
                songObservableList.addAll(songs);
                return null;
            }
        };

        // when the task is finished, add the song list
        // to the UI and set it to be visible.
        task.setOnSucceeded(event -> {
            // set the new list
            songsListView.setItems(songObservableList);
            songsListView.setCellFactory(param -> new SongListViewCell());

            // display song list
            itemsListView.setVisible(false);

            songsListView.getSelectionModel().clearSelection();
            songsListView.setVisible(true);
            otherSources.setVisible(true);
            status.setText("");
            itemsSongsLabel.setText("Songs");
        });

        // start the task
        new Thread(task).start();

    }

    private void displayShowList(Year year) {
        // set the show list to an empty list
        showsListView.setItems(FXCollections.observableArrayList());

        // get all shows that belong to the selected year
        int yearId = year.getId();

        List<Show> shows = Database.getInstance().getShowsWithYearId(yearId);

        ObservableList<Show> showList = FXCollections.observableArrayList();
        showList.addAll(shows);

        // add the list of shows to the list view
        showsListView.setItems(showList);
        showsListView.setCellFactory(param -> new ShowListViewCell());
    }

    /**
     * Show all the items for the show of the currently playing item
     */
    @FXML
    private void viewOtherSources() {
        songsListView.setVisible(false);
        otherSources.setVisible(false);
        displayItemList(selectedShow);
    }

    @FXML
    private void search() {
        String text = searchBar.getText();
        // search for a date
        if (text.length() == 10) {
            Show show = Database.getInstance().getShowWithDate(text);
            if (show != null) {
                Year year = Database.getInstance().getYearWithId(show.getYearId());
                if (year != null) {
                    yearsListView.getSelectionModel().select(year);
                    yearsListView.scrollTo(year);
                    showsListView.getSelectionModel().select(show);
                    showsListView.scrollTo(show);
                }
            }

        } else if (text.length() > 10) { // archive link
            if (text.contains("gd")) {
                String identifier = text.substring(text.indexOf("gd"), text.length());
                Item item = Database.getInstance().getItemWithIdentifier(identifier);

                if (item != null) {
                    Show show = Database.getInstance().getShowWithId(item.getShowId());

                    if (show != null) {
                        Year year = Database.getInstance().getYearWithId(show.getYearId());

                        yearsListView.getSelectionModel().select(year);
                        yearsListView.scrollTo(year);
                        showsListView.getSelectionModel().select(show);
                        showsListView.scrollTo(show);

                        itemsListView.getSelectionModel().select(item);
                    }
                }
            }
        }
    }

    /**
     * Change the UI to a new song.
     * @param song
     */
    public void setSong(Song song) {
        // Resets the seeker, and sets the labels to the new song's info
        setSeekerPosition(0);
        songLabel.setText(song.getTitle());
        showLabel.setText(song.getAlbum());
        playPauseButton.setText("Pause");
    }

//    private int indexOf(Object o) {
//        if (o instanceof Year) {
//            Year year = (Year)o;
//            yearsListView.
//        }
//    }

    /**
     * Sets seeker bar
     * @param dur percentage of silder
     */
    public void setSeekerPosition(double dur) {
        seekSlider.setValue(dur * 100);
    }

    public void setSeekerText(String text) {
        seekerText.setText(text);
    }
}



