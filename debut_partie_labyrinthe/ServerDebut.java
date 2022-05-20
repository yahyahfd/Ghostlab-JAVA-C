import java.util.*;
import java.io.*;
import java.net.*;

public class ServerDebut{
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
            //socket multicast qui se lie a n'importe quelle port disponible en localhost
            MulticastSocket multidiff = new MulticastSocket();
            String messmulti = "Message envoyé par multi-diffusion";

            byte[] req_input = readBytes(in,5);
            String requete = byteToString(req_input);

            System.out.println(requete);
            if(requete.equals("START")){
                for(Partie p : l){
                    int nbJPret = 0;
                    DatagramPacket dp = new DatagramPacket(messmulti.getBytes(),messmulti.length(),InetAddress.getByName(p.ip),p.port);
                    multidiff.send(dp);
                    for(Joueur joueur : p.j){
                        //Il faudrait ajouter un attribut a partie pour savoir si la partie a commencé ou pas
                        if(joueur.ready){
                            nbJPret ++;
                        }
                    }
                    if(nbJPret == p.j.size()){ // si tout les joueurs de p sont pret
                        //vu que la partie va commencer on va ajouter des fantomes a la partie
                        p.fantomes = p.labyrinthe.placerFantome(3); //3 fantomes sont placés;
                        //Il fazut egalement mettre un 'Labyrinthe labyrinthe' comme attribut de partie pour que cette section fonctionne
                        //ainsi qu'un attribut 'LinkedList<Fantome> fantomes'
                        byte[] welco = new byte[]{ //il faut ajouter un ip d'adresse D(multi-diffusion) (entre 224.0.0.0 et 239.255.255.255)et un port egalement comme attribut a partie
                            'W','E','L','C','O',' ',(byte)p.num,' ',(byte)p.labyrinthe.getHauteur(),' ',(byte)p.labyrinthe.getWidth(),' ',(byte)p.fantomes.size(),' ',(byte)p.ip,' ',(byte)p.port,'*','*','*'
                        };
                        out.write(welco);
                        out.flush();
                        for(Joueur joueur : p.j){
                            int[] posJ = p.labyrinthe.placerJoueur(joueur);
                            byte[] posit = new byte[]{
                                'P','O','S','I','T',' ',(byte)joueur.id,' ',(byte)posJ[1],' ',(byte)posJ[0],'*','*','*'
                            };
                            out.write(posit);
                            out.flush();
                        }
                    } 
                }
            }
            in.close();
            out.close();
            multidiff.close();


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

  

    
}