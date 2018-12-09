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

    final static String[] GENRES = {"Action", "Adventure", "Animation",
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

    static HashMap<Integer, User> userRatingsMap;
    static HashMap<Integer, HashMap<String, Float>> userGenresMap;
    static HashMap<Integer, Movie> moviesMap;
    static Map<Integer, ArrayList<Integer>> userLSH;



    public static void main(String[] args) {
        readRatingsCSV();
        readMoviesCSV();
        generateUserGenreMatrix();
        computeSimilarUsersLSH();
        System.out.println(moviesMap.size());

        System.out.println(Collections.singletonList(userLSH.get(0)));
    }

    private static void readRatingsCSV() {
        String csvFile = "/Users/boran/Desktop/CS425-Project/ml-20m/ratings.csv";
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
        String csvFile = "/Users/boran/Desktop/CS425-Project/ml-20m/movies.csv";
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

        // R^n
        int n = 3;

        int stages = GENRES.length;
        int buckets = 165;

        LSHSuperBit lsh = new LSHSuperBit(stages, buckets, n);

        for (Map.Entry<Integer, HashMap<String, Float>> user: userGenresMap.entrySet()) {
            ArrayList<Float> vector = new ArrayList<>();
            for (String genre: GENRES) {
                vector.add(user.getValue().get(genre));
            }
            int[] hash = lsh.hash(vector);
            addSimilarUsers(hash[0], user.getKey());
        }
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

    private static void movieMovieFiltering() {

    }
}
