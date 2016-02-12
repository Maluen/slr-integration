package misc;

// Based on http://stackoverflow.com/a/723914

import java.io.File;
import java.io.IOException;

public final class JavaProcess {

    private JavaProcess() {}        

    public static Process exec(Class klass, String args, File workingDirectory) throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, className, args);
        builder.directory(workingDirectory);
        
        //System.out.println(javaBin + " " + "-cp" + " " + classpath + " " + className);

        Process process = builder.start();
        return process;
    }

}