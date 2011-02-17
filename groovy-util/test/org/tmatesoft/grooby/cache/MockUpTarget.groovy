package org.tmatesoft.grooby.cache

import java.lang.annotation.*

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target([ElementType.METHOD])
@interface MockUpTarget {
}