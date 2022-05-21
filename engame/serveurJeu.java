import java.net.*;
import java.util.LinkedList;
import java.io.*;


public class serveurJeu{
  static LinkedList<Partie> l = new LinkedList<Partie>();
  Partie p = new Partie(4);
  Joueur test = new Joueur("AZERTYUI","4242"); //Joueur test

  public serveurJeu(){
    }

    public void EnJeu(Socket s) throws IOException{
      try{
        InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
        BufferedReader br=new BufferedReader(new InputStreamReader(in));
        System.out.println("connected");//tmp
        boolean iquit_req = false;
        System.out.println(!end_of_Game(p,iquit_req));
        while(!end_of_Game(p,iquit_req)){
          System.out.println(!end_of_Game(p,iquit_req));
          byte[] req_input=readBytes(in,5);//requete
          // String requete = new String(req_input);
          String requete = byteToString(req_input);
          System.out.println(requete);//tmp
          byte[] bytes;

          switch(requete){
            case "UPMOV":

            case "DOMOV":

            case "LEMOV":

            case "RIMOV":
              System.out.println("requete recu");
              byte[] byteScore,byteX,byteY; //point et coord du joueur

              readBytes(in,1);//space
              byte[] d_input=readBytes(in,3); //distance
              int distance = Integer.parseInt(byteToString(d_input));

              System.out.println(byteToString(readBytes(in,3))); //etoiles
              boolean touchGhost = movebis(p.getLab(),test,distance,requete);   //deplacmement du joueur en recuperant le boolean indiquant si il a croisé un fantome
              System.out.println("touché ou pas : "+ touchGhost);
              byteX = intToByte(test.getX(),3);
              byteY = intToByte(test.getY(),3);
              if (touchGhost) {
                byteScore = intToByte(test.getScore(),4); //convertion du score en bytes
                bytes = new byte[]{
                'M','O','V','E','F',' ',byteX[2],byteX[1],byteX[0],' ',byteY[2],byteY[1],byteY[0],' ',byteScore[3],byteScore[2],byteScore[1],byteScore[0],'*','*','*'//[MOVEF␣x␣y␣p***]
                };
                System.out.println("Ghost touched!");
              }else{
                bytes = new byte[]{
                'M','O','V','E','!',' ',byteX[2],byteX[1],byteX[0],' ',byteY[2],byteY[1],byteY[0],'*','*','*'   // [MOVE!␣x␣y***]
                };
                System.out.println("Moved!");
              }
              out.write(bytes);
              pw.flush();
              break;
            case "IQUIT":
              readBytes(in,3); //etoiles
              bytes = new byte[]{
                'G','O','B','Y','E','*','*','*'
              };
              out.write(bytes);
              pw.flush();
              iquit_req = true;
              p.j.remove(test);
              // DECONNECTION
              break;
            case "GLIS?":
              readBytes(in,3); //etoiles
              bytes = new byte[]{
                'G','L','I','S','!',' ',(byte)(p.getPlayers()) ,'*','*','*'     //[GLIS!␣s***]
              };
              out.write(bytes);
              pw.flush();
              for (Joueur j : p.getJoueurs()) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );     //[GPLYR␣id␣x␣y␣p***] s fois
                outputStream.write("GPLYR ".getBytes());
                outputStream.write((j.getId()+" ").getBytes());
                outputStream.write((j.getX()+" ").getBytes());
                outputStream.write((j.getY()+" ").getBytes());
                outputStream.write(String.valueOf(j.getScore()).getBytes());
                outputStream.write("***".getBytes());
                bytes = outputStream.toByteArray();
                out.write(bytes);
                pw.flush();
              }
              break;
            case "MALL?":
              System.out.println("Not coded yet");
              break;
            case "SEND?":
              System.out.println("Not coded yet");
              break;
          }
        }


