import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "formHandlerServlet", value = "/formHandlerServlet")
public class FormHandlerServlet extends HttpServlet {

    public void init() {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        serviceRequest(request, response);
    }
    public void serviceRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        String charset = "UTF-8";
        req.setCharacterEncoding(charset);
        resp.setContentType("text/html; charset=" + charset);

        PrintWriter out = resp.getWriter();

        RequestDispatcher requestDispatcher = req.getRequestDispatcher("carsSearcherServlet");
        requestDispatcher.forward(req, resp);
        out.close();
    }

    public void destroy() {
    }
}