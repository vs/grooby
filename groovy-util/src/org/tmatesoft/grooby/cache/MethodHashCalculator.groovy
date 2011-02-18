package org.tmatesoft.grooby.cache

import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

class MethodHashCalculator extends CodeVisitorSupport {

    @Delegate
    AstDigest digest

    String calculateMethodId(MethodNode methodNode, List<String> skippedAnnotations) {
        this.digest = new AstDigest()

        Closure filterAnnotations = {AnnotationNode annotationNode ->
            String className = annotationNode?.classNode?.name
            return skippedAnnotations && !skippedAnnotations.contains(className) && className != MethodHash.name
        }

        includeAnnotations(methodNode, filterAnnotations)
        visitBlockStatement(methodNode.code)
        return calculateHash()
    }

    @Override
    void visitBlockStatement(BlockStatement block) {
        enter(block)
        super.visitBlockStatement(block)
        leave(block)
    }

    @Override
    void visitForLoop(ForStatement forLoop) {
        enter(forLoop)
        includeParameter(forLoop.variable)
        super.visitForLoop(forLoop)
        leave(forLoop)
    }

    @Override
    void visitWhileLoop(WhileStatement loop) {
        enter(loop)
        super.visitWhileLoop(loop)
        leave(loop)
    }

    @Override
    void visitDoWhileLoop(DoWhileStatement loop) {
        enter(loop)
        super.visitDoWhileLoop(loop)
        leave(loop)
    }

    @Override
    void visitIfElse(IfStatement ifElse) {
        enter(ifElse)
        super.visitIfElse(ifElse)
        leave(ifElse)
    }

    @Override
    void visitExpressionStatement(ExpressionStatement statement) {
        enter(statement)
        super.visitExpressionStatement(statement)
        leave(statement)
    }

    @Override
    void visitReturnStatement(ReturnStatement statement) {
        enter(statement)
        super.visitReturnStatement(statement)
        leave(statement)
    }

    @Override
    void visitAssertStatement(AssertStatement statement) {
        enter(statement)
        super.visitAssertStatement(statement)
        leave(statement)
    }

    @Override
    void visitTryCatchFinally(TryCatchStatement statement) {
        enter(statement)
        super.visitTryCatchFinally(statement)
        leave(statement)
    }

    @Override
    void visitSwitch(SwitchStatement statement) {
        enter(statement)
        super.visitSwitch(statement)
        leave(statement)
    }

    @Override
    void visitCaseStatement(CaseStatement statement) {
        enter(statement)
        super.visitCaseStatement(statement)
        leave(statement)
    }

    @Override
    void visitBreakStatement(BreakStatement statement) {
        enter(statement)
        includeString(statement.label)
        super.visitBreakStatement(statement)
        leave(statement)
    }

    @Override
    void visitContinueStatement(ContinueStatement statement) {
        enter(statement)
        includeString(statement.label)
        super.visitContinueStatement(statement)
        leave(statement)
    }

    @Override
    void visitSynchronizedStatement(SynchronizedStatement statement) {
        enter(statement)
        super.visitSynchronizedStatement(statement)
        leave(statement)
    }

