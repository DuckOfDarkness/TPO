
package zad2;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Service {

    private final String alpha2CodeOfCountry;
    private static String choseCurrency, currencyCode, city, country, sky, wind, temp, pressure, humidity, rate1, rate2;

    public Service(String kraj) {
        Service.country = kraj;
        Locale.setDefault(Locale.ENGLISH);

        Map<String, String> getAlpha2CodeMap = new HashMap<>();
        for (String iso : Locale.getISOCountries()) {
            Locale l = new Locale("", iso);
            getAlpha2CodeMap.put(l.getDisplayCountry(), iso);
        }

        //alpha2CodeOfCountry = getAlpha2Code(kraj);
        alpha2CodeOfCountry = getAlpha2CodeMap.get(kraj);
        System.out.println("Country: " + alpha2CodeOfCountry);
        currencyCode = String.valueOf(Currency.getInstance(new Locale("", alpha2CodeOfCountry)));
        System.out.println("Kod Waluty: " + currencyCode);
    }

    public String getWeather(String miasto) {

        String keyAPI = "aaa82991268dc1675a588bf8964074b1";
        String source = "https://api.openweathermap.org/data/2.5/weather?q=" + miasto + "," + alpha2CodeOfCountry + "&APPID=" + keyAPI;
        URL url;
        String result = "";

        String filename = alpha2CodeOfCountry + "_" + miasto + "_" + "weather.json";
        //Tworzenie pliku zawierajacego dane pogodowe + mapy
        try {
            getContent(new URL(source), filename);
            url = new URL(source);
            result = getStringFromURL(url);
            show(result);
            infoInitiate(result);

        } catch (IOException mal) {
            mal.printStackTrace();
        }

        return result;
    }

    public double getRateFor(String kod_waluty) {
        URL url;
        String filename = "exchangerate.json";
        String fullText = "";
        choseCurrency = kod_waluty;

        try {
            url = new URL("https://api.exchangerate.host/convert?from=" + currencyCode + "&to=" + kod_waluty);
            getContent(url, filename);
            Map<String, Double> exchangeMap = getGsonStringDoubleMap(filename);
            fullText = String.valueOf(exchangeMap.get("info"));
        } catch (IOException i) {
            i.printStackTrace();
        }
        System.out.println("Rate: " + getExtractionValue(fullText, "[0-9]+.[0-9]+"));
        double resault = getExtractionValue(fullText, "[0-9]+.[0-9]+");
        rate1 = String.valueOf(resault);
        return resault;
    }

    public double getNBPRate() {
        String resault = " ";
        URL url;
        URL url2 = null;
        if (alpha2CodeOfCountry.equals("PL") || alpha2CodeOfCountry.equals("pl")) {
            rate2 = "1.0";
            System.out.println("PLN rate: " + rate2);
            System.out.println("///////////////////////////////////////////");
            return Double.parseDouble(rate2);
        } else {
            try {
                url = new URL("http://www.nbp.pl/kursy/kursya.html");
                url2 = new URL("http://www.nbp.pl/kursy/kursyb.html");
                resault = getExtractionString(getStringFromURL(url), currencyCode + "</td>\\s+<td class\\=\"right\">[0-9]+,[0-9]+", "[0-9]+,[0-9]+");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException i) {
                resault = getExtractionString(getStringFromURL(url2), currencyCode + "</td>\\s+<td class\\=\"right\">[0-9]+,[0-9]+", "[0-9]+,[0-9]+");
            }
        }
        //Zamiana przecinka na kropke
        for (int i = 0; i < resault.length(); i++) {
            if (resault.charAt(i) == ',') {
                resault = resault.substring(0, i) + "." + resault.substring(i + 1);
            }
        }
        rate2 = resault;
        System.out.println("PLN rate: " + rate2);
        System.out.println("///////////////////////////////////////////");
        return Double.parseDouble(rate2);
    }

    //A method for searching numeric values with regexes
    public double getExtractionValue(String fullText, String phrase, String searched) {
        //Searches entire text for a phrase
        Pattern pattern = Pattern.compile(phrase);
        Matcher matcher = pattern.matcher(fullText);
        matcher.find();
        String x = matcher.group();
        //Extracts a value from a phrase
        pattern = Pattern.compile(searched);
        matcher = pattern.matcher(x);
        matcher.find();
        String z = matcher.group();
        return Double.parseDouble(z);
    }

    public double getExtractionValue(String fullText, String searched) {
        //Searches entire text for a phrase
        Pattern pattern = Pattern.compile(searched);
        Matcher matcher = pattern.matcher(fullText);
        matcher.find();
        String x = matcher.group();
        return Double.parseDouble(x);
    }

    //A method for searching numeric values with regexes
    public String getExtractionString(String fullText, String phrase, String searched) {
        //Searches entire text for a phrase
        Pattern pattern = Pattern.compile(phrase);
        Matcher matcher = pattern.matcher(fullText);
        matcher.find();
        String x = matcher.group();
        //Extracts a value from a phrase
        pattern = Pattern.compile(searched);
        matcher = pattern.matcher(x);
        matcher.find();
        return matcher.group();
    }

    //Method for retrieving the contents of a link to a file with the given name
    public void getContent(URL url, String filename) throws MalformedURLException, FileNotFoundException {
        File weather = new File(filename);
        FileOutputStream fileOutputStream = new FileOutputStream(weather);
        try (InputStream inputStream = url.openStream();
             ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream)) {
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            System.out.println("[getContent: Blad] Strona nie odnaleziona. Prawdopodbnie pomyliles sie w wprowadzaniu danych.");
        }
    }

    //Create a map from json
    public Map<String, String> getGsonStringStringMap(String patchOfJsonFile) throws IOException {
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get(patchOfJsonFile));
        Map<String, String> map = gson.fromJson(reader, Map.class);
        reader.close();
        return map;
    }

    public Map<String, Double> getGsonStringDoubleMap(String patchOfJsonFile) throws IOException {
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get(patchOfJsonFile));
        Map<String, Double> map = gson.fromJson(reader, Map.class);
        reader.close();
        return map;
    }

    //Retrieving a string from url
    public String getStringFromURL(URL url) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null)
                result.append(line);
        } catch (IOException e) {
            System.out.println("[getStringFromURL: Blad] Strona nie odnaleziona. Prawdopodbnie pomyliles sie w wprowadzaniu danych.");
        }
        return result.toString();
    }

    public double convertKelwinToCelsjusz(double kelvin) {
        return Math.round((kelvin - 273.15) * 100.0) / 100.0;
    }

    public void show(String s) throws IOException {
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(s, new TypeToken<Map<String, Object>>() {
        }.getType());
        map.forEach((x, y) -> System.out.println(x + " : " + y));
    }

    //initiating weather information
    public void infoInitiate(String s) throws IOException {
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(s, new TypeToken<Map<String, Object>>() {
        }.getType());
        //city, country, sky, wind, temperature, feels_like, temp_min, temp_max, pressure, humidity;
        String[] values = {"temp", "feels_like", "temp_min", "temp_max", "pressure", "humidity"};
        String weatherInfo = (map.get("main")).toString();
        for (int i = 0; i < values.length; i++) {
            if (i == 0 || i == 1 || i == 2) {
                values[i] = String.valueOf(convertKelwinToCelsjusz(getExtractionValue(weatherInfo, values[i] + "=[0-9]+.[0-9]+", "[0-9]+.[0-9]+")));
            } else {
                values[i] = String.valueOf(getExtractionValue(weatherInfo, values[i] + "=[0-9]+.[0-9]+", "[0-9]+.[0-9]+"));
            }
        }
        city = (map.get("name")).toString();
        temp = values[0];
        pressure = values[4];
        humidity = values[5];
        wind = String.valueOf(getExtractionString(String.valueOf(map.get("wind")), "speed=[0-9]+.[0-9]+", "[0-9]+.[0-9]"));
        sky = String.valueOf(getExtractionString(String.valueOf(map.get("weather")), "description=[a-zA-Z]+ ?[a-zA-Z]+", "[^description][a-zA-Z]+ ?[a-zA-Z]+"));
        if (sky.charAt(0) == '=') {
            sky = sky.substring(1);
        }
        System.out.println(sky);
    }

    public static String getCity() {
        return city;
    }

    public static String getCountry() {
        return country;
    }

    public static String getSky() {
        return sky;
    }

    public static String getWind() {
        return wind;
    }

    public static String getTemp() {
        return temp;
    }

    public static String getPressure() {
        return pressure;
    }

    public static String getHumidity() {
        return humidity;
    }

    public static String getRate1() {
        return rate1;
    }

    public static String getRate2() {
        return rate2;
    }

    public static String getChoseCurrency() {
        return choseCurrency;
    }

    public static String getCurrencyCode() {
        return currencyCode;
    }
}