package ui;

import javafx.stage.Stage;
import model.Pixel;
import service.Bruit;
import service.Convert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DenoisingController {

    private final MainView view;
    private Pixel[][] matriceOriginale;
    private BufferedImage imageBruitee;

    public DenoisingController(Stage stage) {
        this.view = new MainView(stage);
    }

    public void initialize() {
        view.setOnImageSelected(this::handleImageSelection);
        view.setOnNoiseChanged(this::updateNoisyImage);
        view.setOnSaveRequested(this::saveNoisyImage);
        view.show();
    }

    private void handleImageSelection(File file) {
        try {
            BufferedImage imageOriginale = ImageIO.read(file);
            matriceOriginale = Convert.convertirImageEnMatrice(file.getAbsolutePath());
            view.setOriginalImage(imageOriginale);
            updateNoisyImage(view.getNoiseLevel());
            view.enableSave(true);
        } catch (IOException e) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
