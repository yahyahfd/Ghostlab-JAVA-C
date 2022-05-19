import java.util.*;
import java.io.*;
import java.net.*;

public class ServerDebut{
    public static int port;
    public static Labyrinthe lab= new Labyrinthe(); //temporaire dans le switch case il faudra recuperer le labyrinthe de la partie courante

    public static void main(String args[]) throws IOException {                                                                         
        try{
            DatagramSocket dso = new DatagramSocket();
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
                dso.send(paquet);
            }   
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
                                                                  
    }     
}