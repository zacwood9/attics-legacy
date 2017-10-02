package me.zacwood.attics;

import javafx.scene.control.TableRow;
import org.apache.http.client.fluent.Request;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Item extends TableRow<String> implements Comparable<Item> {

    private String identifier;
    private String date;
    private int downloads;
    private String avgRating;
    private String description;
    private JsonObject metadata;
    private List<Song> songs;

    public Item(String identifier, String date, int downloads, String avgRating, String description) {
        this.identifier = identifier;
        this.date = date;
        this.downloads = downloads;
        this.avgRating = avgRating;
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDate() {
        return date;
    }

    public int getDownloads() {
        return downloads;
    }

    public String getAvgRating() {
        return avgRating;
    }

    public String getDescription() {
        return description;
    }

    private void loadMetadata() throws IOException {
        String requestUrl = "http://archive.org/metadata/" + identifier;

        // get the metadata for the item as a json stream
        InputStream jsonStream = Request.Get(requestUrl).execute().returnContent().asStream();

        BufferedReader streamReader = new BufferedReader(new InputStreamReader(jsonStream));

        StringBuilder result = new StringBuilder();
        String line;

        // read each line of the stream
        while ((line = streamReader.readLine()) != null) {
            result.append(line);
        }
        streamReader.close();

        JsonReader jsonReader = Json.createReader(new StringReader(result.toString()));
        metadata = jsonReader.readObject();
    }

    private JsonObject getMetadata() throws IOException {
        if (metadata != null) {
            return metadata;
        }
        else  {
            loadMetadata();
            return metadata;
        }
    }

    /**
     * @return LinkedList of Song objects of all songs in the item
     * @throws IOException
     */
    public List<Song> getSongs() throws IOException {
        if (songs != null) return songs;

        songs = new LinkedList<>();

        // loop through all the files in the metadata
        for (JsonValue fileVal : getMetadata().get("files").asJsonArray()) {
            JsonObject file = fileVal.asJsonObject();
            String fileName = file.get("name").toString();

            if (fileName.contains(".mp3")) {

                // store all the metadata fields if they exist, else a blank string
                String name = file.getOrDefault("name", Json.createValue("")).toString().replace("\"", "");
                String title = file.getOrDefault("title", Json.createValue("")).toString().replace("\"", "");

                String trackStr = file.getOrDefault("track", Json.createValue(0)).toString().replace("\"", "");
                int track = (trackStr == null) ? 0 : Integer.parseInt(trackStr);

                String album = file.getOrDefault("album", Json.createValue("")).toString().replace("\"", "");
                String length = file.getOrDefault("length", Json.createValue("")).toString().replace("\"", "");

                songs.add(new Song(name, title, track, album, length, identifier));
            }
        }

        return songs;
    }

    /**
     * @param song
     * @return index of song in this item's song array
     */
    public int indexOfSong(Song song) {
        for (int i = 0; i < songs.size(); i++) {
            if(song.equals(songs.get(i))) return i;
        }
        return -1;
    }

    public String toString() {
        return String.format("Identifier: %s\n\nDate: %s\n\nDownloads: %d\n\nAverage Rating: %s",
                identifier, date, downloads, avgRating);
    }

    @Override
    public int compareTo(Item o) {
        return Integer.compare(o.getDownloads(), getDownloads());
    }
}
