package mg.p16.Util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class Mapping {
    String className;
    String methodName;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Mapping() {
    }

    public Mapping(String className, String methodName) {
        setClassName(className);
        setMethodName(methodName);
    }

    // to execute the method inside the controllers annoted by a map
    public Object invokeMethod() throws Exception {
        Class<?> clazz = Class.forName(className);
        Method method = clazz.getDeclaredMethod(methodName);
        Object in_stance = clazz.getDeclaredConstructor().newInstance();
        return method.invoke(in_stance);
    }

    public Set<VerbAction> getVerbactions() {
        return verbactions;
    }
   
    public void addVerbAction(String methodName, String verb) throws Exception {
        // Vérifiez si le verbe est déjà utilisé
        for (VerbAction action : verbactions) {
            if (action.getVerb().equals(verb)) {
                throw new Exception("HTTP verb " + verb + " already used by method: " + action.getMethodName());
            }
        }       
        verbactions.add(new VerbAction(methodName, verb));
    }
}
