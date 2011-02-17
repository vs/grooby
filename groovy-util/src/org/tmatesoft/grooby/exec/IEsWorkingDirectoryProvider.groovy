package org.tmatesoft.grooby.exec


interface IEsWorkingDirectoryProvider {

    String getName()

    File prepareWorkingDirectory()
}
