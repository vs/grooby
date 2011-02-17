package org.tmatesoft.grooby.exec

@Singleton
class EsEchoCommandExecutor extends EsCommandExecutor {

    String getCommandName() {
        'echo'
    }

    @Override
    String getExecutable() {
        'echo'
    }

    @Override
    IEsCommandOutputHandler getOutputHandler(String subcommand) {
        RETURN_OUTPUT_HANDLER
    }
}
