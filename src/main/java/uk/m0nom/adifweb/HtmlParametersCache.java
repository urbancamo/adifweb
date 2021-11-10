package uk.m0nom.adifweb;

import uk.m0nom.adifweb.domain.HtmlParameters;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HtmlParametersCache {
    private final static int MAXSIZE = 1000;

    private static final HashMap<String, HtmlParameters> parametersCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, HtmlParameters> eldest) {
            return this.size() > MAXSIZE;
        }
    };

    public static void put(String timestamp, HtmlParameters parameters) {
        parametersCache.put(timestamp, parameters);
    }

    public HtmlParameters get(String timestamp) {
        return parametersCache.get(timestamp);
    }
}
