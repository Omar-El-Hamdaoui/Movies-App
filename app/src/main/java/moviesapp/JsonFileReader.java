    package moviesapp;

    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;

    import java.io.File;
    import java.io.IOException;
    import java.util.*;

    public class JsonFileReader {
        private static final String filePath = "app/src/text.json";
        public static List<Movie> thisIsAllMovies = new ArrayList<>();
        public static void main(String[] args) {


            try {
                // Créez un objet ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode = objectMapper.readTree(new File(filePath));

                // Accédez aux données spécifiques à partir du nœud JSON
                int page = jsonNode.get("page").asInt();
                int totalResults = jsonNode.get("total_results").asInt();

                System.out.println("Page: " + page);
                System.out.println("Total Results: " + totalResults);

                // Accédez aux résultats individuels
                JsonNode resultsNode = jsonNode.get("results");
                for (JsonNode result : resultsNode) {
                    Movie movie = objectMapper.treeToValue(result, Movie.class);
                    thisIsAllMovies.add(movie);
                }
                for(Movie movie :thisIsAllMovies){
                    System.out.println(movie);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static String getFilePath(){
            return filePath;
        }
    }
