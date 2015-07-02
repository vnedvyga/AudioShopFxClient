package ua.org.oa.nedvygav.models;


import com.google.gson.annotations.SerializedName;

public class Artist {

    @SerializedName("id")
    private long id;

    @SerializedName("genreId")
    private long genreId;

    @SerializedName("name")
    private String name;

    @SerializedName("originCountry")
    private String originCountry;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getGenreId() {
        return genreId;
    }

    public String getCoutry() {
        return originCountry;
    }

    @Override
    public String toString() {
        return name+", Country: "+originCountry;
    }
}
