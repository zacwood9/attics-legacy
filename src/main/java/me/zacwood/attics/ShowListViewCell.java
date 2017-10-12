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
    @FXML
    private Label date;

    @FXML
    private Label numberOfItems;

    @FXML
    private VBox vBox;

    @Override
    protected void updateItem(Show show, boolean empty) {
        super.updateItem(show, empty);

        if (empty || show == null) {
            setText(null);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ShowListViewCell.fxml"));
                loader.setController(this);
                loader.load();
            } catch (IOException e) {
                System.err.println(e.toString());
            }

            date.setText(show.getDate());
            String sql = String.format("SELECT COUNT(*) AS total FROM items where showId=%d", show.getId());
            ResultSet result = Database.getInstance().rawSQL(sql);
            try {
                result.next();
                int num = result.getInt("total");
                numberOfItems.setText(num + " recordings");
                result.close();
            } catch (SQLException e) {
                System.err.println(e.toString());
            }

            setText(null);
            setGraphic(vBox);

        }
    }
}
