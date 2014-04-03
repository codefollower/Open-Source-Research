
import java.util.*;

interface A {
    List<String> f();
}

interface B extends A {
    List f();
}