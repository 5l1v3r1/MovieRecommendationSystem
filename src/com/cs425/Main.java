package com.cs425;

import LSH.LSHSuperBit;
import LSH.SuperBit;
import Models.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    // +1 for id convenience
    public final static int USER_NUMBER = 138494;

    private final static int CF = 2;

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
    private static ConcurrentHashMap<Integer, Movie> moviesMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<Integer, ArrayList<Integer>> userLSH = new ConcurrentHashMap<>();
    private static ArrayList<ArrayList<MovieRating>> userSimilarMoviesMap = new ArrayList<>();

    private static ConcurrentHashMap<Integer, ArrayList<Integer>> moviesLSH = new ConcurrentHashMap<>();

    private static LSHSuperBit userUserLSH = new LSHSuperBit(31, 1488, GENRES.length);

    private static LSHSuperBit movieMovieLSH = new LSHSuperBit(30, 100, 1000);
    static SuperBit sb;

    private static float globalMovieRatingsAverage = 0.0f;


    public static void main(String[] args) {
        fillUserSimilarMoviesMap();
        Thread thread1 = new Thread(Main::readMoviesCSV);

        thread1.start();

        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread thread2 = new Thread(() -> {
            readRatingsCSV();
            generateUserGenreMatrix();
            computeSimilarUsersLSH();
        });

        Thread thread3 = new Thread(Main::movieMovieFiltering);

        thread2.start();
        thread3.start();

        try {
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sb = movieMovieLSH.getSb();

        System.out.println("select case: user-user(1), item-item(2)");
        Scanner scanner = new Scanner(System.in);
        int menu = scanner.nextInt();
        int choice;

        while (menu != -1) {
            switch (menu) {
                case 1:
                    System.out.println("enter userId");
                    choice = scanner.nextInt();
                    computeRecommendedMoviesFromSimilarUsers(choice);
                    System.out.println(Collections.singletonList(getRecommendedMovies(choice)));
                    System.out.println("select case: user-user(1), item-item(2)");
                    menu = scanner.nextInt();
                    break;
                case 2:
                    System.out.println("enter userId");
                    choice = scanner.nextInt();
                    computeBaselineRatings(choice);
                    System.out.println("select case: user-user(1), item-item(2)");
                    menu = scanner.nextInt();
                    break;
            }
        }
    }

    private static void readRatingsCSV() {
        String csvFile = "/Users/boranyildirim/Downloads/ml-20m/ratings.csv";
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
        String csvFile = "/Users/boranyildirim/Downloads/ml-20m/movies.csv";
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
        for (MovieRating r : userRatings) {
            if (!userRatingsMap[userId].getRatings().contains(r))
                movies.add(moviesMap.get(r.getId()));
        }

        return movies;
    }

    private static void movieMovieFiltering () {
        long start = System.currentTimeMillis();
        computeSimilarMoviesLSH();
        endTimer(start);
        System.out.println(Collections.singletonList(moviesLSH.get(1)));
    }

    private static void computeRatingDeviations() {
        for (int i = 1; i < USER_NUMBER; i++) {
            userRatingsMap[i].calculateRatingDeviation(userRatingsMap[i].getAverageRating(), globalMovieRatingsAverage);
        }
        for (HashMap.Entry<Integer, Movie> movie: moviesMap.entrySet()) {
            movie.getValue().calculateRatingDeviation(movie.getValue().getAverageRating(), globalMovieRatingsAverage);
        }
    }

    private static void computeBaselineRatings() {
        for (int i = 1; i < USER_NUMBER; i++) {
            computeBaselineRatings(i);
        }
    }

    private static void computeBaselineRatings(int i) {
        computeRatingDeviations();
        int deletedBucket = deleteSparseBucket();

        User user = userRatingsMap[i];
        ArrayList<MovieRating> ratings = user.getRatings();

        ArrayList<MovieSim> similarList;
        ArrayList<MovieEstimate> estimates = new ArrayList<>();

        for (HashMap.Entry<Integer, Movie> movie: moviesMap.entrySet()) {
            MovieRating tmp = new MovieRating(movie.getValue().getMovieId());
            if (!ratings.contains(tmp)) {
                int hash[] = movieMovieLSH.hash(movie.getValue().getRatingsVector());
                int bucket = hash[hash.length - 1];
                if (bucket != deletedBucket) {
                    similarList = new ArrayList<>();
                    ArrayList<Integer> list = moviesLSH.get(bucket);

                    boolean[] v1 = sb.signature(movie.getValue().getRatingsVector());
                    for (Integer movieId : list) {
                        tmp = new MovieRating(movieId);
                        if (ratings.contains(tmp)) {
                            boolean[] v2 = sb.signature(moviesMap.get(movieId).getRatingsVector());
                            double cosineSim = sb.similarity(v1, v2);

                            MovieSim movieSim = new MovieSim(tmp.getId(), cosineSim);
                            similarList.add(movieSim);
/*
                            if (cosineSim > 0.4 && cosineSim < 1.0) {
                                System.out.println("UnwatchedMovie: " + movie.getValue().getTitle() + " is similar to " +
                                        moviesMap.get(movieId).getTitle() + " with cosine similarity " + cosineSim);
                                float bxi = (globalMovieRatingsAverage + user.getRatingDeviation() +
                                        movie.getValue().getRatingDeviation());
                                System.out.println("Baseline estimate for user " + user.getUserId() + " movie " +
                                        movie.getValue().getTitle() + " is " + bxi);
                                System.out.println("Baseline rating plus the weighted avg of deviations is " +
                                        bxi + ((cosineSim * movie.getValue().getRatingDeviation()) +
                                        (cosineSim * moviesMap.get(movieId).getRatingDeviation())) / (cosineSim * 2));
                                System.out.println();
                            }*/
                        }
                    }
                    if (similarList.size() > CF) {
                        similarList.sort(Comparator.comparing(m -> m.cosineSim));
                        double numerator = 0;
                        double denominator = 0;
                        for (int j = 1; j < CF; j++) {
                            MovieSim current = similarList.get(similarList.size() - j);
                            float bxk = (globalMovieRatingsAverage + user.getRatingDeviation() +
                                    moviesMap.get(current.getMovieId()).getRatingDeviation());

                            numerator += current.cosineSim * bxk;
                            denominator += current.cosineSim;
                        }

                        estimates.add(new MovieEstimate(movie.getKey(), numerator / denominator));
                    }
                }
            }
        }

        estimates.sort(Comparator.comparing(e -> e.estimateRating));

        for (int j = 1; j <= estimates.size(); j++) {
            System.out.println(estimates.get(j - 1));
            //System.out.println(moviesMap.get(estimates.get(estimates.size() - j).movieId));
        }
    }

    static int deleteSparseBucket() {
        int maxSize = Integer.MIN_VALUE;
        int maxBucket = -1;
        for (HashMap.Entry<Integer, ArrayList<Integer>> bucket: moviesLSH.entrySet()) {
            if (bucket.getValue().size() > maxSize) {
                maxSize = bucket.getValue().size();
                maxBucket = bucket.getKey();
            }
        }
        moviesLSH.remove(maxBucket);
        return maxBucket;
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

    private static void addSimilarEntities(int bucket, int id, ConcurrentHashMap<Integer, ArrayList<Integer>> map) {
        new Thread(() -> {
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
        }).start();
    }
}
