package ui;

import javafx.stage.Stage;
import model.*;
import service.*;
import org.apache.commons.math3.linear.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DenoisingController {
    private final MainView view;
    private Pixel[][] matriceOriginale;
    private BufferedImage imageBruitee;
    private BufferedImage imageOriginale;
    private int maxDivisions = 1;
    private List<Imagette> currentImagettes;
    private boolean isModeGlobal = false;
    private VecteurPatch vecteurPatch;
    private double[][][] canauxACP; // [0]=R, [1]=G, [2]=B
    private ACPResult[] acpResults; // Résultats ACP pour chaque canal

    public DenoisingController(MainView view) {
        this.view = view;
        initialize();
    }

    private void initialize() {
        view.setOnImageSelected(this::handleImageSelection);
        view.setOnNoiseChanged(this::updateNoisyImage);
        view.setOnSaveRequested(this::saveNoisyImage);
        view.setOnCutRequested(this::cutImage);
        view.setOnExtractPatchesRequested(this::extractPatches);
        view.setOnDenoiseRequested(this::performDenoising);
    }

    public void setModeGlobal(boolean modeGlobal) {
        this.isModeGlobal = modeGlobal;
        if (modeGlobal) {
            view.lancerModeGlobal();
        } else {
            view.lancerModeLocal();
        }
    }

    private void handleImageSelection(File file) {
        try {
            BufferedImage imageChargee = ImageIO.read(file);
            int largeur = imageChargee.getWidth();
            int hauteur = imageChargee.getHeight();
            
            // Ajuster la taille pour être divisible par 16
            int nouvelleLargeur = largeur - (largeur % 16);
            int nouvelleHauteur = hauteur - (hauteur % 16);
            
            if (nouvelleLargeur != largeur || nouvelleHauteur != hauteur) {
                imageOriginale = resizeImage(imageChargee, nouvelleLargeur, nouvelleHauteur);
                view.showError("Image redimensionnée à " + nouvelleLargeur + "x" + nouvelleHauteur +
                        " pour correspondre à une taille divisible par 16");
            } else {
                imageOriginale = imageChargee;
                view.showError("");
            }

            Photo photo = new Photo(imageOriginale, imageOriginale.getWidth(), imageOriginale.getHeight());
            matriceOriginale = Convert.convertirImageEnMatrice(photo);
            view.setOriginalImage(imageOriginale);
            view.setPossiblePatchSizes(imageOriginale.getWidth(), imageOriginale.getHeight());
            updateNoisyImage(view.getNoiseLevel());
            view.enableSave(true);
            view.enableCut(!isModeGlobal);

            // Calcul du nombre maximum de divisions
            maxDivisions = (imageOriginale.getWidth() / 16) * (imageOriginale.getHeight() / 16);
            if (maxDivisions < 1) maxDivisions = 1;

        } catch (IOException e) {
            view.showError("Erreur de lecture de l'image");
            e.printStackTrace();
        }
    }

    private void updateNoisyImage(double noiseLevel) {
        if (matriceOriginale != null) {
            Pixel[][] matriceBruitee = Bruit.noising(matriceOriginale, noiseLevel);
            imageBruitee = Convert.convertirMatriceEnImage(matriceBruitee);
            view.setNoisyImage(imageBruitee);
        }
    }

    private void saveNoisyImage(File saveFile) {
        if (imageBruitee != null && saveFile != null) {
            try {
                ImageIO.write(imageBruitee, "png", saveFile);
                view.showError("Image sauvegardée avec succès");
            } catch (IOException e) {
                view.showError("Erreur lors de la sauvegarde");
                e.printStackTrace();
            }
        }
    }

    private void extractPatches() {
        int patchSize = view.getPatchSize();
        int step = view.getPatchStep();

        if (isModeGlobal) {
            if (imageBruitee == null) {
                view.showError("Veuillez d'abord charger une image et appliquer du bruit");
                return;
            }

            ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(imageBruitee, step, patchSize);
            List<ArrayList<ArrayList<Patch>>> wrapper = new ArrayList<>();
            wrapper.add(patches);
            view.displayPatches(wrapper);

            vecteurPatch = new VecteurPatch();
            vecteurPatch.ajouterDepuisListe(patches);
            vecteurPatch.afficherExtraits(10);

            // Extraire les canaux pour ACP
            canauxACP = new double[3][][];
            canauxACP[0] = vecteurPatch.getCanal("R");
            canauxACP[1] = vecteurPatch.getCanal("G");
            canauxACP[2] = vecteurPatch.getCanal("B");

            view.enableDenoise(true);
            return;
        }

        // Mode local
        if (currentImagettes == null || currentImagettes.isEmpty()) {
            view.showError("Veuillez d'abord découper l'image en imagettes");
            return;
        }

        List<ArrayList<ArrayList<Patch>>> allPatches = new ArrayList<>();
        
        for (Imagette imagette : currentImagettes) {
            BufferedImage img = imagette.getImage();
            if (img.getWidth() < patchSize || img.getHeight() < patchSize) {
                view.showError("La taille de patch est trop grande pour certaines imagettes");
                return;
            }

            ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(img, step, patchSize);
            allPatches.add(patches);
        }

        view.displayPatches(allPatches);
        view.enableDenoise(true);
    }

    private void performDenoising() {
        if (isModeGlobal) {
            performGlobalDenoising();
        } else {
            performLocalDenoising();
        }
    }

    private void performGlobalDenoising() {
        if (vecteurPatch == null || canauxACP == null) {
            view.showError("Veuillez d'abord extraire les patchs");
            return;
        }

        // 1. Demander le type de seuillage et le nombre de composantes à conserver
        List<String> choices = Arrays.asList("Seuillage dur", "Seuillage doux");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Seuillage dur", choices);
        dialog.setTitle("Choix du seuillage");
        dialog.setHeaderText("Sélectionnez le type de seuillage à appliquer");
        dialog.setContentText("Méthode:");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) return;

        boolean seuillageDur = result.get().equals("Seuillage dur");

        // 2. Calculer les ACP pour chaque canal
        acpResults = new ACPResult[3];
        String[] canaux = {"Rouge", "Vert", "Bleu"};
        
        for (int i = 0; i < 3; i++) {
            acpResults[i] = ACP.appliquerACP(canauxACP[i]);
            System.out.println("ACP appliquée pour le canal " + canaux[i]);
        }

        // 3. Projeter et seuiller pour chaque canal avec sélection adaptative des composantes
        double[][][] alphaCanaux = new double[3][][];
        double[][][] canauxReconstruits = new double[3][][];
        double seuil = 0;
        double energieTotale = 0;
        double energieConservee = 0;
        
        // Calcul de l'énergie totale pour déterminer le nombre de composantes à conserver
        for (int i = 0; i < 3; i++) {
            for (double val : acpResults[i].getValeursPropres()) {
                energieTotale += val;
            }
        }
        
        // On conserve les composantes qui représentent 95% de l'énergie totale
        double energieCible = 0.95 * energieTotale;
        int[] composantesConservees = new int[3];
        
        for (int i = 0; i < 3; i++) {
            double energieCanal = 0;
            for (double val : acpResults[i].getValeursPropres()) {
                energieCanal += val;
            }
            
            double energieCourante = 0;
            int k = 0;
            for (; k < acpResults[i].getValeursPropres().length; k++) {
                energieCourante += acpResults[i].getValeursPropres()[k];
                if (energieCourante >= 0.95 * energieCanal) {
                    break;
                }
            }
            composantesConservees[i] = k + 1; // On conserve k+1 composantes
        }
        
        for (int i = 0; i < 3; i++) {
            // Calcul des contributions (alpha)
            alphaCanaux[i] = Proj.calculerContributions(acpResults[i].getU(), acpResults[i].getVc());
            
            // Calcul du seuil optimal basé sur les composantes non conservées
            double[] allAlpha = new double[alphaCanaux[i].length * composantesConservees[i]];
            int idx = 0;
            for (int k = 0; k < alphaCanaux[i].length; k++) {
                for (int j = 0; j < composantesConservees[i]; j++) {
                    allAlpha[idx++] = alphaCanaux[i][k][j];
                }
            }
            
            double median = calculateMedianAbsolute(allAlpha);
            seuil = median * 0.6745 * 2; // Coefficient plus agressif pour mieux éliminer le bruit
            
            // Appliquer le seuillage uniquement sur les composantes conservées
            double[][] alphaSeuille = new double[alphaCanaux[i].length][alphaCanaux[i][0].length];
            
            for (int k = 0; k < alphaCanaux[i].length; k++) {
                for (int j = 0; j < alphaCanaux[i][k].length; j++) {
                    if (j < composantesConservees[i]) {
                        double a = alphaCanaux[i][k][j];
                        if (seuillageDur) {
                            alphaSeuille[k][j] = Math.abs(a) >= seuil ? a : 0.0;
                        } else {
                            alphaSeuille[k][j] = Math.signum(a) * Math.max(Math.abs(a) - seuil, 0);
                        }
                    } else {
                        alphaSeuille[k][j] = 0; // On élimine les composantes non conservées
                    }
                }
            }
            
            // Reconstruction des vecteurs
            canauxReconstruits[i] = Reconstruction.reconstruireVecteurs(
                alphaSeuille, 
                acpResults[i].getU(), 
                acpResults[i].getmV()
            );
        }

        // 4. Reconstruction de l'image avec moyenne des pixels superposés
        ReconstructionService reconstructionService = new ReconstructionService();
        BufferedImage imageReconstruite = reconstructionService.reconstruireImageDepuisACP(
            canauxReconstruits[0], // R
            canauxReconstruits[1], // G
            canauxReconstruits[2], // B
            vecteurPatch.getVecteurs(),
            imageOriginale.getWidth(),
            imageOriginale.getHeight(),
            view.getPatchSize()
        );

        // 5. Affichage du résultat
        view.setDenoisedImage(imageReconstruite);
        view.showError("Débruitage global terminé. Composantes conservées: R=" + composantesConservees[0] + 
                      ", G=" + composantesConservees[1] + ", B=" + composantesConservees[2]);
    }

    private void performLocalDenoising() {
        if (currentImagettes == null || currentImagettes.isEmpty()) {
            view.showError("Veuillez d'abord découper l'image en imagettes");
            return;
        }

        int patchSize = view.getPatchSize();
        int step = view.getPatchStep();

        // 1. Demander le type de seuillage
        List<String> choices = Arrays.asList("Seuillage dur", "Seuillage doux");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Seuillage dur", choices);
        dialog.setTitle("Choix du seuillage");
        dialog.setHeaderText("Sélectionnez le type de seuillage à appliquer");
        dialog.setContentText("Méthode:");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) return;

        boolean seuillageDur = result.get().equals("Seuillage dur");

        // 2. Préparer l'image reconstruite
        BufferedImage imageReconstruite = new BufferedImage(
            imageOriginale.getWidth(), 
            imageOriginale.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );

        // 3. Traiter chaque imagette séparément
        for (Imagette imagette : currentImagettes) {
            BufferedImage img = imagette.getImage();
            
            // Extraire les patchs de l'imagette
            ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(img, step, patchSize);
            VecteurPatch vp = new VecteurPatch();
            vp.ajouterDepuisListe(patches);
            
            // Extraire les canaux
            double[][][] canaux = new double[3][][];
            canaux[0] = vp.getCanal("R");
            canaux[1] = vp.getCanal("G");
            canaux[2] = vp.getCanal("B");
            
            // Appliquer ACP et seuillage pour chaque canal
            double[][][] canauxReconstruits = new double[3][][];
            
            for (int i = 0; i < 3; i++) {
                ACPResult resultACP = ACP.appliquerACP(canaux[i]);
                double[][] alpha = Proj.calculerContributions(resultACP.getU(), resultACP.getVc());
                
                // Calcul du nombre de composantes à conserver (95% de l'énergie)
                double energieTotale = Arrays.stream(resultACP.getValeursPropres()).sum();
                double energieCourante = 0;
                int k = 0;
                for (; k < resultACP.getValeursPropres().length; k++) {
                    energieCourante += resultACP.getValeursPropres()[k];
                    if (energieCourante >= 0.95 * energieTotale) {
                        break;
                    }
                }
                int composantesConservees = k + 1;
                
                // Calcul du seuil
                double[] allAlpha = new double[alpha.length * composantesConservees];
                int idx = 0;
                for (int m = 0; m < alpha.length; m++) {
                    for (int n = 0; n < composantesConservees; n++) {
                        allAlpha[idx++] = alpha[m][n];
                    }
                }
                
                double median = calculateMedianAbsolute(allAlpha);
                double seuil = median * 0.6745 * 1.5; // Seuil un peu moins agressif pour le local
                
                // Appliquer le seuillage
                double[][] alphaSeuille = new double[alpha.length][alpha[0].length];
                
                for (int m = 0; m < alpha.length; m++) {
                    for (int n = 0; n < alpha[m].length; n++) {
                        if (n < composantesConservees) {
                            double a = alpha[m][n];
                            if (seuillageDur) {
                                alphaSeuille[m][n] = Math.abs(a) >= seuil ? a : 0.0;
                            } else {
                                alphaSeuille[m][n] = Math.signum(a) * Math.max(Math.abs(a) - seuil, 0);
                            }
                        } else {
                            alphaSeuille[m][n] = 0; // Éliminer les composantes non conservées
                        }
                    }
                }
                
                canauxReconstruits[i] = Reconstruction.reconstruireVecteurs(
                    alphaSeuille, 
                    resultACP.getU(), 
                    resultACP.getmV()
                );
            }
            
            // Reconstruction de l'imagette
            ReconstructionService reconstructionService = new ReconstructionService();
            BufferedImage imagetteReconstruite = reconstructionService.reconstruireImageDepuisACP(
                canauxReconstruits[0], // R
                canauxReconstruits[1], // G
                canauxReconstruits[2], // B
                vp.getVecteurs(),
                img.getWidth(),
                img.getHeight(),
                patchSize
            );
            
            // Copier l'imagette reconstruite dans l'image finale
            Point position = imagette.getPosition();
            Graphics2D g = imageReconstruite.createGraphics();
            g.drawImage(imagetteReconstruite, position.x, position.y, null);
            g.dispose();
        }

        // 4. Affichage du résultat
        view.setDenoisedImage(imageReconstruite);
        view.showError("Débruitage local terminé (" + (seuillageDur ? "seuillage dur" : "seuillage doux") + ")");
    }

    private double calculateMedianAbsolute(double[] values) {
        double[] absValues = Arrays.stream(values)
            .map(Math::abs)
            .toArray();
        Arrays.sort(absValues);
        
        if (absValues.length % 2 == 0) {
            return (absValues[absValues.length/2] + absValues[absValues.length/2 - 1]) / 2.0;
        } else {
            return absValues[absValues.length/2];
        }
    }

    private void cutImage() {
        try {
            if (imageOriginale == null) {
                view.showError("Aucune image chargée !");
                return;
            }

            int divisions = view.getNombreDivisions();

            if (divisions < 1) {
                view.showError("Le nombre de divisions doit être ≥ 1");
                return;
            }

            if (divisions > maxDivisions) {
                view.showError("Nombre de divisions trop grand (max " + maxDivisions + ")");
                return;
            }

            Photo photo = new Photo(imageBruitee, imageOriginale.getWidth(), imageOriginale.getHeight());
            List<Imagette> imagettes = DecoupeImage.decoupeImage(photo, divisions);
            currentImagettes = imagettes;

            if (!imagettes.isEmpty()) {
                int width = imagettes.get(0).getImage().getWidth();
                int height = imagettes.get(0).getImage().getHeight();
                view.setPossiblePatchSizes(width, height);
            }

            view.displayImagettes(imagettes);
            view.enableDenoise(false); // Disable denoise until patches are extracted
            view.showError("Découpe réussie : " + divisions + " imagettes");

            // Enable extract patches button after cutting
            view.enableExtractPatches(true);
        } catch (Exception e) {
            view.showError("Erreur lors de la découpe");
            e.printStackTrace();
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }
}



