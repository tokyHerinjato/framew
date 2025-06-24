package mg.p16.Servlet;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.Part;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.MultipartConfig;

import com.google.gson.Gson;

import mg.p16.Annotation.ControllerAnnotation;
import mg.p16.Annotation.FieldAnnotation;
import mg.p16.Annotation.Get;
import mg.p16.Annotation.JsonAnnotation;
import mg.p16.Annotation.MappingAnnotation;
import mg.p16.Annotation.ParamAnnotation;
import mg.p16.Annotation.ParamObjectAnnotation;
import mg.p16.Annotation.Post;
import mg.p16.Util.Mapping;
import mg.p16.Util.ModelView;
import mg.p16.Util.MySession;
import mg.p16.Util.VerbAction;
import mg.p16.Util.FilePart;
import mg.p16.Util.Multipart;

@MultipartConfig
public class FrontServlet extends HttpServlet {
    private String controllerPackage;
    private ArrayList<String> controllerNames;
    private HashMap<String, Mapping> mappings;

    public void init() {
        this.controllerPackage = "mg.p16.Controller";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        try {
            this.controllerNames = this.getListeControllers(this.controllerPackage, request);
            this.mappings = this.getMethodFromController(controllerNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String path = this.getURIWithoutContextPath(request);
        if (path.contains("?")) {
            int index = path.indexOf("?");
            path = path.substring(0, index);
        }
        out.println("Request Path: " + path);
        try {
            if (mappings == null || controllerNames == null) {
                out.println("erreur IO : view terminal");
            } else {
                if (mappings.containsKey(path)) {
                    Mapping m = mappings.get(path);
                    // out.println("Nom de la classe : " + m.getClassName());
                    // out.println("Nom de la méthode : " + m.getMethodeName());
                    // out.println("-------------------------------");

                    try {
                        Class<?> clizz = Class.forName(m.getClassName());
                        Method mixx = null;
                        for(Method method : clizz.getDeclaredMethods()) {
                            if (method.getName().equals(m.getMethodName())) {
                                mixx = method;
                                break;
                            }
                        }

                        if (mixx == null) {
                            out.println("Error 404 : Method not found : "+m.getClassName());
                            throw new NoSuchMethodException(
                                    "Method " + m.getClassName() + " not found in class " + m.getClassName());
                        }

                        out.println("Invoking method: " + mixx.getName() + " on class: " + clizz.getName());
                        System.out.println("Invoking method: " + mixx.getName() + " on class: " + clizz.getName());
                        Object[] params = this.getParameterValue(request, mixx, ParamAnnotation.class, out);
                        Object result = null;
                        result = mixx.invoke(clizz.newInstance(), params);
                        out.println("Resultat de la méthode : " + result);


                        if(mixx.isAnnotationPresent(JsonAnnotation.class)) {
                            if(result instanceof ModelView) {                                
                                response.setContentType("application/json");
                                Gson gson = new Gson();
                                ModelView modelView = (ModelView) result;
                                System.out.println("tonga eto eto yyyyy");
                                HashMap<String, Object> data = modelView.getData();
                                System.out.println("tonga eto eto xpppp");
                                String inona = gson.toJson(data);
                            } else {
                                response.setContentType("application/json");
                                Gson gson = new Gson();
                                String inona = gson.toJson(result);
                                out.println(inona);
                            }
                        }
                        if (result instanceof ModelView) {
                            ModelView modelView = (ModelView) result;
                            String urlGoal = modelView.getUrl();
                            out.println("Forwarding to URL: " + urlGoal);
                            HashMap<String, Object> data = modelView.getData();
                            System.out.println(data);
                            if (data != null) {
                                for (String key : data.keySet()) {
                                    request.setAttribute(key, data.get(key));
                                    out.println("Setting attribute: " + key + " = " + data.get(key));
                                }
                            }
                            request.getRequestDispatcher(urlGoal).forward(request, response);
                        } else if (result instanceof String) {
                            out.println(result.toString());
                        } else {
                            out.println("error return type");
                        }
                    } catch (Exception e) {
                        out.println(e.getMessage());
                    }
                } else {
                    out.println("Aucune méthode associée à cette URL ou 404 not found");
                }
            }
        } catch (Exception e) {
            out.println("General error:");
            out.println(e.getMessage());
        }

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

    // public Method getMethodByName(Mapping m, Class<?> clazz) {
    //     try {
    //         Method[] methods = clazz.getDeclaredMethods();
    //         for (Method method : methods) {
    //             if (method.getName().equals(m.getMethodName())) {
    //                 return method;
    //             }
    //         }
    //     } catch (Exception e) {
    //         // e.printStackTrace();
    //     }
    //     return null;
    // }

    public static Object createParamObject(Class<?> clazz, HttpServletRequest request) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (field.isAnnotationPresent(FieldAnnotation.class)) {
                fieldName = field.getAnnotation(FieldAnnotation.class).value();
            }
            String paramValue = request.getParameter(fieldName);
            if (paramValue != null) {
                field.setAccessible(true);
                field.set(instance, convertParameterValue(paramValue, field.getType()));
            }
        }
        return instance;
    }

    public ArrayList<String> getListeControllers(String packageName, HttpServletRequest request) throws IOException {
        ArrayList<String> controllerClasses = new ArrayList<>();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = getClass().getClassLoader().getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
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
                                    if (clazz != null) {
                                        if (clazz.isAnnotationPresent(ControllerAnnotation.class)) {
                                            Field tempField = null;
                                            for (Field field : clazz.getDeclaredFields()) {
                                                if (field.getType().equals(MySession.class)) {
                                                    try {
                                                        tempField = field;
                                                        break;
                                                    } catch (Exception e) {
                                                        System.out.println("error set field : " + e.getMessage());
                                                    }
                                                }
                                            }
                                            if (tempField != null) {
                                                try {
                                                    tempField.setAccessible(true);
                                                    tempField.set(clazz.getDeclaredConstructor().newInstance(),
                                                            new MySession(request.getSession()));
                                                    System.out.println("ReqX.getSession");
                                                } catch (Exception err) {
                                                    System.out.println(err.getMessage());
                                                }
                                            }
                                            controllerClasses.add(clazz.getName());
                                        }
                                    }
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
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
                            try {
                                Class<?> clazz = Class.forName(className);
                                if (clazz.isAnnotationPresent(ControllerAnnotation.class)) {
                                    controllerClasses.add(clazz.getName());
                                }
                            } catch (ClassNotFoundException e) {
                                // e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return controllerClasses;
    }

    public HashMap<String, Mapping> getMethodFromController(ArrayList<String> controllers)
            throws ServletException,IOException {
        HashMap<String, Mapping> res = new HashMap<>();
        HashMap<String, String> urlMap = new HashMap<>();

        for (String controller : controllers) {
            try {
                Class<?> clazz = Class.forName(controller);
                Method[] methods = clazz.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.isAnnotationPresent(MappingAnnotation.class)) {
                        String url = m.getAnnotation(MappingAnnotation.class).value();
                        if (urlMap.containsKey(url)) {
                            throw new IOException("url" + url + " already associated");
                        } else {
                            urlMap.put(url, clazz.getName() + "." + m.getName());
                            res.put(url, new Mapping(controller, m.getName()));
                        }
                        

                        System.out.println("Methode annotee : "+m.getName());
                        System.out.println("Annotation value : "+url);

                    }
                }
                
            } catch (Exception e) {
                // e.printStackTrace();
                throw new ServletException("Erreur : Package introuvable");
            }
        }
        return res;
    }

