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

    //private int itemNum;

    @FXML
    private VBox vBox;

    public ShowListViewCell() {
        setLoader();
        //itemNum = 0;
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
            setGraphic(null);
        } else {
            if(loader == null) {
                setLoader();
            }

            int itemNum = Database.getInstance().getItemCountForShow(show.getId());
            numberOfItems.setText(itemNum + " recordings");

            // set date and count fields
            date.setText(show.getDate());

            venue.setText(show.getVenue());

            setText(null);
            setGraphic(vBox);

        }
    }
}
