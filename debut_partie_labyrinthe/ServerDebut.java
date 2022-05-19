import java.util.*;
import java.io.*;
import java.net.*;

public class ServerDebut{
    Socket socket;  
    LinkedList<Partie> l = new LinkedList<Partie>();
    public static Labyrinthe lab= new Labyrinthe(); //temporaire dans le switch case il faudra recuperer le labyrinthe de la partie courante

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
            
            byte[] req_input = readBytes(in,5);
            String requete = byteToString(req_input);

            System.out.println(requete);
            if(requete.equals("START")){
                for(Partie p : l){
                    int nbJPret = 0;
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
                        byte[] welco = new byte[]{ //infos pour IP et port manquant
                            'W','E','L','C','O',' ',(byte)p.num,' ',(byte)p.labyrinthe.getHauteur(),' ',(byte)p.labyrinthe.getWidth(),' ',(byte)p.fantomes.size(),' ',(byte)"IP_multi_diffusion",' ',(byte)"port_multi_diffusion",'*','*','*'
                        };
                        out.write(welco);
                        out.flush();
                        for(Joueur joueur : p.j){
                            int[] posJ = p.labyrinthe.placerJoueur(joueur);
                            byte[] posit = new byte[]{
                                'P','O','S','I','T',' ',(byte)joueur.id,' ',(byte)posJ[0],' ',(byte)posJ[1],'*','*','*'
                            };
                            out.write(posit);
                            out.flush();
                        }
                    } 
                }
            }


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

  

    public static void main(String args[]) throws IOException {                                                                         
        try{
            

            /*DatagramSocket dso = new DatagramSocket();
            byte[] data;
            boolean prets = true; //pour test il faudra le rendre adaptable selon la capacité de la partie
            while(true){
                String s = "";
                if(!prets){
                    s = "En attente de joueurs";
                }else{
                    LinkedList<Fantome> fantomes = lab.placerFantome(3); //par defaut 3 pour l'instant
                    s = ("WELCO id_de_la_partie " + lab.getHauteur() + " " + lab.getWidth() + " " + fantomes.size() + " IP_multi_diffusion" + " " + "port_multi_diffusion");
                    //ici fonction qui ajoute joueur au labyrinthe
                    s += ("POSIT " + "id_joueur" + " " + "joueur.getPosX()" + " " + "joueur.getPosY()");
                }
                data = s.getBytes();
                DatagramPacket paquet = new DatagramPacket(data,data.length);
                dso.send(paquet);*/
            }   
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
                                                                  
    }     
}