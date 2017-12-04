package com.jforexcn.shared.client;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by simple on 1/29/16.
 */
public class StrategyManager {
    public static ConcurrentHashMap<String, Class<?>> sStrategyMap = new ConcurrentHashMap<String, Class<?>>();

    public static void register(String strategyName, Class<?> strategyClass) {
        sStrategyMap.put(strategyName, strategyClass);
    }

    public static <T> T get(String strategyName) {
        Class<T> strategyClass = (Class<T>) sStrategyMap.get(strategyName);
        try {
            return strategyClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error instantiating " + strategyName + "; error: " + e);
        }
    }

    public static String listAllStrategies() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Class<?>> entry : sStrategyMap.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(entry.getKey());
        }
        return sb.toString();
    }
}
