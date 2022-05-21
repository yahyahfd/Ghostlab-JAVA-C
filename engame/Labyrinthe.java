import java.util.*;

public class Labyrinthe{
	private int hauteur;
	private int width;
	public Case[][] maze;

	public Labyrinthe(){ //0: Vide 1: Mur 2: Joueur 3: Fantome
		this.hauteur = 6;
		this.width = 7;
		this.maze = new Case[hauteur][width];
		maze[0][0] = new Mur(); maze[1][0] =  new Mur(); maze[2][0] =  new Mur(); maze[4][0] =  new Mur(); maze[5][0] =  new Mur();
		maze[5][1] =  new Mur(); maze[0][2] =  new Mur(); maze[1][2] =  new Mur(); maze[2][2] =  new Mur(); maze[3][2] =  new Mur(); maze[3][2] =  new Mur();
		maze[5][2] =  new Mur(); maze[0][3] =  new Mur(); maze[2][3] =  new Mur(); maze[5][3] =  new Mur(); maze[0][4] =  new Mur(); maze[5][4] =  new Mur();
		maze[0][5] =  new Mur(); maze[2][5] =  new Mur(); maze[3][5] =  new Mur(); maze[0][6] =  new Mur(); maze[2][6] =  new Mur(); maze[3][6] =  new Mur();
		maze[4][6] =  new Mur(); maze[5][6] =  new Mur();
	}

	public int getHauteur(){
		return this.hauteur;
	}

	public int getWidth(){
		return this.width;
	}

	public Case[][] getMaze(){
		return this.maze;
	}

	public int nbCaseVide(){
		int ret = 0;
		for(int i = 0; i<this.maze.length;i++){
			for(int j = 0;j<this.maze[i].length;j++){
				if(maze[i][j] == null){
					ret++;
				}
			}
		}
		return ret;
	}

	public LinkedList<Fantome> placerFantome(int nbFantome){ //remplit le labyrinthe de fantome et renvoie une liste les enumerant
		if (nbFantome > (this.nbCaseVide()/2)){ //on met une limite de fantome correspondant a la moitié du nombre de cases vide
			nbFantome = this.nbCaseVide()/2;
		}
		LinkedList<Fantome> fantomes = new LinkedList<Fantome>();
		Random r = new Random();
		for(int i=0;i<nbFantome;i++){
			while(true){
				int x = r.nextInt(this.width);
				int y = r.nextInt(this.hauteur);
				if(maze[y][x] == null){
					fantomes.add(new Fantome(x,y));
					break;
				}
			}
		}
		return fantomes;
	}
}
