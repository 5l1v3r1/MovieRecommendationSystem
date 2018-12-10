package com.cs425;

import LSH.LSHSuperBit;
import Models.Movie;
import Models.Rating;
import Models.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    private final static String[] GENRES = {"Action", "Adventure", "Animation",
                                    "Children's", "Comedy", "Crime",
                                    "Documentary", "Drama",
                                    "Fantasy", "Film-Noir",
                                    "Horror",
                                    "Musical", "Mystery",
                                    "Romance",
                                    "Sci-Fi",
                                    "Thriller",
                                    "War", "Western",
                                    "(no genres listed)"};

    private static HashMap<Integer, User> userRatingsMap;
    private static HashMap<Integer, HashMap<String, Float>> userGenresMap;
    private static HashMap<Integer, Movie> moviesMap;
    private static HashMap<Integer, ArrayList<Integer>> userLSH;
    private static HashMap<Integer, ArrayList<Rating>> userSimilarMoviesMap;

    private static LSHSuperBit lsh = new LSHSuperBit(GENRES.length, 372, 3);

    public static void main(String[] args) {
        readRatingsCSV();
        readMoviesCSV();
        generateUserGenreMatrix();
        computeSimilarUsersLSH();
        System.out.println(userRatingsMap.size());

        //getRecommendedMoviesFromSimilarUsers();
        computeRecommendedMoviesFromSimilarUsers(1);
        getRecommendedMovies(1);
    }

    private static void readRatingsCSV() {
        String csvFile = "/Users/boranyildirim/Downloads/ml-20m/ratings.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine();

            userRatingsMap = new HashMap<>();

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] rating = line.split(cvsSplitBy);

                User tmp = new User(Integer.parseInt(rating[0]));
                if (userRatingsMap.containsKey(tmp.getUserId())) {
                    userRatingsMap.get(tmp.getUserId()).addRating(Integer.parseInt(rating[1]), Float.parseFloat(rating[2]), rating[3]);
                }
                else {
                    tmp.addRating(Integer.parseInt(rating[1]), Float.parseFloat(rating[2]), rating[3]);
                    userRatingsMap.put(tmp.getUserId(), tmp);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        normalizeRatings();
    }

    private static void normalizeRatings() {
        for (Map.Entry<Integer, User> entry: userRatingsMap.entrySet()) {
            entry.getValue().normalizeRatings();
        }
    }

    private static void readMoviesCSV() {
        String csvFile = "/Users/boranyildirim/Downloads/ml-20m/movies.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine();

            moviesMap = new HashMap<>();

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] movie = line.split(cvsSplitBy);

                Movie tmp = new Movie(Integer.parseInt(movie[0]), movie[1], movie[2]);
                moviesMap.put(tmp.getMovieId(), tmp);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateUserGenreMatrix() {
        userGenresMap = new HashMap<>();
        for (Map.Entry<Integer, User> entry: userRatingsMap.entrySet()) {
            for (String genre: GENRES) {
                computeUserGenreValue(entry.getKey(), genre);
            }
        }
    }

    private static void computeUserGenreValue(int userId, String genre) {
        User user = userRatingsMap.get(userId);

        float total = 0.0f;
        int count = 0;

        for (Rating rating: user.getRatings()) {
            if (moviesMap.get(rating.getMovieId()).getGenres().contains(genre)) {
                total += rating.getRating();
                count ++;
            }
        }

        if (userGenresMap.containsKey(userId))
            userGenresMap.get(userId).put(genre, count == 0 ? 0.0f : total / count);
        else {
            HashMap<String, Float> tmp = new HashMap<>();
            tmp.put(genre, count == 0 ? 0.0f : total / count);

            userGenresMap.put(userId, tmp);
        }
    }

    private static void computeSimilarUsersLSH() {
        userLSH = new HashMap<>();

        for (Map.Entry<Integer, HashMap<String, Float>> user: userGenresMap.entrySet()) {
            ArrayList<Float> vector = mapToList(user.getValue());
            int[] hash = lsh.hash(vector);
            addSimilarUsers(hash[0], user.getKey());
        }
    }

    private static ArrayList<Float> mapToList(HashMap<String, Float> map) {
        ArrayList<Float> vector = new ArrayList<>();
        for (String genre: GENRES) {
            vector.add(map.get(genre));
        }
        return vector;
    }

    private static void addSimilarUsers(Integer hashValue, Integer userId) {
        if (userLSH.containsKey(hashValue)) {
            if (!userLSH.get(hashValue).contains(userId)) {
                userLSH.get(hashValue).add(userId);
            }
        }
        else {
            ArrayList<Integer> tmp = new ArrayList<>();
            tmp.add(userId);
            userLSH.put(hashValue, tmp);
        }
    }

    private static int getHashedBucketOfUser (int userId) {
        int hash[] = lsh.hash(mapToList(userGenresMap.get(userId)));
        return hash[0];
    }

    private static void getRecommendedMoviesFromSimilarUsers() {
        for (Integer userId: userGenresMap.keySet()) {
            computeRecommendedMoviesFromSimilarUsers(userId);
        }
    }

    private static void computeRecommendedMoviesFromSimilarUsers (int userId) {
        System.out.println("computeRecommendedMoviesFromSimilarUsers");
        int bucket = getHashedBucketOfUser(userId);
        ArrayList<Integer> similarUsers = userLSH.get(bucket);
        userSimilarMoviesMap = new HashMap<>();

        for (Integer uid : similarUsers) {
            if (uid != userId) {
                ArrayList<Rating> ratings = userRatingsMap.get(uid).getRatings();

                for (Rating r : ratings) {
                    if (r.getRating() >= 0.75) {
                        addRecommendedMovies(userId, r);
                    }
                }
            }
        }
        System.out.println("end computeRecommendedMoviesFromSimilarUsers");
    }

    private static void addRecommendedMovies(Integer userId, Rating r) {
        if (userSimilarMoviesMap.containsKey(userId)) {
            if (!userSimilarMoviesMap.get(userId).contains(r)) {
                userSimilarMoviesMap.get(userId).add(r);
            }
        }
        else {
            ArrayList<Rating> tmp = new ArrayList<>();
            tmp.add(r);
            userSimilarMoviesMap.put(userId, tmp);
        }
    }

    private static ArrayList<Movie> getRecommendedMovies(int userId) {
        System.out.println("getRecommendedMovies");
        ArrayList<Movie> movies = new ArrayList<>();
        ArrayList<Rating> userRatings = userSimilarMoviesMap.get(userId);
        for (Rating r : userRatings) {
            movies.add(moviesMap.get(r.getMovieId()));
        }

        for (Movie m: movies) {
            System.out.println(m.getMovieId() + ", " + m.getTitle());
        }
        return movies;
    }

    private static void movieMovieFiltering() {

    }
}
