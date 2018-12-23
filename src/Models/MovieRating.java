package Models;

public class MovieRating extends Rating {
    private String timestamp;

    MovieRating(int movieId, float rating, String timestamp) {
        super(movieId, rating);
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
