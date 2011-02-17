package org.tmatesoft.grooby.exec

abstract class EsCommandExecutor {

    static final IEsCommandOutputHandler RETURN_OUTPUT_HANDLER = new IEsCommandOutputHandler() {
        Object handleOutput(String out, String err) {
            return out
        }
    }

    abstract String getCommandName()

    abstract String getExecutable()

    abstract IEsCommandOutputHandler getOutputHandler(String subcommand)

    Object executeCommand(String prefix, File workDir, String commandLine, IEsCommandLogger logger) {
        List commandTokens = tokenize(commandLine)
        String commandName = commandTokens[0]
        assert commandName == getCommandName()
        commandTokens[0] = getExecutable()
        String subcommandName = commandTokens[1]
        IEsCommandOutputHandler handler = getOutputHandler(subcommandName)

        ProcessBuilder processBuilder = new ProcessBuilder(commandTokens).directory(workDir)
        Process process = processBuilder.start()
        StringBuffer stdout = new StringBuffer()
        StringBuffer stderr = new StringBuffer()
        try {
            Thread stdoutReader = process.consumeProcessOutputStream(stdout)
            Thread stderrReader = process.consumeProcessErrorStream(stderr)
            process.waitFor()
            stdoutReader.join()
            stderrReader.join()
        }
        finally {
            process.destroy()
        }

        String out = stdout.toString()
        String err = stderr.toString()
        logger?.logCommand(prefix, workDir, commandLine, out, err)

        if (process.exitValue() != 0) {
            throw new AssertionError("Failed to execute $commandLine:\n$err")
        }
        return handler?.handleOutput(out, err)
    }

    List tokenize(String commandArgs) {
        final String QUOTE = '\''

        String[] tokens = commandArgs.split()
        List args = []
        int quoteStartIdx = -1

        def insideQuotes = {
            return quoteStartIdx >= 0
        }

        def joinTokens = { int from, int to ->
            return tokens[from..to].join(' ')
        }

        def extractQuotes = { int idx ->
            String quotes = joinTokens(quoteStartIdx, idx)
            quoteStartIdx = -1
            return quotes
        }

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens[i]
            boolean lastToken = i == tokens.size() - 1

            if (token == QUOTE) {
                if (insideQuotes()) {
                    String arg = extractQuotes(i)
                    args.add(arg)
                    continue
                } else if (lastToken) {
                    args.add(QUOTE)
                    continue
                } else {
                    quoteStartIdx = i
                    continue
                }
            }

            if (token.startsWith(QUOTE) && token.endsWith(QUOTE)) {
                if (insideQuotes()) {
                    String arg = extractQuotes(i)
                    args.add(arg)
                    continue
                }
                args.add(token)
                continue
            }

            if (token.startsWith(QUOTE)) {
                if (insideQuotes()) {
                    throw new AssertionError("Token $token starts inner quotes inside input string $commandArgs")
                }
                quoteStartIdx = i
                continue
            }

            if (token.endsWith(QUOTE)) {
                if (!insideQuotes()) {
                    throw new AssertionError("Token $token ends not opened quotes inside input string $commandArgs")
                }
                String arg = extractQuotes(i)
                args.add(arg)
                continue
            }

            if (insideQuotes()) {
                if (lastToken) {
                    String arg = extractQuotes(i)
                    args.add(arg)
                }
                continue
            }
            args.add(token)
        }
        args = removeSingleQuotes(args)
        return args
    }

    private List removeSingleQuotes(List tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens[i]
            if (token.length() > 1 && token.startsWith("'") && token.endsWith("'")) {
                String modifiedToken = token.substring(1, token.length() - 1)
                tokens[i] = modifiedToken
            }
        }
        return tokens
    }
}
