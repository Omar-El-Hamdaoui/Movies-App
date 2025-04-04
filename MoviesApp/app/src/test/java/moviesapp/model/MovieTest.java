package moviesapp.model;
import moviesapp.Movie;
import moviesapp.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


public class MovieTest {

    @Test
    public void testMovieCreation() {
        Movie movie = new Movie();
        assertNotNull(movie);
    }

    @Test
    public void testGettersAndSetters() {
        Movie movie = new Movie();

        movie.setAdult(true);
        assertTrue(movie.isAdult());

        movie.setBackdropPath("/backdrop.jpg");
        assertEquals("/backdrop.jpg", movie.getBackdropPath());

        int[] genreIds = {1, 2, 3};
        movie.setGenreIds(genreIds);
        assertArrayEquals(genreIds, movie.getGenreIds());

        movie.setId(123);
        assertEquals(123, movie.getId());

        movie.setOriginalLanguage("en");
        assertEquals("en", movie.getOriginalLanguage());

        movie.setOriginalTitle("Original Title");
        assertEquals("Original Title", movie.getOriginalTitle());

        movie.setOverview("This is the movie overview.");
        assertEquals("This is the movie overview.", movie.getOverview());

        movie.setPopularity(7.8);
        assertEquals(7.8, movie.getPopularity());

        movie.setPosterPath("/poster.jpg");
        assertEquals("/poster.jpg", movie.getPosterPath());

        movie.setReleaseDate("2022-01-01");
        assertEquals("2022-01-01", movie.getReleaseDate());

        movie.setTitle("Movie Title");
        assertEquals("Movie Title", movie.getTitle());

        movie.setVideo(true);
        assertTrue(movie.isVideo());

        movie.setVoteAverage(8.5);
        assertEquals(8.5, movie.getVoteAverage());

        movie.setVoteCount(1000);
        assertEquals(1000, movie.getVoteCount());
    }

    @Test
    public void testToStringMethod() {
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setAdult(false);
        movie.setBackdropPath("/test/backdrop.jpg");
        movie.setGenreIds(new int[]{1, 2, 3});
        movie.setId(456);
        movie.setOriginalLanguage("en");
        movie.setOriginalTitle("Original Title");
        movie.setOverview("This is a test movie.");
        movie.setPopularity(9.2);
        movie.setPosterPath("/test/poster.jpg");
        movie.setReleaseDate("2022-02-12");
        movie.setVideo(false);
        movie.setVoteAverage(9.0);
        movie.setVoteCount(500);

        String expectedToString = "The movie Is 'Test Movie',\n" +
                "Adult: false,\n" +
                "Backdrop Path: '/test/backdrop.jpg',\n" +
                "Genre Ids=[1, 2, 3],\n" +
                "Id: 456,\n" +
                "Original Language: 'en',\n" +
                "Original Title: 'Original Title',\n" +
                "Overview: 'This is a test movie.',\n" +
                "Popularity: 9.2,\n" +
                "Poster Path: '/test/poster.jpg',\n" +
                "Release Date: '2022-02-12',\n" +
                "Video: false,\n" +
                "Vote Average: 9.0,\n" +
                "Vote Count: 500\n" +
                "-------------------------------------------------------------------------";

        assertEquals(expectedToString, movie.toString());
    }
}