    @Override
    void visitThrowStatement(ThrowStatement statement) {
        enter(statement)
        super.visitThrowStatement(statement)
        leave(statement)
    }

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        enter(call)
        includeBoolean(call.implicitThis)
        super.visitMethodCallExpression(call)
        leave(call)
    }

    @Override
    void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        enter(call)
        includeString(call.method)
        includeClassNode(call.ownerType)
        super.visitStaticMethodCallExpression(call)
        leave(call)
    }

    @Override
    void visitConstructorCallExpression(ConstructorCallExpression call) {
        enter(call)
        includeBoolean(call.usingAnonymousInnerClass)
        includeClassNode(call.type)
        super.visitConstructorCallExpression(call)
        leave(call)
    }

    @Override
    void visitBinaryExpression(BinaryExpression expression) {
        enter(expression)
        includeToken(expression.operation)
        super.visitBinaryExpression(expression)
        leave(expression)
    }

    @Override
    void visitTernaryExpression(TernaryExpression expression) {
        enter(expression)
        super.visitTernaryExpression(expression)
        leave(expression)
    }

    @Override
    void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        enter(expression)
        super.visitShortTernaryExpression(expression)
        leave(expression)
    }

    @Override
    void visitPostfixExpression(PostfixExpression expression) {
        enter(expression)
        includeToken(expression.operation)
        super.visitPostfixExpression(expression)
        leave(expression)
    }

    @Override
    void visitPrefixExpression(PrefixExpression expression) {
        enter(expression)
        includeToken(expression.operation)
        super.visitPrefixExpression(expression)
        leave(expression)
    }

    @Override
    void visitBooleanExpression(BooleanExpression expression) {
        enter(expression)
        super.visitBooleanExpression(expression)
        leave(expression)
    }

    @Override
    void visitNotExpression(NotExpression expression) {
        enter(expression)
        super.visitNotExpression(expression)
        leave(expression)
    }

    @Override
    void visitClosureExpression(ClosureExpression expression) {
        enter(expression)
        includeParameters(expression.parameters)
        super.visitClosureExpression(expression)
        leave(expression)
    }

    @Override
    void visitTupleExpression(TupleExpression expression) {
        enter(expression)
        super.visitTupleExpression(expression)
        leave(expression)
    }

    @Override
    void visitListExpression(ListExpression expression) {
        enter(expression)
        includeBoolean(expression.wrapped)
        super.visitListExpression(expression)
        leave(expression)
    }

    @Override
    void visitArrayExpression(ArrayExpression expression) {
        enter(expression)
        super.visitArrayExpression(expression)
        leave(expression)
    }

    @Override
    void visitMapExpression(MapExpression expression) {
        enter(expression)
        super.visitMapExpression(expression)
        leave(expression)
    }

    @Override
    void visitMapEntryExpression(MapEntryExpression expression) {
        enter(expression)
        super.visitMapEntryExpression(expression)
        leave(expression)
    }

    @Override
    void visitRangeExpression(RangeExpression expression) {
        enter(expression)
        includeBoolean(expression.inclusive)
        super.visitRangeExpression(expression)
        leave(expression)
    }

    @Override
    void visitSpreadExpression(SpreadExpression expression) {
        enter(expression)
        super.visitSpreadExpression(expression)
        leave(expression)
    }

    @Override
    void visitSpreadMapExpression(SpreadMapExpression expression) {
        enter(expression)
        super.visitSpreadMapExpression(expression)
        leave(expression)
    }

    @Override
    void visitMethodPointerExpression(MethodPointerExpression expression) {
        enter(expression)
        super.visitMethodPointerExpression(expression)
        leave(expression)
    }

    @Override
    void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        enter(expression)
        super.visitUnaryMinusExpression(expression)
        leave(expression)
    }

    @Override
    void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        enter(expression)
        super.visitUnaryPlusExpression(expression)
        leave(expression)
    }

    @Override
    void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        enter(expression)
        super.visitBitwiseNegationExpression(expression)
        leave(expression)
    }

    @Override
    void visitCastExpression(CastExpression expression) {
        enter(expression)
        includeClassNode(expression.type)
        super.visitCastExpression(expression)
        leave(expression)
    }

    @Override
    void visitConstantExpression(ConstantExpression expression) {
        enter(expression)
        includeString(expression.constantName)
        includeString(String.valueOf(expression.value))
        super.visitConstantExpression(expression)
        leave(expression)
    }

    @Override
    void visitClassExpression(ClassExpression expression) {
        enter(expression)
        includeClassNode(expression.type)
        super.visitClassExpression(expression)
        leave(expression)
    }

    @Override
    void visitVariableExpression(VariableExpression expression) {
        enter(expression)
        includeString(expression.name)
        super.visitVariableExpression(expression)
        leave(expression)
    }

    @Override
    void visitDeclarationExpression(DeclarationExpression expression) {
        enter(expression)
        includeToken(expression.operation)
        super.visitDeclarationExpression(expression)
        leave(expression)
    }

    @Override
    void visitPropertyExpression(PropertyExpression expression) {
        enter(expression)
        super.visitPropertyExpression(expression)
        leave(expression)
    }

    @Override
    void visitAttributeExpression(AttributeExpression expression) {
        enter(expression)
        super.visitAttributeExpression(expression)
        enter(expression)
    }

    @Override
    void visitFieldExpression(FieldExpression expression) {
        enter(expression)
        includeFieldNode(expression.field)
        super.visitFieldExpression(expression)
        leave(expression)
    }

    @Override
    void visitRegexExpression(RegexExpression expression) {
        enter(expression)
        includeExpression(expression.regex)
        super.visitRegexExpression(expression)
        leave(expression)
    }

    @Override
    void visitGStringExpression(GStringExpression expression) {
        enter(expression)
        includeString(expression.getText())
        super.visitGStringExpression(expression)
        leave(expression)
    }

    @Override
    void visitCatchStatement(CatchStatement statement) {
        enter(statement)
        includeParameter(statement.variable)
        super.visitCatchStatement(statement)
        leave(statement)
    }

    @Override
    void visitArgumentlistExpression(ArgumentListExpression ale) {
        enter(ale)
        super.visitArgumentlistExpression(ale)
        leave(ale)
    }

    @Override
    void visitClosureListExpression(ClosureListExpression cle) {
        enter(cle)
        includeBoolean(cle.wrapped)
        super.visitClosureListExpression(cle)
        leave(cle)
    }

    @Override
    void visitBytecodeExpression(BytecodeExpression cle) {
        enter(cle)
        super.visitBytecodeExpression(cle)
        leave(cle)
    }
}