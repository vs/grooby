package org.tmatesoft.grooby.example

import java.lang.reflect.Method
import org.tmatesoft.grooby.Skip
import org.tmatesoft.grooby.Transformable
import org.tmatesoft.grooby.cache.InsertMethodHash
import org.tmatesoft.grooby.cache.MethodHash

@InsertMethodHash(applyToMethodsWith = Transformable, doNotIncludeToHash = Skip)
class InsertHash {

    @Transformable
    void insertHash() {
        println 'Hello, World!'
    }

    @Skip
    @Transformable
    void insertHashWithSkip() {
        insertHash()
    }

    @Transformable
    void insertHashWithNoSkip() {
        insertHash()
    }

    void doNotInsertHash() {
        insertHash()
    }

    public static void main(String[] args) {
        Method[] methods = InsertHash.getMethods()
        methods.findAll {Method method ->
            !method.isSynthetic()
        }.each {Method method ->
            MethodHash hash = method.getAnnotation(MethodHash)
            System.out.println("${method.name}: ${hash?.value()}");
        }
    }
}
