package moviesapp.model;

import moviesapp.ListMoviesCommand;
import moviesapp.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListMoviesCommandTest {
    private ListMoviesCommand listMoviesCommand;
    private List<Movie> testMovies;

    @BeforeEach
    public void setUp() {
        listMoviesCommand = new ListMoviesCommand();
        testMovies = createTestMovies();
    }

    @Test
    public void testFilterMoviesWithTitle() {
        listMoviesCommand.setTitle("Star Wars");
        List<Movie> filteredMovies = listMoviesCommand.filterMovies(testMovies);
        assertEquals(2, filteredMovies.size());
    }

    @Test
    public void testFilterMoviesWithGenreIds() {
        // Simulation de la recherche par genre en définissant directement l'attribut dans la classe ListMoviesCommand
        List<Integer> genreIds = new ArrayList<>();
        genreIds.add(28); // Action
        genreIds.add(12); // Adventure
        listMoviesCommand.genreIds = List.of(genreIds.toArray(new Integer[0]));
        List<Movie> filteredMovies = listMoviesCommand.filterMovies(testMovies);
        assertEquals(2, filteredMovies.size());
    }

    @Test
    public void testAddFavoriteMovie() {
        Movie movieToAdd = testMovies.get(0);
        listMoviesCommand.addFavoriteMovie(movieToAdd);
        assertEquals(1, listMoviesCommand.favoriteMovies.size());
    }



    private List<Movie> createTestMovies() {
        List<Movie> movies = new ArrayList<>();
        movies.add(createMovie("Star Wars: Episode IV - A New Hope", new int[]{28, 12}, 8.6, "1977-05-25"));
        movies.add(createMovie("Star Wars: Episode V - The Empire Strikes Back", new int[]{28, 12}, 8.7, "1980-05-21"));
        movies.add(createMovie("The Godfather", new int[]{80, 18}, 9.2, "1972-03-24"));
        return movies;
    }

    private Movie createMovie(String title, int[] genreIds, double voteAverage, String releaseDate) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenreIds(genreIds);
        movie.setVoteAverage(voteAverage);
        movie.setReleaseDate(releaseDate);
        return movie;
    }
}

