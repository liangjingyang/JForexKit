package com.jforexcn.shared.lib;


import com.dukascopy.api.IStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Created by simple on 1/29/16.
 */
public class StrategyManager {
    public static ConcurrentHashMap<String, Class<?>> sStrategyMap = new ConcurrentHashMap<String, Class<?>>();

    public static void register(String strategyName, Class<?> strategyClass) {
        sStrategyMap.put(strategyName, strategyClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String strategyName) {
        try {
            Class<T> strategyClass = (Class<T>) sStrategyMap.get(strategyName);
            if (strategyClass != null) {
                return strategyClass.newInstance();
            } else {
                JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager sjfm = jc.getStandardFileManager(null, null, null);
                File javaFile = new File(strategyName);
                String qualifiedClassName = getQualifiedName(javaFile.getAbsolutePath());
                jc.getTask(null, null, null, null, null, sjfm.getJavaFileObjects(javaFile)).call();
                sjfm.close();

                URL[] urls = new URL[]{new URL("file://.")};
                URLClassLoader ucl = new URLClassLoader(urls);
                Class targetClass = ucl.loadClass(qualifiedClassName);
                return (T) targetClass.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error instantiating " + strategyName + "; error: " + e);
        }
    }

    private static String getQualifiedName(String path) throws Exception {
        String fileContents = readFileContents(path);
        String className = findRegex(fileContents, "class ([\\p{Alnum}.]+) ");
        String packageName = findRegex(fileContents, "package ([\\p{Alnum}.]+);");
        if (packageName.isEmpty()) {
            throw new RuntimeException("Please define the package of your strategy");
        }
        return packageName + "." + className;
    }

    private static String readFileContents(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }


    private static String findRegex(String src, String regexpString) {
        Pattern pattern = Pattern.compile(regexpString, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(src);
        return matcher.find() ? matcher.group(1) : "";
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
