package org.tmatesoft.grooby.exec


interface IEsExecutableCommand {

    boolean matches(String command)

    Object execute(File workDir, String command, IEsCommandLogger logger)

    Object execute(IEsWorkingDirectoryProvider workDirProvider, String command, IEsCommandLogger logger)
}