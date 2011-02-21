package org.tmatesoft.grooby.cache

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class InsertMethodHashAstTransformation implements ASTTransformation {

    void visit(ASTNode[] astNodes, SourceUnit source) {
        if (astNodes == null) {
            return
        }

        boolean annotationFound = false
        ClassNode target = null
        Class targetMethodIndicator = null
        List<String> skippedAnnotations = null

        astNodes.each {node ->
            if (!node) {
                return
            }
            if (node instanceof AnnotationNode && node.classNode?.name == InsertMethodHash.class.getName()) {
                annotationFound |= true;
                AnnotationNode annotationNode = node

                Expression methodTargetIndicatorExpression = annotationNode.getMember('applyToMethodsWith')
                if (methodTargetIndicatorExpression != null) {
                    assert methodTargetIndicatorExpression instanceof ClassExpression
                    assert methodTargetIndicatorExpression.type?.name != null
                    targetMethodIndicator = loadClass(methodTargetIndicatorExpression.type)
                }

                Expression skipExpression = annotationNode.getMember('doNotIncludeToHash')
                if (skipExpression != null) {
                    if (skipExpression instanceof ClassExpression) {
                        skippedAnnotations = [skipExpression.type.name]
                    } else if (skipExpression instanceof ListExpression) {
                        skippedAnnotations = skipExpression.expressions.collect {Expression e -> e.type.name}
                    }
                }
            }
            if (node instanceof ClassNode) {
                target = node;
            }
        }
        if (annotationFound && target) {
            validateTestMethods(target, targetMethodIndicator)
            insertMethodIds(target, targetMethodIndicator, skippedAnnotations)
        }
    }

    private Class loadClass(ClassNode classNode) {
        try {
            return Class.forName(classNode.name, false, this.getClass().getClassLoader())
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Failed to load class ${classNode.name}")
        }
    }

    // No test ids before we insert them

    void validateTestMethods(ClassNode classNode, Class targetMethodIndicator) {
        iterateMethods(classNode, targetMethodIndicator, { MethodNode methodNode ->
            List<AnnotationNode> methodIdAnnotations = methodNode.getAnnotations(new ClassNode(MethodHash))
            assert methodIdAnnotations == null || methodIdAnnotations.isEmpty()
        })
    }

    void insertMethodIds(ClassNode classNode, Class targetMethodIndicator, List<String> skippedAnnotations) {
        iterateMethods(classNode, targetMethodIndicator, { MethodNode methodNode ->
            insertTestId(methodNode, skippedAnnotations)
        })
    }

    String insertTestId(MethodNode methodNode, List<String> skippedAnnotations) {
        MethodHashCalculator methodIdCalculator = new MethodHashCalculator()
        String methodId = methodIdCalculator.calculateMethodId(methodNode, skippedAnnotations)

        AnnotationNode methodHashAnnotation = new AnnotationNode(new ClassNode(MethodHash))
        methodHashAnnotation.addMember('value', new ConstantExpression(methodId))
        methodNode.addAnnotation(methodHashAnnotation)
        return methodId
    }

    void iterateMethods(ClassNode classNode, Class targetMethodIndicator, Closure closure) {
        if (targetMethodIndicator == null) {
            classNode.methods.each(closure)
        } else {
            classNode.methods.findAll {MethodNode methodNode ->
                List<AnnotationNode> annotations = methodNode.getAnnotations(new ClassNode(targetMethodIndicator))
                return annotations != null && !annotations.isEmpty()
            }.each(closure)
        }
    }
}
