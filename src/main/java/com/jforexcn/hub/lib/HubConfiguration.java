package com.jforexcn.hub.lib;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforexcn.hub.HubStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Created by simple(simple.continue@gmail.com) on 29/04/2018.
 */

public class HubConfiguration {
    public static final String CONFIG_FILE_NAME = "HubStrategy.properties";
    public static final String DEFAULT_SCOPE = "Default";
    public static final String SINGLE_SCOPE = "Single";

    public static final TreeMap<String, Object> propertiesMap = new TreeMap<>();


    public static String getConfigKey(String[] keys) {
        return keys[0] + "." + keys[1] + "." + keys[2] + "." + keys[3];
    }

    public static String getConfigKey(String className, String scope, String typeName, String fieldName) {
        return className + "." + scope + "." + typeName + "." + fieldName;
    }

    public static void load(IContext context) throws JFException {
        Properties fileProperties = new Properties();
        File fileDir = context.getFilesDir();
        try {
            String filePath = CONFIG_FILE_NAME;
            File configFile = new File(filePath);
            if (!configFile.exists()) {
                filePath = fileDir.getParentFile().getAbsolutePath() + File.separator + CONFIG_FILE_NAME;
            }
            InputStream inputStream = new FileInputStream(filePath);
            fileProperties.load(inputStream);
            inputStream.close();
            propertiesMap.clear();
            for (Map.Entry<Object, Object> entry : fileProperties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
//                context.getConsole().getInfo().println("========= key: " + key);
//                context.getConsole().getInfo().println("========= value: " + value);
                String[] keys = key.split("\\.");
                String typeName = keys[2];
                String mapKey = getConfigKey(keys);
                if (keys.length == 5) {
                    Object mapValue = propertiesMap.get(mapKey);
                    if (mapValue == null) {
                        mapValue = new ArrayList<Object>();
                        propertiesMap.put(mapKey, mapValue);
                    }
                    ((ArrayList<Object>) mapValue).add(convertValue(typeName, value));
                } else {
                    propertiesMap.put(mapKey, convertValue(typeName, value));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new JFException(e);
        }
    }

    public static Object convertValue(String typeName, String value) {
        if ("Instrument".equals(typeName)) {
            return Instrument.fromString(value);
        } else if ("Period".equals(typeName)) {
            return Period.valueOf(value);
        } else if ("OrderCommand".equals(typeName)) {
            return IEngine.OrderCommand.valueOf(value);
        } else if ("OfferSide".equals(typeName)) {
            return OfferSide.valueOf(value);
        } else if ("AppliedPrice".equals(typeName)) {
            return IIndicators.AppliedPrice.valueOf(value);
        } else if ("MaType".equals(typeName)) {
            return IIndicators.MaType.valueOf(value);
        } else if ("Filter".equals(typeName)) {
            return Filter.valueOf(value);
        } else if ("SubStrategyName".equals(typeName)) {
            return HubStrategy.SubStrategyName.valueOf(value);
        } else if ("Integer".equals(typeName)) {
            return Integer.valueOf(value);
        } else if ("Double".equals(typeName)) {
            return Double.valueOf(value);
        } else if ("Float".equals(typeName)) {
            return Float.valueOf(value);
        } else if ("Boolean".equals(typeName)) {
            return Boolean.valueOf(value);
        } else {
            return value;
        }
    }

    public static Object getConfig(String mapKey) {
        if (propertiesMap.containsKey(mapKey)) {
            return propertiesMap.get(mapKey);
        }
        return null;
    }

    public static void printConfig(IContext context, String startsWith) {
        context.getConsole().getInfo().println("========== " + startsWith + " print config start ==========");
        for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
            if (entry.getKey().startsWith(startsWith)) {
                context.getConsole().getInfo().println(entry.getKey() + ": " + entry.getValue().toString());
            }
        }
        context.getConsole().getInfo().println("========== " + startsWith + " print config end ==========");
    }

    public static void printConfig(IContext context) {
        context.getConsole().getInfo().println("=== HubConfiguration Start ===");
        for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
            context.getConsole().getInfo().println(entry.getKey() + ": " + entry.getValue());
        }
        context.getConsole().getInfo().println("=== HubConfiguration End ===");
    }

}
