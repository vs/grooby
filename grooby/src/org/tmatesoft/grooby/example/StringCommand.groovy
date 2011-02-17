package org.tmatesoft.grooby.example

import org.tmatesoft.grooby.exec.EsExecutableCommand
import org.tmatesoft.grooby.exec.ExecuteString
import org.tmatesoft.grooby.exec.IEsCommandLogger

@ExecuteString
class StringCommand {

    private static final IEsCommandLogger LOGGER = new IEsCommandLogger() {
        void logCommand(String prefix, File workDir, String command, String out, String err) {
            println "\$ $command\n$out\n$err"
        }
    }

    @ExecuteString(value = EsExecutableCommand, logger = 'LOGGER')
    public static void main(String[] args) {
        "echo Hello there!"
    }
}
