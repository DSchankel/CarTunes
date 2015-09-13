package com.ourcompany.cartunes;

/**
 * Created by DScha_000 on 8/26/2015.
 */
public class Song {
    // Instance Variables: each song instance will contain these 3 items of information
    // additional variables can be added or more information needs to be retrieved
    private long id;
    private String title;
    private String artist;

    // Constructor Method: used to construct each song instance
    public Song(long songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    // get Methods: so that we can later retrieve the information from each song instance
    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}

}
