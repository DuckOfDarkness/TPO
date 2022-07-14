import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainServer {

    private static final Logger log = Logger.getLogger(MainServer.class.getName());
    private static final List<SocketChannel> usersList = new ArrayList<>();
    private static String avalaibleTopics = "";
    private static final Map<String, ArrayList<String>> mapsOfTopics = new HashMap<>();
    private static final Map<String, ArrayList<SocketChannel>> subscriptionMap = new HashMap<>();
    private static final List<String> listOfTopics = new ArrayList<>();
    private static final List<String> exempleNewsList = new ArrayList<>();
    private static boolean fuse = false;
    private static final Charset charset  = Charset.forName("ISO-8859-2");
    private static final int BSIZE = 1024;
    private static final ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);
    private static final StringBuffer reqString = new StringBuffer();

    public static void main(String[] args){

        // Input arguments: host, port //
        new MainServer(args[0], Integer.parseInt(args[1]));
    }

    public MainServer(String host, int port) {
        connect(host, port);
    }

    private static void connect(String host, int port){
        ServerSocketChannel serverChannel = null;
        try{
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(host, port));
            serverChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.log(Level.INFO, "[Server] Otwieram nasluch.\n");

            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while(iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) {
                        log.log(Level.INFO, "[Server] Nawiazano nowe polaczenie.\n");
                        SocketChannel cc = serverChannel.accept();
                        cc.configureBlocking(false);
                        cc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        continue;
                    }

                    if (key.isReadable()) {
                        SocketChannel cc = (SocketChannel) key.channel();
                        serviceRequest(cc);
                        continue;
                    }
                }
            }
        }catch (Exception e){
            try {
                serverChannel.close();
                connect(host, port);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void serviceRequest(SocketChannel sc) throws IOException {
        if (!sc.isOpen()) return;

        System.out.print("Wiadomosc przychodząca: ");
        reqString.setLength(0);
        bbuf.clear();

        try {
            readLoop:
            while (true) {
                int n = sc.read(bbuf);
                if (n > 0) {
                    bbuf.flip();
                    CharBuffer cbuf = charset.decode(bbuf);
                    while (cbuf.hasRemaining()) {
                        char c = cbuf.get();
                        if (c == '\r' || c == '\n') break readLoop;
                        else {
                            reqString.append(c);
                        }
                    }
                }
            }

            String cmd = reqString.toString();
            System.out.println(reqString);

            //Inquiry codes
            //Client                                          Admin
            // 1 - greeting and sending a list of topics
            // 2 - subscribing a topic                        5 - sending the list
            // 3 - unsubscribing                              6 - adding a topic
            // 4 - disconnect                                 7 - removing the topic
            // A - receiving new news                         8 - adding news
            // P - sample news                                9 - disconnect


            if (cmd.charAt(0) == '1') {
                    log.log(Level.INFO, "[Server] Polaczono z nowym klientem. Wysylam liste dostepnych tematow.\n");
                    usersList.add(sc);
                    sc.write(charset.encode("1," + avalaibleTopics));
            }
            if (cmd.charAt(0) == '2') {
                log.log(Level.INFO, "[Server] Pojawila sie nowa informacja na temat subskrypcji kanalu informacyjnego. Przesylam tresci.\n");
                cmd = cmd.substring(2);
                editSubsciberTopic(cmd, sc, false);
                sc.write(charset.encode("2," + cmd + "," + getTopicsContent(cmd)));
                printMap();
            }

            if (cmd.charAt(0) == '3') {
                log.log(Level.INFO, "[Server] Klient zrezygnowal z subskrypcji tematu: " + cmd.substring(2) + ".\n");
                cmd = cmd.substring(2);
                editSubsciberTopic(cmd, sc, true);
                printMap();
            }
            if (cmd.charAt(0) == '4') {
                log.log(Level.INFO, "[Server] Klient zamknal polaczenie.\n");
                usersList.remove(sc);
                sc.write(charset.encode("4"));
                sc.socket().close();
            }
            if (cmd.charAt(0) == '5') {

                    log.log(Level.INFO, "[Server] Administrator publikacji przeslal liste tematow.\n");
                    listOfTopics.clear();
                    avalaibleTopics = cmd.substring(2);
                    extractTopicsFromQueryToList(cmd, listOfTopics);
                    sendTopicListToUsers();
                    System.out.println("Avalaible Topics: " + listOfTopics);
                    initMaps();
                    sc.write(charset.encode("5"));
            }
            if (cmd.charAt(0) == 'P') {
                if(!fuse){
                    log.log(Level.INFO, "[Server] Administrator publikacji przeslal liste newsow.\n");
                    int lastIndex = cmd.indexOf("]");
                    cmd = cmd.substring(3, lastIndex);
                    extractTopicsFromQueryToList(cmd, exempleNewsList);
                    mapsOfTopics.put("Nauka", new ArrayList<>(Collections.singleton(exempleNewsList.get(0))));
                    mapsOfTopics.put("Pogoda", new ArrayList<>(Collections.singleton(exempleNewsList.get(1))));
                    mapsOfTopics.put("Sport", new ArrayList<>(Collections.singleton(exempleNewsList.get(2))));
                    fuse = true;
                }
            }

            if (cmd.charAt(0) == '6') {
                log.log(Level.INFO, "[Server] Administrator publikacji przeslal nowy temat.\n");
                avalaibleTopics += cmd.substring(1);
                sendTopicListToUsers();
                mapsOfTopics.put(cmd.substring(2), new ArrayList<>());
                subscriptionMap.put(cmd.substring(2), new ArrayList<>());
                System.out.println("avalaibleTopics: "+listOfTopics);
                System.out.println("avalaibleTopicsString: "+avalaibleTopics);
                printMap();
            }
            if (cmd.charAt(0) == '7') {
                String deleteTopic = cmd.substring(2);
                log.log(Level.INFO, "[Server] Administrator publikacji usunal temat: "+deleteTopic+".\n");
                listOfTopics.remove(deleteTopic);
                mapsOfTopics.remove(deleteTopic);
                subscriptionMap.remove(deleteTopic);
                setAvalaibleTopics();
                sendMessageAboutDeleteTopicToUsers(deleteTopic);
                System.out.println("avalaibleTopics: "+listOfTopics);
                System.out.println("avalaibleTopicsString: "+avalaibleTopics);
                printMap();
            }
            if (cmd.charAt(0) == '8') {
                log.log(Level.INFO, "[Server] Administrator publikacji przeslal nowy news.\n");
                String[] tab = extractNewsFromMessage(cmd);
                addNewsMessage(tab[0], tab[1]);
            }
            if (cmd.charAt(0) == '9') {
                log.log(Level.INFO, "[Server] Administrator publikacji zamknal polaczenie.\n");
                sc.write(charset.encode("9"));
                sc.socket().close();
                avalaibleTopics = "";
                sendTopicListToUsers();
            }
        } catch (Exception exc) {
            avalaibleTopics = "";
            sendTopicListToUsers();
            log.log(Level.INFO, "[Server] Polączenie zostalo zerwane. Ponawiam probe polaczenia.\n");
            exc.printStackTrace();
            try { sc.close();
                sc.socket().close();
            } catch (Exception e) {}
        }
    }

    private static void addNewsMessage(String key, String message) throws IOException {
        ArrayList<String> tempList = mapsOfTopics.get(key);
        tempList.add(message);
        mapsOfTopics.remove(key);
        mapsOfTopics.put(key, tempList);
        sendNewsToSubscribers(key);
    }

    private static void sendNewsToSubscribers(String topic) throws IOException {
        for(SocketChannel sc : subscriptionMap.get(topic)){
            sc.write(charset.encode("A,"+topic+","+getLastContent(topic)));
        }
    }

    private static void sendTopicListToUsers() throws IOException {
        for(SocketChannel sc : usersList){
            sc.write(charset.encode("1,"+avalaibleTopics));
        }
    }

    private static void sendMessageAboutDeleteTopicToUsers(String topic) throws IOException {
        for(SocketChannel sc : usersList){
            sc.write(charset.encode("7,"+topic));
        }
    }

    private static String getLastContent(String topic){
        ArrayList<String> contents = mapsOfTopics.get(topic);
        return contents.get(contents.size()-1);
    }

    private static String getTopicsContent(String topic){
        ArrayList<String> contents = mapsOfTopics.get(topic);
        StringBuilder returnedMesssage = new StringBuilder();
        for(String s : contents){
            returnedMesssage.append(s).append(",");
        }
        return returnedMesssage.toString();
    }

    private static String[] extractNewsFromMessage(String message){
        message = message.substring(2);
        int index = message.indexOf(',');
        return new String[]{(message.substring(0, index)), (message.substring(index+1))};
    }

    private static void extractTopicsFromQueryToList(String message, List<String> lista){
        if(message.contains(",")){
            int index = message.indexOf(",");
            lista.add(message.substring(0,index));
            extractTopicsFromQueryToList(message.substring(index+1), lista);
        }else {
            lista.remove("5");
            lista.add(message);
        }
    }

    private static void setAvalaibleTopics(){
        avalaibleTopics = String.join(",", listOfTopics);
    }

    private static void initMaps(){
        for(String s : listOfTopics){
            mapsOfTopics.put(s, new ArrayList<>());
            subscriptionMap.put(s, new ArrayList<>());
        }
    }

    private static void editSubsciberTopic(String topic, SocketChannel subscriber, boolean delete){
        ArrayList<SocketChannel> tempList = subscriptionMap.get(topic);
        if(delete) tempList.remove(subscriber);
        else
        {
            try {
                tempList.add(subscriber);
            }catch (NullPointerException n){
                n.printStackTrace();
            }
        }
            subscriptionMap.put(topic, tempList);
    }

    private static void printMap(){
        System.out.println("Aktualne subskrypcje:");
        subscriptionMap.remove("5");
        mapsOfTopics.remove("5");
        for (Map.Entry<String, ArrayList<SocketChannel>> pairs : subscriptionMap.entrySet()) {
            if(!pairs.getKey().equals("")) System.out.println(pairs.getKey() + " = " + pairs.getValue());
        }
    }

}



