package org.tmatesoft.grooby.exec

import java.util.regex.Pattern

enum EsTestExecutableCommand implements IEsExecutableCommand {

    TEST

    Pattern pattern = Pattern.compile('^test .*$')
    List<String> results = []
    Map<File, String> workDirResults = [:]
    Map<String, String> workDirProviderResults = [:]

    boolean matches(String command) {
        return pattern.matcher(command).matches()
    }

    Object execute(File workDir, String command, IEsCommandLogger logger) {
        assert logger == null
        if (matches(command)) {
            def args = command.substring('test '.length())
            if (workDir != null) {
                workDirResults[workDir] = args
            } else {
                results << args
            }
            return args
        }
        return null
    }

    Object execute(IEsWorkingDirectoryProvider workDirProvider, String command, IEsCommandLogger logger) {
        assert logger == null
        if (matches(command)) {
            def args = command.substring('test '.length())
            if (workDirProvider != null) {
                EsTestDirectoryMapping.DirectoryProvider dir = workDirProvider
                workDirProviderResults[dir.name] = args
            }
            return args
        }
        return null
    }
}
