package info.kgeorgiy.ja.treshchev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.Path;

public class RunImplementor {

    private static final String PATH_TO_OUTPUT = "C:\\Users\\artem\\IdeaProjects\\JavaAdvanced\\solutions\\java-solutions\\Sample.jar";

    public static void main(String[] args) {
        Implementor implementor = new Implementor();
        try {
            implementor.implementJar(A.class, Path.of(PATH_TO_OUTPUT));
        } catch (ImplerException e) {
            System.err.println("ImplerException caught: " + e.getMessage());
        }
    }

}
