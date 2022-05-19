import java.net.*;
import java.util.LinkedList;
import java.io.*;

public class serveurJeu{
  Socket socket;
  LinkedList<Partie> parties = new LinkedList<String>();
  LinkedList<Fantome> fantomes = new LinkedList<String>();

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

        }
    }

    public static boolean end_of_Game(int m){
      boolean res = False;
      if (fantomes.isEmpty() || parties.get(m).getPlayers()==0) {
        res = True;
      }
      return res;
    }


}
