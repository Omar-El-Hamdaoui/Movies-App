package moviesapp.model;

import moviesapp.Movie;
import moviesapp.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class UserTest {
    User user1 = new User( "A");
    @Test
    public void testGetName() {

        assertThat(user1.getName()).isEqualTo("A");
    }

    @Test
    public void testGetId() {
        User user2 = new User("B");
        assertThat(user1.getId()).isEqualTo(1);
        assertThat(user2.getId()).isEqualTo(2);

        System.out.println("User 1 - ID: " + user1.getId() + ", Username: " + user1.getName());
        System.out.println("User 2 - ID: " + user2.getId() + ", Username: " + user2.getName());
    }

    @Test
    public void testGetFavorites(){

        Movie movie1 = new Movie();
        Movie movie2 = new Movie();

        List<Movie> favorites = new ArrayList<>();

        favorites.add(movie1);
        favorites.add(movie2);

        user1.addMovie(movie1);
        user1.addMovie(movie2);

        assertThat(user1.getFavorites()).isEqualTo(favorites);

        user1.deleteMovie(movie1);
        List<Movie> favTest = new ArrayList<>();
        favTest.add(movie2);
        assertThat(user1.getFavorites()).isEqualTo(favTest);

    }

    @Test
    public void testGetFavoritesNames(){
        Movie movie1 = new Movie();
        Movie movie2 = new Movie();
        movie1.setTitle("ABC");
        movie2.setTitle("XYZ");
        List<Movie> favorites = new ArrayList<>();
        favorites.add(movie1);
        favorites.add(movie2);
        user1.addMovie(movie1);
        user1.addMovie(movie2);

        List<String> favNameTest = new ArrayList<>();
        favNameTest.add("ABC");
        favNameTest.add("XYZ");

        assertThat(user1.getFavoriteNames()).isEqualTo(favNameTest);
    }
}

