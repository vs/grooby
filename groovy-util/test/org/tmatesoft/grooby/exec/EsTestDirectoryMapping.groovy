package org.tmatesoft.grooby.exec

class EsTestDirectoryMapping implements IEsNameDirectoryMapping {

    IEsWorkingDirectoryProvider getDirectoryProvider(String name) {
        return new DirectoryProvider(name)
    }

    class DirectoryProvider implements IEsWorkingDirectoryProvider {

        String name

        DirectoryProvider(String name) {
            this.name = name
        }

        String getName() {
            return name
        }

        File prepareWorkingDirectory() {
            return null
        }
    }
}
