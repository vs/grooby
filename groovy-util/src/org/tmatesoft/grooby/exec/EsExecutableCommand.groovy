package org.tmatesoft.grooby.exec

import java.util.regex.Pattern

public enum EsExecutableCommand implements IEsExecutableCommand {

    ECHO('^echo .*$', EsEchoCommandExecutor.instance),

    final Pattern pattern
    final EsCommandExecutor commandExecutor

    EsExecutableCommand(String patternString, EsCommandExecutor executor) {
        pattern = Pattern.compile(patternString)
        commandExecutor = executor
    }

    boolean matches(String command) {
        return pattern.matcher(command).matches()
    }

    Object execute(File workDir, String command, IEsCommandLogger logger) {
        if (matches(command)) {
            return commandExecutor.executeCommand(null, workDir, command, logger)
        }
        return command
    }

    Object execute(IEsWorkingDirectoryProvider workDirProvider, String command, IEsCommandLogger logger) {
        if (matches(command)) {
            return commandExecutor.executeCommand(workDirProvider.getName(), workDirProvider.prepareWorkingDirectory(), command, logger)
        }
        return command
    }
}