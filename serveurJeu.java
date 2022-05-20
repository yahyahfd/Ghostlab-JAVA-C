import java.net.*;
import java.util.LinkedList;
import java.io.*;

public class serveurJeu implements Runnable{
  Socket socket;
  LinkedList<Partie> parties = new LinkedList<Partie>();
  Partie p = new Partie();
  p.fantomes = p.getLab().placerFantome(4); //4 fantomes pour le test
  Joueur test = new Joueur("AZERTYUI","7777"); //Joueur test

  public serveurJeu(Socket s){
        this.socket = s;
    }

    @Override
    public void run() {
        try {
          InputStream in = this.socket.getInputStream();
          OutputStream out = this.socket.getOutputStream();
          PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
          BufferedReader br=new BufferedReader(new InputStreamReader(in));

          byte[] req_input=readBytes(in,5); //requete
          String requete = byteToString(req_input);
          byte[] bytes;

          switch(requete){
            case "UPMOV":

            case "DOMOV":

            case "LEMOV":

            case "RIMOV":

              byte[] byteScore,byteX,byteY; //point et coord du joueur

              readBytes(in,1);//space
              byte[] d_input=readBytes(in,3); //distance
              int distance = Integer.parseInt(byteToString(d_input));
              readBytes(in,3); //etoiles
              boolean touchGhost = move(p.getLab(),test,distance,requete);   //deplacmement du joueur en recuperant le boolean indiquant si il a croisé un fantome

              byteX = intToByte(test.getX(),3);
              byteY = intToByte(test.getY(),3);
              if (touchGhost) {
                byteScore = intToByte(test.getScore(),4); //convertion du score en bytes
                bytes = new byte[]{
                'M','O','V','E','F',' ',byteX[2],byteX[1],byteX[0],' ',byteY[2],byteY[1],byteY[0],' ',byteScore[3],byteScore[2],byteScore[1],byteScore[0],'*','*','*'//[MOVEF␣x␣y␣p***]
                };
              }else{
                bytes = new byte[]{
                'M','O','V','E','!',' ',byteX[2],byteX[1],byteX[0],' ',byteY[2],byteY[1],byteY[0],'*','*','*'   // [MOVE!␣x␣y***]
                };
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
              // DECONNECTION ?
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
                outputStream.write(j.getId()+" ".getBytes());
                outputStream.write(j.getX()+" ".getBytes());
                outputStream.write(j.getY()+" ".getBytes());
                outputStream.write(j.getScore().getBytes());
                outputStream.write("***".getBytes());
                bytes = outputStream.toByteArray();
                out.write(bytes);
                pw.flush();
              }
              break;
            case "MALL?":
              break;
            case "SEND?":
              break;
          }

          br.close();
          pw.close();
          socket.close();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }

    public boolean move(Labyrinthe maze,Joueur j,int distance, String direction){ // boolean pour savoir si croiser
      Case[][] l = maze.getMaze();
      int tmp = distance;
      boolean touch = false;
      switch (direction) {
        case "UPMOV":
          while (tmp != 0  || !(l[j.getX()][j.getY()-1] instanceof Mur)) {
            j.setNewPos(j.getX(),j.getY()-1);
            tmp--;
            for (Fantome f : p.fantomes) {
              if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
            }
          }
          break;
        case "DOMOV":
          while (tmp != 0  || !(l[j.getX()][j.getY()+1] instanceof Mur)) {
            j.setNewPos(j.getX(),j.getY()+1);
            tmp--;
            for (Fantome f : p.fantomes) {
              if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
            }
          }
          break;
        case "LEMOV":
          while (tmp != 0  || !(l[j.getX()-1][j.getY()] instanceof Mur)) {
            j.setNewPos(j.getX()-1,j.getY());
            tmp--;
            for (Fantome f : p.fantomes) {
              if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
            }
          }
          break;
        case "RIMOV":
          while (tmp != 0  || !(l[j.getX()+1][j.getY()] instanceof Mur)) {
            j.setNewPos(j.getX()+1,j.getY());
            tmp--;
            for (Fantome f : p.fantomes) {
              if(j.getX() == f.getX() && j.getY() == f.getY()) touch = true;
            }
          }
          break;
      }
      j.incrScore(10);  //+10 points (à changer)
      return touch;  //boolean indiquant si fantome croisé

    }

    public boolean end_of_Game(int m){
      boolean res = false;
      if (p.fantomes.isEmpty() || parties.get(m).getPlayers()==0) {
        res = true;
      }
      return res;
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


}
