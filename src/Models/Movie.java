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
        float[] tmp = new float[1000];
/*
        new Thread(() -> {
            for (int i = 0; i < 2500; i++) {
                tmp[i] = ratings.getOrDefault(i, 0.0f);
            }
        }).start();

        new Thread(() -> {
            for (int i = 2500; i < 5000; i++) {
                tmp[i] = ratings.getOrDefault(i, 0.0f);
            }
        }).start();

        new Thread(() -> {
            for (int i = 5000; i < 7500; i++) {
                tmp[i] = ratings.getOrDefault(i, 0.0f);
            }
        }).start();

        new Thread(() -> {
            for (int i = 7500; i < 10000; i++) {
                tmp[i] = ratings.getOrDefault(i, 0.0f);
            }
        }).start();
*/
        for (int i = 0; i < 1000; i++) {
            tmp[i] = ratings.getOrDefault(i, 0.0f);
        }

        return tmp;
    }
}
