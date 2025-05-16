package model;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Imagette {
    private BufferedImage image;
    private Point position;

    public Imagette(BufferedImage image, Point position) {
        this.image = image;
        this.position = position;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Point getPosition() {
        return position;
    }
}
	