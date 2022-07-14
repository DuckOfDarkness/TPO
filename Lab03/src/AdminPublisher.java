import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminPublisher {

    private static final Logger log = Logger.getLogger(AdminPublisher.class.getName());
    private static final HashMap<String, ArrayList<String>> mapOfTopics = new HashMap<>();
    private static Charset charset;
    private static SocketChannel channel = null;
    private static final ArrayList<String> exempleNews = new ArrayList<>();


    public static void main(String[] args) throws IOException {

        //Example data
        ArrayList<String> exempleNewsSience = new ArrayList<>();
        ArrayList<String> exempleNewsWeather = new ArrayList<>();
        ArrayList<String> exempleNewsSport = new ArrayList<>();

        exempleNewsSience.add("Kaczy piknik naukowy w ramach XVII Kaczego Festiwalu Kaczej Nauki");
        exempleNewsWeather.add("IMGW wydał alerty pierwszego stopnia przed burzami z gradem dla siedmiu województw centralnej i południowej części Polski. Opadom deszczu towarzyszyć będzie silny wiatr w porywach do 70 km/h.");
        exempleNewsSport.add("Kaczki mistrzem stawu w picipolo.");

        exempleNews.add("Kaczy piknik naukowy w ramach XVII Kaczego Festiwalu Kaczej Nauki");
        exempleNews.add("IMGW wydał alerty pierwszego stopnia przed burzami z gradem dla siedmiu województw centralnej i południowej części Polski. Opadom deszczu towarzyszyć będzie silny wiatr w porywach do 70 km/h.");
        exempleNews.add("Kaczki mistrzem stawu w picipolo.");

        mapOfTopics.put("Nauka", exempleNewsSience);
        mapOfTopics.put("Pogoda", exempleNewsWeather);
        mapOfTopics.put("Sport", exempleNewsSport);


        new AdminPublisherGUI();
            connect();
        }

    public static HashMap<String, ArrayList<String>> getMapsOfTopics() {
        return mapOfTopics;
    }

    //Download available topics
    private static String getAvalaibleTopics(){
        int counter = 0;
        Set<Map.Entry<String, ArrayList<String>>> entries = mapOfTopics.entrySet();
        Iterator<Map.Entry<String, ArrayList<String>>> listOfTopicsIterator = entries.iterator();
        StringBuilder topics = new StringBuilder("");
        while (listOfTopicsIterator.hasNext()){
            Map.Entry<String, ArrayList<String>> entry = listOfTopicsIterator.next();
            topics.append(entry.getKey());
            if(counter++<2) topics.append(",");
        }
        return topics.toString();
    }

    //Adds a new topic
    public static void addTopic(String key, ArrayList<String> value) {
        AdminPublisher.mapOfTopics.put(key, value);
        log.log(Level.INFO, "[AdminPublisher] Dodaje nowy temat: "+key+".\n");
        try {
            channel.write(charset.encode("6,"+key+"\n"));
        } catch (IOException | NullPointerException e) {
            log.log(Level.INFO, "[AdminPublisher] Serwer jest niedostepny.\n");
        }

    }

    //Deletes the selected topic
    public static void deleteTopic(String key){
        AdminPublisher.mapOfTopics.remove(key);
        log.log(Level.INFO, "[AdminPublisher] Usuwam temat: "+key+".\n");
        try {
            channel.write(charset.encode("7,"+key+"\n"));
        } catch (IOException | NullPointerException  e) {
            log.log(Level.INFO, "[AdminPublisher] Serwer jest niedostepny.\n");
        }
    }

    //Adds a new message to the subject
    public static void addNewsMessage(String key, String message) throws IOException {
        ArrayList<String> tempList = mapOfTopics.get(key);
        tempList.add(message);
        mapOfTopics.remove(key);
        mapOfTopics.put(key, tempList);
        channel.write(charset.encode("8,"+key+","+message+"\n"));
    }

    //Retrieves all available messages from the selected subject
    public static String getMessage(String key){
        StringBuilder message = new StringBuilder("");
        for(String s : mapOfTopics.get(key)){
            message.append(s).append("\n");
        }
        return message.toString();
    }

    //Connect
    public static void connect() throws IOException {
        String server = "localhost";
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            int port = 10000;
            channel.connect(new InetSocketAddress(server, port));

            log.log(Level.INFO, "[AdminPublisher] Lączę się z serwerem.\n");

            while (!channel.finishConnect()) {}

        } catch(UnknownHostException exc) {
            System.err.println("Uknown host " + server);
        } catch(Exception exc) {
            connect();
        }

        log.log(Level.INFO, "[AdminPublisher] Polaczenie z serwerem ustanowione.\n");

        charset  = Charset.forName("ISO-8859-2");
        int rozmiar_bufora = 1024;
        ByteBuffer inBuf = ByteBuffer.allocateDirect(rozmiar_bufora);
        CharBuffer cbuf;

        log.log(Level.INFO, "[AdminPublisher] Wysylam liste dostepnych tematow.\n");
        channel.write(charset.encode("5,"+getAvalaibleTopics()+"\n"));

        while (true) {
            int readBytes = 0;
            inBuf.clear();
            try {
                readBytes = channel.read(inBuf);
            }catch (IOException e){
                log.log(Level.INFO, "[AdminPublisher] Polaczenie z serwerem zostalo zerwane. Ponawiam probe polaczenia.\n");
                connect();
            }
            if (readBytes == 0) {
                continue;
            }
            else if (readBytes == -1) {
                connect();
            }
            else {
                inBuf.flip();
                cbuf = charset.decode(inBuf);

                String odSerwera = cbuf.toString();

                System.out.println("[AdminPublisher]: Wiadomosc przychodzaca: " + odSerwera);
                if(odSerwera.charAt(0) == '5'){
                    log.log(Level.INFO, "[AdminPublisher] Przesylam przykladowe newsy.\n");
                    channel.write(charset.encode("P,"+exempleNews+"\n"));

                }
                cbuf.clear();
                if (odSerwera.equals("9")){
                    break;
                }
            }

        }
        System.exit(0);
    }

    public static void disconnect(){
        try {
            if(channel.isConnected())
                channel.write(charset.encode("9\n"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}


