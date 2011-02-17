package org.tmatesoft.grooby.cache

import java.lang.annotation.*
import org.codehaus.groovy.transform.*

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(classes = [InsertMethodHashAstTransformation])
public @interface InsertMethodHash {

  Class applyToMethodsWith()

  Class[] doNotIncludeToHash() default []
}