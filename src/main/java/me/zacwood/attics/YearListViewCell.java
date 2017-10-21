package me.zacwood.attics;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class YearListViewCell extends ListCell<Year> {
    
    private FXMLLoader loader;
    
    @FXML
    private Label year;

    @FXML
    private Label numberOfShows;

    @FXML
    private VBox vBox;

    @Override
    protected void updateItem(Year year, boolean empty) {
        super.updateItem(year, empty);

        if (empty || year == null) {
            setText(null);
        } else {
            try {
                if (loader == null) {
                    loader = new FXMLLoader(getClass().getClassLoader().getResource("YearListViewCell.fxml"));
                    loader.setController(this);
                    loader.load();
                }
            } catch (IOException e) {
                System.err.println(e.toString());
            }

            this.year.setText(year.getYear());

            numberOfShows.setText(Database.getInstance().getShowCountForYear(year.getId()) + " shows");
            setText(null);
            setGraphic(vBox);

        }
    }
}
