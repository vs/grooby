package org.tmatesoft.grooby.exec

import java.util.regex.Pattern
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*

class EsAstTransformationVisitor extends ClassCodeVisitorSupport {

    private static final Pattern CONSTANT_WORK_DIR_PREFIX = Pattern.compile('^(\\w+):')
    private static final Pattern GSTRING_WORK_DIR_PREFIX = Pattern.compile('^\\$(\\w+):')

    SourceUnit sourceUnit
    Class<? extends IEsExecutableCommand> commandClass
    String directoryMappingProperty
    String loggerProperty

    @Override
    void visitClass(ClassNode node) {
        fetchTransformationInfo(node)
        super.visitClass(node)
    }

    @Override
    void visitMethod(MethodNode node) {
        if (node.isSynthetic()) {
            return
        }
        fetchTransformationInfo(node)
        super.visitMethod(node)
    }

    void fetchTransformationInfo(AnnotatedNode node) {
        for (AnnotationNode annotationNode : node.getAnnotations(new ClassNode(ExecuteString))) {
            Expression commandClassExpression = annotationNode.getMember('value')
            Class commandClass
            if (commandClassExpression != null) {
                assert commandClassExpression instanceof ClassExpression
                assert commandClassExpression.type?.name != null
                try {
                    commandClass = Class.forName(commandClassExpression.type.name, false, this.getClass().getClassLoader())
                } catch (ClassNotFoundException cnfe) {
                    throw new RuntimeException("Failed to load command class $commandClassExpression.type?.name specified at $sourceUnit.name")
                }
            } else {
                commandClass = EsExecutableCommand
            }
            assert commandClass.isEnum()
            assert IEsExecutableCommand.isAssignableFrom(commandClass)
            this.commandClass = (Class<? extends IEsExecutableCommand>) commandClass
            
            Expression directoryMappingExpression = annotationNode.getMember('directoryMapping')
            assert !directoryMappingExpression || directoryMappingExpression instanceof ConstantExpression
            directoryMappingProperty = directoryMappingExpression?.value

            Expression loggerExpression = annotationNode.getMember('logger')
            assert !loggerExpression || loggerExpression instanceof ConstantExpression
            loggerProperty = loggerExpression?.value
        }
    }

    @Override
    void visitDeclarationExpression(DeclarationExpression declarationExpression) {
        super.visitDeclarationExpression(declarationExpression)
        def update = { Expression transformedExpression ->
            declarationExpression.rightExpression = transformedExpression
        }
        performTransformation(declarationExpression.rightExpression, update)
    }

    @Override
    void visitExpressionStatement(ExpressionStatement expressionStatement) {
        super.visitExpressionStatement(expressionStatement)
        def update = {Expression transformedExpression ->
            expressionStatement.expression = transformedExpression
        }
        performTransformation(expressionStatement.expression, update)
    }

    @Override
    void visitReturnStatement(ReturnStatement returnStatement) {
        super.visitReturnStatement(returnStatement)
        def update = {Expression transformedExpression ->
            returnStatement.expression = transformedExpression
        }
        performTransformation(returnStatement.expression, update)
    }

    def performTransformation(Expression expression, Closure update) {
        Expression transformedExpression = transform(expression)
        if (transformedExpression) {
            transformedExpression.lineNumber = expression.lineNumber
            transformedExpression.lastLineNumber = expression.lastLineNumber
            update transformedExpression
        }
    }

    Expression transform(Expression expression) {
        switch (expression.class) {
            case ConstantExpression.class:
                ConstantExpression constantExpression = (ConstantExpression) expression
                return transformConstantExpression(constantExpression)
            case GStringExpression.class:
                GStringExpression gStringExpression = (GStringExpression) expression
                return transformGStringExpression(gStringExpression)
        }
        return null
    }

    Expression transformConstantExpression(ConstantExpression expression) {
        String prefix = extractWorkDirPrefix((String) expression.value)
        String commandCandidate
        String workDirName
        if (prefix != null) {
            commandCandidate = expression.value.toString().substring(prefix.length())
            commandCandidate = removeLeadingWhitespace(commandCandidate)
            workDirName = removeTrailingColon(prefix)
        } else {
            commandCandidate = expression.value.toString()
            workDirName = null
        }
        IEsExecutableCommand executableCommand = findExecutableCommand(commandCandidate)
        if (executableCommand == null) {
            return null
        }
        Expression workDirExpression = createMappedWorkDirExpression(workDirName)
        Expression commandExpression = new ConstantExpression(commandCandidate)
        return createExecutionCall(executableCommand, workDirExpression, commandExpression)
    }

    Expression transformGStringExpression(GStringExpression expression) {
        String prefix = extractGStringWorkDirPrefix(expression)
        if (prefix != null) {
            String commandCandidate = expression.text.substring(prefix.length())
            commandCandidate = removeLeadingWhitespace(commandCandidate)
            IEsExecutableCommand executableCommand = findExecutableCommand(commandCandidate)
            if (executableCommand == null) {
                return null
            }
            Expression workDirExpression = expression.values[0]
            Expression commandExpression = createGStringCommandExpression(commandCandidate, expression)
            return createExecutionCall(executableCommand, workDirExpression, commandExpression)
        }
        prefix = extractWorkDirPrefix(expression.text)
        String commandCandidate
        String workDirName
        if (prefix != null) {
            commandCandidate = expression.text.substring(prefix.length())
            commandCandidate = removeLeadingWhitespace(commandCandidate)
            workDirName = removeTrailingColon(prefix)
        } else {
            commandCandidate = expression.text
            workDirName = null
        }
        IEsExecutableCommand executableCommand = findExecutableCommand(commandCandidate)
        if (executableCommand == null) {
            return null
        }
        Expression workDirExpression = createMappedWorkDirExpression(workDirName)
        Expression commandExpression = createMappedGStringCommandExpression(commandCandidate, workDirName, expression)
        return createExecutionCall(executableCommand, workDirExpression, commandExpression)
    }

