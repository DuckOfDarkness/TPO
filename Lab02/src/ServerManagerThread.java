import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerManagerThread extends Thread{

    private final Socket socket;

    public ServerManagerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        System.out.println("Uruchamiam nasluch");
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Otrzymalem wiadomosc : "+inputLine);
                System.out.println("Przetwarzam wiadomosc");
                ServerManager.extractRequest(inputLine);
                String firstOne = ServerManager.getExtractedElement(0);
                if(firstOne.equals("1")){
                    ServerManager.addDictionaryMap(Integer.parseInt(ServerManager.getExtractedElement(2)), ServerManager.getExtractedElement(1));
                    System.out.println("Serwer jezykowy: "+ ServerManager.getExtractedElement(1)+" jest online na porcie "+Integer.parseInt(ServerManager.getExtractedElement(2))+".");
                }else{
                    if(!ServerManager.isIsoExists(ServerManager.getExtractedElement(1))){
                        sender(10001, "Zly slownik");
                    }else{
                        int port = ServerManager.getPortNumber(ServerManager.getExtractedElement(1));
                        sender(port, ServerManager.getExtractedElement(0)+","+ ServerManager.getExtractedElement(2));
                    }
                }
            }
            interrupt();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Sends messages
    public static void sender(int port, String message){
        try(Socket socket = new Socket("127.0.0.1", port)){
            System.out.println("Ustanawiam polaczenie");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Wysylam wiadomosc");
            out.println(message);
            System.out.println("Zamykam polaczenie");
            try{
                socket.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
