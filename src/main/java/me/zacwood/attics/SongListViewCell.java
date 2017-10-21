package me.zacwood.attics;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.IOException;

public class SongListViewCell extends ListCell<Song> {
    @Override
    protected void updateItem(Song song, boolean empty) {
        super.updateItem(song, empty);

        if (empty || song == null) {
            setText(null);
            setGraphic(null);
        } else {

            if (song.getTitle() == null || song.getTitle().equals("")) {
                setText("Untitled");
            } else setText(song.getTitle() + " - " + song.getLength());

        }
    }
}
