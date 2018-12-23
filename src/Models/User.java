package Models;


import java.util.ArrayList;

public class User extends Entity {
    private ArrayList<MovieRating> ratings;

    public User(int userId) {
        super(userId);
        this.ratings = new ArrayList<>();
    }

    public void addRating(int movieId, float rating, String timestamp) {
        MovieRating tmp = new MovieRating(movieId, rating, timestamp);
        ratings.add(tmp);
    }

    public int getUserId() {
        return super.getId();
    }

    public ArrayList<MovieRating> getRatings() {
        return ratings;
    }

    public ArrayList<MovieRating> getRatings(float threshold) {
        ArrayList<MovieRating> tmp = new ArrayList<>();
        for (MovieRating rating: ratings) {
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
        super.setAverageRating(averageRating);

        for (Rating rating : ratings) {
            rating.setRating(rating.getRating() - averageRating);
        }
    }
}