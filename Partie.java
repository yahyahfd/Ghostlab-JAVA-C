import java.util.LinkedList;

public class Partie{
    static int num_bis = 0;
    int num;
    LinkedList<Joueur> j;
    Labyrinthe l;
    LinkedList<Fantome> fantomes;

    public Partie(){
      this.l = new Labyrinthe();
      this.fantomes = new LinkedList<Fantome>();
      this.j = new LinkedList<Joueur>();
      this.num=num_bis;
      num_bis ++;
    }

    public int getPlayers(){
      return this.j.size();
    }

    public void reinitializeAllScore(){
      for (int i=0;i<this.j.size() ;i++ ) {
        this.j.get(i).reinitialize_Score();
      }
    }

    public Labyrinthe getLab(){
      return this.l;
    }

    public LinkedList<Joueur> getJoueurs(){
      return this.j;
    }
}
