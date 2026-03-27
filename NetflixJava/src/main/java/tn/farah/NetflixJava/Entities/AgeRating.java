package tn.farah.NetflixJava.Entities;

public enum AgeRating {
    ALL(1), PLUS_10(2), PLUS_12(3), PLUS_16(4), PLUS_18(5);

    private final int id;

    AgeRating(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}