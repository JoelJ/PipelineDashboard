package com.attask.jenkins.codereviewer;

import hudson.console.ConsoleNote;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Result;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: brianmondido
 * Date: 8/2/12
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockBuildListener implements BuildListener {
    public void started(List<Cause> causes) {
        
    }

    public void finished(Result result) {

    }

    public PrintStream getLogger() {
        return System.out;
    }

    public void annotate(ConsoleNote ann) throws IOException {

    }

    public void hyperlink(String url, String text) throws IOException {

    }

    public PrintWriter error(String msg) {
        PrintWriter printWriter = new PrintWriter(System.out);
        printWriter.println("[MockBuildListener] [error] " + msg);
        return printWriter;
    }

    public PrintWriter error(String format, Object... args) {
        PrintWriter printWriter = new PrintWriter(System.out);
        printWriter.printf("[MockBuildListener] [error] " + format, args);
        return printWriter;
    }

    public PrintWriter fatalError(String msg) {
        PrintWriter printWriter = new PrintWriter(System.out);
        printWriter.println("[MockBuildListener] [error] " + msg);
        return printWriter;
    }

    public PrintWriter fatalError(String format, Object... args) {
        PrintWriter printWriter = new PrintWriter(System.out);
        printWriter.printf("[MockBuildListener] [error] " + format, args);
        return printWriter;
    }
}
