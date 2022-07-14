import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class ClientGUI extends JFrame implements ActionListener {

    private static JMenuItem exit;
    private static JTextField yourT;
    public static JTextArea jTextArea;
    private static JComboBox boxOfTopics;
    private static JButton subs, unsubs;
    public ClientGUI(){

        //Frame setting
        super("Wiadomosci");
        setPreferredSize(new Dimension(300, 500));
        setLocation(50, 50);
        setResizable(false);
        setLayout(new BorderLayout());

        //JMenu
        JMenuBar jMenuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        exit = new JMenuItem("Wyjscie");
        menu.add(exit);
        jMenuBar.add(menu);
        setJMenuBar(jMenuBar);

        //--------------NorthJpanel
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(6, 1));

        JLabel avalaibleTopicsLabel = new JLabel("Dostepne tematy");
        boxOfTopics = new JComboBox<>();
        JLabel yourLabel = new JLabel("Subskrybowane tematy");
        yourT = new JTextField();
        yourT.setEditable(false);
        subs = new JButton("Dodaj subskrypcje");
        unsubs = new JButton("Usun subskrypcje");

        jPanel.add(avalaibleTopicsLabel);
        jPanel.add(boxOfTopics);
        jPanel.add(yourLabel);
        jPanel.add(yourT);
        jPanel.add(subs);
        jPanel.add(unsubs);

        add(jPanel, BorderLayout.NORTH);

        //--------------SouthJPanel
        JPanel southPanel = new JPanel();
       jTextArea = new JTextArea();
       jTextArea.setLineWrap(true);
       jTextArea.setWrapStyleWord(true);
       jTextArea.setEditable(false);
       jTextArea.setSize(10, 10);
       JScrollPane scrollPane = new JScrollPane(jTextArea);
       southPanel.add(scrollPane, jTextArea);
       add(scrollPane, BorderLayout.CENTER);

       //Action

        boxOfTopics.addActionListener(this);
        subs.addActionListener(this);
        unsubs.addActionListener(this);
        exit.addActionListener(this);

        drawList();
        setVisible(true);
        pack();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Client.disconnect();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(source == subs){
            if(boxOfTopics.getSelectedItem() != null) {
                Client.addTopic((Objects.requireNonNull(boxOfTopics.getSelectedItem()).toString()));
                yourT.setText(Client.showTopics());
            }else if(boxOfTopics.getSelectedItem() == null || Client.getTopicsAvalaible().isEmpty())
            {
                messageDialog("Lista tematow jest pusta!", "Blad", 0);
            }

        }
        if(source == unsubs){
            if(boxOfTopics.getSelectedItem() != null) {
                Client.deleteTopic((Objects.requireNonNull(boxOfTopics.getSelectedItem()).toString()));
                yourT.setText("");
                yourT.setText(Client.showTopics());
            }else if(boxOfTopics.getSelectedItem() == null || Client.getTopicsAvalaible().isEmpty())
        {
            messageDialog("Lista tematow jest pusta!", "Blad", 0);
        }
        }
        if(source == exit){
            Client.disconnect();
            System.exit(0);
        }
    }

    public static void setYourT(String s){
        yourT.setText(s);
    }

    //ERROR_MESSAGE(0), WARNING_MESSAGE(2), INFORMATION_MESSAGE(1), QUESTION_MESSAGE(3)
    public static void messageDialog(String message, String title, int type){
        JOptionPane.showMessageDialog(null, message, title, type);
    }

    public static void updateContent(String topic, String content){
        jTextArea.append("\n@"+topic+": "+content);
    }

    public static void drawList(){
        boxOfTopics.setModel(new DefaultComboBoxModel<>(Client.getTopicsAvalaible().toArray(new String[0])));
    }


}
