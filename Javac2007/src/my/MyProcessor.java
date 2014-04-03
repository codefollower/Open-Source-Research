package my;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.NestingKind.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import static javax.lang.model.util.ElementFilter.*;


import java.util.*;

/** 
 * A processor which prints out elements.  Used to implement the
 * -Xprint option; the included visitor class is used to implement
 * Elements.printElements.
 *
 * <p><b>This is NOT part of any API supported by Sun Microsystems.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)

public class MyProcessor extends AbstractProcessor {
    public boolean process(Set<? extends TypeElement> annotations,
				    RoundEnvironment roundEnv) {
        return true;
    }
}


