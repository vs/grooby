package org.tmatesoft.grooby.exec

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.tools.ast.TestHarnessClassLoader
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EsAstTransformationTest {

    @Before
    public void resetTestCommand() {
        EsTestExecutableCommand.TEST.results = []
    }

    @Test
    public void testConstantTransformationAnnotatedClass() {
        def output = "This is valid output"
        def args = ["valid arguments", "go", "here!", output]
        def input = """
    package org.tmatesoft.translator.spec.exec

    @ExecuteString(EsTestExecutableCommand)
    class Temp {
      def call() {
        def i = "test ${args[0]}"
      }

      Object run() {
        call()
        "test ${args[1]}"
        "trest this is not valid argument"
        "test ${args[2]}"
        " test first whitestpace breaks pattern matching"
        "test $output"
      }
    }
    """
        assertValidOutput(input, output, args, false)
    }

    @Test
    public void testWordDirConstantCommandAnnotatedClass() {
        def output = "This is valid output"
        Map argsMap = [:]
        argsMap['/some/path']= "valid arguments"
        argsMap['/another/path'] = "go"
        argsMap['/one/more/path'] = "here!"
        argsMap['/and/last/path'] = output
        def args = argsMap.entrySet().toArray(new Map.Entry[argsMap.size()])
        def input = """
    package org.tmatesoft.translator.spec.exec

    @ExecuteString(EsTestExecutableCommand)
    class Temp {
      def call() {
        File dir = new File('${args[0].key}')
        def i = "\$dir: test ${args[0].value}"
      }

      Object run() {
        call()
        File d1 = new File('${args[1].key}')
        "\$d1: test ${args[1].value}"

        "\$d1: trest this is not valid argument"

        File d2 = new File('${args[2].key}')
        "\$d2: test ${args[2].value}"

        " \$d2: test first whitestpace breaks pattern matching"

        File d3 = new File('${args[3].key}')
        "\$d3: test ${args[3].value}"
      }
    }
    """
        assertValidOutput(input, output, argsMap, true)
    }

    @Test
    public void testWorkDirProviderConstantCommandAnnotatedClass() {
        def output = "This is valid output"
        def argsMap = [:]
        argsMap["label1"] = "valid arguments"
        argsMap["label2"] = "go"
        argsMap["label3"] = "here!"
        argsMap["label4"] = output
        def args = argsMap.entrySet().toArray(new Map.Entry[argsMap.size()])
        def input = """
    package org.tmatesoft.translator.spec.exec

    @ExecuteString(value = EsTestExecutableCommand, directoryMapping = "sandbox")
    class Temp {

      def sandbox = new EsTestDirectoryMapping()

      def call() {
        def i = "${args[0].key}: test ${args[0].value}"
      }

      Object run() {
        call()
        "${args[1].key}: test ${args[1].value}"
        "some_label: trest this is not valid argument"
        "${args[2].key}: test ${args[2].value}"
        " no_valid_label: test first whitestpace breaks pattern matching"
        "${args[3].key}: test ${args[3].value}"
      }
    }
    """
        assertValidOutput(input, output, args, false)
    }

    @Test
    public void testConstantTransformationAnnotatedMethod() {
        def output = "This is valid output"
        def args = ["valid arguments", "go", "here!", output]
        def input = """
    package org.tmatesoft.translator.spec.exec

    class Temp {
      def call() {
        def i = "test this string is not under ExecuteString annotation"
      }
      
      @ExecuteString(EsTestExecutableCommand)
      Object run() {
        "test ${args[0]}"
        call()
        "test ${args[1]}"
        "trest trest is not valid command"
        "test ${args[2]}"
        " test first whitestpace breaks pattern matching"
        "test $output"
      }
    }
    """
        assertValidOutput(input, output, args, false)
    }

    @Test
    public void testGStringTransformationAnnotatedClass() {
        def output = "This is valid output"
        def args = ["valid arguments", "go", "here!", output]
        def input = """
    package org.tmatesoft.translator.spec.exec

    @ExecuteString(EsTestExecutableCommand)
    class Temp {
      def call() {
        def word = 'arguments'
        def i = "test valid \$word"
      }

      Object run() {
        call()
        "test ${args[1]}"
        "trest this is not valid argument"
        "test ${args[2]}"
        " test first whitestpace breaks pattern matching"
        def output = 'output'
        "test This is valid \${output}"
      }
    }
    """
        assertValidOutput(input, output, args, false)
    }
    
    @Test
    public void testWorkDirGStringCommandAnnotatedClass() {
        def output = "This is valid output"
        def argsMap = [:]
        argsMap['/some/path'] = "valid arguments"
        argsMap['/another/path'] = "go"
        argsMap['/one/more/path'] = "here!"
        argsMap['/and/last/path'] = output
        def args = argsMap.entrySet().toArray(new Map.Entry[argsMap.size()])
        def input = """
    package org.tmatesoft.translator.spec.exec

    @ExecuteString(EsTestExecutableCommand)
    class Temp {
      def call() {
        File dir = new File('${args[0].key}')
        def word = 'arguments'
        def i = "\$dir: test valid \$word"
      }

      Object run() {
        call()
        File d1 = new File('${args[1].key}')
        "\$d1: test ${args[1].value}"
        "\$d1: trest this is not valid argument"
        File d2 = new File('${args[2].key}')
        "\$d2: test ${args[2].value}"
        " \$d2: test first whitestpace breaks pattern matching"
        def output = 'output'
        File d3 = new File('${args[3].key}')
        "\$d3: test This is valid \${output}"
      }
    }
    """
        assertValidOutput(input, output, args, true)
    }

    @Test
    public void testWorkDirProviderGStringCommandAnnotatedClass() {
        def output = "This is valid output"
        def argsMap = [:]
        argsMap["label1"] = "valid arguments"
        argsMap["label2"] = "go"
        argsMap["label3"] = "here!"
        argsMap["label4"] = output
        def args = argsMap.entrySet().toArray(new Map.Entry[argsMap.size()])
        def input = """
    package org.tmatesoft.translator.spec.exec

    @ExecuteString(value = EsTestExecutableCommand, directoryMapping = "sandbox")
    class Temp {

      def sandbox = new EsTestDirectoryMapping()

      def call() {
        def word = 'arguments'
        def i = "${args[0].key}: test valid \$word"
      }

      Object run() {
        call()
        "${args[1].key}: test ${args[1].value}"
        "some_label: trest this is not valid argument"
        "${args[2].key}: test ${args[2].value}"
        " invalid_label: test first whitestpace breaks pattern matching"
        def output = 'output'
        "${args[3].key}: test This is valid \${output}"
      }
    }
    """
        assertValidOutput(input, output, args, false)
    }

    @Test
    public void testClosureGStringTransformationAnnotatedClass() {
        def output = "This is valid output"
        def args = ["valid arguments", "go", "here!", output]
        def input = """
    package org.tmatesoft.translator.spec.exec

    @ExecuteString(EsTestExecutableCommand)
    class Temp {

      Object run() {
        def output = 'The value must be overriden'
        def svn = {
          def word = 'arguments'
          "test valid \$word"
          "test ${args[1]}"
          output = 'output'
        }
        svn()
        "test ${args[2]}"
        return "test This is valid \$output"
      }
    }
    """
        assertValidOutput(input, output, args, false)
    }

    @Test
    public void testDeclarationTransformationAnnotatedClass() {
        def output = "This is valid output"
        def args = [output]
        def input = """
    package org.tmatesoft.translator.spec.exec

    @ExecuteString(EsTestExecutableCommand)
    class Temp {

      Object run() {
        def value = "test $output"
        return value
      }
    }
    """
        assertValidOutput(input, output, args, false)
    }

    @Test
    public void testQuotesAnnotatedClass() {
        def output = "Some text \'with quotes\'"
        def args = [output]
        def input = """
    package org.tmatesoft.translator.spec.exec

    @ExecuteString(EsTestExecutableCommand)
    class Temp {

      Object run() {
        "test Some text 'with quotes'"
      }
    }
    """
        assertValidOutput(input, output, args, false)
    }

    private void assertValidOutput(String input, Object output, Object args, boolean files = false) {
        TestHarnessClassLoader loader = new TestHarnessClassLoader(new EsAstTransformation(), CompilePhase.SEMANTIC_ANALYSIS)
        def clazz = loader.parseClass(input)
        def tester = clazz.newInstance()
        Object result = tester.run()
        Assert.assertEquals(output, result)
        if (args instanceof List) {
            Assert.assertEquals(args, EsTestExecutableCommand.TEST.results)
        } else if (args instanceof Map) {
            if (files) {
                Map<File, String> expectedResults = [:]
                args.entrySet().each {Map.Entry e -> expectedResults.put(new File((String) e.key), e.value)}
                Assert.assertEquals(expectedResults, EsTestExecutableCommand.TEST.workDirResults)
            } else {
                Assert.assertEquals(args, EsTestExecutableCommand.TEST.workDirProviderResults)
            }
        }
    }
}
