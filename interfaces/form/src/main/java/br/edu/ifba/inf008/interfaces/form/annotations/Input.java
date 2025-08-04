package br.edu.ifba.inf008.interfaces.form.annotations;

import java.lang.annotations.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Input {
    String label();
    int position();
    InputType type() default SIMPLE;
}
