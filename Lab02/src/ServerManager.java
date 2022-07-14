import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class ServerManager {

    private static final Map<String, Integer> dictionaryMap = new HashMap<>();

    /* extractOfRequestTab
     * 0 - message
     * 1 - iso
     * 2 - port
     * */
    private static final String[] extractOfRequestTab = new String[3];
    private static int indexOfExtractoOfRequestTab = 0;

    public static void main(String[] args) {

        int portNumber = 10000;
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                System.out.println("Pojawilo sie nowe polaczenie: uruchamiam nowy watek");
                new ServerManagerThread(serverSocket.accept()).start();
            }
        }catch (
                IOException e) {
            System.err.println("Port " + portNumber + " jest zajety.");
            System.exit(-1);
        }
    }

    //Adds a dictionary server to the map
    public static void addDictionaryMap(int port, String iso){
        dictionaryMap.put(iso, port);
    }

    //Checks if the country code is on the list of available dictionary servers
    public static boolean isIsoExists(String iso){
        return dictionaryMap.containsKey(iso);
    }

    //Get the port of the dictionary server
    public static int getPortNumber(String iso){
        return dictionaryMap.get(iso);
    }

    //Breaks the query down into its component parts
    public static void extractRequest(String request){
        if(request.contains(",")){
            int index = request.indexOf(",");
            extractOfRequestTab[indexOfExtractoOfRequestTab] = request.substring(0, index);
            indexOfExtractoOfRequestTab++;
            extractRequest(request.substring(index+1));
        }else {
            extractOfRequestTab[indexOfExtractoOfRequestTab] = request;
            indexOfExtractoOfRequestTab = 0;
        }
    }

    //Returns items obtained from the query
    public static String getExtractedElement(int index){
        return extractOfRequestTab[index];
    }

}
