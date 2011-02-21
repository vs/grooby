package org.tmatesoft.grooby.cache

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass("org.tmatesoft.grooby.cache.InsertMethodHashAstTransformation")
public @interface InsertMethodHash {

    Class applyToMethodsWith()

    Class[] doNotIncludeToHash() default []
}