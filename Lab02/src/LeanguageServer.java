import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.nio.file.StandardOpenOption.APPEND;

public class LeanguageServer {

    /**CONFIGURATION DATA**/

    //Port number 10005 - 65535
   //private static final String country = "Niemcy";             private static final int portNumber = 10001;
     private static final String country = "Wielka Brytania";    private static final int portNumber = 10002;
   //private static final String country = "Francja";            private static final int portNumber = 10003;
   //private static final String country = "Rosja";              private static final int portNumber = 10004;
    /**********************/


    private static String filename;
    private static File dictionaryJson;
    private static String isoOfCountry;
    private static Path path;
    private static String[] extractOfRequestTab = new String[2];

    public static void main(String[] args){
        isoOfCountry = getIsoCode(country);
        filename = isoOfCountry + "_dictionary.json";
        path = Paths.get(filename);
        System.out.println(country +" "+isoOfCountry+" "+portNumber);
        creatDictionary();

        //**Uncomment the next line to add a new word to the dictionary */
/**
 addWord("word to translate", "word translated");
 **/
        //****************************************************************/

        System.out.println("Wysylam raport o dostepnosci do serwera");
        String greeting = "1"+","+isoOfCountry+","+portNumber;
        communicationWithTarget(10000, greeting);
        while (true){
            listening();
        }
    }

    //Obtaining the country's iso code
    public static String getIsoCode(String country) {
        //Locale.setDefault(Locale.ENGLISH);
        Map<String, String> getAlpha2CodeMap = new HashMap<>();
        for (String iso : Locale.getISOCountries()) {
            Locale l = new Locale("", iso);
            getAlpha2CodeMap.put(l.getDisplayCountry(), iso);
        }
        return getAlpha2CodeMap.get(country);
    }

    //Creating a dictionary file
    public static void creatDictionary(){
        dictionaryJson = new File(filename);
        try {
            if(!dictionaryJson.exists()){
                dictionaryJson.createNewFile();
                addToJson(addOnePieceOfMap(country, isoOfCountry));}
        }catch (IOException i){
            System.err.println("Nie udalo sie utworzyc pliku: "+i);
        }
    }

    //Adding to the map to the json
    public static void addToJson(Map<String, String> dictionaryMap) throws IOException {
        Gson gson = new Gson();
        Type gsonType = new TypeToken<Map<String, String>>(){}.getType();
        String gsonString = gson.toJson(dictionaryMap, gsonType);
        FileChannel fileOutput = FileChannel.open(path, EnumSet.of(APPEND));
        ByteBuffer byteBuffer = ByteBuffer.wrap(gsonString.getBytes(StandardCharsets.UTF_8));
        fileOutput.write(byteBuffer);
    }

    //Create a single map element
    public static Map<String, String> addOnePieceOfMap(String key, String value){
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put(key, value);
        return tempMap;
    }

    //Returning the translation of a word
    public static String getWord(String keyWord) throws IOException {
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get(filename));
        Map<String, String> tempMap = gson.fromJson(reader, Map.class);
        reader.close();
        return tempMap.getOrDefault(keyWord, "Nie znaleziono tlumaczenia");
    }

    //Receives the message
    public static void listening(){
        System.out.println("Uruchamiam nasłuch");
        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
        ){
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Otrzymane zapytanie: "+inputLine);
                System.out.println("Przetwarzam zapytanie");
                extractRequest(inputLine);
                System.out.println("Wysylam odpowiedz: \""+getWord(extractOfRequestTab[0])+"\" na port "+extractOfRequestTab[1]);
                communicationWithTarget(Integer.parseInt(extractOfRequestTab[1]), getWord(extractOfRequestTab[0]));
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    //Sends a message to the recipient
    private static void communicationWithTarget(int port, String message){
        System.out.println("Ustawiam polaczenie");
        try(Socket socket = new Socket("127.0.0.1", port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Wysylam wiadomosc: "+message);
            out.println(message);
            System.out.println("Zamykam polaczenie");
            try {
                socket.close();
            }catch (Exception f){
                f.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Division of the query into factors
    public static void extractRequest(String request){
        int index = request.indexOf(",");
        extractOfRequestTab[0] = request.substring(0, index);
        extractOfRequestTab[1] = request.substring(index+1);
    }


    //Add a word to the dictionary
    public static void addWord(String word1, String word2) {
        try {
            //TCreates a map from an existing json
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(path);
            Map<String, String> dictionaryMap = new HashMap<>();
            if (dictionaryJson.exists()) {
                dictionaryMap = gson.fromJson(reader, Map.class);
                dictionaryJson.delete();
                creatNewDictionary();
                reader.close();
            }
            //Adds a new word to the map
            dictionaryMap.put(word1, word2);
            //Creates a new json file
            // creatDictionary();
            //Creates a new json
            addToJson(dictionaryMap);
        } catch (IOException i) {
            System.err.println("Nie udało sie zapisac danych: " + i);
        }catch (NullPointerException n){
            System.err.println("Plik jest pusty: "+n);
            try {
                addToJson(addOnePieceOfMap(country, isoOfCountry));
            }catch (IOException e){
                System.err.println("Nie udało sie zapisac danych: " + e);
            }
        }
    }

    //Creating a new dictionary file when adding new elements to avoid json syntax error
    public static void creatNewDictionary(){
        dictionaryJson = new File(filename);
        try {
            dictionaryJson.createNewFile();
        }catch (IOException i){
            System.err.println("Nie udalo sie utworzyc pliku: "+i);
        }
    }
}