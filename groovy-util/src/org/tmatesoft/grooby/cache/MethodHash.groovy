package org.tmatesoft.grooby.cache

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.Target
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD])
public @interface MethodHash {
  String value()
}