package br.edu.ifba.inf008.interfaces.grid.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Viewable {
    String name();
}
