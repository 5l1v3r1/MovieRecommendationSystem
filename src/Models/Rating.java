package Models;

public abstract class Rating {
    private int id;
    private float rating;

    Rating(int id, float rating) {
        this.id = id;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public float getRating() {
        return rating;
    }

    void setRating(float rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rating) {
            Rating r = (Rating) obj;
            return this.id == r.getId();
        }
        return false;
    }
}
