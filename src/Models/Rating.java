package Models;

public class Rating {
    private int movieId;
    private float rating;
    private String timestamp;

    Rating(int movieId, float rating, String timestamp) {
        this.movieId = movieId;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public int getMovieId() {
        return movieId;
    }

    public float getRating() {
        return rating;
    }

    void setRating(float rating) {
        this.rating = rating;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rating) {
            Rating r = (Rating) obj;
            return this.movieId == r.getMovieId();
        }
        return false;
    }
}
