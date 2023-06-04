package info.kgeorgiy.ja.treshchev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Class implementing {@link JarImpler} interface.
 * Provides public methods to create {@code .java} and {@code .jar} file
 * that implements given interface. Note that this class expects exactly the interface for implementation.
 *
 * @author artem
 */
public class Implementor implements JarImpler {

    /**
     * Error message for {@link ClassNotFoundException} caught in
     * {@link Implementor#main(String[])} method of {@link Implementor} class.
     */
    private static final String CLASS_NOT_FOUND_ERROR_MESSAGE = "Wrong class-name! Found: ";

    /**
     * Error message for {@link InvalidPathException} caught in
     * {@link Implementor#main(String[])} method of {@link Implementor} class.
     */
    private static final String INVALID_PATH_ERROR_MESSAGE = "Wrong output path! Found: ";

    /**
     * Error message for {@link ImplerException} caught in
     * {@link Implementor#main(String[])} method of {@link Implementor} class.
     */
    private static final String IMPLER_EXCEPTION_ERROR_MESSAGE = "Some implementation error: ";

    /**
     * Path to created {@code .java} file that implements given interface.
     */
    private Path fileImplPath;

    /**
     * Path to directory where created {@code .java} file that implements given interface.
     */
    private Path clazzPath;

    /**
     * Package-path to created {@code .java} file that implements given interface.
     * Actually this is path of packages of created {@code .java} file.
     * For example, if the given interface is located
     * in package {@code info.kgeorgiy.ja.treshchev.implementor} than
     * Package-path to created {@code .java} file has a value
     * different from the package name by replacement {@code "."} on {@link File#separatorChar}.
     */
    private Path directoryPath;

    /**
     * Contains simple name of implemented interface.
     * This value is got by using {@link Class#getSimpleName()} method.
     */
    private String clazzName;

    /**
     * Main function. Provides to user the console to run {@link Implementor} class.
     * Runs in two variants:
     * <ol>
     *     <li>
     *         if there were 2 arguments {@code token outputPath} of command line
     *         than it creates {@code .java} file in {@code outputPath} directory
     *         containing the implementation of the interface {@code token}
     *         using {@link #implement(Class, Path)} method.
     *     </li>
     *     <li>
     *         if there were 3 arguments {@code -jar token jarFile} of command line
     *         than it creates {@code .jar} file on the {@code jarFile} path
     *         where located compiled {@code .java} file containing the implementation
     *         of the interface {@code token} using {@link #implementJar(Class, Path)} method.
     *     </li>
     * </ol>
     * If there were some errors during the operation of the method
     * than there would be an error message in {@link System#err} stream.
     * If there were less than 2 or more than 3 arguments, or there were 3 arguments and
     * the first argument wasn't {@code -jar}, than method will print
     * corresponding error message in {@link System#err} stream.
     *
     * @param args command line arguments for the app.
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                new Implementor().implement(getArgumentClazz(args[0]), getArgumentPath(args[1]));
            } catch (ClassNotFoundException e) {
                System.err.println(CLASS_NOT_FOUND_ERROR_MESSAGE + args[0]);
            } catch (InvalidPathException e) {
                System.err.println(INVALID_PATH_ERROR_MESSAGE + args[1]);
            } catch (ImplerException e) {
                System.err.println(IMPLER_EXCEPTION_ERROR_MESSAGE + e.getMessage());
            }
        } else if (args.length == 3) {
            if (!args[0].equals("-jar")) {
                System.err.println("Wrong name of first argument! Should be '-jar'");
                return;
            }
            try {
                new Implementor().implementJar(getArgumentClazz(args[1]), getArgumentPath(args[2]));
            } catch (ClassNotFoundException e) {
                System.err.println(CLASS_NOT_FOUND_ERROR_MESSAGE + args[1]);
            } catch (InvalidPathException e) {
                System.err.println(INVALID_PATH_ERROR_MESSAGE + args[2]);
            } catch (ImplerException e) {
                System.err.println(IMPLER_EXCEPTION_ERROR_MESSAGE + e.getMessage());
            }
        } else {
            System.err.println("Wrong count of arguments!");
        }
    }

    /**
     * Casts string to {@code Class} type.
     *
     * @param arg name of class that should be casts.
     * @return The Class object associated with the class or interface
     * with the given string name.
     * @throws ClassNotFoundException if the string doesn't associate with any classes or interfaces.
     */
    private static Class<?> getArgumentClazz(String arg) throws ClassNotFoundException {
        return Class.forName(arg);
    }

