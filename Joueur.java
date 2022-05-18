public class Joueur {
    String id;
    String port;
    int num_partie = -1; //-1 dans aucune partie
    boolean ready = false;

    public Joueur(String i,String p){//on complète l'id par des #, si trop long on coupe, normalment le nom est de taille 8 dès le départ
        String new_id = i;
        for(int x = new_id.length();x<8;x++){
            new_id = new_id +"#";
        }
        if(new_id.length()>8){
            this.id = new_id.substring(0, 8);
        }else{
            this.id = new_id;
        }
        this.port=p;
    }
}
