package org.tmatesoft.grooby.exec

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.transform.*

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