    /**
     * Casts string to {@code Path} type.
     *
     * @param arg name of path that should be casts.
     * @return a Path by converting a path string.
     * @throws InvalidPathException if there was invalid path as argument.
     */
    private static Path getArgumentPath(String arg) throws InvalidPathException {
        return Path.of(arg);
    }

    /**
     * Creates {@code .java} file that contains the implementation
     * of the interface passed as a parameter {@code clazz} on the path
     * passed as parameter {@code path}.
     * As a result, the java code of the class with the Impl suffix is generated,
     * implementing the specified interface.
     * The generated class compiles without errors.
     * The generated class is public and not abstract.
     * Methods of the generated class ignore their arguments and return default values.
     *
     * @param clazz interface that should be implemented.
     * @param path  path where {@code .java} file should be located.
     * @throws ImplerException if some errors occurred during the implementation.
     */
    @Override
    public void implement(Class<?> clazz, Path path) throws ImplerException {
        if (Modifier.isPrivate(clazz.getModifiers()) || clazz.isPrimitive() || !clazz.isInterface()) {
            throw new ImplerException("Error trying implemented private interface!");
        }
        this.clazzName = clazz.getSimpleName();
        // Простое имя класса *Impl.java
        String clazzImplName = this.clazzName + "Impl.java";
        String packageName = clazz.getPackage().getName();
        String packagePath = packageName.replace('.', File.separatorChar);

        try {
            this.directoryPath = Path.of(packagePath);
            this.clazzPath = path.resolve(this.directoryPath);
            this.fileImplPath = this.clazzPath.resolve(clazzImplName);
        } catch (InvalidPathException e) {
            throw new ImplerException("Invalid path of class!", e);
        }
        try {
            Files.createDirectories(this.clazzPath);
        } catch (IOException e) {
            throw new ImplerException("Couldn't create directories for output path!", e);
        }
        try (BufferedWriter codePrinter = Files.newBufferedWriter(this.fileImplPath, StandardCharsets.UTF_8)) {
            if (!packageName.isEmpty()) {
                codePrinter.write("package " + packageName + ";");
                codePrinter.newLine();
            }
            codePrinter.write("public class " + this.clazzName + "Impl implements " + clazz.getCanonicalName() + " {");
            codePrinter.newLine();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                printMethod(codePrinter, method);
            }
            codePrinter.write("}");
            codePrinter.newLine();
        } catch (IOException e) {
            throw new ImplerException("Couldn't write in outputFile!", e);
        }
    }

    /**
     * Prints realization of methods in {@code .java} file.
     * Prints realization of method in interface
     * that was as a parameter {@code clazz} of {@link #implement(Class, Path)} method.
     *
     * @param codePrinter {@link BufferedWriter} to write in output file.
     * @param method      what method should be realized.
     * @throws IOException if some errors occurred during writing in output file.
     */
    private void printMethod(BufferedWriter codePrinter, Method method) throws IOException {
        codePrinter.write("public ");
        Class<?> returnType = printReturnType(codePrinter, method);
        codePrinter.write(method.getName() + " (");
        codePrinter.write(getParameters(method) + ") ");

        printMethodBody(codePrinter, returnType);

        codePrinter.newLine();
    }

    /**
     * Prints method body.
     * Prints the body of method that was the parameter {@code method}
     * of {@link #printMethod(BufferedWriter, Method)} method.
     *
     * @param codePrinter {@link BufferedWriter} to write in output file.
     * @param returnType  type of return value.
     * @throws IOException if some errors occurred during writing in output file.
     */
    private void printMethodBody(BufferedWriter codePrinter, Class<?> returnType) throws IOException {
        codePrinter.write("{");
        codePrinter.newLine();
        printReturnType(codePrinter, returnType);
        codePrinter.newLine();
        codePrinter.write("}");
    }

    /**
     * Prints default return value.
     * Prints default return value of method that was the parameter {@code method}
     * of {@link #printMethod(BufferedWriter, Method)} method.
     * If type of return value is not a primitive
     * then method will print "null" in output file as return value.
     * Else if type of return value is not a {@code void} and is not a {@code boolean}
     * then method will print "0" in output file as return value.
     * Else if type of return value is {@code boolean}
     * then method will print "false" in output file as return value.
     * Otherwise,(in case when type of return value is {@code void})
     * method won't print any return value.
     *
     * @param codePrinter {@link BufferedWriter} to write in output file.
     * @param returnType  type of return value.
     * @throws IOException if some errors occurred during writing in output file.
     */
    private void printReturnType(BufferedWriter codePrinter, Class<?> returnType) throws IOException {
        if (!returnType.isPrimitive()) {
            codePrinter.write("return null;");
        } else {
            if (returnType != void.class) {
                if (returnType == boolean.class) {
                    codePrinter.write("return false;");
                } else {
                    codePrinter.write("return 0;");
                }
            }
        }
    }

    /**
     * Prints the type of return value in declaration of the method.
     * Prints the type of return value in declaration of the method that was
     * a parameter {@code method} of {@link #printMethod(BufferedWriter, Method)} method.
     *
     * @param codePrinter {@link BufferedWriter} to write in output file.
     * @param method      method which type of return value should be printed.
     * @return class that represents type of return value.
     * @throws IOException if some errors occurred during writing in output file.
     */
    private Class<?> printReturnType(BufferedWriter codePrinter, Method method) throws IOException {
        Class<?> returnType = method.getReturnType();
        codePrinter.write(returnType.getCanonicalName() + " ");
        return returnType;
    }

    /**
     * Returns parameters of method.
     * Returns a string that contains all parameters of the method
     * that was a parameter {@code method} of {@link #printMethod(BufferedWriter, Method)} method.
     *
     * @param method method which parameters should be returned.
     * @return string that contains all method parameters,
     * or empty string if method doesn't contain any parameters.
     */
    private String getParameters(Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            ret.append(parameters[i].getCanonicalName()).append(" arg").append(i).append(", ");
        }
        return ret.length() > 0 ? ret.substring(0, ret.length() - 2) : "";
    }

    /**
     * Creates {@code .jar} file that contains realization of interface.
     * Creates {@code .jar} file where is located compiled file that
     * contains implementation of the given interface.
     *
     * @param aClass interface that should be implemented.
     * @param path   where {@code .jar} file should be located.
     * @throws ImplerException if some errors occurred during implementation
     *                         or creating {@code .jar} file
     */
    @Override
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {
        Path parentDirectory = path.getParent();
        if (parentDirectory == null) {
            parentDirectory = Path.of(".");
        }
        implement(aClass, parentDirectory);

        compile(aClass);

        createJar(path);

    }

    /**
     * Compiles the {@code .java} file.
     * Compiles the {@code .java} file that was created during {@link #implement(Class, Path)} method.
     *
     * @param token what class should be compiled.
     * @throws ImplerException if some error occurred during compilation.
     */
    private void compile(Class<?> token) throws ImplerException {
        // Should compile `this.fileImplPath` file
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] compilerArguments = new String[5];
        compilerArguments[0] = "-encoding";
        compilerArguments[1] = StandardCharsets.UTF_8.name();
        compilerArguments[2] = "-cp";
        try {
            compilerArguments[3] = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new ImplerException("Error in URI syntax", e);
        } catch (InvalidPathException e) {
            throw new ImplerException("Error in making class-path!", e);
        }
        compilerArguments[4] = this.fileImplPath.toString();
        if (compiler.run(null, null, null, compilerArguments) != 0) {
            throw new ImplerException("Couldn't compile file!");
        }
    }

    /**
     * Creating {@code .jar} file.
     * Creating {@code .jar} file that contains compiled class which was implementing the given interface.
     *
     * @param path where to create {@code .jar} file.
     * @throws ImplerException if some error occurred during creating {@code .jar} file.
     */
    private void createJar(Path path) throws ImplerException {
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(path))) {
            String entry = "";
            try {
                entry = this.directoryPath.resolve(this.clazzName)
                        .toString().replace(File.separatorChar, '/') + "Impl.class";
                jarOutputStream.putNextEntry(new ZipEntry(entry));
            } catch (IOException e) {
                throw new ImplerException("Couldn't put next entry for jar file with zip-entry=\"" + entry + "\"", e);
            }
            Path pathToCompiledClass;
            try {
                pathToCompiledClass = Path.of(this.clazzPath.resolve(this.clazzName) + "Impl.class");
            } catch (InvalidPathException e) {
                throw new ImplerException("Error in path to compiled class", e);
            }
            try {
                Files.copy(pathToCompiledClass, jarOutputStream);
            } catch (IOException e) {
                throw new ImplerException("Error in copying files with path=\"" + pathToCompiledClass + "\"", e);
            }
        } catch (IOException e) {
            throw new ImplerException("Couldn't open jar file!", e);
        }
    }

}
