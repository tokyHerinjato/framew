package mg.p16.Util;

public class Mapping {
    private String className;
    private String methodeName;

    public Mapping() {
    }

    public Mapping(String className, String methodeName) {
        this.className = className;
        this.methodeName = methodeName;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodeName() {
        return this.methodeName;
    }

    public void setMethodeName(String methodeName) {
        this.methodeName = methodeName;
    }
}
