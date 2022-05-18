import java.net.*;
import java.util.LinkedList;
import java.io.*;

public class serveurJeu implements Runnable{
    Socket socket;  
    LinkedList<Partie> l = new LinkedList<Partie>();

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

            int n = this.l.size();
            byte[] bytes = new byte[]{
                'G','A','M','E','S',' ',(byte)n,'*','*','*'
            };
            out.write(bytes);
            pw.flush();
            for(int i = 0; i<n;i++){ //[OGAME m s***]
                Partie tmp = l.get(i);
                int tmp1 = tmp.j.size();
                byte[] bytes2 = new byte[]{
                    'O','G','A','M','E',' ',(byte)i,' ',(byte)tmp1,'*','*','*'
                };
                out.write(bytes2);
                pw.flush();
            }
            byte[] req_input=readBytes(in,5); //requete
            String requete = byteToString(req_input);
            readBytes(in,1);//space
            byte[] id_input=readBytes(in,8);
            readBytes(in,1);//space
            byte[] port_input=readBytes(in,4);
            readBytes(in,1);//space
            String id = byteToString(id_input);
            String port = byteToString(port_input);
            Joueur tmp = new Joueur(id, port); //Le joueur créé lié à ce client
            System.out.println(id);                    
            System.out.println(port); 
            System.out.println(requete);
            if(requete.equals("REGIS")){
                byte[] game_input= readBytes(in, 3);
                String game = byteToString(game_input);
                System.out.println(game);
                if(tmp.num_partie!=-1){//erreur deja dans une partie

                }else{
                    tmp.num_partie = Integer.parseInt(game);
                }
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
    public static void main(String [] args){
        if(args.length != 1){
            System.out.println("Port expected here");
        }else{
            try{
                ServerSocket server=new ServerSocket(Integer.parseInt(args[0]));
                while(true){
                    Socket socket=server.accept();
                    serveurJeu j = new serveurJeu(socket);

                    Joueur j1 = new Joueur("AZERTYUI", "4000");
                    Partie p = new Partie();
                    p.j.add(j1);
                    j1.num_partie=p.num; //on check si -1 avant tout changement
                    j.l.add(p);

                    
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