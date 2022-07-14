import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class AdminPublisherGUI extends JFrame implements ActionListener {

    private static JButton add, remove, addNews, showMessage;
    private static JComboBox listOfTopics;

    public AdminPublisherGUI() {
        setPreferredSize(new Dimension(200, 200));
        setLocation(100, 100);
        setResizable(false);
        setLayout(new GridLayout(5, 1));

        //------JComboBox--------------------

        listOfTopics = new JComboBox<>();
        drawList();

        listOfTopics.addActionListener(this);
        add(listOfTopics);

        //------JButtons--------------------

        add = new JButton("Dodaj temat");
        remove = new JButton("Usun temat");
        addNews = new JButton("Dodaj nowa wiadomosc");
        showMessage = new JButton("Zobacz wiadomosci");

        add.addActionListener(this);
        remove.addActionListener(this);
        addNews.addActionListener(this);
        showMessage.addActionListener(this);

        add(add);
        add(remove);
        add(addNews);
        add(showMessage);

        pack();
        setVisible(true);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                AdminPublisher.disconnect();
            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source == remove){
            try {
                if(!(listOfTopics.getSelectedItem() == null)) {
                    if (JOptionPane.showOptionDialog(null, "Czy na pewno chcesz usunac ten temat?", "Usuwanie tematu", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) != 1) {
                        AdminPublisher.deleteTopic(Objects.requireNonNull(listOfTopics.getSelectedItem()).toString());
                        JOptionPane.showMessageDialog(null, "Temat usuniety", "Usuwanie", JOptionPane.INFORMATION_MESSAGE);
                        drawList();
                    }
                }else {
                    JOptionPane.showMessageDialog(null, "Lista tematow jest pusta", "Blad", JOptionPane.ERROR_MESSAGE);
                }
            }catch (NullPointerException exc){
                exc.printStackTrace();
            }
        }
        if(source == add){
            String topic = JOptionPane.showInputDialog(null, "Dodaj nowy temat", "Nowy temat", JOptionPane.INFORMATION_MESSAGE);
            try {
                if(!topic.trim().equals("")){
                    AdminPublisher.addTopic(topic, new ArrayList<>());
                    drawList();
                    JOptionPane.showMessageDialog(null, "Temat dodany", "Nowy temat", JOptionPane.INFORMATION_MESSAGE);
                }
                else JOptionPane.showMessageDialog(null, "Nazwa tematu nie moze byc pusta", "Blad", JOptionPane.ERROR_MESSAGE);
            }catch (NullPointerException n){
            }
        }
        if(source == addNews){
            if(!(listOfTopics.getSelectedItem() == null)) {
                String message = JOptionPane.showInputDialog(null, "Dodaj nowa wiadomosc", "Temat: " + Objects.requireNonNull(listOfTopics.getSelectedItem()), JOptionPane.INFORMATION_MESSAGE);
                try {
                    if (!message.trim().equals("")) {
                        AdminPublisher.addNewsMessage(Objects.requireNonNull(listOfTopics.getSelectedItem()).toString(), message);
                        JOptionPane.showMessageDialog(null, "Wiadomosc dodana", "Wiadomosc", JOptionPane.INFORMATION_MESSAGE);
                    } else
                        JOptionPane.showMessageDialog(null, "Tresc wiadomosci nie moze byc pusta", "Blad", JOptionPane.ERROR_MESSAGE);
                } catch (NullPointerException | IOException n) {
                    n.printStackTrace();
                }
            }else{
                JOptionPane.showMessageDialog(null, "Lista tematow jest pusta", "Blad", JOptionPane.ERROR_MESSAGE);
            }
        }
        if(source == showMessage){
            if(!(listOfTopics.getSelectedItem() == null)) {
                String selectedTopic = Objects.requireNonNull(listOfTopics.getSelectedItem()).toString();
                JOptionPane.showMessageDialog(null, AdminPublisher.getMessage(selectedTopic), "Temat: " + selectedTopic, JOptionPane.PLAIN_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(null, "Lista tematow jest pusta", "Blad", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void drawList(){
        listOfTopics.setModel(new DefaultComboBoxModel<>(AdminPublisher.getMapsOfTopics().keySet().toArray(new String[0])));
    }
}
