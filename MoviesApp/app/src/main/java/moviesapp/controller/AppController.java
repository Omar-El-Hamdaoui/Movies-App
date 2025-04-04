package moviesapp.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import javafx.scene.web.WebView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.*;
import java.awt.ScrollPane;
import java.io.*;
import java.nio.charset.StandardCharsets;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import moviesapp.Movie;

import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class AppController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField fromYearField;
    @FXML
    private TextField toYearField;
    @FXML
    private ComboBox<String> genreComboBox;
    @FXML
    private TextField ratingField;
    @FXML
    private Button searchButton;
    @FXML
    private Button favoritesButton;
    @FXML
    private ListView<Movie> moviesListView;
    @FXML
    private Button prevPageButton;

    @FXML
    private Button nextPageButton;


    private Set<Movie> favoriteMovies = new HashSet<>();
    private int currentUiPage = 1;
    private final int apiPagesPerUiPage = 1; // Nombre de pages de l'API chargées par page de l'UI
    private final int totalApiPages = 500; // Total des pages de l'API à charger
    private final int totalPagesUi = totalApiPages / apiPagesPerUiPage;
    private Set<Movie> allMovies = new HashSet<>();
    private Map<String, Integer> genreNameToIdMap=Genre.getGenreMap();
    private String favoritesFilePath = "favorites.txt";



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genreComboBox.getItems().add("None");
        genreComboBox.getItems().addAll(genreNameToIdMap.keySet());
        genreComboBox.setValue("None");
        loadMovies();
        fetchAllMovies();
        loadFavorites();

        moviesListView.setCellFactory(param -> new ListCell<Movie>() {
            @Override
            protected void updateItem(Movie movie, boolean empty) {

                super.updateItem(movie, empty);
                if (empty || movie == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(10);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.setPadding(new Insets(5, 10, 5, 10));

                    VBox vBoxText = new VBox(5);
                    Label titleLabel = new Label(movie.getTitle());
                    Label yearLabel = new Label(extractYearSafe(movie.getReleaseDate()));
                    vBoxText.getChildren().addAll(titleLabel, yearLabel);

                    HBox starsBox = createStarsBox(movie.getVoteAverage());

                    Button likeButton = new Button(favoriteMovies.contains(movie) ? "Unlike" : "Like");
                    likeButton.setOnAction(event -> {
                        toggleFavorite(movie);
                        likeButton.setText(favoriteMovies.contains(movie) ? "Unlike" : "Like");
                        moviesListView.refresh();
                    });

                    // Bind the width of the title and year labels to the width of the ListView minus some padding
                    titleLabel.maxWidthProperty().bind(moviesListView.widthProperty().multiply(0.3)); // 30% of list view width
                    yearLabel.maxWidthProperty().bind(moviesListView.widthProperty().multiply(0.1)); // 10% of list view width


                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
                    yearLabel.setStyle("-fx-font-size: 12px;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    ImageView posterView = poster(movie, 110);
                    HBox.setHgrow(posterView, Priority.NEVER); // Ensure the poster doesn't grow

                    hBox.getChildren().addAll(poster(movie, 110), vBoxText, spacer, starsBox, likeButton);
                    HBox.setHgrow(spacer, Priority.ALWAYS); // Grow the spacer to push everything else to the right
                    HBox.setHgrow(vBoxText, Priority.SOMETIMES);

                    setGraphic(hBox);
                }

                // Set mouse click event outside of the updateItem method, as it needs to be set only once
                moviesListView.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1) {
                        handleMovieClick();
                    }
                });
            }
        });
    }

    private String extractYearSafe(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4) {
            return "1890";
        }
        return releaseDate.substring(0, 4); // Extraher et retourner l'année
    }




    @FXML
    private void handleSearchButtonAction() {
        String name = nameField.getText();
        String fromYear = fromYearField.getText();
        String toYear = toYearField.getText();
        String genre = genreComboBox.getValue();
        String rating = ratingField.getText();

        List<Movie> filteredMovies = searchMovies(name, fromYear, toYear, genre, rating); // You need to implement this method.
        moviesListView.getItems().setAll(filteredMovies);
    }

    private List<Movie> searchMovies(String name, String fromYear, String toYear, String genreName, String ratingString) {
        double parsedRating = 0;
        if (ratingString != null && !ratingString.isEmpty()) {
            try {
                parsedRating = Double.parseDouble(ratingString);
            } catch (NumberFormatException e) {
                // Handle the case where the rating is not a valid double
            }
        }
        final double rating = parsedRating; // Make rating effectively final

        // Make genreId effectively final by not modifying it after initialization
        final Integer genreId = (genreName != null && !genreName.equals("None"))
                ? getGenreIdByName(genreName)
                : null;


        // Since fromYear and toYear are used in a lambda expression, they must be effectively final
        final String finalFromYear = fromYear;
        final String finalToYear = toYear;
        if ( Integer.parseInt(finalFromYear)<0 || Integer.parseInt(finalToYear) < 0){
            return null;
        }

        return allMovies.stream()
                .filter(movie -> name == null || name.isEmpty() || movie.getTitle().toLowerCase().contains(name.toLowerCase()))
                .filter(movie -> genreId == null || Arrays.stream(movie.getGenreIds()).anyMatch(id -> id == genreId))
                .filter(movie -> rating == 0 || movie.getVoteAverage() >= rating)
                .filter(movie -> {
                    if (finalFromYear != null && !finalFromYear.isEmpty() && finalToYear != null && !finalToYear.isEmpty() ) {
                        int year = extractYear(movie.getReleaseDate());
                        int from = Integer.parseInt(finalFromYear);
                        int to = Integer.parseInt(finalToYear);
                        return year >= from && year <= to;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }


    private void loadMovies() {
        List<Movie> movies = new ArrayList<>();
        // Calculez le numéro de la première page de l'API pour la page actuelle de l'UI
        int startApiPage = (currentUiPage - 1) * apiPagesPerUiPage + 1;
        for (int i = 0; i < apiPagesPerUiPage; i++) {
            int apiPage = startApiPage + i;
            movies.addAll(fetchMovies(apiPage));
        }
        moviesListView.getItems().setAll(movies);
        updateNavigationButtons();
    }

    public void fetchAllMovies() {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        allMovies.clear(); // Assurez-vous que la liste est vide avant de commencer le processus de récupération.

        for (int page = 1; page <= totalApiPages; page++) {
            Request request = new Request.Builder()
                    .url("https://api.themoviedb.org/3/movie/popular?language=en-US&page=" + page + "&api_key=b8f844e585235d0341ba72bbc763ead2")
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    JsonNode resultsNode = rootNode.path("results");

                    for (JsonNode node : resultsNode) {
                        Movie movie = objectMapper.treeToValue(node, Movie.class);
                        allMovies.add(movie); // Accumulez les films de toutes les pages ici.
                    }
                } else {
                    System.out.println("Failed to get response from the API for page " + page);
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Il pourrait être judicieux d'introduire une certaine forme de gestion des erreurs ou de réessayer la logique ici.
            }
        }
    }



    public List<Movie> fetchMovies(int page) {
        List<Movie> moviesOnPage = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/popular?language=en-US&page=" + page + "&api_key=b8f844e585235d0341ba72bbc763ead2")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode resultsNode = rootNode.path("results");

                for (JsonNode node : resultsNode) {
                    Movie movie = objectMapper.treeToValue(node, Movie.class);
                    moviesOnPage.add(movie);
                }
            } else {
                System.out.println("Failed to get response from the API for page " + page);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return moviesOnPage;
    }

    private Integer getGenreIdByName(String genreName) {
        if (genreName == null || genreNameToIdMap == null) {
            return null;
        }
        return genreNameToIdMap.get(genreName);
    }


    private int extractYear(String releaseDate) {
        if (releaseDate == null || releaseDate.isEmpty() || releaseDate.length() < 4) {
            return 1800; // Retourne une année par défaut si la date de sortie n'est pas valide
        }
        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (NumberFormatException e) {
            System.err.println("Erreur lors de l'extraction de l'année pour la date: " + releaseDate);
            return -1; // Retourne une valeur d'année invalide en cas d'erreur
        }
    }







    private HBox createStarsBox(double rating) {
        HBox starsBox = new HBox();
        starsBox.setSpacing(5);
        starsBox.setAlignment(Pos.CENTER);
        for (int i = 1; i <= 5; i++) {
            Label starLabel = new Label();
            if (i <= Math.floor(rating/2)) {
                starLabel.setText("\u2605"); // Unicode solid star
            } else {
                starLabel.setText("\u2606"); // Unicode outline star
            }
            starsBox.getChildren().add(starLabel);
        }
        return starsBox;
    }




    private void toggleFavorite(Movie movie) {
        if (favoriteMovies.contains(movie)) {
            favoriteMovies.remove(movie); // Supprimer le film s'il est déjà dans les favoris
        } else {
            boolean isAlreadyFavorite = favoriteMovies.stream()
                    .anyMatch(favorite -> favorite.getId() == movie.getId());
            if (!isAlreadyFavorite) {
                favoriteMovies.add(movie); // Ajouter le film s'il n'est pas déjà dans les favoris
            }
        }
        moviesListView.refresh(); // Rafraîchir la ListView pour refléter le changement
        saveFavorites();
    }


    @FXML
    private void handleFavoritesButtonAction() {
        moviesListView.getItems().setAll(favoriteMovies);
    }


    @FXML
    private void handleShowAllMoviesAction() {
        loadMovies(); // Load all movies back into the list view
    }
    @FXML
    private void restart(){
        favoriteMovies.clear();
        moviesListView.getItems().clear();
        loadMovies();
        clearFavoritesFile();
    }
    private void handleMovieClick() {
        Movie selectedMovie = moviesListView.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            // Fetch director's name
            String directorName = String.valueOf(fetchMovieDirector(selectedMovie.getId()));

            // Create a new custom dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Movie Details");

            // Remove default icon (information icon)
            dialog.setGraphic(null);

            // Set the header with movie title and director's name
            VBox headerBox = new VBox();
            Label titleLabel = new Label(selectedMovie.getTitle());
            Label directorLabel = new Label("Director: " + directorName);
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
            directorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
            headerBox.getChildren().addAll(titleLabel, directorLabel);
            headerBox.setStyle("-fx-background-color: #37474F; -fx-padding: 10;");
            dialog.getDialogPane().setHeader(headerBox);

            // TextArea for movie details (on the right side)
            TextArea textArea = new TextArea(getMovieDetails(selectedMovie));
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-text-fill: black;");

            // ImageView for the poster (on the left side)
            ImageView posterView = new ImageView();
            posterView.setPreserveRatio(true);
            posterView.setFitHeight(300); // Adjust the height as needed

            try {
                // Load the poster image
                String posterPath = selectedMovie.getPosterPath();
                if (posterPath != null && !posterPath.isEmpty()) {
                    String posterUrl = "https://image.tmdb.org/t/p/w500" + posterPath;
                    Image posterImage = new Image(posterUrl, true);
                    posterView.setImage(posterImage);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                // Handle the error here, maybe show an error message to the user
            }

            // Create a GridPane for layout
            GridPane gridPane = new GridPane();
            gridPane.add(posterView, 0, 0); // Add poster to the left side
            gridPane.add(textArea, 1, 0); // Add text area to the right side
            gridPane.setStyle("-fx-background-color: transparent;"); // Make sure the GridPane is transparent
            GridPane.setHgrow(posterView, Priority.ALWAYS); // Allow poster to grow horizontally
            GridPane.setVgrow(posterView, Priority.ALWAYS); // Allow poster to grow vertically
            GridPane.setHgrow(textArea, Priority.ALWAYS); // Allow text area to grow horizontally
            GridPane.setVgrow(textArea, Priority.ALWAYS); // Allow text area to grow vertically

            // Set the backdrop as the background of the dialog pane
            String backdropUrl = "https://image.tmdb.org/t/p/w500" + selectedMovie.getBackdropPath();
            dialog.getDialogPane().setStyle(
                    "-fx-background-image: url('" + backdropUrl + "');" +
                            "-fx-background-size: cover;" +
                            "-fx-background-position: center center;" +
                            "-fx-background-repeat: no-repeat;"
            );

            // Add buttons to view the video about the movie, recommendations, and director's movies
            ButtonType videoButtonType = new ButtonType("Video", ButtonBar.ButtonData.OTHER);
            ButtonType recommendationButtonType = new ButtonType("Recommendations", ButtonBar.ButtonData.OTHER);
            ButtonType directorsMoviesButtonType = new ButtonType("Director's Movies", ButtonBar.ButtonData.OTHER); // New button for director's movies
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

            dialog.getDialogPane().getButtonTypes().addAll(closeButton, videoButtonType, recommendationButtonType, directorsMoviesButtonType); // Add new button types

            dialog.setResultConverter(buttonType -> {
                if (buttonType == videoButtonType) {
                    String videoUrl = getMovieVideoUrl(selectedMovie.getId());
                    if (videoUrl != null && !videoUrl.isEmpty()) {
                        openVideoInBrowser(videoUrl); // Open the video in a browser
                    } else {
                        // Handle the case where no video URL is found
                        System.out.println("No video URL found for the movie.");
                    }
                } else if (buttonType == recommendationButtonType) {
                    // Fetch and display recommendations for the selected movie
                    List<Movie> recommendations = fetchRecommendations(selectedMovie.getId());
                    if (!recommendations.isEmpty()) {
                        // Show the recommendations in a new dialog or window
                        showRecommendations(recommendations);
                    } else {
                        // Handle the case where no recommendations are found
                        System.out.println("No recommendations found for the movie.");
                    }
                } else if (buttonType == directorsMoviesButtonType) {
                    int directorId = fetchDirectorId(selectedMovie.getId()); // Fetch the director's ID
                    List<Movie> directorsMovies = fetchMoviesByDirectorId(directorId); // Fetch movies by this director
                    if (!directorsMovies.isEmpty()) {
                        // Show the director's movies in a new dialog or window
                        showDirectorsMovies(directorsMovies, directorName); // You'll need to implement this method
                    } else {
                        System.out.println("No movies found for the director.");
                    }
                }
                return null;
            });

            // Add the GridPane to the dialog
            dialog.getDialogPane().setContent(gridPane);

            // Show the custom dialog
            dialog.showAndWait();
        }
    }



    private List<Movie> fetchRecommendations(int movieId) {
        List<Movie> recommendations = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignorer les propriétés inconnues


        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/recommendations?language=en-US&api_key=b8f844e585235d0341ba72bbc763ead2")
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode resultsNode = rootNode.path("results");
                for (JsonNode node : resultsNode) {
                    // Vérifier si media_type est présent dans l'objet JSON
                    if (node.has("media_type")) {
                        // Ignorer la propriété media_type lors de la désérialisation
                        ((ObjectNode) node).remove("media_type");
                    }
                    // Désérialiser l'objet JSON en utilisant la classe Movie
                    Movie movie = objectMapper.treeToValue(node, Movie.class);
                    recommendations.add(movie);
                }
            } else {
                System.out.println("Failed to get response from the API for recommendations for movie ID: " + movieId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recommendations;
    }
    private void showRecommendations(List<Movie> recommendations) {
        moviesListView.getItems().setAll(recommendations);
    }



    private String getMovieDetails(Movie movie) {
        return "Id: " + movie.getId() + '\n' +
                "Original Language:  " + movie.getOriginalLanguage() + '\n' +
                "Original Title:  " + movie.getOriginalTitle() + '\n' +
                "Overview:  " + movie.getOverview() + '\n' +
                "Popularity: " + movie.getPopularity() + '\n' +
                "Release Date:  " + movie.getReleaseDate() + '\n' +
                "Vote Average: " + movie.getVoteAverage() + '\n' +
                "Vote Count: " + movie.getVoteCount();
    }




    private void setPosterAsync(Movie movie, ImageView imageView, int width) {
        Task<Image> loadImageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                // Charger l'image depuis l'URL
                String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();

                return new Image(posterUrl);
            }
        };

        loadImageTask.setOnSucceeded(event -> {
            Image posterImage = loadImageTask.getValue();
            imageView.setImage(posterImage);
        });

        new Thread(loadImageTask).start();
    }

    private ImageView poster(Movie movie, int width) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(width); // Largeur de l'image
        imageView.setPreserveRatio(true); // Conserver le ratio de l'image

        // Utiliser le cache d'images pour éviter de recharger les mêmes images plusieurs fois
        Cache<String, Image> imageCache = Caffeine.newBuilder()
                .maximumSize(100) // Consider adjusting or using .maximumWeight() with a weigher based on image sizes.
                .expireAfterAccess(Duration.ofMinutes(10)) // Adjust based on use case.
                .softValues() // Consider using softValues() for memory-sensitive caching.
                .build();

        String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();

        Image cachedImage = imageCache.getIfPresent(posterUrl);
        if (cachedImage != null) {
            imageView.setImage(cachedImage);
        } else {
            // Si l'image n'est pas en cache, la charger de manière asynchrone
            setPosterAsync(movie, imageView, width);
        }
        return imageView;
    }


    @FXML
    private void handlePrevPageAction() {
        if (currentUiPage > 1) {
            currentUiPage--;
            loadMovies();
        }
    }

    @FXML
    private void handleNextPageAction() {
        if (currentUiPage < totalPagesUi) {
            currentUiPage++;
            loadMovies();
        }
    }
    private void updateNavigationButtons() {
        prevPageButton.setDisable(currentUiPage <= 1);
        nextPageButton.setDisable(currentUiPage >= totalPagesUi);
    }


    private void saveFavorites() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> jsonFavorites = favoriteMovies.stream().map(movie -> {
            try {
                return objectMapper.writeValueAsString(movie);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());

        try {
            Files.write(Paths.get(favoritesFilePath), jsonFavorites);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadFavorites() {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(favoritesFilePath);
        favoriteMovies.clear(); // Réinitialisez l'ensemble des favoris

        if (file.exists()) {
            try {
                List<String> jsonFavorites = Files.readAllLines(Paths.get(favoritesFilePath));
                for (String jsonFavorite : jsonFavorites) {
                    try {
                        Movie movie = objectMapper.readValue(jsonFavorite, Movie.class);
                        favoriteMovies.add(movie);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openVideoInBrowser(String videoUrl) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(videoUrl));
        } catch (IOException | java.net.URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private String getMovieVideoUrl(int movieId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/videos?language=en-US&api_key=b8f844e585235d0341ba72bbc763ead2")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode resultsNode = rootNode.path("results");

                // Iterate through the results to find the first video key
                for (JsonNode node : resultsNode) {
                    String videoKey = node.path("key").asText();
                    if (videoKey != null && !videoKey.isEmpty()) {
                        // Assuming the video is hosted on YouTube
                        String videoUrl = "https://www.youtube.com/watch?v=" + videoKey;
                        return videoUrl;
                    }
                }
            } else {
                System.out.println("Failed to get response from the API for fetching video URL.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    private void clearFavoritesFile() {
        try (PrintWriter writer = new PrintWriter(favoritesFilePath, StandardCharsets.UTF_8)) {
            // Écrire dans le fichier sans contenu le vide
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String fetchMovieDirector(int movieId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=b8f844e585235d0341ba72bbc763ead2")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(responseBody);
                JSONArray crewArray = (JSONArray) jsonObject.get("crew");

                for (Object crewObj : crewArray) {
                    JSONObject crewMember = (JSONObject) crewObj;
                    String job = (String) crewMember.get("job");
                    if ("Director".equals(job)) {
                        return (String) crewMember.get("name");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Director Not Found"; // Return this if director's name not found
    }
    private List<Movie> fetchMoviesByDirectorId(int directorId) {
        List<Movie> directorMovies = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore unknown properties

        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/person/" + directorId + "/movie_credits?api_key=b8f844e585235d0341ba72bbc763ead2")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode crewNode = rootNode.path("crew");

                for (JsonNode node : crewNode) {
                    if ("Director".equals(node.path("job").asText())) {
                        Movie movie = objectMapper.treeToValue(node, Movie.class);
                        if (!(movie.getVoteCount()==0)) {
                            directorMovies.add(movie);
                        }

                    }
                }
            } else {
                System.out.println("Failed to get response from the API for director ID: " + directorId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return directorMovies;
    }




    public int fetchDirectorId(int movieId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=b8f844e585235d0341ba72bbc763ead2")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(responseBody);
                JSONArray crewArray = (JSONArray) jsonObject.get("crew");

                for (Object crewObj : crewArray) {
                    JSONObject crewMember = (JSONObject) crewObj;
                    String job = (String) crewMember.get("job");
                    if ("Director".equals(job)) {
                        return ((Long) crewMember.get("id")).intValue(); // Casting long to int
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if director not found or an error occurs
    }

    public void showDirectorsMovies(List<Movie> directorsMovies, String directorName) {
         moviesListView.getItems().setAll(directorsMovies);

    }




}