package ui;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import model.Imagette;
import model.Patch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class MainView {
    private final Stage stage;
    private final ImageView originalView = new ImageView();
    private final ImageView noisyView = new ImageView();
    private final ImageView denoisedView = new ImageView();
    private final Slider noiseSlider = new Slider(0, 100, 30);
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
    private final Button denoiseButton = new Button("Débruiter l'image");
    private final TilePane patchesPane = new TilePane();
    private final ScrollPane patchesScrollPane = new ScrollPane();
    private HBox decoupeControls;
    private Runnable onExtractPatchesRequested;
    private Consumer<File> onImageSelected;
    private Consumer<Double> onNoiseChanged;
    private Consumer<File> onSaveRequested;
    private Runnable onCutRequested;
    private Runnable onDenoiseRequested;

    public MainView(Stage stage) {
        this.stage = stage;
        setupUI();
    }

    private void setupUI() {
        stage.setTitle("Débruitage d'images par ACP");

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

        originalView.setFitWidth(300);
        originalView.setFitHeight(250);
        originalView.setPreserveRatio(true);
        originalView.setSmooth(true);

        noisyView.setFitWidth(300);
        noisyView.setFitHeight(250);
        noisyView.setPreserveRatio(true);
        noisyView.setSmooth(true);

        denoisedView.setFitWidth(300);
        denoisedView.setFitHeight(250);
        denoisedView.setPreserveRatio(true);
        denoisedView.setSmooth(true);

        imagettesPane.setHgap(10);
        imagettesPane.setVgap(10);
        imagettesPane.setPadding(new Insets(10));
        imagettesPane.setPrefColumns(3);

        imagettesScrollPane.setContent(imagettesPane);
        imagettesScrollPane.setFitToWidth(true);
        imagettesScrollPane.setPrefViewportHeight(200);
        imagettesScrollPane.setStyle("-fx-background: white; -fx-border-color: lightgray;");

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
        patchesScrollPane.setStyle("-fx-background: white; -fx-border-color: lightgray;");

        decoupeControls = new HBox(10,
            new Label("Nombre de divisions (n):"), divisionsField,
            cutButton
        );
        decoupeControls.setAlignment(Pos.CENTER_LEFT);

        HBox imageContainer = new HBox(10, 
            new VBox(5, new Label("Originale"), originalView),
            new VBox(5, new Label("Bruitée"), noisyView),
            new VBox(5, new Label("Débruitée"), denoisedView)
        );
        imageContainer.setAlignment(Pos.CENTER);

        VBox mainContent = new VBox(10,
            selectImage,
            new Label("Niveau de bruit:"),
            noiseSlider,
            new HBox(10, saveButton),
            decoupeControls,
            errorLabel,
            imageContainer,
            imagetteBlock,
            new Label("Paramètres d'extraction de patchs:"),
            patchControls,
            new Label("Patchs extraits:"),
            patchesScrollPane
        );
        mainContent.setPadding(new Insets(15));

        ScrollPane mainScrollPane = new ScrollPane(mainContent);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        stage.setScene(new Scene(mainScrollPane, 1000, 800));
        stage.setMinWidth(1000);
        stage.setMinHeight(800);
    }

    public void setPossiblePatchSizes(int imageWidth, int imageHeight) {
        patchSizeCombo.getItems().clear();
        patchStepCombo.getItems().clear();
        
        // Taille maximale de patch = 1/4 de la plus petite dimension
        int maxSize = Math.min(imageWidth, imageHeight) / 4;
        maxSize = maxSize - (maxSize % 4); // S'assurer que c'est divisible par 4
        
        for (int size = 4; size <= maxSize; size += 4) {
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

    public void show() { stage.show(); }
    public int getNombreDivisions() { 
        try {
            return Integer.parseInt(divisionsField.getText());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
    public int getPatchSize() { return patchSizeCombo.getValue() != null ? patchSizeCombo.getValue() : 8; }
    public int getPatchStep() { return patchStepCombo.getValue() != null ? patchStepCombo.getValue() : 4; }

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

    public void setOnExtractPatchesRequested(Runnable handler) { this.onExtractPatchesRequested = handler; }
    public void setOnDenoiseRequested(Runnable handler) { this.onDenoiseRequested = handler; }
    public double getNoiseLevel() { return noiseSlider.getValue(); }
    public void setOriginalImage(BufferedImage image) { originalView.setImage(SwingFXUtils.toFXImage(image, null)); }
    public void setNoisyImage(BufferedImage image) { noisyView.setImage(SwingFXUtils.toFXImage(image, null)); }
    public void setDenoisedImage(BufferedImage image) { denoisedView.setImage(SwingFXUtils.toFXImage(image, null)); }

    public void displayImagettes(List<Imagette> imagettes) {
        imagettesPane.getChildren().clear();
        for (Imagette imagette : imagettes) {
            ImageView iv = new ImageView(SwingFXUtils.toFXImage(imagette.getImage(), null));
            iv.setFitWidth(100);
            iv.setPreserveRatio(true);
            imagettesPane.getChildren().add(iv);
        }
    }

    public void setOnImageSelected(Consumer<File> handler) { this.onImageSelected = handler; }
    public void setOnNoiseChanged(Consumer<Double> handler) { this.onNoiseChanged = handler; }
    public void setOnSaveRequested(Consumer<File> handler) { this.onSaveRequested = handler; }
    public void setOnCutRequested(Runnable handler) { this.onCutRequested = handler; }

    public void enableSave(boolean enabled) { saveButton.setDisable(!enabled); }
    public void enableCut(boolean enabled) { cutButton.setDisable(!enabled); }
    public void enableDenoise(boolean enabled) { denoiseButton.setDisable(!enabled); }
    public void enableExtractPatches(boolean enabled) {
        extractPatchesButton.setDisable(!enabled);
    }

    public void showError(String message) { errorLabel.setText(message); }

    public void lancerModeGlobal() {
        decoupeControls.setVisible(false);
        imagetteBlock.setVisible(false);
        cutButton.setDisable(true);
        extractPatchesButton.setDisable(false); // Enable for global mode
        denoiseButton.setDisable(true);
        showError("Mode global : extraire les patchs directement de l'image");
    }

    public void lancerModeLocal() {
        decoupeControls.setVisible(true);
        imagetteBlock.setVisible(true);
        cutButton.setDisable(false);
        extractPatchesButton.setDisable(false); // Enable for local mode after cutting
        denoiseButton.setDisable(true);
        showError("Mode local : découper l'image en imagettes d'abord");
    }
}



package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainApp extends Application {

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

            // ✅ On définit ici le bon mode dans le contrôleur, qui appellera lui-même la bonne méthode dans la vue
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
