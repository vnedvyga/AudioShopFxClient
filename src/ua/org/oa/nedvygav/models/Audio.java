package ua.org.oa.nedvygav.models;


import com.google.gson.annotations.SerializedName;

public class Audio {

    @SerializedName("id")
    private long id;

    @SerializedName("albumId")
    private long albumId;

    @SerializedName("artistId")
    private long artistId;

    @SerializedName("name")
    private String name;

    @SerializedName("duration")
    private String duration;

    @SerializedName("price")
    private String price;

    public long getId() {
        return id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public long getArtistId() {
        return artistId;
    }

    public String getName() {
        return name;
    }

    public String getDuration() {
        return duration;
    }

    public String getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name+" ("+duration+") Price: "+price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Audio audio = (Audio) o;

        return id == audio.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
