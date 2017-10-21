package me.zacwood.attics;

import javafx.scene.control.TableRow;
import org.apache.http.client.fluent.Request;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.print.DocFlavor;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Item implements Comparable<Item> {

    private int id;
    private int showId;
    private String identifier;
    private int numReviews;
    private int downloads;
    private String avgRating;
    private String description;
    private String source;
    private JsonObject metadata;
    private List<Song> songs;

    public Item(int id, int showId, String identifier, int downloads, String avgRating, int numReviews, String description, String source) {
        this.id = id;
        this.showId = showId;
        this.identifier = identifier;
        this.numReviews = numReviews;
        this.downloads = downloads;
        this.avgRating = avgRating;
        this.description = description;
        this.source = source;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public String getIdentifier() {
        return identifier;
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

    private void loadMetadata() {
        String requestUrl = "http://archive.org/metadata/" + identifier;

        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonObject getMetadata() {
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
    public List<Song> getSongs()  {
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

    public HashMap<String, String> getSourceInfo() {
        HashMap<String, String> result = new HashMap();
        result.put("identifier", identifier);
        result.put("description", description);
        result.put("date", getMetadata().getString("date"));
        result.put("source", getMetadata().getString("source"));
        result.put("taper", getMetadata().getString("taper"));
        result.put("lineage", getMetadata().getString("lineage"));
        return result;
    }

    public int getId() {
        return id;
    }

    public int getNumReviews() {
        return numReviews;
    }

    public String getSource() {
        return source;
    }

    /**
     * @param song
     * @return index of song in this item's song array
     */
    public int indexOfSong(Song song) {
        for (int i = 0; i < getSongs().size(); i++) {
            if(song.equals(songs.get(i))) return i;
        }
        return -1;
    }

    public String toString() {
        return String.format("Identifier: %s\n\nDownloads: %d\n\nAverage Rating: %s",
                identifier, downloads, avgRating);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Item) {
            Item other = (Item)o;
            return id == other.id;
        }
        return false;
    }

    @Override
    public int compareTo(Item o) {
        return Integer.compare(o.getDownloads(), getDownloads());
    }
}
