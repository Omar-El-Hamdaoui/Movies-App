package moviesapp;

import picocli.CommandLine;
import picocli.CommandLine.Command;


@Command(name = "appcli", mixinStandardHelpOptions = true, version = "1.0")
public class AppCLI {

    private ListMoviesCommand listMoviesCommand;

    public AppCLI(ListMoviesCommand listMoviesCommand) {
        this.listMoviesCommand = listMoviesCommand;
    }

    public static void main(String[] args) {
        // Créez une instance de ListMoviesCommand
        ListMoviesCommand listMoviesCommand = new ListMoviesCommand();

        // Créez une instance de AppCLI en passant l'instance de ListMoviesCommand
        AppCLI appCLI = new AppCLI(listMoviesCommand);

        // Créez une instance de CommandLine avec l'instance de AppCLI
        CommandLine commandLine = new CommandLine(appCLI);

        // Ajoutez la sous-commande "list" avec l'instance de ListMoviesCommand
        commandLine.addSubcommand("AllMovies", listMoviesCommand);

        // Exécutez l'application avec les arguments
        commandLine.execute(args);
    }

}