        br.close();
        pw.close();
        s.close();
      } catch (Exception e) {
          System.out.println(e);
          e.printStackTrace();
      }



    }

    // public boolean move(Labyrinthe maze,Joueur j,int distance, String direction){ // boolean pour savoir si croiser fantome
    //
    //   Case[][] l = maze.getMaze();
    //   int tmp = distance;
    //   System.out.println("debut move, distance : "+tmp+" coordonnées : "+j.getX()+" "+j.getY());
    //   boolean touch = false;
    //   switch (direction) {
    //     case "UPMOV":
    //       System.out.println("upmove avant while");
    //       while ((tmp != 0  || !(l[j.getX()-1][j.getY()] instanceof Mur)) && inBounds(j.getX()-1,j.getY(),maze)) {
    //         j.setNewPos(j.getX()-1,j.getY());
    //         System.out.println("nouvelle pos : "+ j.getX()+" "+j.getY());
    //         tmp--;
    //         for (Fantome f : p.fantomes) {
    //           if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
    //         }
    //       }
    //       break;
    //     case "DOMOV":
    //       while ((tmp != 0  || !(l[j.getX()+1][j.getY()] instanceof Mur)) && inBounds(j.getX()+1,j.getY(),maze)) {
    //         j.setNewPos(j.getX()+1,j.getY());
    //         System.out.println("nouvelle pos : "+ j.getX()+" "+j.getY());
    //         tmp--;
    //         for (Fantome f : p.fantomes) {
    //           if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
    //         }
    //       }
    //       break;
    //     case "LEMOV":
    //       while ((tmp != 0  || !(l[j.getX()][j.getY()-1] instanceof Mur))&& inBounds(j.getX(),j.getY()-1,maze)) {
    //         j.setNewPos(j.getX(),j.getY()-1);
    //         System.out.println("nouvelle pos : "+ j.getX()+" "+j.getY());
    //         tmp--;
    //         for (Fantome f : p.fantomes) {
    //           if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
    //         }
    //       }
    //       break;
    //     case "RIMOV":
    //       while ((tmp != 0  || !(l[j.getX()][j.getY()+1] instanceof Mur))&& inBounds(j.getX(),j.getY()+1,maze)) {
    //         j.setNewPos(j.getX(),j.getY()+1);
    //         System.out.println("nouvelle pos : "+ j.getX()+" "+j.getY());
    //         tmp--;
    //         for (Fantome f : p.fantomes) {
    //           if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
    //         }
    //       }
    //       break;
    //   }
    //   j.incrScore(10);  //+10 points (à changer)
    //   return touch;  //boolean indiquant si fantome croisé
    //
    // }
    public boolean movebis(Labyrinthe maze,Joueur j,int distance, String direction){
      Case[][] l = maze.getMaze();

      System.out.println("debut move, distance : "+distance+" coordonnées : "+j.getX()+" "+j.getY());
      boolean touch = false;
      switch (direction) {
        case "UPMOV":
          System.out.println("distance : "+ distance);
          for (int i=0;i<distance ;i++ ) {
            if (!(l[j.getX()-1][j.getY()] instanceof Mur) && inBounds(j.getX()-1,j.getY(),maze)) {
              j.setNewPos(j.getX()-1,j.getY());
              System.out.println("nouvelle pos : "+ j.getX()+" "+j.getY());
            }
            for (Fantome f : p.fantomes) {
              if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
            }
          }
          break;
        case "DOMOV":
          System.out.println("distance : "+ distance);
          for (int i=0;i<distance ;i++ ) {
            if (!(l[j.getX()+1][j.getY()] instanceof Mur) && inBounds(j.getX()+1,j.getY(),maze)) {
              j.setNewPos(j.getX()+1,j.getY());
              System.out.println("nouvelle pos : "+ j.getX()+" "+j.getY());
            }
            for (Fantome f : p.fantomes) {
              if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
            }
          }
          break;
        case "LEMOV":
          System.out.println("distance : "+ distance);
          for (int i=0;i<distance ;i++ ) {
            if (!(l[j.getX()][j.getY()-1] instanceof Mur) && inBounds(j.getX(),j.getY()-1,maze)) {
              j.setNewPos(j.getX(),j.getY()-1);
              System.out.println("nouvelle pos : "+ j.getX()+" "+j.getY());
            }
            for (Fantome f : p.fantomes) {
              if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
            }
          }
          break;
        case "RIMOV":
          System.out.println("distance : "+ distance);
          for (int i=0;i<distance ;i++ ) {
            if (!(l[j.getX()][j.getY()+1] instanceof Mur) && inBounds(j.getX(),j.getY()+1,maze)) {
              j.setNewPos(j.getX(),j.getY()+1);
              System.out.println("nouvelle pos : "+ j.getX()+" "+j.getY());
            }
            for (Fantome f : p.fantomes) {
              if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
            }
          }
          break;
      }
      j.incrScore(10);  //+10 points (à changer)
      return touch;  //boolean indiquant si fantome croisé
    }


    public boolean end_of_Game(Partie p,boolean deco){
      boolean res = false;
      if ((p.getFantomes().isEmpty() || p.getPlayers()==0) || deco) {
        res = true;
      }
      return res ;
    }

    public static String byteToString(byte[] b){
      String result = "";
      for(int i=0; i< b.length ; i++) {
        result += (char) (b[i] & 0xFF);
      }
      return result;
    }

    public static byte[] intToByte(int myint,int nbOfBytes){
      byte[] bytes = new byte[nbOfBytes];
      for (int i = nbOfBytes-1; i >= 0; i--) {
        bytes[i] = (byte)(myint >>> (i * 8));
      }
      return bytes;
    }
    public static byte[] readBytes(InputStream is, int bytesToRead) throws IOException{
      int result = 0;
      int bytesRead = 0;
      byte[] input=new byte[bytesToRead];
      while(bytesRead < bytesToRead){
        result = is.read(input,bytesRead,bytesToRead - bytesRead);
        if(result == -1) break;
          bytesRead = bytesRead + result;
        }
        return input;
    }

    public boolean inBounds(int x, int y,Labyrinthe l){
      return ((x >= 0) && (x < l.getHauteur()) && (y >= 0) && (y < l.getWidth())) ;
    }
    public static void main(String [] args){

        if(args.length != 1){
            System.out.println("Port expected here");
        }else{
          try{
            ServerSocket server=new ServerSocket(Integer.parseInt(args[0]));
            while(true){
                Socket socket=server.accept();
                serveurJeu j = new serveurJeu();

                j.EnJeu(socket);
            }
          } catch (Exception e) {
              System.out.println(e);
              e.printStackTrace();
          }


        }
    }


}
