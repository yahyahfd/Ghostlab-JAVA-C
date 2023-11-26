import java.net.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.io.*;

public class serveurJeu implements Runnable{
    Socket socket;
    static LinkedList<Partie> l = new LinkedList<Partie>();

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
            showGames(out, pw);
            Joueur j = null;
            int max_player = 2;
            boolean ready = false;
            while(ready==false){ // On ne reçoit que des requêtes valides (tests dans le client lors d'envois)
                byte[] req_input=readBytes(in,5); //requete
                String requete = byteToString(req_input);
                if(requete.equals("NEWPL")){
                    readBytes(in,1);//space
                    byte[] id_input=readBytes(in,8);
                    readBytes(in,1);//space
                    byte[] port_input=readBytes(in,4);
                    String id = byteToString(id_input);
                    String port = byteToString(port_input);
                    readBytes(in, 3); //***
                    if(j == null ){
                        j = new Joueur(id,port);
                        Partie tmp = new Partie();
                        if(j.num_partie == -1){
                            tmp.addToPartie(j);
                            addPartie(tmp);
                            byte[] tosend = new byte[]{
                                'R','E','G','O','K',' ',(byte)tmp.num,'*','*','*'
                            };
                            out.write(tosend);
                            pw.flush();
                        }else{
                            byte[] tosend = new byte[]{
                                'R','E','G','N','O','*','*','*'
                            };
                            out.write(tosend);
                            pw.flush();
                            System.out.println("Vous avez déjà rejoint une partie");
                        }
                    }else{
                        byte[] tosend = new byte[]{
                            'R','E','G','N','O','*','*','*'
                        };
                        out.write(tosend);
                        pw.flush();
                        System.out.println("Vous avez déjà rejoint une partie");
                    }
                }else if(requete.equals("REGIS") ){
                    readBytes(in,1);//space
                    byte[] id_input=readBytes(in,8);
                    readBytes(in,1);//space
                    byte[] port_input=readBytes(in,4);
                    readBytes(in,1);//space
                    String id = byteToString(id_input);
                    String port = byteToString(port_input);
                    byte[] game_n  = readBytes(in, 1);
                    int game_n_i = game_n[0];
                    readBytes(in, 3);
                    if(j == null ){
                        j = new Joueur(id,port);
                        Partie tmp;
                        ListIterator <Partie> tmpl = l.listIterator();
                        while(tmpl.hasNext()){
                            tmp = tmpl.next();
                            if(tmp.num == game_n_i && j.num_partie == -1){
                                if(tmp.j.size()<max_player){
                                    tmp.addToPartie(j);
                                    byte[] tosend = new byte[]{
                                        'R','E','G','O','K',' ',(byte)game_n_i,'*','*','*'
                                    };
                                    out.write(tosend);
                                    pw.flush();
                                }else{
                                    System.out.println("La partie que vous voulez rejoindre est pleine");
                                }
                                break;
                            }
                        }
                        if(j.num_partie == -1){
                            byte[] tosend = new byte[]{
                                'R','E','G','N','O','*','*','*'
                            };
                            j = null;
                            out.write(tosend);
                            pw.flush();
                        }
                    }else{
                        byte[] tosend = new byte[]{
                            'R','E','G','N','O','*','*','*'
                        };
                        out.write(tosend);
                        pw.flush();
                        System.out.println("Vous avez déjà rejoint une partie");
                    }
                }else if(requete.equals("START")){
                    readBytes(in, 3);//***
                    if(j==null){
                        System.out.println("Rejoignez d'abord une partie");
                    }else{
                        j.ready = true;
                        ready = true;
                        j.ready = true;
                        ListIterator <Partie> tmpl = l.listIterator();
                        Partie tmp;
                        while(tmpl.hasNext()){
                            tmp = tmpl.next();
                            if(tmp.num == j.num_partie){
                                tmp.ready = true;
                                if(tmp.j.size() == max_player){
                                    Joueur test;
                                    ListIterator<Joueur> tmpj = tmp.j.listIterator();
                                    while(tmpj.hasNext()){
                                        test = tmpj.next();
                                        if(test.ready == false){
                                            tmp.ready = false;
                                            break;
                                        }
                                    }
                                }else{
                                    tmp.ready = false;
                                }
                                if(tmp.ready){
                                    tmpl.remove();
                                    break;
                                }
                            }
                        }
                        for(Partie p : l){
                            int nbJPret = 0;
                            while(p.ready == false){
                                for(Joueur joueur : p.j){

                                    if(joueur.ready){
                                        nbJPret ++;
                                    }
                                }

                                if(nbJPret == p.j.size()){ // si tout les joueurs de p sont pret
                                    p.ready = true;
                                    p.fantomes = p.labyrinthe.placerFantome(3); //3 fantomes sont placés;
                                    byte[] welco = new byte[]{ //il faut ajouter un ip d'adresse D(multi-diffusion) (entre 224.0.0.0 et 239.255.255.255)et un port egalement comme attribut a partie
                                        'W','E','L','C','O',' ',(byte)p.num,' ',(byte)p.labyrinthe.getHauteur(),' ',(byte)p.labyrinthe.getWidth(),' ',(byte)p.fantomes.size()/*, ' ' ,(byte)p.ip, ' ' ,(byte)p.port*/,'*','*','*'
                                    };
                                    out.write(welco);
                                    pw.flush();

                                    for(Joueur joueur : p.j){
                                        int[] posJ = p.labyrinthe.placerJoueur(joueur);
                                        byte[] posit = new byte[]{
                                            'P','O','S','I','T',' '};
                                        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
                                        byte[] jid = joueur.id.getBytes();
                                        bos1.write(posit);
                                        bos1.write(jid);
                                        byte[] positjid = bos1.toByteArray();
                                        byte[] p3= new byte[]{' ',(byte)posJ[1],' ',(byte)posJ[0],'*','*','*'};
                                        bos1.close();
                                        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                                        bos2.write(positjid);
                                        bos2.write(p3);
                                        byte[] positFinal = bos2.toByteArray();
                                        bos2.close();
                                        out.write(positFinal);
                                        pw.flush();
                                    }
                                }
                                nbJPret = 0;
                            }
                        }
                    }
                }else if(requete.equals("UNREG")){
                    readBytes(in, 3);//***
                    byte[] tosend;
                    if(j == null){
                        System.out.println("Vous n'êtes dans aucune partie.");
                        tosend = new byte[]{
                            'D','U','N','N','O','*','*','*'
                        };
                    }else{
                        Partie tmp = null;
                        ListIterator <Partie> tmpl = l.listIterator();
                        while(tmpl.hasNext()){
                            tmp = tmpl.next();
                            if(tmp.num == j.num_partie){
                                tmp.j.remove(j);
                                break;
                            }else{
                                tmp = null;
                            }
                        }
                        if(tmp!=null){
                            j = null;
                            tosend = new byte[]{
                                'U','N','R','O','K',' ',(byte)tmp.num,'*','*','*'
                            };
                            if(tmp.j.size() == 0){
                                l.remove(tmp);
                            }
                        }else{
                            System.out.println("La partie spécifiée n'existe pas.");
                            tosend = new byte[]{
                                'D','U','N','N','O','*','*','*'
                            };
                        }
                    }
                    out.write(tosend);
                    pw.flush();
                }else if(requete.equals("SIZE?")){//SIZE? m***
                    readBytes(in,1);//space
                    byte[] game_n  = readBytes(in, 1);
                    int game_n_i = game_n[0];
                    readBytes(in, 3);//***
                    ListIterator <Partie> tmpl = l.listIterator();
                    Partie tmp = null;
                    byte[] tosend;
                    while(tmpl.hasNext()){
                        tmp = tmpl.next();
                        if(tmp.num == game_n_i){
                            //SIZE! m h w***
                            int h = tmp.labyrinthe.getHauteur();
                            int w = tmp.labyrinthe.getWidth();
                            tosend = new byte[]{
                                'S','I','Z','E','!',' ',(byte)tmp.num,' ',(byte)h,' ',(byte)w,'*','*','*'
                            };
                            out.write(tosend);
                            pw.flush();
                            break;
                        }else{
                            tmp = null;
                        }
                    }
                    if(tmp == null){//DUNNO***
                        tosend = new byte[]{
                            'D','U','N','N','O','*','*','*'
                        };
                        out.write(tosend);
                        pw.flush();
                    }
                }else if(requete.equals("LIST?")){ //LIST? m***
                    readBytes(in,1);//space
                    byte[] game_n  = readBytes(in, 1);
                    int game_n_i = game_n[0];
                    readBytes(in, 3);//***
                    ListIterator <Partie> tmpl = l.listIterator();
                    Partie tmp = null;
                    byte[] tosend;
                    while(tmpl.hasNext()){
                        tmp = tmpl.next();
                        if(tmp.num == game_n_i){
                            //LIST! m s***
                            tosend = new byte[]{
                                'L','I','S','T','!',' ',(byte)tmp.num,' ',(byte)tmp.j.size(),'*','*','*'
                            };
                            out.write(tosend);
                            pw.flush();
                            //s * PLAYR id***
                            int s = tmp.j.size();
                            for(int i = 0;i<s;i++){
                                String id = tmp.j.get(i).id;
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                                outputStream.write("PLAYR ".getBytes());
                                outputStream.write(id.getBytes());
                                outputStream.write("***".getBytes());
                                tosend = outputStream.toByteArray();
                                out.write(tosend);
                                pw.flush();
                            }
                            break;
                        }else{
                            tmp = null;
                        }
                    }
                    if(tmp == null){//DUNNO***
                        tosend = new byte[]{
                            'D','U','N','N','O','*','*','*'
                        };
                        out.write(tosend);
                        pw.flush();
                    }
                }else if(requete.equals("GAME?")){ //GAME?***
                    readBytes(in, 3);//***
                    showGames(out, pw);
                }else{//Nous ne tombons jamais dans ce cas.
                    System.out.print(requete);
                }
            }