    String extractWorkDirPrefix(String str) {
        return str.find(CONSTANT_WORK_DIR_PREFIX)
    }

    String extractGStringWorkDirPrefix(GStringExpression gStringExpression) {
        String prefix = gStringExpression.text.find(GSTRING_WORK_DIR_PREFIX)
        if (prefix != null) {
            // do not allow escaped arguments, e.g. "\$workDir: command $something",
            // for that case gStringExpression.strings[0] is "\$workDir: command "
            // and gStringExpression.strings[1] is "".
            if (!gStringExpression.strings.isEmpty() &&
                    !gStringExpression.strings[0].value.find(GSTRING_WORK_DIR_PREFIX)) {
                return prefix
            }
        }
        return null
    }

    IEsExecutableCommand findExecutableCommand(String commandCandidate) {
        for (IEsExecutableCommand command: commandClass.getEnumConstants()) {
            if (command.matches(commandCandidate)) {
                return command
            }
        }
        return null
    }

    MethodCallExpression createMappedWorkDirExpression(String workDirName) {
        if (workDirName == null) {
            return null
        }
        if (directoryMappingProperty == null) {
            throw new AssertionError("Failed to map $workDirName: directoryMapping annotation member is missing.")
        }
        return new MethodCallExpression(
                new VariableExpression(directoryMappingProperty),
                new ConstantExpression('getDirectoryProvider'),
                new ArgumentListExpression(
                        new ConstantExpression(workDirName)
                )
        )
    }

    Expression createGStringCommandExpression(String command, GStringExpression gStringExpression) {
        if (gStringExpression.strings.size() == 2 && gStringExpression.values.size() == 1) {
            return new ConstantExpression(command)
        }
        Expression commandStringExpression = gStringExpression.strings[1]
        String commandString
        if (commandStringExpression instanceof ConstantExpression) {
            commandString = commandStringExpression.value
        } else if (commandStringExpression instanceof GStringExpression) {
            commandString = commandStringExpression.text
        } else {
            throw new AssertionError("Failed to handle string expression at GString $gStringExpression.text")
        }

        commandString = removeLeadingColon(commandString)
        commandString = removeLeadingWhitespace(commandString)

        List<ConstantExpression> strings = gStringExpression.strings.subList(1, gStringExpression.strings.size())
        strings[0] = new ConstantExpression(commandString)
        List<Expression> values = gStringExpression.values.subList(1, gStringExpression.values.size())
        return new GStringExpression(command, strings, values)
    }

    Expression createMappedGStringCommandExpression(String command, String workDirName, GStringExpression gStringExpression) {
        if (workDirName == null) {
            return gStringExpression
        }
        if (gStringExpression.strings.size() == 1 && gStringExpression.values.size() == 0) {
            return new ConstantExpression(command)
        }
        Expression commandStringExpression = gStringExpression.strings[0]
        String commandString
        if (commandStringExpression instanceof ConstantExpression) {
            commandString = commandStringExpression.value
        } else if (commandStringExpression instanceof GStringExpression) {
            commandString = commandStringExpression.text
        } else {
            throw new AssertionError("Failed to handle string expression at GString $gStringExpression.text")
        }
        commandString = commandString.substring(workDirName.length())
        commandString = removeLeadingColon(commandString)
        commandString = removeLeadingWhitespace(commandString)

        gStringExpression.strings[0] = new ConstantExpression(commandString)
        return new GStringExpression(command, gStringExpression.strings, gStringExpression.values)
    }

    MethodCallExpression createExecutionCall(IEsExecutableCommand command,
                                             Expression workDirExpression,
                                             Expression commandExpression) {
        if (workDirExpression == null) {
            if (directoryMappingProperty != null) {
                workDirExpression = createMappedWorkDirExpression('')
            } else {
                workDirExpression = new CastExpression(new ClassNode(File), new ConstantExpression(null))
            }
        }
        Expression loggerExpression;
        if (loggerProperty != null) {
            loggerExpression = new VariableExpression(loggerProperty)
        } else {
            loggerExpression = new ConstantExpression(null)
        }

        return new MethodCallExpression(
                new PropertyExpression(
                        new ClassExpression(new ClassNode(commandClass)),
                        new ConstantExpression(command.name())
                ),
                new ConstantExpression('execute'),
                new ArgumentListExpression([workDirExpression, commandExpression, loggerExpression])
        )
    }

    private static String removeLeadingWhitespace(String str) {
        if (str.startsWith(' ')) {
            return str.substring(' '.length())
        }
        return str
    }

    private static String removeLeadingColon(String str) {
        if (str.startsWith(':')) {
            return str.substring(':'.length())
        }
        return str
    }

    private static String removeTrailingColon(String str) {
        if (str.endsWith(':')) {
            return str.substring(0, str.length() - ':'.length())
        }
        return str
    }
}
