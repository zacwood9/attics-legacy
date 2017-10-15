package me.zacwood.attics;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemListViewCell extends ListCell<Item>{

    private FXMLLoader loader;

    @FXML
    private Label identifier;

    @FXML
    private Label downloads;

    @FXML
    private Label rating;

    @FXML
    private VBox vBox;

    @Override
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (loader == null ) {
                try {
                    loader = new FXMLLoader(getClass().getClassLoader().getResource("ItemListViewCell.fxml"));
                    loader.setController(this);
                    loader.load();
                } catch (IOException e) {
                    System.err.println(e.toString());
                }
            }
            // set downloads and identifier fields
            String source = "Default";

            if(item.getSource().toLowerCase().contains("matrix")) {
                source = "Matrix";
            } else if (item.getSource().toLowerCase().contains("sbd") || item.getSource().toLowerCase().contains("soundboard")) {
                source = "Soundboard";
            } else if (item.getSource().toLowerCase().contains("aud")) {
                source = "AUD";
            }

            identifier.setText(source);
            downloads.setText(Integer.toString(item.getDownloads()) + " downloads");

            // get avg. rating
            rating.setText(item.getAvgRating());

            setText(null);
            setGraphic(vBox);

        }
    }
}
