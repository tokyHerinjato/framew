package mg.p16.Util;

import java.util.HashMap;

public class ModelView {
    private String GET;
    private HashMap<String, Object> data;

    public ModelView(String GET) {
        this.GET = GET;
        this.data = new HashMap<>();
    }

    public ModelView() {
    }

    public ModelView(String GET, HashMap<String, Object> data) {
        this.GET = GET;
        this.data = data != null ? data : new HashMap<>(); // Ensure data is not null
    }

    public String getGET() {
        return GET;
    }

    public void setGET(String GET) {
        this.GET = GET;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void addObject(String key, Object value) {
        this.data.put(key, value);
    }
}
