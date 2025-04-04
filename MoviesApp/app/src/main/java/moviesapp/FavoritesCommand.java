package moviesapp;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesCommand {
    private static final String FAVORITES_FILE = "favoritesCommand.txt";

    private Set<Movie> favoriteMovies;

    public FavoritesCommand() {
        this.favoriteMovies = loadFavoritesFromFile();
    }

    public void removeFavoriteMovie(Movie movie) {
        if (favoriteMovies.remove(movie)) {
            saveFavoritesToFile();
            System.out.println("Movie removed from favorites: " + movie.getTitle());
        } else {
            System.out.println("Movie not found in favorites: " + movie.getTitle());
        }
    }

    public void listFavoriteMovies() {
        if (favoriteMovies.isEmpty()) {
            System.out.println("No favorite movies.");
        } else {
            System.out.println("Favorite Movies:");
            favoriteMovies.forEach(movie -> System.out.println(movie.getTitle()));
        }
    }

    protected Set<Movie> loadFavoritesFromFile() {
        Set<Movie> favorites = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedReader reader = new BufferedReader(new FileReader(FAVORITES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Movie movie = objectMapper.readValue(line, Movie.class);
                    favorites.add(movie);
                } catch (JsonParseException e) {
                    System.out.println("Error parsing line to JSON: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return favorites;
    }

    public void addFavoriteMovie(Movie movie) {
        boolean added = favoriteMovies.add(movie);
        if (added) {
            System.out.println("Movie added to favorites: " + movie.getTitle());
            saveFavoritesToFile(); // Reflect changes immediately in the file
        } else {
            System.out.println("Movie is already in favorites: " + movie.getTitle());
        }
    }





    protected void saveFavoritesToFile() {
        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FAVORITES_FILE))) {
            for (Movie movie : favoriteMovies) {
                // Convert movie to JSON string
                String movieJson = objectMapper.writeValueAsString(movie);
                writer.write(movieJson);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFavoritesToFile(String userName, List<Movie> movies) {
        try {
            PrintWriter writer = new PrintWriter(userName + ".txt");
            for (Movie movie : movies) {
                writer.println(movie);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found.");
            e.printStackTrace();
        }
    }
}
