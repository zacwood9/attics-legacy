package me.zacwood.attics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeSet;

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

    // TODO: make selectable in UI
    private String collection = "GratefulDead";

    private MediaController mediaController;

    public UIController() {

    }

    public void initialize() {
        // populate year list
        TreeSet<Year> years = new TreeSet<>();

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
        yearsListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
//            showsListView.setItems(FXCollections.observableArrayList());
//            itemsListView.setItems(FXCollections.observableArrayList());
            int yearId = newValue.getId();
            ResultSet results = Database.getInstance().rawSQL("SELECT * FROM shows WHERE yearId=" + yearId);

            TreeSet<Show> shows = new TreeSet<>();
            try {
                // iterate through every show
                while (results.next()) {
                    // add it to the set
                    int id = results.getInt("id");
                    String date = results.getString("date");
                    String venue = results.getString("venue");
                    shows.add(new Show(id, yearId, date, venue));
                }

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

        showsListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            //itemsListView.setItems(FXCollections.observableArrayList());
            if (newValue != null) {
                int showId = newValue.getId();
                ResultSet results = Database.getInstance().rawSQL("SELECT * FROM items WHERE showId=" + showId);

                TreeSet<Item> items = new TreeSet<>();
                try {
                    // iterate through every show
                    while (results.next()) {
                        // add it to the set
                        int id = results.getInt("id");
                        String identifier = results.getString("identifier");
                        int downloads = results.getInt("downloads");
                        int numReviews = results.getInt("num_reviews");
                        String avgRating = results.getString("avg_rating");
                        String description = results.getString("description");
                        String source = results.getString("source");
                        items.add(new Item(id, identifier, downloads, avgRating, numReviews, description, source));
                    }

                    ObservableList<Item> itemList = FXCollections.observableArrayList();
                    itemList.addAll(items);

                    // add the set to the list view
                    itemsListView.setItems(itemList);
                    itemsListView.setCellFactory(param -> new ItemListViewCell());

                    results.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }));

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

//     /**
//      * Sets seeker bar
//      * @param dur percentage of silder
//      */
//     public void setSeeker(double dur) {
//         seekSlider.setValue(dur * 100);
//     }

}



