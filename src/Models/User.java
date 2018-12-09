package Models;


import java.util.ArrayList;

public class User {
    private int userId;
    private ArrayList<Rating> ratings;

    public User(int userId) {
        this.userId = userId;
        this.ratings = new ArrayList<>();
    }

    public void addRating(int movieId, float rating, String timestamp) {
        Rating tmp = new Rating(movieId, rating, timestamp);
        ratings.add(tmp);
    }

    public int getUserId() {
        return userId;
    }

    public ArrayList<Rating> getRatings() {
        return ratings;
    }

    public void normalizeRatings() {
        float total = 0.0f;
        int count = 0;
        for (Rating rating: ratings) {
            total += rating.getRating();
            count++;
        }

        float averageRating = total / count;

        for (Rating rating : ratings) {
            rating.setRating(rating.getRating() - averageRating);
        }
    }
}