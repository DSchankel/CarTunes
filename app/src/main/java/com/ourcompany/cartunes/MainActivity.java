package com.ourcompany.cartunes;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ourcompany.cartunes.MusicService.MusicBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// GitHubTest!!!
public class MainActivity extends Activity {

    // Declare the song list variables to store and show songs respectively
    private ArrayList<Song> songList;
    private ListView songView;

    // Service
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

    // Activity and playback pause flags
    private boolean paused=false, playbackPaused=false;

    private TextView nowPlayingTextView;
    private TextView songPosnText;
    private TextView songDurText;
    private Button pauseButton;
    private Button vetoButton;
    public SeekBar songProgBar;
    private int songDur;
    private int songPlace;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Access the list view that will show the songs
        songView = (ListView)findViewById(R.id.song_list);
        // Create an instance of the list
        songList = new ArrayList<Song>();
        // Get songs from the device using a helper method created below
        getSongList();
        // Sort the song list alphabetically by artist
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getArtist().compareTo(b.getArtist());
            }
        });
        // Create an instance of the SongAdapter
        SongAdapter songAdt = new SongAdapter(this, songList);
        // then set the adapter on the ListView
        songView.setAdapter(songAdt);
        songProgBar = (SeekBar)findViewById(R.id.song_prog_bar);
        songPosnText = (TextView)findViewById(R.id.song_position);
        songDurText = (TextView)findViewById(R.id.song_duration);
        songProgBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    musicSrv.seek(progress);
                    updateTime();
                }
            }
        });
    }

    // Connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            // Get service
            musicSrv = binder.getService();
            // Pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    // Start and bind the service when the activity starts
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    // User song select
    public void songPicked(View view) {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused) {
            playbackPaused=false;
        }
        songProgBar.postDelayed(everySecond, 1000);
    }

    private Runnable everySecond = new Runnable() {
        @Override
        public void run() {
            if(songProgBar != null){
                songProgBar.setProgress(musicSrv.getPosn());
            }

            if(musicSrv.isPng()) {
                songProgBar.postDelayed(everySecond, 1000);
                updateTime();
            }
        }
    };

    private void updateTime() {
        songDur = musicSrv.getDur();
        songProgBar.setMax(songDur);
        do {
            songPlace = musicSrv.getPosn();
            int dSec = (int) (songDur/1000) % 60;
            int dMin = (int) ((songDur/(1000*60)) % 60);
            int dHour = (int) ((songDur/(1000*60*60)) % 24);

            int pSec = (int) (songPlace/1000) % 60;
            int pMin = (int) ((songPlace/(1000*60)) % 60);
            int pHour = (int) ((songPlace/(1000*60*60)) % 24);

            if(dHour==0){
                songPosnText.setText(String.format("%02d:%02d",pMin,pSec));
                songDurText.setText(String.format("%02d:%02d",dMin,dSec));
            }else{
                songPosnText.setText(String.format("%02d:%02d:%02d",pHour,pMin,pSec));
                songDurText.setText(String.format("%02d:%02d:%02d",dHour,dMin,dSec));
            }
            try{
                Log.d("Value: ",String.valueOf((int) (songPlace*100/songDur)));
                if(songProgBar.getProgress() <=100) {
                    break;
                }
            }catch (Exception e) {}
        }while (songProgBar.getProgress() <=100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Menu item selected
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Create a helper method to retrieve audio file information from the device
    public void getSongList() {
        // Query external audio by...
        // creating a content resolver instance
        ContentResolver musicResolver = getContentResolver();
        // retrieving the URI for external music files
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // and creating a cursor instance using the now instantiated content resolver
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        // now iterate over the results for as long as we have valid data
        if(musicCursor!=null && musicCursor.moveToFirst()){
            // retrieve the column indexes for the data items we want for each query result
            // and place the indexes in these variables
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            // now add the information to our song list by...
            do {
                // passing the retrieved indexes to the get methods
                // thereby placing the desired data into the variables below
                long thisID = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                // and finally creating song objects and adding them to the list
                songList.add(new Song(thisID, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }


    public void start() {
        musicSrv.go();
    }

    public void pause(View view) {
        if (playbackPaused==false) {
            playbackPaused=true;
            musicSrv.pausePlayer();
            pauseButton = (Button) findViewById(R.id.pause_button);
            pauseButton.setText("Play");
        }
        else if (playbackPaused==true) {
            playbackPaused=false;
            musicSrv.go();
            pauseButton = (Button) findViewById(R.id.pause_button);
            pauseButton.setText("Pause");
        }
        songProgBar.postDelayed(everySecond, 1000);
    }
    // These next two have if statements to prevent throwing exceptions when app is enhanced

    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }
    // Not sure if this should be "else return false"

    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    // Play next track
    public void nextSong(View view) {
        musicSrv.playNext();
        songDur = musicSrv.getDur();
        songProgBar.setMax(songDur);
        songProgBar.postDelayed(everySecond, 1000);
        // Since weird stuff will happen if the user interacts with controls while paused...
        if(playbackPaused) {
            playbackPaused=false;
        }
    }

    public void endActivity(View view) {
        stopService(playIntent);
        musicSrv=null;
        System.exit(0);
    }


    @Override
    protected void onPause() {
        super.onPause();
        paused=true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(paused) {
            paused=false;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }
}
