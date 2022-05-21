import java.net.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.io.*;

public class serveurJeuTest implements Runnable{
    Socket socket;  
    static LinkedList<Partie> l = new LinkedList<Partie>();

    public serveurJeuTest(Socket s){
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
                        for(Partie p : l){
                            int nbJPret = 0;                            
                            while(p.ready == false){
                                for(Joueur joueur : p.j){
                                    //Il faudrait ajouter un attribut a partie pour savoir si la partie a commencé ou pas
                                    
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
                                    /*InetAddress iptmp = InetAddress.getByName(p.ipMD);
                                    byte[] ipmdtmp = iptmp.getAddress();
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    bos.write(welco);
                                    bos.write(ipmdtmp);
                                    byte[] welcoIP = bos.toByteArray();
                                    byte[] port = { ' ',(byte)p.portMD,'*','*','*'};
                                    bos = new ByteArrayOutputStream();
                                    bos.write(welcoIP);
                                    bos.write(port);
                                    byte[] repWelco = bos.toByteArray();
                                    out.write(repWelco);
                                    out.flush();*/
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
                
                       /* ready = true;
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
                                    System.out.println(" il manque" + (max_player-tmp.j.size()) + "joueurs");
                                }             
                                if(tmp.ready){
                                    tmpl.remove();
                                    break;
                                }
                            }
                        }*/
                        /*for(Partie p : l){
                            if(!p.ready && p.j.size() == max_player){
                                while(!p.ready){
                                    for
                                    p.fantomes = p.labyrinthe.placerFantome(3); //trois fantomes sont placés
                                    String ipMD = "ip";
                                    String portMD = "port";
                                    byte[] welco = new byte[]{ //il faut ajouter un ip d'adresse D(multi-diffusion) (entre 224.0.0.0 et 239.255.255.255)et un port egalement comme attribut a partie
                                         'W','E','L','C','O',' ',(byte)p.num,' ',(byte)p.labyrinthe.getHauteur(),' ',(byte)p.labyrinthe.getWidth(),' ',(byte)p.fantomes.size(),' '
                                     };/*(byte)p.ipipMD.getBytes(),' ',(byte)p.portportMD.getBytes(),'*','*','*'*/
                                   /* out.write(welco); */
                                    /*out.write((ipMD + " " + portMD + "***").getBytes());*/
                                    /*out.flush(); */
                               /* }
                            }
                        } */                                                                           
                    
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
            
            while(j != null){
                //C'est bon, debut_game ici côté serveur
            }


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

    public static void main(String [] args){
        if(args.length != 1){
            System.out.println("Port expected here");
        }else{
            try{
                ServerSocket server=new ServerSocket(Integer.parseInt(args[0]));
                while(true){
                    Socket socket=server.accept();
                    serveurJeuTest j = new serveurJeuTest(socket);

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
