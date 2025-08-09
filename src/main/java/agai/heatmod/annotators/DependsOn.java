package agai.heatmod.annotators;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DependsOn {
    Class[] clazz() default {};
}
