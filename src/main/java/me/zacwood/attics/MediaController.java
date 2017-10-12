package me.zacwood.attics;

import javafx.application.Platform;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Media controller for Attics UI
 * @author Zachary Wood
 */
public class MediaController {

    // private Item item;
    // private ExecutorService downloadQueue;
    // private List<Song> playQueue;
    // private Song currentSong;

    // private UIController uiController;

    // private double volume = 100;

    // // thread to update the seek bar
    // private Timer seekUpdater;

    // public MediaController(UIController uiController) {
    //     downloadQueue = Executors.newSingleThreadExecutor();
    //     playQueue = new LinkedList<>();

    //     this.uiController = uiController;
    // }


    // ////////// Media Control methods //////////

    // /**
    //  * Method to play a given song.
    //  * @param song
    //  */
    // public void play(Song song) throws IOException {

    //     // if there's a current song
    //     if (currentSong != null) {
    //         // resume if the requested song current song
    //         if (currentSong.equals(song) && currentSong.getMediaPlayer().getStatus() == MediaPlayer.Status.PAUSED) {
    //             currentSong.getMediaPlayer().play();
    //             return;
    //         } else { // if it's a new song, stop the current song
    //             currentSong.getMediaPlayer().stop();
    //         }
    //     }

    //     currentSong = song;

    //     // update the queue
    //     setQueueFromSong(currentSong);

    //     // ready the stream if there's no mediaplayer
    //     if (currentSong.getMediaPlayer() == null) {
    //         currentSong.readyStream();
    //     }

    //     // reset UI
    //     currentSong.getMediaPlayer().setVolume(volume);
    //     uiController.setCurrentSongLabel(currentSong.getTitle());
    //     uiController.setCurrentShowLabel(item.getDate());
    //     restartSeeker();

    //     currentSong.getMediaPlayer().play();

    //     // if there's another song in the queue
    //     if (playQueue.size() > 1 && playQueue.get(1) != null) {
    //         // download it
    //         playQueue.get(1).readyStream();

    //         // set the next song in the queue to play when current song is finished
    //         currentSong.getMediaPlayer().setOnEndOfMedia(() -> {
    //             try {
    //                 play(playQueue.get(1));
    //             } catch (IOException e) {
    //                 System.err.println(e.toString());
    //             }
    //         });
    //     }


    // }

    // public void resume() {
    //     if (currentSong != null) {
    //         currentSong.getMediaPlayer().play();
    //     }
    // }

    // public void pause() {
    //     if (currentSong != null) {
    //         currentSong.getMediaPlayer().pause();
    //     }
    // }

    // public void seekTo(double time) {
    //     currentSong.getMediaPlayer().seek(currentSong.getMediaPlayer().getTotalDuration().multiply(time));
    // }

    // public void setVolume(double volume) {
    //     this.volume = volume;
    //     if (currentSong != null && currentSong.getMediaPlayer() != null)
    //         Platform.runLater(() -> currentSong.getMediaPlayer().setVolume(this.volume));
    // }

    // ///////////////////////////////////

    // ///////// Helper methods //////////


    // /**
    //  * Converts a double to a String representing minutes, M:SS
    //  */
    // private String doubleToMinutes(double time) {
    //     double decimal = time - Math.floor(time);
    //     int seconds = (int)((decimal / 10 * 60) * 10);
    //     int minutes = (int)time;
    //     return String.format("%d:%02d", minutes, seconds);
    // }

    // /**
    //  * Restarts seeker at 0 and sets it to update every second
    //  */
    // private void restartSeeker() {
    //     // reset the seeker to 0
    //     Platform.runLater(() -> uiController.setSeeker(0));

    //     // cancel the current timer
    //     if (seekUpdater != null) seekUpdater.cancel();
    //     seekUpdater = new Timer();

    //     // create a new TimerTask that updates the timer
    //     TimerTask t = new TimerTask() {
    //         @Override
    //         public void run() {
    //             if (currentSong != null && currentSong.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
    //                 double percent = currentSong.getMediaPlayer().getCurrentTime().toMillis() / currentSong.getMediaPlayer().getTotalDuration().toMillis();
    //                 Platform.runLater( () -> {
    //                     uiController.setSeeker(percent);
    //                     double current = currentSong.getMediaPlayer().getCurrentTime().toMinutes();
    //                     double total = currentSong.getMediaPlayer().getTotalDuration().toMinutes();
    //                     uiController.setCurrentSongTime(String.format("%s - %s", doubleToMinutes(current), doubleToMinutes(total)));
    //                 });

    //             }
    //         }
    //     };

    //     // set the Timer to run the task every second
    //     seekUpdater.scheduleAtFixedRate(t, 0, 1000);
    // }

    // /**
    //  * Method that updates the song queue to start with the given song and end at the end of the item
    //  * @param song
    //  * @throws IOException
    //  */
    // private void setQueueFromSong(Song song) throws IOException {
    //     int indexOfSong = item.indexOfSong(song);
    //     playQueue.clear();

    //     // add the rest of the songs in the item to the play queue
    //     for (int i = indexOfSong; i < item.getSongs().size(); i++) {
    //         playQueue.add(item.getSongs().get(i));
    //     }
    // }

    // ///////////////////////////////////

    // public Song getCurrentSong() {
    //     return currentSong;
    // }

    // public Item getItem() {
    //     return item;
    // }

    // public void setItem(Item item) throws Exception {
    //     this.item = item;
    // }


}
