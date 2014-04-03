package my.test;

public class AttrTest<S,P extends V, V extends InterfaceTest,T extends ExtendsTest,E extends ExtendsTest&InterfaceTest> extends ExtendsTest implements InterfaceTest {}

interface InterfaceTest {}
class ExtendsTest {}