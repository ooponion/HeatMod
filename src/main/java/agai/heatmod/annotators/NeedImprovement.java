package agai.heatmod.annotators;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedImprovement {
    String requirement() default "";
}
