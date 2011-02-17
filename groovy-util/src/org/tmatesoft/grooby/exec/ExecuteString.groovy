package org.tmatesoft.grooby.exec

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE, ElementType.METHOD])
@GroovyASTTransformationClass(classes = [EsAstTransformation.class])
public @interface ExecuteString {
  Class<? extends IEsExecutableCommand> value() default EsExecutableCommand.class

  String directoryMapping() default ''

  String logger() default ''
}