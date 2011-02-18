package org.tmatesoft.grooby.cache

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target([ElementType.METHOD])
@interface MockUpTarget {
}