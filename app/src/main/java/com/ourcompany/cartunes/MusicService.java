package com.ourcompany.cartunes;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by DScha_000 on 8/26/2015.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {

    // Media Player
    private MediaPlayer player;
    // Song List
    private ArrayList<Song> songs;
    // Current Position
    private int songPosn;
    // Binder
    private final IBinder musicBind = new MusicBinder();
    // Title of current song
    private String songTitle="";
    // Notification ID
    private static final int NOTIFY_ID=1;
    // Shuffle flag anf random
    private boolean shuffle=false;
    private Random rand;

    public void onCreate() {
        // Create the service
        super.onCreate();
        // Initialize position
        songPosn=0;
        // Random
        rand=new Random();
        // Create player
        player = new MediaPlayer();
        // Initialize
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        // Set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // Set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    // Pass song list
    public void setList(ArrayList<Song> theSongs) {
        songs=theSongs;
    }

    // Binder
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    // Activity will bind to service
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }
    // Release resources when unbound
    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    // Play a song
    public void playSong() {
        // Play
        player.reset();
        // Get song
        Song playSong = songs.get(songPosn);
        // Get title
        songTitle=playSong.getTitle();
        // Get id
        long currSong = playSong.getID();
        // Set URI
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong
        );
        // Set the data source
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    // Set the song
    public void setSong(int songIndex) {
        songPosn=songIndex;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // Check if playback has reached the end of the track
        if(player.getCurrentPosition()>0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Start playback
        mp.start();
        // Notification
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }

    // Playback methods
    public int getPosn() {
        return player.getCurrentPosition();
    }
    public int getDur() {
        return player.getDuration();
    }
    public boolean isPng() {
        return player.isPlaying();
    }
    public void pausePlayer() {
        player.pause();
    }
    public void seek(int posn) {
        player.seekTo(posn);
    }
    public void go() {
        player.start();
    }

    // Skip to previous track
    public void playPrev() {
        songPosn--;
        if(songPosn<0) songPosn=songs.size()-1;
        playSong();
    }
    // Skip to next track
    public void playNext() {
        if(shuffle) {
            int newSong = songPosn;
            while(newSong==songPosn) {
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn>=songs.size()) songPosn=0;
        }
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    // Toggle shuffle
    public void setShuffle() {
        if(shuffle) shuffle=false;
        else shuffle=true;
    }
}
