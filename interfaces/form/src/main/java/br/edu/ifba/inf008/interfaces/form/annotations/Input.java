package br.edu.ifba.inf008.interfaces.form.annotations;

import br.edu.ifba.inf008.interfaces.form.enums.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Input {
    String label();
    int position();
    InputType type() default InputType.SIMPLE;
    boolean optional() default true;
}
