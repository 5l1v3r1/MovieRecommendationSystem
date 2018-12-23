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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rating) {
            Rating r = (Rating) obj;
            return super.getId() == r.getId();
        }
        else if (obj instanceof Movie) {
            Movie m = (Movie) obj;
            return super.getId() == m.getMovieId();
        }
        return false;
    }
}
