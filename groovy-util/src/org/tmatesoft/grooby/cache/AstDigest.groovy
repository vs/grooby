package org.tmatesoft.grooby.cache

import java.security.MessageDigest
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.ast.*

class AstDigest {

    final static String HASH_FUNCTION = 'SHA-1'
    final static int RAW_HASH_LENGTH = 20
    final static String HEX_CHARS = '0123456789abcdef'

    final MessageDigest digest

    AstDigest() {
        digest = MessageDigest.getInstance(HASH_FUNCTION)
    }

    String calculateHash() {
        byte[] rawDigest = digest.digest()
        return toHexString(rawDigest)
    }

    void includeBoolean(boolean bool) {
        digest.update((byte) bool ? 1 : 0)
    }

    void includeString(String data) {
        if (!data) {
            return
        }
        digest.update(data.getBytes('UTF-8'))
    }

    void includeInt(int i) {
        digest.update((byte) 0xff & i)
        digest.update((byte) 0xff & (i >> 8))
        digest.update((byte) 0xff & (i >> 16))
        digest.update((byte) 0xff & (i >> 24))
    }

    void includeClassNode(ClassNode classNode) {
        includeString(classNode.name)
        includeBoolean(classNode == ClassNode.THIS)
        includeBoolean(classNode == ClassNode.SUPER)
    }

    void includeExpression(Expression expression) {
        if (!expression) {
            return
        }
        expression.visit(this)
    }

    void includeFieldNode(FieldNode fieldNode) {
        if (!fieldNode) {
            return
        }
        includeClassNode(fieldNode.type)
        includeString(fieldNode.name)
        includeExpression(fieldNode.initialExpression)
    }

    void includeParameter(Parameter parameter) {
        if (!parameter) {
            return
        }
        includeClassNode(parameter.type)
        includeString(parameter.name)
        includeExpression(parameter.getInitialExpression())
    }

    void includeParameters(Parameter[] parameters) {
        if (!parameters) {
            return
        }
        for (Parameter parameter: parameters) {
            includeParameter(parameter)
        }
    }

    void includeToken(Token token) {
        if (!token) {
            return
        }
        includeInt(token.type)
    }

    void includeAnnotation(AnnotationNode annotationNode) {
        includeClassNode(annotationNode.classNode)
        for (e in annotationNode.members) {
            includeString(e.key)
            includeExpression(e.value)
        }
    }

    void includeAnnotations(AnnotatedNode annotatedNode, Closure include = {AnnotationNode annotation -> return true}) {
        if (!annotatedNode) {
            return
        }
        annotatedNode.getAnnotations().each {AnnotationNode annotationNode ->
            if (include(annotationNode)) {
                includeAnnotation(annotationNode)
            }
        }
    }

    void enter(ASTNode node) {
        includeString("e ${node.class.simpleName}")
        if (node instanceof AnnotatedNode) {
            includeAnnotations(node)
        }
    }

    void leave(ASTNode node) {
        includeString("l ${node.class.simpleName}")
    }

    private static final String toHexString(final byte[] rawDigest) {
        final int a = decodeInt32(rawDigest, 0)
        final int b = decodeInt32(rawDigest, 4)
        final int c = decodeInt32(rawDigest, 8)
        final int d = decodeInt32(rawDigest, 12)
        final int e = decodeInt32(rawDigest, 16)
        char[] result = new char[2 * RAW_HASH_LENGTH]
        formatHexChar(result, 0, a)
        formatHexChar(result, 8, b)
        formatHexChar(result, 16, c)
        formatHexChar(result, 24, d)
        formatHexChar(result, 32, e)
        return new String(result)
    }

    private static int decodeInt32(final byte[] data, final int offset) {
        int r = data[offset] << 8

        r |= data[offset + 1] & 0xff
        r <<= 8

        r |= data[offset + 2] & 0xff
        return (r << 8) | (data[offset + 3] & 0xff)
    }

    private static void formatHexChar(final char[] buffer, final int offset, int i) {
        int o = offset + 7
        while (o >= offset && i != 0) {
            buffer[o--] = HEX_CHARS[i & 0xf]
            i >>>= 4
        }
        while (o >= offset) {
            buffer[o--] = '0'
        }
    }
}
