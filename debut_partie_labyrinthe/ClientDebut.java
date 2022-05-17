import java.util.*;
import java.io.*;
import java.net.*;

public class ClientDebut{
    public static int port;

    public static void main(String[] args){                                                                                   
        try{
            Socket sock = new Socket("nadim", port);
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            String messServ = br.readLine();
            System.out.println(messServ);
            Scanner outputClient = new Scanner(System.in);
            String out = outputClient.next();
            pw.print(out);
            pw.flush();
            pw.close();
            br.close();
            sock.close();
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        
    }                                                                                   
}