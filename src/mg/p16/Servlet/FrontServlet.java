package mg.p16.Servlet;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mg.p16.Annotation.ControllerAnnotation;
import mg.p16.Annotation.MappingAnnotation;
import mg.p16.Util.Mapping;

public class FrontServlet extends HttpServlet {
    private String controllerPackage;
    private ArrayList<String> controllerNames;
    private HashMap<String, Mapping> mappings;

    public void init() throws ServletException {
        this.controllerPackage = "mg.p16.Controller";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        // initialisation des controllers
        this.controllerNames = this.getListeControllers(this.controllerPackage, out);
        // initialisation du hashMap
        this.mappings = this.getMethodFromController(controllerNames, out);

        String path = this.getURIWithoutContextPath(request);
        out.println(path);
        /* Prendre le mapping correspondant a l'url */
        try {
            if (mappings.containsKey(path)) {
                Mapping m = mappings.get(path);
                out.print("\n");
                out.println("Nom de la classe : " + m.getClassName());
                out.println("Nom de la méthode : " + m.getMethodeName());
                out.println("-------------------------------");
                /// recuperer la classe par son nom
                Class<?> clizz = Class.forName(m.getClassName());
                // récuperer la methode par son nom
                Method mixx = clizz.getMethod(m.getMethodeName());
                // invoquer la methode sur l'instance de la classe
                Object result = mixx.invoke(null);
                out.println("résultat de la methode : " + result);
                // -----------------------------------------------
            } else {
                out.print("\n");
                out.println("Aucune méthode associé a cette url");
            }
        } catch (Exception e) {
            // TODO: handle exception
        if (mappings.containsKey(path)) {
            Mapping m = mappings.get(path);
            out.print("\n");
            out.println("Nom de la classe : " + m.getClassName());
            out.println("Nom de la méthode : " + m.getMethodeName());
        } else {
            out.print("\n");
            out.println("Aucune méthode associé a cette url");
        }
        // out.println(this.mappings.v);

        String requestedPage = request.getPathInfo();
        out.println("path : /" + requestedPage);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    public String getServletInfo() {
        return "FrontServlet";
    }

    public Method getMethodByName(Mapping m, Class<?> clizz) {
        Method[] methods = clizz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(m.getMethodeName())) {
                return method;
            }
        }
        return null;
    }

    public ArrayList<String> getListeControllers(String packageName, PrintWriter out) {
        ArrayList<String> controllerClasses = new ArrayList<>();
        String path = packageName.replace('.', '/');

        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                out.println("Scanning: " + resource);
                if (resource.getProtocol().equals("file")) {
                    File directory = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
                    if (directory.exists() && directory.isDirectory()) {
                        File[] files = directory.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile() && file.getName().endsWith(".class")) {
                                    String className = packageName + "." + file.getName().replace(".class", "");
                                    try {
                                        Class<?> clazz = Class.forName(className);
                                        if (clazz.isAnnotationPresent(ControllerAnnotation.class)) {
                                            controllerClasses.add(clazz.getName());
                                        }
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                    }
                                }
                            }
                        }
                    }
                } else if (resource.getProtocol().equals("jar")) {
                    String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
                    try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.getName().startsWith(path) && entry.getName().endsWith(".class")) {
                                String className = entry.getName().replace('/', '.').replace(".class", "");
                                Class<?> clazz = Class.forName(className);
                                if (clazz.isAnnotationPresent(ControllerAnnotation.class)) {
                                    controllerClasses.add(clazz.getName());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
        return controllerClasses;
    }

    public HashMap<String, Mapping> getMethodFromController(ArrayList<String> controllers, PrintWriter out) {
        HashMap<String, Mapping> res = new HashMap<>();
        try {
            out.println("Scanning 2 method");
            for (String controller : controllers) {
                Class<?> clazz = Class.forName(controller);
                Method[] methods = clazz.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.isAnnotationPresent(MappingAnnotation.class)) {
                        String url = m.getAnnotation(MappingAnnotation.class).value();
                        if (res.containsKey(url)) {
                            String methodPresent = res.get(url).getClassName() + ":" + res.get(url).getMethodeName();
                            String new_method = clazz.getName() + "." + m.getName();
                            try {
                                throw new Exception("url dumped ... ok");
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                        }
                        res.put(m.getAnnotation(MappingAnnotation.class).value(), new Mapping(controller, m.getName()));
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return res;
    }

    public String getURIWithoutContextPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}