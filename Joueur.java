public class Joueur {
    String id;
    String port;
    int posX;
    int posY;
    int point;
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
        this.posX = -1;
        this.posY = -1;
        this.point = 0;
    }

    public int getScore(){
      return this.point;
    }

    public String getId(){
      return this.id;
    }

    public void setNewPos(int x,int y){
      this.posX = x;
      this.posY = y;
    }

    public int getX(){
      return this.posX;
    }

    public int getY(){
      return this.posY;
    }
}