    public String getURIWithoutContextPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    public static Object convertParameterValue(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(value);
        } else if (type == char.class || type == Character.class) {
            if (value.length() != 1) {
                throw new IllegalArgumentException("Invalid character value:" + value);
            }
            return value.charAt(0);
        }
        return null;
    }

    public static Object[] getParameterValue(HttpServletRequest request, Method method,
            Class<ParamAnnotation> annotationClass, PrintWriter out) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];
        try {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.getType().equals(MySession.class)) {
                    parameterValues[i] = new MySession(request.getSession());
                    System.out.println("req.getSession()");
                } else if (parameter.isAnnotationPresent(ParamAnnotation.class)) {
                    System.out.println("parammmmmmmmm -------------------------------------------------------");
                    String paramName = parameter.getAnnotation(ParamAnnotation.class).value();
                    System.out.println("Parameter name: " + paramName); // Affiche le nom du paramètre

                    System.out.println();

                    if (parameters[i].getType().equals(Multipart.class)) {
                        System.out.println("Parameter type: " + parameters[i].getType()); // Affiche le type du paramètre

                        Part part;
                        System.out.println("uiuhiuhiuhih");
                        System.out.println("Request Method: " + request.getMethod()); 
                        System.out.println("Content-Type: " + request.getContentType());  // Cela doit être "multipart/form-data"
                        try {
                            System.out.println("poooooooooooo");
                            part = request.getPart(paramName);
                            System.out.println("Retrieving part for: " + part.getSubmittedFileName()); // Affiche un message pour indiquer qu'on récupère la partie
                            if (part == null) {
                                System.out.println("Part is null for parameter: " + paramName);
                            } else {
                                System.out.println("Part retrieved successfully: " + part.getSubmittedFileName());
                            }
                            if (part != null) {
                                System.out.println("File submitted: " + part.getSubmittedFileName()); // Affiche le nom du fichier soumis
                                System.out.println("Processing file part for: " + paramName); // Message de traitement de la partie
    
                                parameterValues[i] = new Multipart(part.getSubmittedFileName(), part.getSize(), part.getInputStream());
                            } else {
                                System.out.println("No file submitted for: " + paramName); // Affiche si aucun fichier n'est soumis
                                parameterValues[i] = null;
                            }
                        } catch (Exception e) {
                            System.err.println("erreurrrrr : "+e.getMessage());
                        }
                    } else {
                        String paramValue = request.getParameter(paramName);
                        System.out.println("Retrieving parameter value for: " + paramName + " = " + paramValue); // Affiche la valeur du paramètre

                        parameterValues[i] = convertParameterValue(paramValue, parameters[i].getType());
                    }

                    System.out.println("-------------------------------------------------------");

                } else if (parameter.isAnnotationPresent(ParamObjectAnnotation.class)) {
                    parameterValues[i] = createParamObject(parameter.getType(), request);
                } else {
                    throw new Exception("ETU002479 : Erreur servlet de parametre misy tsy annoté");
                }
            }
        } catch (Exception e) {
            // e.printStackTrace(out);
            out.println(e.getMessage());
        }
        return parameterValues;
    }
}
