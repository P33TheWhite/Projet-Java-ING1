package ui;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.Imagette;
import model.Patch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainView {
    // Déclaration des composants de l'interface
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
    private VBox imagetteBlock;
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
    private HBox decoupeControls;
    private final ImageView denoisedView = new ImageView();
    private final Button denoiseButton = new Button("Débruiter l'image");
    private Runnable onDenoiseRequested;
    private final TextArea qualityReportArea = new TextArea();
    private final ProgressIndicator loadingIndicator = new ProgressIndicator();
    private final ToggleButton themeToggle = new ToggleButton("🌙");
    private boolean darkMode = false;

    public MainView(Stage stage) {
        this.stage = stage;
        setupUI();
        setupTheme();
    }

    private void setupUI() {
        stage.setTitle("Débruitage d'images par ACP");

        // Style CSS de base
        String css = """
            .root {
                -fx-font-family: 'Segoe UI', Arial;
                -fx-base: #ececec;
            }
            
            .dark-theme {
                -fx-base: #2b2b2b;
                -fx-background: #333333;
                -fx-control-inner-background: #3c3c3c;
                -fx-text-fill: #f4f4f4;
            }
            
            .progress-bar {
			    -fx-accent: #3498db; /* Blue color */
			    -fx-background-color: #e0e0e0;
			    -fx-border-color: #bdbdbd;
			    -fx-border-radius: 3px;
			    -fx-padding: 2px;
			}
			
			.progress-bar .track {
			    -fx-background-color: transparent;
			}
			
			.progress-bar .bar {
			    -fx-background-color: linear-gradient(to right, #3498db, #2980b9); /* Gradient blue */
			    -fx-background-insets: 0;
			    -fx-background-radius: 2px;
			}

            
            .progress-bar .track {
                -fx-background-color: transparent;
            }
            
            .progress-bar .bar {
                -fx-background-color: linear-gradient(to right, #4CAF50, #8BC34A);
                -fx-background-insets: 0;
                -fx-background-radius: 2px;
            }
            
            .button {
                -fx-background-radius: 4px;
                -fx-padding: 6px 12px;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);
            }
            
            .button:hover {
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);
            }
            
            .text-field, .combo-box, .text-area, .scroll-pane {
                -fx-background-radius: 4px;
                -fx-border-radius: 4px;
            }
            
            .label-title {
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-padding: 0 0 5px 0;
            }
            
            .error-label {
                -fx-text-fill: #ff4444;
                -fx-font-weight: bold;
            }
            
            .image-view-container {
                -fx-border-color: #bdbdbd;
                -fx-border-radius: 4px;
                -fx-background-color: #f5f5f5;
                -fx-padding: 5px;
            }
            
            .dark-theme .image-view-container {
                -fx-background-color: #3c3c3c;
                -fx-border-color: #555555;
            }
            .loading-indicator {
			    -fx-progress-color: #2196F3;
			    -fx-alignment: CENTER;
			}

        """;        

        themeToggle.setStyle("-fx-font-size: 16px;");

        themeToggle.setSelected(darkMode);


        themeToggle.setText(darkMode ? "☀️" : "\u263D"); // ☀️ or ☽

        themeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            darkMode = newVal;
            applyTheme();
            themeToggle.setText(newVal ? "☀️" : "\u263D");
        });



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
        
        qualityReportArea.setEditable(false);
        qualityReportArea.setWrapText(true);
        qualityReportArea.setPrefRowCount(4);

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
        
        denoiseButton.setDisable(true);
        denoiseButton.setOnAction(e -> {
            if (onDenoiseRequested != null) {
                onDenoiseRequested.run();
            }
        });
        
        loadingIndicator.setVisible(false);
        loadingIndicator.getStyleClass().add("loading-indicator");


        
        // Configuration des ImageView avec style
        VBox originalContainer = new VBox(5, new Label("Originale"));
        originalContainer.getStyleClass().add("image-view-container");
        originalContainer.getChildren().add(originalView);
        originalContainer.setAlignment(Pos.TOP_CENTER);
        originalContainer.setPrefWidth(400);

        VBox noisyContainer = new VBox(5, new Label("Bruitée"));
        noisyContainer.getStyleClass().add("image-view-container");
        noisyContainer.getChildren().add(noisyView);
        noisyContainer.setAlignment(Pos.TOP_CENTER);
        noisyContainer.setPrefWidth(400);

        StackPane denoisedStack = new StackPane(denoisedView, loadingIndicator);
        denoisedStack.setPrefSize(400, 300);
        StackPane.setAlignment(loadingIndicator, Pos.CENTER);
        StackPane.setAlignment(denoisedView, Pos.TOP_CENTER);  // Align image top-left instead of center


        VBox.setVgrow(denoisedStack, Priority.NEVER);

        VBox denoisedContainer = new VBox(5, new Label("Débruitée"), denoisedStack);
        denoisedContainer.getStyleClass().add("image-view-container");
        denoisedContainer.setAlignment(Pos.TOP_CENTER);
        denoisedContainer.setPrefWidth(400);

        originalView.setFitWidth(400);
        originalView.setFitHeight(300);
        originalView.setPreserveRatio(true);
        originalView.setSmooth(true);

        noisyView.setFitWidth(400);
        noisyView.setFitHeight(300);
        noisyView.setPreserveRatio(true);
        noisyView.setSmooth(true);

        denoisedView.setFitWidth(400);
        denoisedView.setFitHeight(300);
        denoisedView.setPreserveRatio(true);
        denoisedView.setSmooth(true);


        imagettesPane.setHgap(10);
        imagettesPane.setVgap(10);
        imagettesPane.setPadding(new Insets(10));
        imagettesPane.setPrefColumns(3);

        imagettesScrollPane.setContent(imagettesPane);
        imagettesScrollPane.setFitToWidth(true);
        imagettesScrollPane.setPrefViewportHeight(200);

        imagetteBlock = new VBox(5, new Label("Imagettes générées:"), imagettesScrollPane);

        patchSizeCombo.setPrefWidth(100);
        patchStepCombo.setPrefWidth(100);

        HBox patchControls = new HBox(10,
            new Label("Taille de patch:"), patchSizeCombo,
            new Label("Pas:"), patchStepCombo,
            extractPatchesButton,
            denoiseButton
        );
        patchControls.setAlignment(Pos.CENTER_LEFT);

        patchesPane.setHgap(5);
        patchesPane.setVgap(5);
        patchesPane.setPadding(new Insets(5));
        patchesPane.setPrefColumns(6);

        patchesScrollPane.setContent(patchesPane);
        patchesScrollPane.setFitToWidth(true);
        patchesScrollPane.setPrefViewportHeight(200);

        decoupeControls = new HBox(10,
            new Label("Nombre de divisions (n):"), divisionsField,
            cutButton
        );
        decoupeControls.setAlignment(Pos.CENTER_LEFT);

        HBox imageContainer = new HBox(10, 
                originalContainer,
                noisyContainer,
                denoisedContainer
        );
        imageContainer.setAlignment(Pos.CENTER);

        HBox topControls = new HBox(10, selectImage, themeToggle);
        topControls.setAlignment(Pos.CENTER_LEFT);

        VBox mainContent = new VBox(15,
            topControls,
            new Label("Niveau de bruit (0-30):"),
            noiseSlider,
            new HBox(10, saveButton),
            decoupeControls,
            errorLabel,
            imageContainer,
            imagetteBlock,
            new Label("Paramètres d'extraction de patchs:"),
            patchControls,
            new Label("Patchs extraits:"),
            patchesScrollPane,
            new Label("Rapport de qualité:"),
            qualityReportArea
        );
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: -fx-base;");

        errorLabel.getStyleClass().add("error-label");
        
        ScrollPane mainScrollPane = new ScrollPane(mainContent);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(mainScrollPane, 1200, 850);
        scene.getStylesheets().add("data:text/css," + css);
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(800);
        
        extractPatchesButton.setOnAction(e -> {
            if (onExtractPatchesRequested != null) {
                onExtractPatchesRequested.run();
            }
        });
    }

    private void setupTheme() {
        applyTheme();
    }

    private void applyTheme() {
        if (darkMode) {
            stage.getScene().getRoot().getStyleClass().add("dark-theme");
        } else {
            stage.getScene().getRoot().getStyleClass().remove("dark-theme");
        }
    }

    public void startProgress() {
        System.out.println("Starting progress...");
        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            denoisedView.setFitWidth(400); // Set initial width
            denoisedView.setFitHeight(300); // Set initial height
        });
    }

    public void completeProgress() {
        System.out.println("Completing progress...");
        Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
        });
    }






    /**
     * Définit les tailles de patch possibles en fonction des dimensions de l'image
     * @param imageWidth Largeur de l'image
     * @param imageHeight Hauteur de l'image
     */
    public void setPossiblePatchSizes(int imageWidth, int imageHeight) {
        patchSizeCombo.getItems().clear();
        patchStepCombo.getItems().clear();
        for (int size = 4; size <= Math.min(imageWidth, imageHeight); size += 4) {
            if (imageWidth % size == 0 && imageHeight % size == 0) {
                patchSizeCombo.getItems().add(size);
            }
        }
        if (!patchSizeCombo.getItems().isEmpty()) {
            patchSizeCombo.getSelectionModel().selectFirst();
        }
        updatePossibleSteps();
        patchSizeCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> updatePossibleSteps()
        );
    }

    /**
     * Met à jour les pas possibles en fonction de la taille de patch sélectionnée
     */
    private void updatePossibleSteps() {
        Integer selectedSize = patchSizeCombo.getValue();
        if (selectedSize == null) return;

        patchStepCombo.getItems().clear();
        for (int step = 1; step <= selectedSize; step++) {
            if (selectedSize % step == 0) {
                patchStepCombo.getItems().add(step);
            }
        }
        patchStepCombo.getSelectionModel().selectFirst();
    }

    /**
     * Affiche la fenêtre principale
     */
    public void show() { stage.show(); }
    
    /**
     * Récupère le nombre de divisions demandé
     * @return Le nombre de divisions
     */
    public int getNombreDivisions() { return Integer.parseInt(divisionsField.getText()); }
    
    /**
     * Récupère la taille de patch sélectionnée
     * @return La taille de patch
     */
    public int getPatchSize() { return patchSizeCombo.getValue() != null ? patchSizeCombo.getValue() : 8; }
    
    /**
     * Récupère le pas de patch sélectionné
     * @return Le pas de patch
     */
    public int getPatchStep() { return patchStepCombo.getValue() != null ? patchStepCombo.getValue() : 4; }
    
    /**
     * Récupère le niveau de bruit sélectionné
     * @return Le niveau de bruit
     */
    public double getNoiseLevel() { return noiseSlider.getValue(); }

    /**
     * Affiche les patchs extraits dans l'interface
     * @param allPatches Liste des patchs à afficher
     */
    public void displayPatches(List<ArrayList<ArrayList<Patch>>> allPatches) {
        patchesPane.getChildren().clear();
        for (List<ArrayList<Patch>> imagettePatches : allPatches) {
            for (ArrayList<Patch> patchList : imagettePatches) {
                for (Patch patch : patchList) {
                    ImageView iv = new ImageView(
                        SwingFXUtils.toFXImage((BufferedImage) patch.getImage(), null));
                    iv.setFitWidth(50);
                    iv.setPreserveRatio(true);
                    Tooltip.install(iv, new Tooltip(
                        "Taille: " + patch.getMatrice().length + "x" + patch.getMatrice()[0].length +
                        "\nPosition: (" + patch.getPremierPixelPos()[0] + "," + patch.getPremierPixelPos()[1] + ")"));
                    patchesPane.getChildren().add(iv);
                }
            }
        }
    }

    // Setters pour les handlers d'événements
    public void setOnExtractPatchesRequested(Runnable handler) { this.onExtractPatchesRequested = handler; }
    public void setOriginalImage(BufferedImage image) { originalView.setImage(SwingFXUtils.toFXImage(image, null)); }
    public void setNoisyImage(BufferedImage image) { noisyView.setImage(SwingFXUtils.toFXImage(image, null)); }
    public void setOnDenoiseRequested(Runnable handler) { this.onDenoiseRequested = handler; }
    public void setDenoisedImage(BufferedImage image) {
        if (image == null) {
            denoisedView.setImage(null);
        } else {
            denoisedView.setImage(SwingFXUtils.toFXImage(image, null));
        }
    }


    /**
     * Affiche les imagettes générées
     * @param imagettes Liste des imagettes à afficher
     */
    public void displayImagettes(List<Imagette> imagettes) {
        imagettesPane.getChildren().clear();
        for (Imagette imagette : imagettes) {
            ImageView iv = new ImageView(SwingFXUtils.toFXImage(imagette.getImage(), null));
            iv.setFitWidth(100);
            iv.setPreserveRatio(true);
            imagettesPane.getChildren().add(iv);
        }
    }

    // Setters pour les handlers d'événements
    public void setOnImageSelected(Consumer<File> handler) { this.onImageSelected = handler; }
    public void setOnNoiseChanged(Consumer<Double> handler) { this.onNoiseChanged = handler; }
    public void setOnSaveRequested(Consumer<File> handler) { this.onSaveRequested = handler; }
    public void setOnCutRequested(Runnable handler) { this.onCutRequested = handler; }

    /**
     * Active/désactive le bouton d'enregistrement
     * @param enabled true pour activer, false pour désactiver
     */
    public void enableSave(boolean enabled) { saveButton.setDisable(!enabled); }
    
    /**
     * Active/désactive le bouton de découpe
     * @param enabled true pour activer, false pour désactiver
     */
    public void enableCut(boolean enabled) { cutButton.setDisable(!enabled); }
    
    /**
     * Active/désactive le bouton de débruitage
     * @param enabled true pour activer, false pour désactiver
     */
    public void enableDenoise(boolean enabled) { denoiseButton.setDisable(!enabled); }

    /**
     * Affiche un message d'erreur
     * @param message Le message d'erreur à afficher
     */
    public void showError(String message) { errorLabel.setText(message); }

    /**
     * Active le mode global de traitement (sans découpe en imagettes)
     */
    public void lancerModeGlobal() {
        decoupeControls.setVisible(false);
        decoupeControls.setManaged(false);
        imagetteBlock.setVisible(false);
        imagetteBlock.setManaged(false);
        cutButton.setDisable(true);
        extractPatchesButton.setDisable(false);
        denoiseButton.setDisable(true);
        showError("Mode global : pas de découpe en imagettes. Choisissez une image, appliquez du bruit, puis extrayez les patchs.");
    }

    /**
     * Active le mode local de traitement (avec découpe en imagettes)
     */
    public void lancerModeLocal() {
        decoupeControls.setVisible(true);
        decoupeControls.setManaged(true);
        imagetteBlock.setVisible(true);
        imagetteBlock.setManaged(true);
        cutButton.setDisable(false);
        extractPatchesButton.setDisable(false);
        denoiseButton.setDisable(true);
        showError("Mode local : découpez en imagettes avant d'extraire les patchs.");
    }

    /**
     * Affiche le rapport de qualité du débruitage
     * @param report Le rapport à afficher
     */
    public void showQualityReport(String report) {
        qualityReportArea.setWrapText(true); 
        qualityReportArea.setText(report);
        // Fixe la hauteur idéale pour afficher 4 lignes
        qualityReportArea.setPrefRowCount(4); 
        qualityReportArea.setPrefWidth(80); 
    }


    /**
     * Récupère l'image débruitée affichée
     * @return L'image débruitée sous forme de BufferedImage
     */
    public BufferedImage getDenoisedImage() {
        javafx.scene.image.Image fxImage = denoisedView.getImage();
        if (fxImage == null) {
            return null;
        }
        return SwingFXUtils.fromFXImage(fxImage, null);
    }
}