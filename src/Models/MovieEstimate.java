package Models;

public class MovieEstimate {
    public int movieId;
    public double estimateRating;

    public MovieEstimate(int movieId, double estimateRating) {
        this.movieId = movieId;
        this.estimateRating = estimateRating;
    }

    @Override
    public String toString() {
        return movieId + " estimate rating " + estimateRating;
    }
}
