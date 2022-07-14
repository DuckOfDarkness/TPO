import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "tableCreatorServlet", value = "/tableCreatorServlet")
public class TableCreatorServlet extends HttpServlet {

    public void init() {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        serviceRequest(request, response);
    }

    public void serviceRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher rd=req.getRequestDispatcher("/index.jsp");
        rd.include(req, resp);

        String charset = "UTF-8";
        req.setCharacterEncoding(charset);
        resp.setContentType("text/html; charset=" + charset);

        PrintWriter out = resp.getWriter();

        String requestString = (String) req.getAttribute("CarsFound");
        String type = (String) req.getAttribute("Type") ;

        if(requestString.equals("[]")) {
            if(type.equals("empty")){
                out.println("<b><h2>Pole wyszukiwania nie moze być puste.</h2></b>");
            }else{
                out.println("<b><h2>Nie znaleziono samochodów typu  " + type + ".</h2></b>");
            }
        }else{
            Gson gson = new Gson();
            List<String> list = gson.fromJson(requestString, new TypeToken<List<String>>(){}.getType());
            for(String s : list) System.out.println(s);

            if(type.equals("all")){
                out.println("<b><h2>Wszystkie samochody:</h2></b>");
            }else{
                out.println("<b><h2>Znalezione samochody typu "+type+":</h2></b>");
            }
            out.write("<p><table border = '1'><tr><td><b><center>&nbspNazwa&nbsp</center></b></td><td><b><center>&nbspMarka&nbsp</center></b></td>" +
                    "<td><b><center>&nbspTyp&nbsp</center></b></td><td><b><center>&nbspTyp nadwozia&nbsp</center></b></td><td><b><center>&nbspRok produkcji&nbsp</center></b></td><td><b><center>&nbspPojemność skokowa&nbsp</center></b></td><td><b><center>&nbspMoc&nbsp</center></b></td></tr>");
            for(String s : list){
                String[] element = s.split(",");
                for (int i = 0; i < element.length; i++) {
                    out.write("<td><center>&nbsp"+element[i]+"&nbsp</center></td>");
                }
                out.write("</tr>");
            }
            out.write("</table></p>");
        }


    }
}