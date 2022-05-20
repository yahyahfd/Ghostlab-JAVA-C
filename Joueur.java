public class Joueur {
  String id;
  String port;
  int num_partie = -1; //-1 dans aucune partie
  boolean ready = false;
  int point,posx,posy;

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
      this.point = 0;
      this.posx = 1; //A modifier en placant les joueurs au début de la partie
      this.posy = 1;
  }

  public void reinitialize_Score(){
    this.point = 0;
  }

  public void incrScore(int s){
    this.point+=s;
  }

  public int getScore(){
    return this.point;
  }

  public void setNewPos(int x,int y){
    this.posx = x;
    this.posy = y;
  }

  public int getX(){
    return this.posx;
  }

  public int getY(){
    return this.posy;
  }

}
