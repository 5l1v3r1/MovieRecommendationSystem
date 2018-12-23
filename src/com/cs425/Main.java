package com.cs425;

import LSH.LSHSuperBit;
import Models.Movie;
import Models.MovieRating;
import Models.Rating;
import Models.User;

import java.io.*;
import java.util.*;

public class Main {

    // +1 for id convenience
    public final static int USER_NUMBER = 138494;

    private final static String[] GENRES =
            {"Action", "Adventure", "Animation",
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

    // user[0] is empty for id convenience
    private static User[] userRatingsMap = new User[USER_NUMBER];

    private static float[][] userGenresMap = new float[USER_NUMBER][GENRES.length];

    // since movies are blank ids it is better to store them in a hashmap
    private static HashMap<Integer, Movie> moviesMap = new HashMap<>();

    private static HashMap<Integer, ArrayList<Integer>> userLSH = new HashMap<>();
    private static ArrayList<ArrayList<MovieRating>> userSimilarMoviesMap = new ArrayList<>();

    private static HashMap<Integer, ArrayList<Integer>> moviesLSH = new HashMap<>();

    private static LSHSuperBit userUserLSH = new LSHSuperBit(31, 1488, GENRES.length);

    private static LSHSuperBit movieMovieLSH = new LSHSuperBit(10, 100, USER_NUMBER);

    private static float globalMovieRatingsAverage = 0.0f;

    public static void main(String[] args) {
        //fillUserSimilarMoviesMap();
        readMoviesCSV();
        readRatingsCSV();
        //generateUserGenreMatrix();
        //computeSimilarUsersLSH();

        System.out.println(globalMovieRatingsAverage);
        System.out.println(moviesMap.get(1).getAverageRating());
        System.out.println(userRatingsMap[6].getAverageRating());

        System.out.println(moviesMap.get(1).calculateRatingDeviation(moviesMap.get(1).getAverageRating(), globalMovieRatingsAverage));
        System.out.println(moviesMap.get(1).getRatingDeviation());

        System.out.println(userRatingsMap[6].calculateRatingDeviation(userRatingsMap[6].getAverageRating(), globalMovieRatingsAverage));
        System.out.println(userRatingsMap[6].getRatingDeviation());

        movieMovieFiltering();
        //System.out.println(Arrays.toString(moviesMap.get(1).getRatingsVector()));
        //getRecommendedMoviesFromSimilarUsers();
        //computeRecommendedMoviesFromSimilarUsers(6);
        //getRecommendedMovies(6);
    }

    private static void readRatingsCSV() {
        String csvFile = "/Users/boran/Desktop/CS425-Project/ml-20m/ratings.csv";
        String line = "";
        String cvsSplitBy = ",";

        // to calculate global movie rating average
        float totalRating = 0.0f;
        int ratingCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine();

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] rating = line.split(cvsSplitBy);

                User tmp = new User(Integer.parseInt(rating[0]));
                int userId = tmp.getUserId();

                int movieId = Integer.parseInt(rating[1]);
                float movieRating = Float.parseFloat(rating[2]);
                String timestamp = rating[3];

                if (userRatingsMap[userId] != null) {
                    userRatingsMap[userId].addRating(movieId, movieRating, timestamp);
                }
                else {
                    tmp.addRating(movieId, movieRating, timestamp);
                    userRatingsMap[userId] = tmp;
                }

                // add current rating to the movie object to calculate average ratings
                moviesMap.get(movieId).addRating(userId, movieRating);

                totalRating += Float.parseFloat(rating[2]);
                ratingCount++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        globalMovieRatingsAverage = totalRating / ratingCount;

        normalizeRatings();
    }

    private static void normalizeRatings() {
        for (int i = 1; i < userRatingsMap.length; i++) {
            userRatingsMap[i].normalizeRatings();
        }
    }

    private static void readMoviesCSV() {
        String csvFile = "/Users/boran/Desktop/CS425-Project/ml-20m/movies.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine();

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
        for (int i = 1; i < userRatingsMap.length; i++) {
            for (int j = 0; j < GENRES.length; j++){
                computeUserGenreValue(userRatingsMap[i].getUserId(), GENRES[j], j);
            }
        }
    }

