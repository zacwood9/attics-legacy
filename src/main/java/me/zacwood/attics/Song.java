package me.zacwood.attics;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.concurrent.FutureCallback;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Song {
    /**
     * The file name, e.g. d1t01.mp3
     */
    private String name;

    /**
     * Song title, e.g "Bertha"
     */
    private String title;

    private int track;
    private String album;
    private String length;

    private URI filePath;

    private MediaPlayer mediaPlayer;

    /**
     * Identifier of the item to which the song belongs
     */
    private String itemIdentifier;

    public Song(String name, String title, int track, String album, String length, String itemIdentifier) {
        this.name = name;
        this.title = title;
        this.track = track;
        this.album = album;
        this.length = length;
        this.itemIdentifier = itemIdentifier;

        try {
            File temp = new File(String.format("items/%s/%s.mp3", itemIdentifier, title));
            filePath = temp.toURI();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getTrack() {
        return track;
    }

    public String getAlbum() {
        return album;
    }

    public String getLength() {
        return length;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public String getItemIdentifier() {
        return itemIdentifier;
    }

    public URI getFilePath() {
        return filePath;
    }

    public void download() {
        try {
            File file = new File(filePath);

            if (!file.exists()) { // if the file for this song doesn't exist
                // create the directories to store the file
                Files.createDirectories(Paths.get("items/" + itemIdentifier));

                String requestUrl = String.format("http://archive.org/download/%s/%s", itemIdentifier, name);
                System.out.println("Downloading " + title);
                // download song and store it in file
                Request.Get(requestUrl).execute().saveContent(file);
            }

            // create a Media Player for the song if it doesn't already exist
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer(new Media(filePath.toASCIIString()));

        } catch (IOException e) {
            System.err.println(e.toString());
        }

    }

    public void downloadAsync() throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            String requestUrl = String.format("http://archive.org/download/%s/%s", itemIdentifier, name);
            Files.createDirectories(Paths.get("items/" + itemIdentifier));

            Async.newInstance().execute(Request.Get(requestUrl), new FutureCallback<Content>() {
                @Override
                public void completed(Content content) {
                    try {
                        Files.write(Paths.get(filePath), content.asBytes());
                        mediaPlayer = new MediaPlayer(new Media(filePath.toASCIIString()));
                    } catch (IOException e) {
                        System.err.println(e.toString());
                    }
                }

                @Override
                public void failed(Exception e) {

                }

                @Override
                public void cancelled() {

                }
            });
        }


    }

    public boolean equals(Song s) {
        return s.getFilePath() == filePath && s.getTrack() == track;
    }
}
