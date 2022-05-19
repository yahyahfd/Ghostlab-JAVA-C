import java.util.*;
import java.io.*;
import java.net.*;

public class ClientDebut{
    public static int port;

    public static void main(String[] args){                                                                                   
        try{
            /*DatagramSocket dso = new DatagramSocket(port);
            byte[] data= new byte[60];
            DatagramPacket paquet = new DatagramPacket(data,60);
            while(true){
                dso.receive(paquet);
                String s = new String(paquet.getData(),0,60);
                System.out.println("Serveur :" + s);
            }*/
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        
    }                                                                                   
}