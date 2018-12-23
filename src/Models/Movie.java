package Models;


import java.util.ArrayList;
import java.util.Arrays;

public class Movie extends Entity{
    private String title;
    private ArrayList<String> genres;

    private float totalRatings;
    private int numberOfRatings;

    public Movie(int movieId, String title, String genres) {
        super(movieId);
        this.title = title;
        genresStringToArrayList(genres);
        totalRatings = 0.0f;
        numberOfRatings = 0;
    }

    private void genresStringToArrayList(String genres) {
        String [] tmp = genres.split("\\|");
        this.genres = new ArrayList<>(Arrays.asList(tmp));
    }

    public int getMovieId() {
        return super.getId();
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public void addRating(float rating) {
        totalRatings += rating;
        numberOfRatings++;
    }

    public float getAverageRating() {
        return totalRatings / numberOfRatings;
    }
}
