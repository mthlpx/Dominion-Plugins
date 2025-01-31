package me.teux.api.utils;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {
    public static List<Class<?>> getClasses(Plugin plugin, String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if (protocol.equals("jar")) {
                processJar(resource, packageName, classes);
            } else if (protocol.equals("file")) {
                File file = new File(URLDecoder.decode(resource.getPath(), "UTF-8"));
                processDirectory(file, packageName, classes);
            }
        }
        return classes;
    }

    private static void processJar(URL resource, String packageName, List<Class<?>> classes) throws Exception {
        JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
        try (JarFile jar = jarConn.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            String packagePath = packageName.replace('.', '/') + "/";

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    String className = entryName
                            .replace('/', '.')
                            .replace(".class", "");

                    classes.add(Class.forName(className));
                }
            }
        }
    }

    private static void processDirectory(File directory, String packageName, List<Class<?>> classes) throws Exception {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
    }
}