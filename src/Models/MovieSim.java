package Models;

public class MovieSim {

    private int movieId;
    public Double cosineSim;

    public MovieSim(int movieId, Double cosineSim) {
        this.movieId = movieId;
        this.cosineSim = cosineSim;
    }

    public Double getCosineSim() {
        return cosineSim;
    }

    public int getMovieId() {
        return movieId;
    }
}
