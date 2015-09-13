package com.ourcompany.cartunes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by DScha_000 on 8/26/2015.
 */
public class SongAdapter extends BaseAdapter {

    // Declare instance variables for song list and layout
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    // Create a constructor method to instantiate the declared variables
    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs=theSongs;
        songInf=LayoutInflater.from(c);
    }

    // Make the getCount method return the size of our song list
    @Override
    public int getCount() {
        return songs.size();
    }

    // Don't need yet
    @Override
    public Object getItem(int arg0) {
        return null;
    }

    // Don't need yet
    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map song to layout
        LinearLayout songLay = (LinearLayout)songInf.inflate(R.layout.song, parent, false);
        // get title and artist views from xml
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
        // get song using position
        Song currSong = songs.get(position);
        // get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        //set position as tag
        songLay.setTag(position);
        return songLay;
    }

}
