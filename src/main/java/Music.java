/**
 * Created by Rui Lebre (ruilebre@ua.pt) on 8/23/17.
 */
public class Music {
    private String trackTitle;
    private String artist;
    private String album;
    private String dateAdded;

    public Music(String trackTitle, String artist, String album, String dateAdded) {
        this.trackTitle = trackTitle;
        this.artist = artist;
        this.album = album;
        this.dateAdded = dateAdded;
    }

    public String getTrackTitle() {
        return trackTitle;
    }

    public void setTrackTitle(String trackTitle) {
        this.trackTitle = trackTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getYoutubeQuery() {
        return trackTitle + " " + artist;
    }
}
