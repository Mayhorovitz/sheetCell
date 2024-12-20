package servlets.sheetView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.api.SheetDTO;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

@WebServlet("/dynamicAnalysis")
public class DynamicAnalysisServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String cellValuesJson = request.getParameter("cellValues");

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Double>>() {}.getType();
            Map<String, Double> cellValues = gson.fromJson(cellValuesJson, type);

            Engine engine = ServletUtils.getEngine(getServletContext());
            SheetDTO temporarySheetDTO = engine.performDynamicAnalysis(sheetName, cellValues);
            String sheetJson = gson.toJson(temporarySheetDTO);
            response.setContentType("application/json");
            response.getWriter().write(sheetJson);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(e.getMessage());
        }
    }
}


