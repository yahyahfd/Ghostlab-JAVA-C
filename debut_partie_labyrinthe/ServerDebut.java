import java.util.*;
import java.io.*;
import java.net.*;

public class ServerDebut{
    public static int port;
    public static Labyrinthe lab= new Labyrinthe(); //temporaire dans le switch case il faudra recuperer le labyrinthe de la partie courante

    public static void main(String args[]) throws IOException {                                                                         
        try{
            ServerSocket serv = new ServerSocket(port);
            while(true){
                Socket socket = serv.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                pw.print("En attente de joueurs\n");
                pw.flush();
                String rep = br.readLine();
                switch(rep){
                    case "START": //A adapter pour s'activer quand tout les clients inscrit ont envoyé START
                        LinkedList<Fantome> fantomes = lab.placerFantome(3); //par defaut 3 pour l'instant
                        pw.print("WELCO id_de_la_partie " + lab.getHauteur() + " " + lab.getWidth() + " " + fantomes.size() + "IP_multi_diffusion" + " " + "port_multi_diffusion");
                        pw.flush();
                        //ici fonction qui ajoute joueur au labyrinthe
                        pw.print("POSIT " + "id_joueur" + " " + "joueur.getPosX()" + " " + "joueur.getPosY()");
                        pw.flush();
                        break;
                }
                br.close();
                pw.close();
                socket.close();
            }   
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
                                                                  
    }     
}