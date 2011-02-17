package org.tmatesoft.grooby.cache

import java.lang.annotation.*

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD])
public @interface MockUpAnnotation {
}