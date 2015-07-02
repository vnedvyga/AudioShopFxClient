package ua.org.oa.nedvygav.models;

import com.google.gson.annotations.SerializedName;

public class Album {

    @SerializedName("id")
    private long id;

    @SerializedName("artist_id")
    private long artistId;

    @SerializedName("name")
    private String name;

    @SerializedName("year")
    private int year;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }

    public long getArtistId() {
        return artistId;
    }

    @Override
    public String toString() {
        return name+" ("+year+")";
    }


}
