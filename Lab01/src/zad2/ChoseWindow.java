package zad2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChoseWindow extends JFrame implements ActionListener {

    private final JTextField[] jTextFields;
    private final JButton[] jButtons;
    private static String[] tempJTextFieldDefaultText;

    ChoseWindow() {
        super("Wprowadz dane");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(300, 200));
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2));
        setVisible(true);
        pack();

        JLabel[] jLabel = new JLabel[3];
        String[] jLabelName = {" Kraj", " Miasto", " Waluta"};

        tempJTextFieldDefaultText = new String[]{Service.getCountry(), Service.getCity(), Service.getCurrencyCode()};

        jTextFields = new JTextField[3];
        String[] jTextFieldDefaultText = {Service.getCountry(), Service.getCity(), Service.getChoseCurrency()};

        jButtons = new JButton[2];
        String[] jButtonsName = {"Zatwierdz", "Anuluj"};

        for (int i = 0; i < jLabelName.length; i++) {
            jLabel[i] = new JLabel(jLabelName[i]);
            jTextFields[i] = new JTextField(jTextFieldDefaultText[i]);
            if (i > 0) jButtons[i - 1] = new JButton(jButtonsName[i - 1]);
        }
        for (int i = 0; i < 5; i++) {
            if (i < 3) jTextFields[i].addActionListener(this);
            else jButtons[i - 3].addActionListener(this);
        }

        int labelCunter = 0, textFieldCouter = 0, buttonCounter = 0;

        for (int i = 0; i < 8; i++) {
            if (i % 2 == 0 && i < 6) {
                add(jLabel[labelCunter]);
                labelCunter++;
            } else if (i % 2 != 0 && i < 6) {
                add(jTextFields[textFieldCouter]);
                textFieldCouter++;
            } else {
                add(jButtons[buttonCounter]);
                buttonCounter++;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == jButtons[0]) {
            try {
                Service newService = new Service(jTextFields[0].getText());
                newService.getWeather(jTextFields[1].getText());
                newService.getRateFor(jTextFields[2].getText());
                newService.getNBPRate();
                this.dispose();
                MyFrame.setFX(jTextFields[1].getText());
                String[] newData = new String[]{Service.getCity(), Service.getSky(), Service.getTemp(), Service.getPressure(), Service.getHumidity(), Service.getWind(), Service.getRate1(), Service.getRate2()};
                MyFrame.setLabels(newData);

            } catch (NullPointerException n) {
                for (int i = 0; i < jTextFields.length; i++) {
                    jTextFields[i].setText(tempJTextFieldDefaultText[i]);
                }
                JOptionPane.showMessageDialog(null, "[NullPointerException] Wprowadzono bledne dane. \nPamietaj by wpisac nazwe kraju i miasta po angielsku!", "Blad!", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalStateException il) {
                for (int i = 0; i < jTextFields.length; i++) {
                    jTextFields[i].setText(tempJTextFieldDefaultText[i]);
                }
                JOptionPane.showMessageDialog(null, "[IllegalStateException] Wprowadzono bledne dane. \nPamietaj by wpisac nazwe kraju i miasta po angielsku!", "Blad!", JOptionPane.ERROR_MESSAGE);

            }
        }
        if (source == jButtons[1]) {
            this.dispose();
        }
    }
}
