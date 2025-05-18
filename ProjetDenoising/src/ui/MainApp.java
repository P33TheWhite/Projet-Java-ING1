

package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainApp extends Application {
	
	/**
	 * Méthode principale appelée au lancement de l'application JavaFX.
	 * Elle affiche une boîte de dialogue permettant à l'utilisateur de choisir
	 * entre deux méthodes de traitement d'image : globale ou locale (imagettes),
	 * puis instancie l'interface utilisateur et le contrôleur de traitement
	 * en fonction du choix effectué.
	 *
	 * @param primaryStage La fenêtre principale (stage) fournie par JavaFX.
	 */
    @Override
    public void start(Stage primaryStage) {
        List<String> choix = Arrays.asList("Approche globale", "Approche locale (imagettes)");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Approche globale", choix);
        dialog.setTitle("Choix de la méthode");
        dialog.setHeaderText("Sélectionne la méthode de traitement des patchs");
        dialog.setContentText("Méthode :");

        Optional<String> resultat = dialog.showAndWait();

        if (resultat.isEmpty()) {
            System.out.println("Aucun mode sélectionné. Fermeture.");
            Platform.exit();
            return;
        }

        String mode = resultat.get();

        try {
            MainView vue = new MainView(primaryStage);
            DenoisingController controller = new DenoisingController(vue);

            
            if (mode.equals("Approche globale")) {
                controller.setModeGlobal(true);  // Mode global
            } else {
                controller.setModeGlobal(false); // Mode local
            }

            vue.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
