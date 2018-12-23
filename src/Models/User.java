package Models;


import java.util.ArrayList;

public class User {
    private int userId;
    private ArrayList<Rating> ratings;
    private int LSHBucket;

    public User(int userId) {
        this.userId = userId;
        this.ratings = new ArrayList<>();
        LSHBucket = -1;
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

    public ArrayList<Rating> getRatings(float threshold) {
        ArrayList<Rating> tmp = new ArrayList<>();
        for (Rating rating: ratings) {
            if (rating.getRating() > threshold) {
                tmp.add(rating);
            }
        }
        return tmp;
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

    public int getLSHBucket() {
        return LSHBucket;
    }

    public void setLSHBucket(int LSHBucket) {
        this.LSHBucket = LSHBucket;
    }
}