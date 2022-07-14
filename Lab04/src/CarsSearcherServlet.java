import com.google.gson.Gson;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@WebServlet(name = "carsSearcherServlet", value = "/carsSearcherServlet")
public class CarsSearcherServlet extends HttpServlet implements Serializable{


    private static final String filePatch = "cars.bin";     //C:\apache-tomcat-9.0.62\bin\cars.bin

    public CarsSearcherServlet() {
    }

    public void init() {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        serviceRequest(request, response);
    }

    public void serviceRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        String charset = "UTF-8";
        req.setCharacterEncoding(charset);
        res.setContentType("text/html; charset=" + charset);

        PrintWriter out = res.getWriter();

        String type = null;
        Enumeration<String> pnams = req.getParameterNames();
        while (pnams.hasMoreElements()) {
            String name = pnams.nextElement();
            String value = req.getParameter(name);
            type = value.trim();//.toLowerCase(Locale.ROOT);
            if(type.length() <= 3) type = value.trim().toUpperCase(Locale.ROOT);
            else type = value.trim();
        }

        List<String> lista;
        assert type != null;
        type = type.trim();
        if(type.equals("")) {
            type = "all";
            lista = searchForATypeCar();
            System.out.println(lista);
        }else{
            lista = searchForATypeCar(type);
        }

            String gson = new Gson().toJson(lista);

            req.setAttribute("CarsFound", gson);
            req.setAttribute("Type", type);

            RequestDispatcher requestDispatcher = req.getRequestDispatcher("tableCreatorServlet");
            requestDispatcher.forward(req, res);

        out.close();
    }
    private List<String> searchForATypeCar(String type){
        List<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(","+type+",");
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filePatch), StandardCharsets.UTF_8)){
            String line;
            while((line = bufferedReader.readLine()) != null){
                if(pattern.matcher(line).find()) {
                    list.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    private List<String> searchForATypeCar(){
        List<String> list = new ArrayList<>();
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filePatch), StandardCharsets.UTF_8)){
            String line;
            while((line = bufferedReader.readLine()) != null){
                    list.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}