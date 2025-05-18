package model;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Classe représentant une petite image (imagette) associée à une position dans une image plus grande.
 */
public class Imagette {
    private final BufferedImage image;
    private final Point position;

    /**
     * Constructeur d'une imagette.
     * 
     * @param image    L'image contenue dans l'imagette
     * @param position La position (coordonnées x,y) de l'imagette dans l'image d'origine
     */
    public Imagette(BufferedImage image, Point position) {
        this.image = image;
        this.position = position;
    }

    /**
     * Accesseur pour récupérer l'image de l'imagette.
     * 
     * @return L'objet BufferedImage de l'imagette
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Accesseur pour récupérer la position de l'imagette.
     * 
     * @return Un objet Point représentant la position (x,y)
     */
    public Point getPosition() {
        return position;
    }
}
