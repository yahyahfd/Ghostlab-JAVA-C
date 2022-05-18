import java.util.LinkedList;

public class Partie{
    static int num_bis = 0;
    int num;
    LinkedList<Joueur> j = new LinkedList<Joueur>();
    public Partie(){
        this.num=num_bis;
        num_bis ++;
    }
}
