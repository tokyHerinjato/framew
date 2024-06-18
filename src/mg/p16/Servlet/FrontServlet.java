package mg.p16.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;

import java.lang.reflect.*;

import mg.p16.annotations.*;
import mg.p16.utils.*;

public class FrontServlet extends HttpServlet {
    private List<String> controllers;
    private HashMap<String, Mapping> map;

    @Override
    public void init() throws ServletException {
        String packageToScan = this.getInitParameter("package");
        try {
            this.controllers = new Function().getAllclazzsStringAnnotation(packageToScan, AnnotationController.class);
            this.map = new Function().scanControllersMethods(this.controllers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String path = new Function().getURIWithoutContextPath(request);

        if (path.contains("?")) {
            int index = path.indexOf("?");
            path = path.substring(0, index);
        }

        if (map.containsKey(path)) {
            Mapping m = map.get(path);
            try {
                Class<?> clazz = Class.forName(m.getClassName());
                Method[] methods = clazz.getDeclaredMethods();
                Method targetMethod = null;

                for (Method method : methods) {
                    if (method.getName().equals(m.getMethodName())) {
                        targetMethod = method;
                        break;
                    }
                }

                if (targetMethod != null) {
                    Object result = targetMethod.invoke(clazz.newInstance(), params);

                    if (result instanceof String) {
                        out.println(
                                "Resultat de l'execution de la méthode " + " " + m.getMethodName() + " est " + result);
                    } else if (result instanceof ModelView) {
                        ModelView modelView = (ModelView) result;
                        String destinationUrl = modelView.getUrl();
                        HashMap<String, Object> data = modelView.getData();
                        for (String key : data.keySet()) {
                            request.setAttribute(key, data.get(key));
                        }
                        RequestDispatcher dispatcher = request.getRequestDispatcher(destinationUrl);
                        dispatcher.forward(request, response);
                    } else {
                        out.println("Le type de retour n'est ni un String ni un ModelView");
                    }
                } else {
                    out.println("Méthode non trouvée : " + m.getMethodName());
                }
            } catch (Exception e) {
                out.println("Erreur lors de l'exécution de la méthode : " + e.getMessage());
                e.printStackTrace(out);
            }
        } else {
            out.println("404 NOT FOUND");
        }
    }
}
