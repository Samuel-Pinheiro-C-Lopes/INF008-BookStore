package br.edu.ifba.inf008.interfaces.database.util;

import br.edu.ifba.inf008.interfaces.database.*;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Field;

public class LazyLoadingEvocationHandler<T extends IEntity> implements InvocationHandler {
    private final IDatabasePlugin<T> plugin;
    private IEntity entity;
    private boolean full;

    public LazyLoadingEvocationHandler(final IEntity partial, IDatabasePlugin<T> plugin) {
        this.entity = partial;
        this.plugin = plugin;
        this.full = false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (full == false) {
            if (plugin.parametrizedSelect(this.entity) == false) {
                final Object primaryKey = this.entity.getPrimaryKey();

                throw new IllegalStateException("Couldn't lazy load entity of type [" + entity.getClass().getName() + "] and id [" + (primaryKey != null ? primaryKey.toString() : "NULL") + "].");
            }

            full = true;
        }

        return method.invoke(this.entity, args);
    }
}
