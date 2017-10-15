package me.zacwood.attics;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowListViewCell extends ListCell<Show> {
    
    private FXMLLoader loader;
    
    @FXML
    private Label date;

    @FXML
    private Label numberOfItems;

    @FXML
    private Label rating;

    @FXML
    private Label venue;

    private int itemNum = -1;

    @FXML
    private VBox vBox;

    public ShowListViewCell() {
        setLoader();

        // TODO: move to update, add null check 

    }

    private void setLoader() {
        try {
            loader = new FXMLLoader(getClass().getClassLoader().getResource("ShowListViewCell.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    @Override
    protected void updateItem(Show show, boolean empty) {
        super.updateItem(show, empty);

        if (empty || show == null) {
            setText(null);
        } else {
            if(loader == null) {
                setLoader();
            }

            // set date and count fields
            date.setText(show.getDate());



            // get avg. rating
            // ResultSet result = Database.getInstance().rawSQL("SELECT avg_rating FROM items WHERE showId=" + show.getId());
            // double sum = 0;
            // int count = 0;
            // try {
            //     while(result.next()) {
            //         double rating = result.getDouble("avg_rating");
            //         if (rating != 0) {
            //             sum += rating;
            //             count++;
            //         }
            //     }
            // } catch (SQLException e) {
            //     e.printStackTrace();
            // }
            // double avg = sum / count;
            // rating.setText(String.format("Rating: %.02f / 5", avg));

            if (itemNum == -1) {
                String sql = String.format("SELECT COUNT(*) AS total FROM items where showId=%d", show.getId());
                ResultSet result = Database.getInstance().rawSQL(sql);
                try {
                    result.next();
                    itemNum = result.getInt("total");
                    result.close();
                } catch (SQLException e) {
                    System.err.println(e.toString());
                }
            }

            numberOfItems.setText(itemNum + " recordings");
            //rating.setText("");
            venue.setText(show.getVenue());

            setText(null);
            setGraphic(vBox);

        }
    }
}
