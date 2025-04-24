package ui;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

public class MainView {
    private final Stage stage;
    private final ImageView originalView = new ImageView();
    private final ImageView noisyView = new ImageView();
    private final Slider noiseSlider = new Slider(0, 100, 20);
    private final Button saveButton = new Button("Enregistrer l'image bruitée");

    private Consumer<File> onImageSelected;
    private Consumer<Double> onNoiseChanged;
    private Consumer<File> onSaveRequested;

    public MainView(Stage stage) {
        this.stage = stage;
        setupUI();
    }

    private void setupUI() {
        stage.setTitle("Image Bruitée");

        Button selectImage = new Button("Choisir une image");
        selectImage.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Sélectionnez une image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png"));
            File file = chooser.showOpenDialog(stage);
            if (file != null && onImageSelected != null) {
                onImageSelected.accept(file);
            }
        });

        noiseSlider.setSnapToTicks(true);
        noiseSlider.setShowTickLabels(true);
        noiseSlider.setShowTickMarks(true);
        noiseSlider.setMin(0);
        noiseSlider.setValue(10); // Valeur par defaut
        noiseSlider.setMax(30);
        noiseSlider.setMajorTickUnit(10);
        noiseSlider.setMinorTickCount(0);
        noiseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (onNoiseChanged != null) {
                onNoiseChanged.accept(newVal.doubleValue());
            }
        });

        saveButton.setDisable(true);
        saveButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Enregistrer l'image bruitée");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
            File file = chooser.showSaveDialog(stage);
            if (file != null && onSaveRequested != null) {
                onSaveRequested.accept(file);
            }
        });

        originalView.setFitWidth(300);
        originalView.setPreserveRatio(true);
        noisyView.setFitWidth(300);
        noisyView.setPreserveRatio(true);

        HBox imageContainer = new HBox(20, originalView, noisyView);
        VBox layout = new VBox(10,
                selectImage,
                new Label("Niveau de bruit :"),
                noiseSlider,
                saveButton,
                imageContainer
        );
        layout.setPadding(new Insets(10));

        stage.setScene(new Scene(layout, 700, 480));
    }

    public void show() {
        stage.show();
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

    public void setOriginalImage(BufferedImage image) {
        originalView.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public void setNoisyImage(BufferedImage image) {
        noisyView.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public double getNoiseLevel() {
        return noiseSlider.getValue();
    }

    public void enableSave(boolean enabled) {
        saveButton.setDisable(!enabled);
    }
}
