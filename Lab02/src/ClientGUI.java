import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

public class ClientGUI extends JFrame implements ActionListener {

    private static JButton confirmButton, cleanButton;
    private static JTextField word, iso, resault;
    private static JMenuItem exit;
    private final int portNumber;

    /**CONFIGURATION DATA**/

    public static void main(String[] args) {

        int portNumber = 10005; //10005 - 65535
        new ClientGUI(portNumber);
    }

    /***********************/

    public ClientGUI(int portNumber){
        this.portNumber = portNumber;
        setPreferredSize(new Dimension(200, 300));
        int locationXY = 50;
        setLocation(locationXY, locationXY);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(9, 1));

        //~~~~~~~TOP MENU~~~~~~//
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        exit = new JMenuItem("Zamknij");
        exit.addActionListener(this);
        menuBar.add(menu);
        menu.add(exit);
        setJMenuBar(menuBar);
        //~~~~~~~~~~~~~~~~//

        JLabel resaultName = new JLabel("Tłumaczenie");
        resault = new JTextField("Wynik");

        JLabel jField1 = new JLabel("Słowo do przetlumaczenia");
        word = new JTextField();
        word.setText("");

        JLabel jField2 = new JLabel("Kod ISO kraju");
        iso = new JTextField();
        iso.setText("");

        confirmButton = new JButton("Tłumacz");
        cleanButton = new JButton("Czyść");

        //Actionlistener
        word.addActionListener(this);
        iso.addActionListener(this);
        confirmButton.addActionListener(this);
        cleanButton.addActionListener(this);

        //ADD
        jPanel.add(resaultName);
        jPanel.add(resault);
        jPanel.add(jField1);
        jPanel.add(word);
        jPanel.add(jField2);
        jPanel.add(iso);
        jPanel.add(confirmButton);
        jPanel.add(cleanButton);


        jPanel.setVisible(true);
        add(jPanel);
        setVisible(true);
        setResizable(false);
        pack();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(source == cleanButton){
            word.setText("");
            iso.setText("");
        }
        if(source == confirmButton){
            if(!stringValidation(word.getText()) || !stringValidation(iso.getText())){
                stringError();
                word.setText("");
                iso.setText("");
            }else{
                ClientService.send(word.getText().trim()+","+iso.getText().toUpperCase(Locale.ROOT).trim()+","+portNumber, portNumber);
            }
        }
        if(source == exit){
            this.dispose();
        }
    }

    public static boolean stringValidation(String s){
        return !s.equals("") && !s.equals(" ");
    }

    public static void wrongDictionary(){
        JOptionPane.showMessageDialog(null, "W systemie nie zarejestrowano slownika dla podanego kraju", "Blad!", JOptionPane.ERROR_MESSAGE);
    }

    public static void noTranslate(){
        JOptionPane.showMessageDialog(null, "W systemie nie zarejestrowano tlumaczenia dla podanego slowa", "Blad!", JOptionPane.ERROR_MESSAGE);
    }

    public static void stringError(){
        JOptionPane.showMessageDialog(null, "Pole nie moze być puste", "Blad!", JOptionPane.ERROR_MESSAGE);
    }

    public static void setAnswer(String word){
        resault.setText(word);
    }

}