            // boolean iquit_req = false;
            // while(j != null && !end_of_Game(tmp,iquit_req)){
            //   byte[] req_input=readBytes(in,5);//requete
            //   String requete = byteToString(req_input);
            //   byte[] bytes;
            //
            //   switch(requete){
            //     case "UPMOV":
            //
            //     case "DOMOV":
            //
            //     case "LEMOV":
            //
            //     case "RIMOV":
            //       byte[] byteScore,byteX,byteY; //point et coord du joueur
            //       readBytes(in,1);//space
            //       byte[] d_input=readBytes(in,3); //distance
            //       int distance = Integer.parseInt(byteToString(d_input));;
            //       readBytes(in,3); //etoiles
            //       boolean touchGhost = movebis(tmp.getLab(),j,distance,requete);   //deplacmement du joueur en recuperant le boolean indiquant si il a croisé un fantome
            //       byteX = intToByte(j.getX(),3);
            //       byteY = intToByte(j.getY(),3);
            //       if (touchGhost) {
            //         byteScore = intToByte(j.getScore(),4); //convertion du score en bytes
            //         bytes = new byte[]{
            //         'M','O','V','E','F',' ',byteX[2],byteX[1],byteX[0],' ',byteY[2],byteY[1],byteY[0],' ',byteScore[3],byteScore[2],byteScore[1],byteScore[0],'*','*','*'//[MOVEF␣x␣y␣p***]
            //         };
            //         System.out.println("Ghost touched!");
            //       }else{
            //         bytes = new byte[]{
            //         'M','O','V','E','!',' ',byteX[2],byteX[1],byteX[0],' ',byteY[2],byteY[1],byteY[0],'*','*','*'   // [MOVE!␣x␣y***]
            //         };
            //         System.out.println("Moved!");
            //       }
            //       out.write(bytes);
            //       pw.flush();
            //       break;
            //     case "IQUIT":
            //       readBytes(in,3); //etoiles
            //       bytes = new byte[]{
            //         'G','O','B','Y','E','*','*','*'
            //       };
            //       out.write(bytes);
            //       pw.flush();
            //       iquit_req = true;
            //       tmp.j.remove(j);
            //       // DECONNECTION
            //       break;
            //     case "GLIS?":
            //       readBytes(in,3); //etoiles
            //       bytes = new byte[]{
            //         'G','L','I','S','!',' ',(byte)(tmp.getPlayers()) ,'*','*','*'     //[GLIS!␣s***]
            //       };
            //       out.write(bytes);
            //       pw.flush();
            //       for (Joueur j : tmp.getJoueurs()) {
            //         ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );     //[GPLYR␣id␣x␣y␣p***] s fois
            //         outputStream.write("GPLYR ".getBytes());
            //         outputStream.write((j.getId()+" ").getBytes());
            //         outputStream.write((j.getX()+" ").getBytes());
            //         outputStream.write((j.getY()+" ").getBytes());
            //         outputStream.write(String.valueOf(j.getScore()).getBytes());
            //         outputStream.write("***".getBytes());
            //         bytes = outputStream.toByteArray();
            //         out.write(bytes);
            //         pw.flush();
            //       }
            //       break;
            //     case "MALL?":
            //       System.out.println("Not coded yet");
            //       break;
            //     case "SEND?":
            //       System.out.println("Not coded yet");
            //       break;
            //   }
            // }


