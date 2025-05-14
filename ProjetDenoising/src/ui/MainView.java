package ui;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;
import model.Imagette;
import model.Patch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainView {
    private final Stage stage;
    private final ImageView originalView = new ImageView();
    private final ImageView noisyView = new ImageView();
    private final Slider noiseSlider = new Slider(0, 30, 10);
    private final TextField divisionsField = new TextField("4");
    private final Button saveButton = new Button("Enregistrer l'image bruitée");
    private final Button cutButton = new Button("Découper l'image");
    private final Label errorLabel = new Label();
    private final TilePane imagettesPane = new TilePane();
    private final ScrollPane imagettesScrollPane = new ScrollPane();
    private final ComboBox<Integer> patchSizeCombo = new ComboBox<>();
    private final ComboBox<Integer> patchStepCombo = new ComboBox<>();
    private final Button extractPatchesButton = new Button("Extraire les patchs");
    private final TilePane patchesPane = new TilePane();
    private final ScrollPane patchesScrollPane = new ScrollPane();
    private Runnable onExtractPatchesRequested;

    private Consumer<File> onImageSelected;
    private Consumer<Double> onNoiseChanged;
    private Consumer<File> onSaveRequested;
    private Runnable onCutRequested;

    public MainView(Stage stage) {
        this.stage = stage;
        setupUI();
    }

    private void setupUI() {
        stage.setTitle("Découpe d'images dynamique");

        Button selectImage = new Button("Choisir une image");
        selectImage.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Sélectionnez une image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg"));
            File file = chooser.showOpenDialog(stage);
            if (file != null && onImageSelected != null) {
                onImageSelected.accept(file);
            }
        });

        // Configuration du slider de bruit
        noiseSlider.setShowTickLabels(true);
        noiseSlider.setShowTickMarks(true);
        noiseSlider.setMajorTickUnit(10);
        noiseSlider.setMinorTickCount(0);
        noiseSlider.setBlockIncrement(10);
        noiseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (onNoiseChanged != null) {
                onNoiseChanged.accept(newVal.doubleValue());
            }
        });

        // Configuration du champ de texte pour les divisions
        divisionsField.setPrefWidth(50);
        divisionsField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        saveButton.setDisable(true);
        saveButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Enregistrer l'image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
            File file = chooser.showSaveDialog(stage);
            if (file != null && onSaveRequested != null) {
                onSaveRequested.accept(file);
            }
        });

        cutButton.setDisable(true);
        cutButton.setOnAction(e -> {
            if (onCutRequested != null) {
                onCutRequested.run();
            }
        });

        // Configuration des ImageView
        originalView.setFitWidth(300);
        originalView.setFitHeight(300);
        originalView.setPreserveRatio(true);
        originalView.setSmooth(true);
        
        noisyView.setFitWidth(300);
        noisyView.setFitHeight(300);
        noisyView.setPreserveRatio(true);
        noisyView.setSmooth(true);

        // Configuration du TilePane pour les imagettes
        imagettesPane.setHgap(10);
        imagettesPane.setVgap(10);
        imagettesPane.setPadding(new Insets(10));
        imagettesPane.setPrefColumns(3);
        
        // Configuration du ScrollPane pour les imagettes
        imagettesScrollPane.setContent(imagettesPane);
        imagettesScrollPane.setFitToWidth(true);
        imagettesScrollPane.setPrefViewportHeight(200);
        imagettesScrollPane.setStyle("-fx-background: white; -fx-border-color: lightgray;");

        // Configuration des contrôles de patch
        patchSizeCombo.setPrefWidth(100);
        patchStepCombo.setPrefWidth(100);
        
        HBox patchControls = new HBox(10,
            new Label("Taille de patch:"), patchSizeCombo,
            new Label("Pas:"), patchStepCombo,
            extractPatchesButton
        );
        patchControls.setAlignment(Pos.CENTER_LEFT);
        
        // Configuration du TilePane pour les patchs
        patchesPane.setHgap(5);
        patchesPane.setVgap(5);
        patchesPane.setPadding(new Insets(5));
        patchesPane.setPrefColumns(6);
        
        // Configuration du ScrollPane pour les patchs
        patchesScrollPane.setContent(patchesPane);
        patchesScrollPane.setFitToWidth(true);
        patchesScrollPane.setPrefViewportHeight(200);
        patchesScrollPane.setStyle("-fx-background: white; -fx-border-color: lightgray;");

        // Organisation des contrôles
        HBox decoupeControls = new HBox(10, 
                new Label("Nombre de divisions (n):"), divisionsField,
                cutButton
        );
        decoupeControls.setAlignment(Pos.CENTER_LEFT);

        HBox imageContainer = new HBox(20, originalView, noisyView);
        imageContainer.setAlignment(Pos.CENTER);
        
        VBox mainContent = new VBox(10,
                selectImage,
                new Label("Niveau de bruit:"),
                noiseSlider,
                new HBox(10, saveButton),
                decoupeControls,
                errorLabel,
                imageContainer,
                new Label("Imagettes générées:"),
                imagettesScrollPane,
                new Label("Paramètres d'extraction de patchs:"),
                patchControls,
                new Label("Patchs extraits:"),
                patchesScrollPane
        );
        mainContent.setPadding(new Insets(15));
        
        // Make the main content scrollable
        ScrollPane mainScrollPane = new ScrollPane(mainContent);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Style de l'erreur
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        stage.setScene(new Scene(mainScrollPane, 800, 700));
        
        extractPatchesButton.setOnAction(e -> {
            if (onExtractPatchesRequested != null) {
                onExtractPatchesRequested.run();
            }
        });
    }    
    
    public void setPossiblePatchSizes(int imageWidth, int imageHeight) {
        patchSizeCombo.getItems().clear();
        patchStepCombo.getItems().clear();

        // Proposer toutes les tailles de patch qui divisent à la fois la largeur et la hauteur de l'image
        for (int size = 4; size <= Math.min(imageWidth, imageHeight); size += 4) {
            // Vérifier que la taille est un diviseur à la fois de la largeur et de la hauteur
            if (imageWidth % size == 0 && imageHeight % size == 0) {
                patchSizeCombo.getItems().add(size);
            }
        }

        // Sélectionner une taille par défaut (par exemple, la plus petite)
        if (!patchSizeCombo.getItems().isEmpty()) {
            patchSizeCombo.getSelectionModel().selectFirst();
        }

        // Mettre à jour les pas possibles pour la taille de patch sélectionnée
        updatePossibleSteps();

        // Mettre à jour dynamiquement les pas lorsque l'on change de taille de patch
        patchSizeCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> updatePossibleSteps()
        );
    }

    private void updatePossibleSteps() {
        Integer selectedSize = patchSizeCombo.getValue();
        if (selectedSize == null) return;

        patchStepCombo.getItems().clear();

        // Permettre tous les pas allant de 1 à la taille sélectionnée
        for (int step = 1; step <= selectedSize; step++) {
            if (selectedSize % step == 0) { // Seules les tailles de pas qui sont des diviseurs de la taille de patch sont possibles
                patchStepCombo.getItems().add(step);
            }
        }

        // Sélectionner un pas par défaut
        patchStepCombo.getSelectionModel().selectFirst();
    }


    
    public void show() {
        stage.show();
    }

    public int getNombreDivisions() {
        try {
            return Integer.parseInt(divisionsField.getText());
        } catch (NumberFormatException e) {
            return 1; // Valeur par défaut si le texte n'est pas un nombre valide
        }
    }
    
    public int getPatchSize() {
        return patchSizeCombo.getValue() != null ? 
               patchSizeCombo.getValue() : 8;
    }
    
    public int getPatchStep() {
        return patchStepCombo.getValue() != null ? 
               patchStepCombo.getValue() : 4;
    }
    
    public void displayPatches(List<ArrayList<ArrayList<Patch>>> allPatches) {
        patchesPane.getChildren().clear();

        for (List<ArrayList<Patch>> imagettePatches : allPatches) {
            for (ArrayList<Patch> patchList : imagettePatches) {
                for (Patch patch : patchList) {
                    ImageView iv = new ImageView(
                        SwingFXUtils.toFXImage((BufferedImage) patch.getImage(), null));
                    iv.setFitWidth(50);
                    iv.setPreserveRatio(true);

                    // Ajouter un tooltip avec les infos du patch
                    Tooltip.install(iv, new Tooltip(
                        "Taille: " + patch.getMatrice().length + "x" + patch.getMatrice()[0].length +
                        "\nPosition: (" + patch.getPremierPixelPos()[0] + "," + patch.getPremierPixelPos()[1] + ")"));

                    patchesPane.getChildren().add(iv);
                }
            }
        }
    }

    
    public void setOnExtractPatchesRequested(Runnable handler) {
        this.onExtractPatchesRequested = handler;
    }

    public double getNoiseLevel() {
        return noiseSlider.getValue();
    }

    public void setOriginalImage(BufferedImage image) {
        originalView.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public void setNoisyImage(BufferedImage image) {
        noisyView.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public void displayImagettes(List<Imagette> imagettes) {
        imagettesPane.getChildren().clear();
        for (Imagette imagette : imagettes) {
            ImageView iv = new ImageView(SwingFXUtils.toFXImage(imagette.getImage(), null));
            iv.setFitWidth(100);
            iv.setPreserveRatio(true);
            imagettesPane.getChildren().add(iv);
        }
    }

    public void setOnImageSelected(Consumer<File> handler) {
        this.onImageSelected = handler;
    }

    public void setOnNoiseChanged(Consumer<Double> handler) {
        this.onNoiseChanged = handler;
    }

    public void setOnSaveRequested(Consumer<File> handler) {
        this.onSaveRequested = handler;
    }

    public void setOnCutRequested(Runnable handler) {
        this.onCutRequested = handler;
    }

    public void enableSave(boolean enabled) {
        saveButton.setDisable(!enabled);
    }

    public void enableCut(boolean enabled) {
        cutButton.setDisable(!enabled);
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }
}