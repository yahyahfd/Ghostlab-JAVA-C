import java.util.LinkedList;

public class Partie{
    static int num_bis = 0;
    int num;
    LinkedList<Joueur> j;
    Labyrinthe l;
    LinkedList<Fantome> fantomes;

    public Partie(int f){
      this.l = new Labyrinthe();
      this.fantomes = this.l.placerFantome(f);
      this.j = new LinkedList<Joueur>();
      this.j.add(new Joueur("AZERTYUI","4242"));
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

    public LinkedList<Fantome> getFantomes(){
      return this.fantomes;
    }

    public LinkedList<Joueur> getJoueurs(){
      return this.j;
    }
}
