package moviesapp.controller;

import java.util.HashMap;
import java.util.Map;

public enum Genre {
    ACTION("Action", 28),
    ADVENTURE("Adventure", 12),
    ANIMATION("Animation", 16),
    COMEDY("Comedy", 35),
    CRIME("Crime", 80),
    DOCUMENTARY("Documentary", 99),
    DRAMA("Drama", 18),
    FAMILY("Family", 10751),
    FANTASY("Fantasy", 14),
    HISTORY("History", 36),
    HORROR("Horror", 27),
    MUSIC("Music", 10402),
    MYSTERY("Mystery", 9648),
    ROMANCE("Romance", 10749),
    SCIENCE_FICTION("Science fiction", 878),
    TV_MOVIE("Tv movie", 10770),
    THRILLER("Thriller", 53),
    WAR("War", 10752),
    WESTERN("Western", 37);

    private final String name;
    private final int id;

    Genre(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public static Map<String, Integer> getGenreMap() {
        Map<String, Integer> genreMap = new HashMap<>();
        for (Genre genre : Genre.values()) {
            genreMap.put(genre.getName(), genre.getId());
        }
        return genreMap;
    }
}
