package mg.p16.Util;

import java.util.Set;

public class Mapping {
    private String className;
    private Set<VerbAction> verbactions;

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
    }

}
