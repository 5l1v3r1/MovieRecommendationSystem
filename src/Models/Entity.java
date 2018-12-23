package Models;

public abstract class Entity {
    private int id;
    private float ratingDeviation;
    private float averageRating;
    private int LSHBucket;

    Entity(int id) {
        this.id = id;
        ratingDeviation = 0.0f;
        averageRating = -1;
        LSHBucket = -1;
    }

    int getId() {
        return id;
    }

    public float getRatingDeviation() {
        return ratingDeviation;
    }

    public float calculateRatingDeviation(float ratingAverage, float globalRatingAverage) {
        return this.ratingDeviation = ratingAverage - globalRatingAverage;
    }

    public float getAverageRating() {
        return averageRating;
    }

    void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public int getLSHBucket() {
        return LSHBucket;
    }

    public void setLSHBucket(int LSHBucket) {
        this.LSHBucket = LSHBucket;
    }
}
