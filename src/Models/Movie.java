package Models;


import java.util.ArrayList;
import java.util.Arrays;

public class Movie {
    private int movieId;
    private String title;
    private ArrayList<String> genres;

    public Movie(int movieId, String title, String genres) {
        this.movieId = movieId;
        this.title = title;
        genresStringToArrayList(genres);
    }

    private void genresStringToArrayList(String genres) {
        String [] tmp = genres.split("\\|");
        this.genres = new ArrayList<>(Arrays.asList(tmp));
    }

    public int getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }
}
