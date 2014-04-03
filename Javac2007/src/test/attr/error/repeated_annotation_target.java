package test.attr.error;
import java.lang.annotation.*;

//@Target()
@Target({ElementType.TYPE,ElementType.TYPE})
public @interface repeated_annotation_target {}
