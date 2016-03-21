package misc;

// Based on http://stackoverflow.com/a/723914
// with absolute classpath based on http://stackoverflow.com/a/35275894
// (it must be absolute or won't work outside eclipse when running with -jar)

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang3.StringUtils;

public final class JavaProcess {

    private JavaProcess() {}        

    public static Process exec(Class klass, String args, File workingDirectory) throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";

        //String classpath = System.getProperty("java.class.path");
        URL[] urls = (((URLClassLoader) Thread.currentThread().getContextClassLoader()) ).getURLs();
        String[] stringUrls = new String[urls.length];
        for (int i=0; i<urls.length; i++) {
        	stringUrls[i] = urls[i].getFile();
        }
        String classpath = StringUtils.join(stringUrls, File.pathSeparator);

        String className = klass.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, className, args);
        builder.directory(workingDirectory);
        
        System.out.println(javaBin + " " + "-cp" + " " + classpath + " " + className);
        
        Process process = builder.start();
        return process;
    }

}