package org.tmatesoft.grooby.cache

import java.lang.reflect.Method
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.tools.ast.TestHarnessClassLoader
import org.junit.Test
import org.tmatesoft.grooby.exec.EsAstTransformation

class InsertMethodIdTransformationTest extends GroovyTestCase {

    @Test
    void testTransformation() {
        String input =
        """
package org.tmatesoft.grooby.cache

@InsertMethodHash(applyToMethodsWith = MockUpTarget, doNotIncludeToHash = [MockUpSkip])
class MockUpClass {

    @MockUpTarget
    void method0() {
        methodHasNoHash()
    }

    @MockUpTarget
    @MockUpSkip
    void method0Skip() {
        methodHasNoHash()
    }

    @MockUpTarget
    @MockUpAnnotation
    void method0Annotated() {
        methodHasNoHash()
    }

    @MockUpTarget
    void method1() {
        int i = 2 + 2
        methodHasNoHash()
        println(i)
    }

    @MockUpTarget
    @MockUpSkip
    void method1Skip() {
        int i = 2 + 2
        methodHasNoHash()
        println(i)
    }

    @MockUpTarget
    @MockUpAnnotation
    void method1Annotated() {
        int i = 2 + 2
        methodHasNoHash()
        println(i)
    }

    @MockUpTarget
    void method2() {
        String s = "meaningless string variable"
        println(s)
    }

    @MockUpTarget
    @MockUpSkip
    void method2Skip() {
        String s = "meaningless string variable"
        println(s)
    }

    @MockUpTarget
    @MockUpAnnotation
    void method2Annotated() {
        String s = "meaningless string variable"
        println(s)
    }

    void methodHasNoHash() {
        println 'Meaningless text!'
    }
}
        """

        Map methodSymbols = [:]
        methodSymbols['method0'] = '0'
        methodSymbols['method0Skip'] = '0'
        methodSymbols['method0Annotated'] = '0.annotated'
        methodSymbols['method1'] = '1'
        methodSymbols['method1Skip'] = '1'
        methodSymbols['method1Annotated'] = '1.annotated'
        methodSymbols['method2'] = '2'
        methodSymbols['method2Skip'] = '2'
        methodSymbols['method2Annotated'] = '2.annotated'

        assertValidMethodIds(input, methodSymbols)
    }

    void assertValidMethodIds(String input, Map<String, String> methodSymbols) {
        TestHarnessClassLoader loader = new TestHarnessClassLoader(new EsAstTransformation(), CompilePhase.SEMANTIC_ANALYSIS)
        def clazz = loader.parseClass(input)
        for (Method method: clazz.methods) {
            MethodHash methodHash = method.getAnnotation(MethodHash)
            String hash = methodHash?.value()
            String symbol = methodSymbols[method.name]
            assertTrue((hash != null && symbol != null) || (hash == null && symbol == null))
            if (hash == null) {
                continue
            }

            for (Method m: clazz.methods) {
                MethodHash mh = m.getAnnotation(MethodHash)
                String h = mh?.value()
                String s = methodSymbols[m.name]

                if (hash.equals(h)) {
                    assertEquals(symbol, s)
                }

                if (symbol.equals(s)) {
                    assertEquals(hash, h)
                }
            }
        }
    }
}