    private static void computeUserGenreValue(int userId, String genre, int genreIndex) {
        User user = userRatingsMap[userId];

        float total = 0.0f;
        int count = 0;

        for (Rating rating: user.getRatings()) {
            if (moviesMap.get(rating.getId()).getGenres().contains(genre)) {
                total += rating.getRating();
                count ++;
            }
        }

        userGenresMap[userId][genreIndex] = count == 0 ? 0.0f : total / count;
    }

    private static void computeSimilarUsersLSH() {
        for (int i = 1; i < userGenresMap.length; i++) {
            int[] hash = userUserLSH.hash(userGenresMap[i]);
            int bucket = hash[hash.length - 1];
            addSimilarUsers(bucket, i);
            userRatingsMap[i].setLSHBucket(bucket);
        }
    }

    private static void addSimilarUsers(Integer hashValue, Integer userId) {
        addSimilarEntities(hashValue, userId, userLSH);
    }

    private static int getHashedBucketOfUser (int userId) {
        int[] hash = userUserLSH.hash(userGenresMap[userId]);
        return hash[0];
    }

    private static void fillUserSimilarMoviesMap() {
        new Thread(() -> {
            userSimilarMoviesMap.add(0, null);
            for (int i = 1; i < USER_NUMBER; i++)
                userSimilarMoviesMap.add(i, new ArrayList<>());
        }).start();
    }

    private static void endTimer(long start) {
        long elapsedTimeMillis = System.currentTimeMillis()-start;
        float elapsedTimeSec = elapsedTimeMillis/1000F;

        System.out.println(elapsedTimeMillis);
        System.out.println(elapsedTimeSec);
    }

    private static void getRecommendedMoviesFromSimilarUsers() {
        for (int i = 1; i < USER_NUMBER; i++) {
            computeRecommendedMoviesFromSimilarUsers(i);
            System.out.println(i);
        }
    }

    private static void computeRecommendedMoviesFromSimilarUsers (int userId) {
        long start = System.currentTimeMillis();

        ArrayList<Integer> similarUsers = userLSH.get(userRatingsMap[userId].getLSHBucket());
        for (Integer uid : similarUsers) {
            if (uid != userId) {
                addRecommendedMovies(userId, userRatingsMap[uid].getRatings(0.75f));
            }
        }
        endTimer(start);
    }

    private static void addRecommendedMovies(Integer userId, ArrayList<MovieRating> ratings) {
        for (MovieRating r : ratings) {
            if (!userSimilarMoviesMap.get(userId).contains(r)) {
                userSimilarMoviesMap.get(userId).add(r);
            }
        }
    }

    private static ArrayList<Movie> getRecommendedMovies(int userId) {
        System.out.println("getRecommendedMovies");
        ArrayList<Movie> movies = new ArrayList<>();
        ArrayList<MovieRating> userRatings = userSimilarMoviesMap.get(userId);
        for (Rating r : userRatings) {
            if (!userRatingsMap[userId].getRatings().contains(r))
                movies.add(moviesMap.get(r.getId()));
        }

        System.out.println(movies.size());
        for (Movie m: movies) {
            System.out.println(m.getMovieId() + ", " + m.getTitle());
        }
        return movies;
    }

    private static void movieMovieFiltering () {
        long start = System.currentTimeMillis();
        computeSimilarMoviesLSH();
        endTimer(start);
        System.out.println(Collections.singletonList(moviesLSH.get(1)));
    }

    private static void computeSimilarMoviesLSH () {
        for (HashMap.Entry<Integer, Movie> movie: moviesMap.entrySet()) {
            int[] hash = movieMovieLSH.hash(movie.getValue().getRatingsVector());
            int bucket = hash[hash.length - 1];
            addSimilarMovies(bucket, movie.getKey());
            moviesMap.get(movie.getKey()).setLSHBucket(bucket);
        }
    }

    private static void addSimilarMovies(int bucket, int movieId) {
        addSimilarEntities(bucket, movieId, moviesLSH);
    }

    private static void addSimilarEntities(int bucket, int id, HashMap<Integer, ArrayList<Integer>> map) {
        if (map.containsKey(bucket)) {
            if (!map.get(bucket).contains(id)) {
                map.get(bucket).add(id);
            }
        }
        else {
            ArrayList<Integer> tmp = new ArrayList<>();
            tmp.add(id);
            map.put(bucket, tmp);
        }
    }
}