            br.close();
            pw.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }

    public static String byteToString(byte[] b){
        String result = "";
        for(int i=0; i< b.length ; i++) {
            result += (char) (b[i] & 0xFF);
        }
        return result;
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

    public static void showGames(OutputStream out,PrintWriter pw) throws IOException{
        int n = l.size();
        byte[] bytes = new byte[]{
            'G','A','M','E','S',' ',(byte)n,'*','*','*'
        };
        out.write(bytes);
        pw.flush();
        for(int i = 0; i<n;i++){ //[OGAME m s***]
            Partie tmp = l.get(i);
            int tmp1 = tmp.j.size();
            byte[] bytes2 = new byte[]{
                'O','G','A','M','E',' ',(byte)tmp.num,' ',(byte)tmp1,'*','*','*'
            };
            out.write(bytes2);
            pw.flush();
        }
    }

    public static void addPartie(Partie p){
        synchronized(l){
            l.add(p);
        }
    }

    public boolean movebis(Labyrinthe maze,Joueur j,int distance, String direction,Partie p){
      Case[][] l = maze.maze;

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
              if(j.getX() == f.getPosX() && j.getY() == f.getPosY()) touch = true;
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
            for (Fantome f :p.fantomes) {
              if(j.getX() == f.getPosX() && j.getY() == f.getPosY()) touch = true;
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
              if(j.getX() == f.getPosX() && j.getY() == f.getPosY()) touch = true;
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
              if(j.getX() == f.getPosX() && j.getY() == f.getPosY()) touch = true;
            }
          }
          break;
      }
      j.point =+ 10 ;  //+10 points (à changer)
      return touch;  //boolean indiquant si fantome croisé
    }

    public boolean end_of_Game(Partie p,boolean deco){
      boolean res = false;
      if ((p.getFantomes().isEmpty() || p.getPlayers()==0) || deco) {
        res = true;
      }
      return res ;
    }

    public static byte[] intToByte(int myint,int nbOfBytes){
      byte[] bytes = new byte[nbOfBytes];
      for (int i = nbOfBytes-1; i >= 0; i--) {
        bytes[i] = (byte)(myint >>> (i * 8));
      }
      return bytes;
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
                    serveurJeu j = new serveurJeu(socket);

                    Thread t = new Thread(j);
                    t.start();
                }
            }catch(Exception e){
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }
}
