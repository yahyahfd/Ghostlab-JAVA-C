import java.util.LinkedList;

public class Partie{
    static int num_bis = 0;
    int num;
    LinkedList<Joueur> j = new LinkedList<Joueur>();
    LinkedList<Fantome> fantomes = new LinkedList<Fantome>();
    Labyrinthe labyrinthe = new Labyrinthe();
    boolean ready = false;
    
    public void addToPartie(Joueur player){
        synchronized(j){
            j.add(player);
            player.num_partie = num;
        }
    }

    public Partie(){
        this.num=num_bis;
        num_bis ++;
    }
}
