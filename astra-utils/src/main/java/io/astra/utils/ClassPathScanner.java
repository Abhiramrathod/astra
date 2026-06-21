package io.astra.utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/** Utility for scanning the classpath to discover classes within a given package. */
public final class ClassPathScanner {
    private ClassPathScanner() {}

    public static List<Class<?>> findClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                if ("file".equals(protocol)) {
                    scanDirectory(new File(resource.toURI()), packageName, classes);
                } else if ("jar".equals(protocol)) {
                    scanJar(resource, path, classes);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package: " + packageName, e);
        }
        return classes;
    }

    private static void scanDirectory(File dir, String packageName, List<Class<?>> classes) {
        File[] files = dir.listFiles(f -> f.getName().endsWith(".class") || f.isDirectory());
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    // skip
                }
            }
        }
    }

    private static void scanJar(URL resource, String packagePath, List<Class<?>> classes) throws Exception {
        String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(packagePath) && name.endsWith(".class")) {
                    String className = name.replace('/', '.').replace(".class", "");
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        // skip
                    }
                }
            }
        }
    }
}
