package Models;

public abstract class Entity {
    private int id;
    private float ratingDeviation;

    Entity(int id) {
        this.id = id;
        ratingDeviation = 0.0f;
    }

    int getId() {
        return id;
    }

    public float getRatingDeviation() {
        return ratingDeviation;
    }

    public void calculateRatingDeviation(float globalRatingAverage) {
        this.ratingDeviation -= globalRatingAverage;
    }
}
