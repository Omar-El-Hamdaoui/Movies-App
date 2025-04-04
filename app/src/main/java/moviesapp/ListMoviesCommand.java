package moviesapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ListMoviesCommand implements Runnable {
    private String title;

    private String partialTitle;

    private Double voteAverage;

    private Double minVoteAverage;

    private Double maxVoteAverage;

    public List<Integer> genreIds;

    private String releaseDate;
    private String releaseDateAfter;
    private String releaseDateBefore;
    private String outputFile;
    private String allDetails;
    public Set<Movie> favoriteMovies = new HashSet<>();
    private FavoritesCommand favoritesCommand;

    public ListMoviesCommand() {
        this.favoritesCommand = new FavoritesCommand();
    }

    public static void main(String[] args) {
        // Initialize your CLI application components
        ListMoviesCommand appCLI = new ListMoviesCommand();
        // Proceed with the application logic
        appCLI.run();
    }


    @Override
    public void run() {
        List<Movie> allTheMovies = getAllTheMovies();
        List<Movie> filteredMovies = filterMovies(allTheMovies);

        // Print the filtered movies or save to a file
        if (outputFile != null) {
            saveResultsToFile(filteredMovies, outputFile);
        } else if (allDetails != null) {
            printResultsToConsole(filteredMovies);
        }

        // Start interactive search if not saving to file
        if (outputFile == null) {
            interactiveSearch(filteredMovies);
        }
    }

    public void addFavoriteMovie(Movie movie) {
        favoriteMovies.add(movie);
        System.out.println("Movie added to favorites: " + movie.getTitle());
    }

    public void printResults(List<Movie> movies) {
        if ("full".equals(allDetails)) {
            printResultsToConsole(movies);
        } else {
            printResultsTitles(movies);
        }
    }

    public static List<Movie> getAllTheMovies() {
        List<Movie> allTheMovies = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new File("src/text.json"));
            JsonNode resultsNode = jsonNode.get("results");

            for (JsonNode result : resultsNode) {
                Movie movie = objectMapper.treeToValue(result, Movie.class);
                allTheMovies.add(movie);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allTheMovies;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public List<Movie> filterMovies(List<Movie> movies) {
        return movies.stream()
                .filter(movie -> (title == null || movie.getTitle().equalsIgnoreCase(title))
                        && (partialTitle == null || movie.getTitle().toLowerCase().contains(partialTitle.toLowerCase()))
                        && (voteAverage == null || (movie.getVoteAverage() >= voteAverage - 0.1 && movie.getVoteAverage() <= voteAverage + 0.1))
                        && (minVoteAverage == null || movie.getVoteAverage() >= minVoteAverage)
                        && (maxVoteAverage == null || movie.getVoteAverage() <= maxVoteAverage)
                        && (genreIds == null || movie.getGenreIds() != null && movie.getGenreIds().length > 0 && movieContainsAnyGenre(movie, genreIds))
                        && (releaseDate == null || movie.getReleaseDate().contains(releaseDate))
                        && (releaseDateAfter == null || movie.getReleaseDate().compareTo(releaseDateAfter) > 0)
                        && (releaseDateBefore == null || movie.getReleaseDate().compareTo(releaseDateBefore) < 0))
                .collect(Collectors.toList());
    }
    private void saveResultsToFile(List<Movie> movies, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            for (Movie movie : movies) {
                writer.write(movie.toString());
                writer.write(System.lineSeparator());
            }
            System.out.println("Results saved to: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printResultsToConsole(List<Movie> movies) {
        for (Movie movie : movies) {
            System.out.println(movie);
        }
    }
    private void printResultsTitles(List<Movie> movies){
        for(Movie movie :movies){
            System.out.println(movie.getTitle());
        }
    }

    private void applyCriteria(String criteria, Scanner scanner) {
        switch (criteria) {
            case "title":
                System.out.println("Enter title:");
                title = scanner.nextLine().trim();
                break;
            case "partialTitle":
                System.out.println("Enter partial title:");
                partialTitle = scanner.nextLine().trim();
                break;
            case "voteAverage":
                System.out.println("Enter vote average:");
                voteAverage = Double.valueOf(scanner.nextLine().trim());
                break;
            case "minVoteAverage":
                System.out.println("Enter minimum vote average:");
                minVoteAverage = Double.valueOf(scanner.nextLine().trim());
                break;
            case "maxVoteAverage":
                System.out.println("Enter maximum vote average:");
                maxVoteAverage = Double.valueOf(scanner.nextLine().trim());
                break;
            case "genreIds":
                System.out.println("Enter genre IDs (comma-separated):");
                String[] ids = scanner.nextLine().trim().split(",");
                genreIds = Arrays.stream(ids).map(Integer::valueOf).collect(Collectors.toList());
                break;
            case "releaseDate":
                System.out.println("Enter release date:");
                releaseDate = scanner.nextLine().trim();
                break;
            case "releaseDateAfter":
                System.out.println("Enter release date after:");
                releaseDateAfter = scanner.nextLine().trim();
                break;
            case "releaseDateBefore":
                System.out.println("Enter release date before:");
                releaseDateBefore = scanner.nextLine().trim();
                break;
            default:
                System.out.println("Invalid criteria. Please try again.");
                break;
        }
    }
    private void interactiveSearch(List<Movie> movies) {
        Scanner scanner = new Scanner(System.in);
        String input;

        // Load favorites from file
        Set<Movie> favorites = loadFavoritesFromFile();

        // Initially print the results
        printResults(movies);

        System.out.println("Press Enter 2 times to start managing favorites...");
        scanner.nextLine();

        do {
            boolean manageFavorites = true;
            while (manageFavorites) {
                System.out.println("Do you want to manage favorite movies? (add, remove, list, done)");
                input = scanner.nextLine();
                switch (input.trim()) {
                    case "add":
                        System.out.println("Enter the title of the movie to add to favorites:");
                        String titleToAdd = scanner.nextLine().trim();
                        Optional<Movie> foundMovie = movies.stream()
                                .filter(movie -> movie.getTitle().equalsIgnoreCase(titleToAdd))
                                .findFirst();
                        if (foundMovie.isPresent()) {
                            favoritesCommand.addFavoriteMovie(foundMovie.get());
                        } else {
                            System.out.println("Movie not found: " + titleToAdd);
                        }
                        break;
                    case "remove":
                        System.out.println("Enter the title of the movie to remove from favorites:");
                        String titleToRemove = scanner.nextLine().trim();
                        Optional<Movie> movieToRemove = favorites.stream()
                                .filter(movie -> movie.getTitle().equalsIgnoreCase(titleToRemove))
                                .findFirst();
                        if (movieToRemove.isPresent()) {
                            favoritesCommand.removeFavoriteMovie(movieToRemove.get());
                        } else {
                            System.out.println("Movie not found in favorites: " + titleToRemove);
                        }
                        break;
                    case "list":
                        favoritesCommand.listFavoriteMovies();
                        break;
                    case "done":
                        manageFavorites = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please choose add, remove, list, or done.");
                        break;
                }
            }

            System.out.println("Do you want to save the results to a file? (yes/no)");
            input = scanner.nextLine().trim();
            if ("yes".equalsIgnoreCase(input)) {
                System.out.println("Please enter your name:");
                String userName = scanner.nextLine().trim();
                favoritesCommand.saveFavoritesToFile(userName,movies);
                System.out.println("Favorites saved to file with the name: " + userName + ".txt");
            }

            System.out.println("Do you want to add criteria to search? (yes/no)");
            input = scanner.nextLine().trim();
            if ("no".equalsIgnoreCase(input)) {
                break; // Exit loop if no further refinement is desired
            }

            System.out.println("Enter criteria for refining search (title, partialTitle, voteAverage, minVoteAverage, maxVoteAverage, genreIds, releaseDate, releaseDateAfter, releaseDateBefore):");
            String criteria = scanner.nextLine().trim();
            applyCriteria(criteria, scanner);

            // Re-filter movies based on the newly applied criteria
            movies = filterMovies(getAllTheMovies()); // Important to re-filter from all movies
            printResults(movies); // Display updated list after re-filtering
        } while (true);
    }
    private Set<Movie> loadFavoritesFromFile() {
        return favoritesCommand.loadFavoritesFromFile();
    }



    private boolean movieContainsAnyGenre(Movie movie, List<Integer> genreIdsToSearch) {
        for (int genreId : genreIdsToSearch) {
            for (int movieGenreId : movie.getGenreIds()) {
                if (genreId == movieGenreId) {
                    return true;
                }
            }
        }
        return false;
    }

}

