package org.tmatesoft.grooby.cache

//@InsertMethodId(applyToMethodsWith = MockUpTarget, doNotIncludeToHash = [MockUpSkip])
class MockUpClassTemplate {

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
