package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.MultipartConfig;

import annotations.*;

import com.google.gson.Gson;

import java.lang.reflect.*;

import utils.Mapping;
import utils.*;

public class FrontController extends HttpServlet {
    private List<String> controllers;
    private HashMap<String, Mapping> map;
    private String authValue;
    private String roleValue; 

    @Override
    public void init() throws ServletException {
        String packageToScan = this.getInitParameter("package");
        this.authValue = this.getInitParameter("auth_name");
        this.roleValue = this.getInitParameter("role_name");
        try {
            this.controllers = new Function().getAllclazzsStringAnnotation(packageToScan, AnnotationController.class);
            this.map = new Function().scanControllersMethods(this.controllers);
        } catch (Exception e) {
            e.getMessage();
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
        response.setContentType("text/html;charset=UTF-8");

        String path = new Function().getURIWithoutContextPath(request);
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }

        // Check if the requested path exists in the map
        if (!map.containsKey(path)) {
            out.println("The map contains nothing: 404 NOT FOUND");
            return;
        }

        Mapping m = map.get(path);
        try {
            Class<?> clazz = Class.forName(m.getClassName());
            Method targetMethod = null;

            // Find the method in the class that matches the mapping
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(m.getMethodName())) {
                    targetMethod = method;
                    break;
                }
            }

            if (targetMethod == null) {
                out.println("Méthode non trouvée : " + m.getMethodName());
                return;
            }
            if (targetMethod.isAnnotationPresent(Auth.class)) {
                if (request.getSession(false) == null || request.getSession(false).getAttribute(this.authValue) == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized action");
                    return;
                }
            }

            if (targetMethod.isAnnotationPresent(Role.class)) {
                Role role = targetMethod.getAnnotation(Role.class);
                String role_name = role.value();

                // Vérifiez la session HTTP
                HttpSession session = request.getSession(false); // Ne crée pas de session si elle n'existe pas
                if (session == null || !role_name.equals(session.getAttribute("role"))) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized action: " + path);
                    return;
                }
            }


            

            Object[] params = Function.getParameterValue(request, response, targetMethod, ParamAnnotation.class, ParamObjectAnnotation.class);

            Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

            // Initialize MySession if required by the controller
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType().equals(MySession.class)) {
                    field.setAccessible(true);
                    field.set(controllerInstance, new MySession(request.getSession()));
                }
            }

            Object result;
            if (targetMethod.getParameterCount() == 1 && targetMethod.getParameterTypes()[0] == FilePart.class) {
                Map<String, FilePart> fileDataMap = MultipartParser.parseMultipartRequest(request);
                FilePart filePart = fileDataMap.get("file");
                result = targetMethod.invoke(controllerInstance, filePart);
            } else {
                result = targetMethod.invoke(controllerInstance, params);
            }

            handleResponse(result, targetMethod, request, response, out);

        } catch (Exception e) {
            e.printStackTrace(out);
            out.println("Erreur lors de l'exécution de la méthode : " + e.getMessage());
        }
    }

    private void handleResponse(Object result, Method targetMethod, HttpServletRequest request,
                                HttpServletResponse response, PrintWriter out) throws IOException, ServletException {
        if (targetMethod.isAnnotationPresent(ResponseAnnotation.class)) {
            response.setContentType("application/json");
            Gson gson = new Gson();
            if (result instanceof ModelView) {
                ModelView modelView = (ModelView) result;
                out.println(gson.toJson(modelView.getData()));
            } else {
                out.println(gson.toJson(result));
            }
        } else if (result instanceof String) {
            out.println("Resultat de l'execution de la méthode " + targetMethod.getName() + " est " + result);
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
    }
}
