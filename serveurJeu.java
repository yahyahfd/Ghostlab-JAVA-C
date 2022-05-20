import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
            boolean party_ready = false;
            int max_player = 3;
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
                    byte[] closing = readBytes(in, 3); //***
                    String closing_s = byteToString(closing);//
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
                    byte[] closing = readBytes(in, 3);
                    String closing_s = byteToString(closing);
                    System.out.print(requete);
                    System.out.print(" "+id+" ");
                    System.out.print(port+" ");
                    System.out.print(game_n_i);
                    System.out.println(closing_s);
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
                    byte[] closing = readBytes(in, 3);
                    String closing_s = byteToString(closing);
                    if(j==null){
                        System.out.println("Rejoignez d'abord une partie");
                    }else{
                        ready = true;
                        j.ready = true;
                        ListIterator <Partie> tmpl = l.listIterator();
                        Partie tmp;
                        boolean party_rd = true;
                        while(tmpl.hasNext()){
                            tmp = tmpl.next();
                            if(tmp.num == j.num_partie){
                                Joueur test;
                                ListIterator<Joueur> tmpj = tmp.j.listIterator();
                                while(tmpj.hasNext()){
                                    test = tmpj.next();
                                    if(test.ready == false){
                                        party_rd = false;
                                        break;
                                    }
                                }
                                int nb = 2; //nb joueurs à définir plus tard par partie. //add variable partie ready, ici on ne remove rien, on sort juste du while
                                if(party_rd == true && tmp.j.size() == nb){
                                    // on commence la partie tmp et on la retire du tableau du serveur
                                    tmpl.remove();
                                    break;
                                }
                            }
                        }
                    }
                }else if(requete.equals("UNREG")){
                    byte[] closing = readBytes(in, 3);
                    String closing_s = byteToString(closing);//
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
                            }
                        }
                        if(tmp!=null){
                            j.num_partie = -1;
                            tosend = new byte[]{
                                'U','N','R','O','K',' ',(byte)tmp.num,'*','*','*'
                            };
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
                    
                }else if(requete.equals("LIST?")){ //LIST? m***
                    //LIST! m s***
                    //s * PLAYR id*** 
                    //sinon DUNNO*** si m n'existe pas
                }else if(requete.equals("GAME?")){ //GAME?***
                    byte[] closing = readBytes(in, 3);//***
                    showGames(out, pw);
                }else{//Nous ne tombons jamais dans ce cas.
                    System.out.print(requete);
                }
            }
            
            
            // Joueur tmp = new Joueur(id, port); //Le joueur créé lié à ce client
            // System.out.println(id);                    
            // System.out.println(port); 
            // System.out.println(requete);
            // if(requete.equals("REGIS")){
            //     byte[] game_input= readBytes(in, 3);
            //     String game = byteToString(game_input);
            //     System.out.println(game);
            //     if(tmp.num_partie!=-1){//erreur deja dans une partie

            //     }else{
            //         tmp.num_partie = Integer.parseInt(game);
            //     }
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
            System.out.println(l.size());
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
                    serveurJeu j = new serveurJeu(socket);

                    // Joueur j1 = new Joueur("AZERTYUI", "4000");
                    // Partie p = new Partie();
                    // p.j.add(j1);
                    // j1.num_partie=p.num; //on check si -1 avant tout changement
                    // j.l.add(p);
                    //add Joueur to Partie synchro
                    //add partie to l synchro on l
                    
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