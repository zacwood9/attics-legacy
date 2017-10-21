package me.zacwood.attics;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;

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
    Button playPauseButton;

    // TODO: make selectable in UI
    private String collection = "GratefulDead";

    private MediaController mediaController;

    public UIController() {
        mediaController = new MediaController(this);
    }

    public void initialize() {
        // populate year list
        List<Year> years = new LinkedList<>();

        String sql = String.format("SELECT * FROM years WHERE collection='%s'", collection);
        ResultSet results = Database.getInstance().rawSQL(sql);

        try {
            // iterate through every year
            while (results.next()) {
                // add it to the set
                int id = results.getInt("id");
                String year = results.getString("year");
                years.add(new Year(id, year, collection));
            }

            Collections.sort(years);

            ObservableList<Year> yearList = FXCollections.observableArrayList();
            yearList.addAll(years);

            // add the set to the list view
            yearsListView.setItems(yearList);
            yearsListView.setCellFactory(param -> new YearListViewCell());

            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initializeListeners();

    }

    public void initializeListeners() {

        // when a year is clicked
        yearsListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {

            showsListView.setItems(FXCollections.observableArrayList());

            int yearId = newValue.getId();
            ResultSet results = Database.getInstance().rawSQL("SELECT * FROM shows WHERE yearId=" + yearId);

            List<Show> shows = new LinkedList<>();
            try {
                // iterate through every show
                while (results.next()) {
                    // add it to the set
                    int id = results.getInt("id");
                    String date = results.getString("date");
                    String venue = results.getString("venue");
                    shows.add(new Show(id, yearId, date, venue));
                }

                Collections.sort(shows);

                ObservableList<Show> showList = FXCollections.observableArrayList();
                showList.addAll(shows);

                // add the set to the list view
                showsListView.setItems(showList);
                showsListView.setCellFactory(param -> new ShowListViewCell());

                results.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));

        // when a show is clicked
        showsListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            //itemsListView.setItems(FXCollections.observableArrayList());
            if (newValue != null) {
                int showId = newValue.getId();
                ResultSet results = Database.getInstance().rawSQL("SELECT * FROM items WHERE showId=" + showId);

                List<Item> items = new LinkedList<>();
                try {
                    // iterate through every show
                    while (results.next()) {
                        // add it to the set
                        items.add(Database.itemFromResult(results));
                    }

                    Collections.sort(items);

                    ObservableList<Item> itemList = FXCollections.observableArrayList();
                    itemList.addAll(items);

                    // add the set to the list view
                    itemsListView.setItems(itemList);
                    itemsListView.setCellFactory(param -> new ItemListViewCell());

                    if (!itemsListView.isVisible()) {
                        songsListView.setVisible(false);
                        itemsListView.setVisible(true);
                    }

                    results.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

        playPauseButton.setOnAction(event -> {
            if (mediaController.getPlayingSong().getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                mediaController.pause();
                playPauseButton.setText("Play");
            } else {
                mediaController.play(mediaController.getPlayingSong());
                playPauseButton.setText("Pause");
            }
        });


    }

    private void displaySongList(Item item) {
        // hide item list, show loading text
        itemsListView.setVisible(false);
        status.setText("Loading " + item.getIdentifier() + "...");

        ObservableList<Song> songObservableList = FXCollections.observableArrayList();

        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Song> songs = item.getSongs();

                // add the songs to an observable list
                songObservableList.addAll(songs);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            // set the new list
            songsListView.setItems(songObservableList);
            songsListView.setCellFactory(param -> new SongListViewCell());

            // display song list
            songsListView.setVisible(true);
            status.setText("");
        });
        new Thread(task).start();

    }


    /**
     * Sets seeker bar
     *
     * @param dur percentage of silder
     */
    public void setSeekerPosition(double dur) {
        seekSlider.setValue(dur * 100);
    }

    public void setSeekerText(String text) {
        seekerText.setText(text);
    }


    public void setSong(Song song) {
        setSeekerPosition(0);
        songLabel.setText(song.getTitle());
        showLabel.setText(song.getAlbum());
        playPauseButton.setText("Pause");
    }

    public void setPlayPauseText(String text) {
        playPauseButton.setText(text);
    }

//     public void initialize() {
//         mediaController = new MediaController(this);


//         // initialize root item
//         root = new TreeItem<>(currentCollection);
//         root.setExpanded(true);

//         years = new TreeMap<>();

//         try {
//             Statement statement = conn.createStatement();
//             String sql = String.format("SELECT DISTINCT date FROM %s;", currentCollection);
//             ResultSet rs = statement.executeQuery(sql);

//             // iterate through every date that an item exists for
//             while (rs.next()) {

//                 // get the year of the show
//                 int year = Integer.parseInt(rs.getString("date").substring(0, 4));

//                 // if the year hasn't been added already, add an empty list
//                 years.computeIfAbsent(year, k -> years.put(year, new ArrayList<>()));

//                 String date = rs.getString("date");

//                 Show show = new Show(date);

//                 // add show to its year
//                 years.get(year).add(show);
//             }

//             // iterate through all the years in the set
//             for (int year : years.keySet()) {
//                 // create the node for the year
//                 TreeItem<String> yearNode = new TreeItem<>(Integer.toString(year));

//                 // get the list of shows for the year
//                 List<Show> showsInYear = years.get(year);

//                 // sort the shows from the year
//                 Collections.sort(showsInYear);

//                 // add all shows from that year to the year node
//                 for (Show show : showsInYear) {
//                     yearNode.getChildren().add(show);
//                 }

//                 root.getChildren().add(yearNode);
//             }

//             rs.close();
//         } catch (SQLException e) {
//             System.out.println(e.toString());
//         }

//         dateList.setRoot(root);
//         dateList.getSelectionModel().selectAll();
//         dateList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

//         initializeListeners();
//     }

//     private void initializeListeners() {
//         // when a date is clicked
//         dateList.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
//             // if the clicked element is not a year
//             if (newValue.getValue().length() > 4) {
//                 // show items for the date
//                 Show show = findShowWithDate(newValue.getValue());
//                 showItems(show);
//             } else { //TODO: show all shows for the year

//             }
//         }));

//         // when an item is clicked
//         itemList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//             try {
//                 // get the item with the clicked identifier
//                 Statement statement = conn.createStatement();
//                 String sql = String.format("SELECT * FROM %s WHERE identifier='%s';", currentCollection, newValue);
//                 ResultSet rs = statement.executeQuery(sql);

//                 rs.next();

//                 // create an item with the result
//                 selectedItem = new Item(rs.getString("identifier"), rs.getString("date"), rs.getInt("downloads"),
//                         rs.getString("avg_rating"), rs.getString("description"));

//                 // fill the info text with item's info
//                 itemInfo.setText(selectedItem.toString());

//                 rs.close();
//             } catch (SQLException e) {
//                 System.err.println(e.getMessage());
//             }
//         });


//         // when the song list is clicked
//         songList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//             try {
//                 // find the song that was clicked, set it as the selected song
//                 for (Song song : selectedItem.getSongs()) {
//                     if (song.getTitle().equals(observable.getValue())) {
//                         selectedSong = song;
//                         break;
//                     }
//                 }
//             } catch (Exception e) {
//                 System.err.println(e);
//             }
//         });

//         // volume slider listener
//         volumeSlider.valueProperty().addListener(observable -> mediaController.setVolume(volumeSlider.getValue() / 100));

//         seekSlider.setOnMousePressed(event -> {
//             mediaController.pause();
//         });
//         // seek slider listener
//         seekSlider.setOnMouseReleased(event -> {
//             mediaController.seekTo(seekSlider.getValue() / 100);
//             mediaController.resume();

//         });

//         // double click listener for items
//         itemList.setOnMouseClicked(event -> {
//             if (event.getClickCount() == 2) {
//                 try {
//                     showSongs();
//                 } catch (Exception e) {
//                     System.err.println(e.toString());
//                 }
//             }
//         });


//         // double click listener for songs
//         songList.setOnMouseClicked(event -> {
//             if (event.getClickCount() == 2) {
//                 try {
//                     mediaController.play(selectedSong);
//                 } catch (Exception e) {
//                     System.err.println(e.toString());
//                 }

//             }
//         });
//     }

//     /**
//      * Method to display all items for a given show
//      *
//      * @param show
//      */
//     private void showItems(Show show) {

//         List<Item> items = new LinkedList<>();

//         try {
//             // select all items from db with the given date
//             Statement statement = conn.createStatement();
//             String sql = String.format("SELECT * FROM %s WHERE date='%s'", currentCollection, show.getDate());
//             ResultSet rs = statement.executeQuery(sql);

//             // add all items to the linked list
//             while (rs.next()) {
//                 Item item = new Item(rs.getString("identifier"), rs.getString("date"), rs.getInt("downloads"),
//                         rs.getString("avg_rating"), rs.getString("description"));
//                 items.add(item);
//             }

//             // sort the list of items by number of downloads
//             Collections.sort(items);

//             // store the list in an OberserableList
//             ObservableList<String> observableList = FXCollections.observableArrayList();
//             for (Item item : items) {
//                 observableList.add(item.getIdentifier());
//             }

//             // display the list
//             itemList.setItems(observableList);
//         } catch (SQLException e) {
//             System.err.println(e.getMessage());
//         }

//     }

//     /**
//      * Method to display all songs for the current item
//      *
//      * @throws Exception
//      */
//     @FXML
//     private void showSongs() throws Exception {

//         ObservableList<String> observableSongList = FXCollections.observableArrayList();
//         for (Song song : selectedItem.getSongs()) {
//             observableSongList.add(song.getTitle());
//         }

//         songList.setItems(observableSongList);

//         mediaController.setItem(selectedItem);
//         selectedItemLabel.setText(selectedItem.getIdentifier());
//     }

//     @FXML
//     private void playSong() throws Exception {
// //        if (mediaController.getCurrentSong() != null) {
// //            mediaController.play(mediaController.getCurrentSong());
// //        }
// //        else
//         mediaController.play(selectedSong);

//     }

//     @FXML
//     private void pauseSong() throws Exception {
//         mediaController.pause();
//     }

//     @FXML
//     private void downloadItem() throws Exception {
//         for (Song song : selectedItem.getSongs()) {
//             song.downloadAsync();
//         }
//     }

//     /**
//      * Returns a show that has the given date
//      * @param date
//      * @return
//      */
//     private Show findShowWithDate(String date) {
//         for (int year : years.keySet()) {
//             for (Show show : years.get(year)) {
//                 if (show.getDate().equals(date)) {
//                     return show;
//                 }
//             }
//         }
//         return null;
//     }

//     public void setCurrentSongLabel(String text) {
//         this.currentSongLabel.setText(text);
//     }

//     public void setCurrentShowLabel(String text) {
//         this.currentShowLabel.setText(text);
//     }

//     public void setCurrentAction(String text) {
//         this.currentAction.setText(text);
//     }

//     public void setCurrentSongTime(String time) {
//         currentSongTime.setText(time);
//     }


}



