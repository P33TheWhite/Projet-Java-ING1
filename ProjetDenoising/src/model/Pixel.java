package model;

public class Pixel {
    private int rouge;
    private int vert;
    private int bleu;;
    private boolean estSuperpose;

    public Pixel(int rouge, int vert, int bleu) {
        this.rouge = clamp(rouge);
        this.vert = clamp(vert);
        this.bleu = clamp(bleu);
    }

    public int getRouge() {
        return rouge;
    }

    public int getVert() {
        return vert;
    }

    public int getBleu() {
        return bleu;
    }

    public void setRouge(int rouge) {
        this.rouge = clamp(rouge);
    }

    public void setVert(int vert) {
        this.vert = clamp(vert);
    }

    public void setBleu(int bleu) {
        this.bleu = clamp(bleu);
    }
    
    public void setestSuperpose(boolean estSuperpose){
    	this.estSuperpose=estSuperpose;
    }
    
    public boolean getestSuperpose(){
    	return estSuperpose;
    }

    public int toRGB() {
        return (rouge << 16) | (vert << 8) | bleu;
    }

    public static Pixel fromRGB(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return new Pixel(r, g, b);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}