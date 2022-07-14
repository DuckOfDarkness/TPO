package zad2;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MyFrame extends JFrame implements ActionListener {

    private static final JDesktopPane jDesktopPane = new JDesktopPane();
    private final JMenuItem exit, change;
    private static String city, sky, temp, pressure, humindity, wind, current, currentPLN;
    private static JFXPanel fxPanel;
    private static JLabel[] weatherLabelsName;
    private static String[] weatherName;

    public MyFrame() {
        super("Dane");
        JFrame frame = new JFrame("TPO: Zadanie 2");

        city = Service.getCity();
        sky = Service.getSky();
        temp = Service.getTemp();
        pressure = Service.getPressure();
        humindity = Service.getHumidity();
        wind = Service.getWind();
        current = Service.getRate1();
        currentPLN = Service.getRate2();


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.setLocation(20, 20);
        frame.setLayout(new BorderLayout());

        setLocation(900, 70);

        //----------top menu
        JMenuBar mb = new JMenuBar();

        JMenu menu = new JMenu("Menu");
        mb.add(menu);
        JMenuBar menuBar = new JMenuBar();

        change = new JMenuItem("Zmien dane");
        change.addActionListener(this);
        exit = new JMenuItem("Zakończ");
        exit.addActionListener(this);
        menuBar.add(menu);
        menu.add(change);
        menu.add(exit);
        setJMenuBar(menuBar);
        //--------------------


        //------Wikipedia
        fxPanel = new JFXPanel();
        fxPanel.setSize(new Dimension(800, 600));


        frame.add(fxPanel);

        frame.add(jDesktopPane);

        JPanel data = new JPanel();
        data.setLayout(new GridLayout(8, 2));
        weatherName = new String[]{" Miasto: ", city, " Niebo: ", sky, " Temperatura: ", temp, " Cisnienie: ", pressure, " Wiglotność: ", humindity,
                " Wiatr: ", wind, " Kurs waluty: ", current, " Kurs PLN: ", currentPLN};
        weatherLabelsName = new JLabel[weatherName.length];

        for (int j = 0; j < weatherLabelsName.length; j++) {
            weatherLabelsName[j] = new JLabel(weatherName[j]);
            data.add(weatherLabelsName[j]);
        }
        add(data);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(250, 200));
        setResizable(false);
        setVisible(true);
        pack();

        add(jDesktopPane);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.pack();

        Platform.runLater(() -> initFX(fxPanel, Service.getCity()));
    }

    public static void setLabels(String[] newData) {
        for (int i = 0; i < weatherName.length; i++) {
            if (i % 2 != 0)
                weatherLabelsName[i].setText(newData[i / 2]);
        }
    }

    public static void setFX(String city) {
        Platform.runLater(() -> initFX(fxPanel, city));
    }

    private static void initFX(JFXPanel fxPanel, String city) {
        Group group = new Group();
        Scene scene = new Scene(group);
        fxPanel.setScene(scene);
        WebView webView = new WebView();
        group.getChildren().add(webView);
        WebEngine webEngine = webView.getEngine();
        webEngine.load("https://en.wikipedia.org/wiki/" + city);

    }

    public static void setCity(String city) {
        MyFrame.city = city;
    }

    public static void setSky(String sky) {
        MyFrame.sky = sky;
    }

    public static void setTemp(String temp) {
        MyFrame.temp = temp;
    }

    public static void setPressure(String pressure) {
        MyFrame.pressure = pressure;
    }

    public static void setHumindity(String humindity) {
        MyFrame.humindity = humindity;
    }

    public static void setWind(String wind) {
        MyFrame.wind = wind;
    }

    public static void setCurrent(String current) {
        MyFrame.current = current;
    }

    public static void setCurrentPLN(String currentPLN) {
        MyFrame.currentPLN = currentPLN;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == exit) {
            System.exit(0);
        }
        if (source == change) {
            new ChoseWindow();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Frame::new);

    }
}
