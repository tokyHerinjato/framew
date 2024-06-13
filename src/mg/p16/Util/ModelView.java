package mg.p16.Util;

import java.util.HashMap;
import java.util.Objects;

public class ModelView {
    private String url; // url de destination
    private HashMap<String, Object> data; // données à envoyer vers cette view

    public ModelView() {
    }

    public ModelView(String url, HashMap<String, Object> data) {
        this.url = url;
        this.data = data;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, Object> getData() {
        return this.data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void addObject(String key, Object valeur) {
        // mettre les données dans hashmap
        HashMap<String, Object> h = new HashMap<>();
        h.put(key, valeur);
        this.setData(h);
    }
}