import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger log = Logger.getLogger(Client.class.getName());
    private static final ArrayList<String> topics = new ArrayList<>();
    private static ArrayList<String> topicsAvalaible = new ArrayList<>();
    private static ArrayList<String> messageList = new ArrayList<>();
    private static SocketChannel channel;
    private static Charset charset;

    public static void main(String[] args) throws IOException {
        new ClientGUI();
        connect();
    }

    public static void addTopic(String topic){
        if(!topics.contains(topic)){
            topics.add(topic);
            ClientGUI.messageDialog("Subskrypcja tematu \""+topic+"\" została dodana", "Subskrypcja", 1);
            try {
                Client.subscribe(topic);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }else{
            ClientGUI.messageDialog("Ten temat znajduje sie juz na liscie subskrypcji", "Subskrypcja", 1);
        }
    }

    public static void deleteTopic(String topic){
        if(!topics.contains(topic) && topic != null){
            ClientGUI.messageDialog("Nie subskrybujesz podanego tematu", "Blad usuwania subskrypcji", 2);
        }
        else if(topic !=null){
            ClientGUI.messageDialog("Subskrypcja zostala usunieta.", "Subskrypcja", 1);
            topics.remove(topic);
            try {
                unSubscribe(topic);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void subscribe(String topic) throws IOException {
        channel.write(charset.encode("2,"+topic+"\n"));
    }

    public static void unSubscribe(String topic) throws IOException {
        channel.write(charset.encode("3,"+topic+"\n"));
    }

    public static ArrayList<String> getTopicsAvalaible(){
        return topicsAvalaible;
    }

    private static void connect() throws IOException {
        channel = null;
        String server = "localhost";
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            int port = 10000;
            channel.connect(new InetSocketAddress(server, port));
            log.log(Level.INFO, "[Client] Lączę się z serwerem\n");

            while (!channel.finishConnect()) {
            }

        } catch(UnknownHostException exc) {
            System.err.println("Uknown host " + server);
        } catch(Exception exc) {
            connect();
        }

        log.log(Level.INFO, "[Client] Nawiazano polaczenie\n");

        charset  = Charset.forName("ISO-8859-2");
        int rozmiar_bufora = 1024;
        ByteBuffer inBuf = ByteBuffer.allocateDirect(rozmiar_bufora);
        CharBuffer cbuf;

        log.log(Level.INFO, "[Client] wysyłam zapytanie o liste dostepnych tematow.\n");
        channel.write(charset.encode("1\n"));

        while (true) {
            inBuf.clear();
            int readBytes = 0;
            try {
                readBytes = channel.read(inBuf);
            }catch (IOException e){
                log.log(Level.INFO, "[Client] Utracilem polaczenie z serwererm. Nawiązuje nowe polaczenie.\n");
                topicsAvalaible.clear();
                connect();
            }
            if (readBytes == 0) {
                continue;
            }
            else if (readBytes == -1) {
                break;
            }
            else {
                inBuf.flip();
                cbuf = charset.decode(inBuf);

                String odSerwera = cbuf.toString();

                System.out.println("Wiadomosc przychodzaca:" + odSerwera);
                extractMessage(odSerwera);

                if(odSerwera.charAt(0) == '1')
                {
                    if(odSerwera.endsWith(",")){
                        ClientGUI.setYourT("");
                    }
                    messageList.remove(0);
                   if(messageList.get(0).equals("")){
                       messageList.remove(0);
                   }
                    topicsAvalaible = messageList;
                    messageList = new ArrayList<>();
                    ClientGUI.drawList();
                }
                if(odSerwera.charAt(0) == '2'){
                    StringBuilder content = new StringBuilder();
                    String topic = messageList.get(1);
                    messageList.remove(0);
                    messageList.remove(0);

                    for(String s : messageList){
                        content.append(s).append("\n");
                    }
                    if(messageList.size()>1)
                    ClientGUI.updateContent(topic, content.toString());
                    messageList = new ArrayList<>();
                }

                if(odSerwera.charAt(0) == 'A'){
                    ClientGUI.updateContent(messageList.get(1), messageList.get(2));
                    messageList = new ArrayList<>();
                }
                if(odSerwera.charAt(0) == '7'){
                    if(odSerwera.endsWith(",")){
                        ClientGUI.setYourT("");
                    }
                    messageList.remove(0);
                    if(messageList.get(0).equals("")){
                        messageList.remove(0);
                    }
                    topicsAvalaible.removeIf(s -> s.equals(messageList.get(0)));
                    topics.removeIf(s -> s.equals(messageList.get(0)));

                    messageList = new ArrayList<>();
                    ClientGUI.drawList();
                    ClientGUI.setYourT(showTopics());

                }
                cbuf.clear();
                if (odSerwera.equals("4")) {
                    break;
                }
            }
        }
        System.exit(0);
    }

    private static void extractMessage(String message){
        if(message.contains(",")){
            int index = message.indexOf(",");
            messageList.add(message.substring(0,index));
            extractMessage(message.substring(index+1));
        }
        else messageList.add(message);
    }

    public static String showTopics(){
        StringBuilder yourTopics = new StringBuilder();
        for(String s : topics){
            yourTopics.append(s);
            if(!Objects.equals(s, topics.get(topics.size() - 1))){
                yourTopics.append(", ");
            }
        }
        return yourTopics.toString();
    }

    public static void disconnect(){
        try {
            if(channel.isConnected()) {
                channel.write(charset.encode("4\n"));
            }else System.exit(0);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}