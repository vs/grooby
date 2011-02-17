package org.tmatesoft.grooby.exec

public interface IEsCommandLogger {

    void logCommand(String prefix, File workDir, String command, String out, String err)
}