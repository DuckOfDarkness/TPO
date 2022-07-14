import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientService {

    public static void send(String request, int port){
        try(Socket socket = new Socket("127.0.0.1", 10000)) {
            System.out.println("Otwieram polaczenie z serwerem");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("wysylam wiadomosc: "+request);
            out.println(request);
            listen(port);
        }catch (IOException e) {
            System.out.println("listening for a connection error");
            System.out.println(e.getMessage());
        }
    }

    public static void listen(int port){
        try(ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String inputLine;
            System.out.println("Uruchamiam nasluch");
            if ((inputLine = in.readLine()) != null){
                System.out.println("Nadeszla wiadomosc: "+inputLine);
                if(inputLine.equals("Zly slownik")){
                    ClientGUI.wrongDictionary();
                }else if(inputLine.equals("Nie znaleziono tlumaczenia")){
                    ClientGUI.noTranslate();
                }else{
                    ClientGUI.setAnswer(inputLine);
                    try {
                        System.out.println("Zamykam nasluch");
                        in.close();
                        socket.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

