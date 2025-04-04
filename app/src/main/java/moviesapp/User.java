package moviesapp;

import moviesapp.Movie;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final int id;
    private final String name;
    private static int counter=0;
    private List<Movie> favorites = new ArrayList<>();

    public User(String name) {
        this.id = ++counter;
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public int getId(){
        return id;
    }
    public void addMovie(Movie movie){
        if(favorites.contains(movie)){}
        else favorites.add(movie);
    }
    public void deleteMovie(Movie movie){
        if(!favorites.contains(movie)){}
        else favorites.remove(movie);
    }
    public List<Movie> getFavorites(){return favorites;}
    public List<String> getFavoriteNames(){
        return favorites.stream().map(Movie::getTitle).toList();
    }
}
