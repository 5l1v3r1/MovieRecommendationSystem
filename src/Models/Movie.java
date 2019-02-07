package Models;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.cs425.Main.USER_NUMBER;

public class Movie extends Entity{
    private String title;
    private ArrayList<String> genres;

    private HashMap<Integer, Float> ratings;

    private float totalRatings;
    private int numberOfRatings;

    public Movie(int movieId, String title, String genres) {
        super(movieId);
        this.title = title;
        genresStringToArrayList(genres);
        ratings = new HashMap<>();
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

    public void addRating(int uid, float rating) {
        ratings.put(uid, rating);
        totalRatings += rating;
        numberOfRatings++;
    }

    public float getAverageRating() {
        if (super.getAverageRating() == -1)
            super.setAverageRating(totalRatings / numberOfRatings);
        return super.getAverageRating();
    }

    public float[] getRatingsVector() {
        float[] tmp = new float[2000];

        for (int i = 0; i < 2000; i++) {
            tmp[i] = ratings.getOrDefault(i, 0.0f);
        }

        return tmp;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id= " + super.getId() +
                " title= '" + title + '\'' +
                ", genres= " + genres +
                '}';
    }
}
