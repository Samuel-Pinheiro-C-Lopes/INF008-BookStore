package br.edu.ifba.inf008.interfaces.database.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name();
    boolean primaryKey() default false;
}
