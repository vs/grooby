package org.tmatesoft.grooby.exec

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.*

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class EsAstTransformation implements ASTTransformation {
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (astNodes == null) {
            return
        }

        boolean annotationFound = false
        ASTNode target = null
        astNodes.each {node ->
            if (!node) {
                return
            }
            if (node instanceof AnnotationNode && node.classNode?.name == ExecuteString.class.getName()) {
                annotationFound |= true;
            }
            if (node instanceof ClassNode || node instanceof MethodNode) {
                target = node;
            }
        }

        if (annotationFound && target) {
            final GroovyCodeVisitor visitor = new EsAstTransformationVisitor(sourceUnit: sourceUnit)
            if (target instanceof ClassNode) {
                visitor.visitClass(target)
            } else {
                visitor.visitMethod(target)
            }
        }
    }
}