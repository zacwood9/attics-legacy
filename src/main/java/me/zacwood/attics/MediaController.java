package me.zacwood.attics;

import javafx.application.Platform;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Media controller for Attics UI
 *
 * @author Zachary Wood
 */
public class MediaController {

    private ExecutorService downloadQueue;
    private List<Song> playQueue;
    private Song playingSong;
    private Item playingItem;

    private UIController uiController;

    private double volume = 100;

    // thread to update the seek bar
    private Timer seekUpdater;

    public MediaController(UIController uiController) {
        downloadQueue = Executors.newSingleThreadExecutor();
        playQueue = new LinkedList<>();

        this.uiController = uiController;
    }


    ////////// Media Control methods //////////

    /**
     * Method to play a given song.
     *
     * @param song
     */
    public void play(Song song)  {

        // if there's a current song
        if (playingSong != null) {
            // resume if the requested song current song
            if (playingSong.equals(song) && playingSong.getMediaPlayer().getStatus() == MediaPlayer.Status.PAUSED) {
                playingSong.getMediaPlayer().play();
                return;
            } else { // if it's a new song, stop the current song
                playingSong.getMediaPlayer().stop();
            }
        }

        playingSong = song;

        if (playingItem == null || !playingItem.getIdentifier().equals(playingSong.getItemIdentifier())) {
            playingItem = Database.getInstance().getItemWithIdentifier(playingSong.getItemIdentifier());
        }

        // update the queue
        setQueueFromSong(playingSong);

        // ready the stream if there's no mediaplayer
        if (playingSong.getMediaPlayer() == null) {
            playingSong.readyStream();
        }

        // reset UI
        playingSong.getMediaPlayer().setVolume(volume);
        uiController.setSong(playingSong);

        restartSeeker();

        playingSong.getMediaPlayer().play();

        // if there's another song in the queue
        if (playQueue.size() > 1 && playQueue.get(1) != null) {
            // download it
            playQueue.get(1).readyStream();

            // set the next song in the queue to play when current song is finished
            playingSong.getMediaPlayer().setOnEndOfMedia(() -> play(playQueue.get(1)));
        }


    }

    public void resume() {
        if (playingSong != null) {
            playingSong.getMediaPlayer().play();
        }
    }

    public void pause() {
        if (playingSong != null) {
            playingSong.getMediaPlayer().pause();
        }
    }

    public void seekTo(double time) {
        playingSong.getMediaPlayer().seek(playingSong.getMediaPlayer().getTotalDuration().multiply(time));
    }

    public void setVolume(double volume) {
        this.volume = volume;
        if (playingSong != null && playingSong.getMediaPlayer() != null)
            Platform.runLater(() -> playingSong.getMediaPlayer().setVolume(this.volume));
    }

    ///////////////////////////////////

    ///////// Helper methods //////////


    /**
     * Converts a double to a String representing minutes, M:SS
     */
    private String doubleToMinutes(double time) {
        double decimal = time - Math.floor(time);
        int seconds = (int) ((decimal / 10 * 60) * 10);
        int minutes = (int) time;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Restarts seeker at 0 and sets it to update every second
     */
    private void restartSeeker() {

        // cancel the current timer
        if (seekUpdater != null) seekUpdater.cancel();
        seekUpdater = new Timer();

        // create a new TimerTask that updates the timer
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                if (playingSong != null && playingSong.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                    double percent = playingSong.getMediaPlayer().getCurrentTime().toMillis() / playingSong.getMediaPlayer().getTotalDuration().toMillis();
                    Platform.runLater(() -> {
                        uiController.setSeekerPosition(percent);
                        double current = playingSong.getMediaPlayer().getCurrentTime().toMinutes();
                        double total = playingSong.getMediaPlayer().getTotalDuration().toMinutes();
                        uiController.setSeekerText(String.format("%s - %s", doubleToMinutes(current), doubleToMinutes(total)));
                    });

                }
            }
        };

        // set the Timer to run the task every second
        seekUpdater.scheduleAtFixedRate(t, 0, 1000);
    }

    /**
     * Method that updates the song queue to start with the given song and end at the end of the item
     *
     * @param song
     * @throws IOException
     */
    private void setQueueFromSong(Song song) {
        int indexOfSong = playingItem.indexOfSong(song);
        playQueue.clear();

        // add the rest of the songs in the item to the play queue
        for (int i = indexOfSong; i < playingItem.getSongs().size(); i++) {
            playQueue.add(playingItem.getSongs().get(i));
        }
    }

    ///////////////////////////////////

    public Song getPlayingSong() {
        return playingSong;
    }

    public Item getPlayingItem() {
        return playingItem;
    }

    public void setPlayingItem(Item item) throws Exception {
        this.playingItem = item;
    }


}
