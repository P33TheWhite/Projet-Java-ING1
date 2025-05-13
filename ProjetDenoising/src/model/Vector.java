package model;

public class Vector {
	private Pixel[] matrice;
	private int s2;
	private int[] premierPixelPos;
	
	public Vector(Pixel[][] matrice, int s, int[] premierPixelPos) throws IllegalArgumentException {
		
	    if (matrice.length != s || matrice[0].length != s) {
	        throw new IllegalArgumentException("La matrice doit être carrée de taille s × s.");
	    }
		
		int index = 0;
		this.s2 = s*s;
		this.premierPixelPos = premierPixelPos;
		this.matrice = new Pixel[this.s2];
		for (int i = 0; i < s; i++) {
			for(int j = 0; j < s; j++) {
				this.matrice[index] = matrice[i][j] ;
				index++;
			}
		}
	}
	
	public Pixel[] getMatrice() {
		return this.matrice;
	}
	
	public int getS2() {
		return this.s2;
	}
	
	public int[] getPremierPixelPos() {
		return this.premierPixelPos;
	}
}
