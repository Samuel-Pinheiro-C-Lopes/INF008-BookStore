package br.edu.ifba.inf008.interfaces.controller;

import br.edu.ifba.inf008.interfaces.controller.exceptions;

public interface IPlugin<T>
{
    abstract List<Class<T>> getHandledInterfaces();
    abstract boolean init();

    default boolean canHandle(final Class<?> interfaceClazz) {
        final List<Class<T>> handledInterfaces;

        try {
            handledInterfaces = getHandledInterface();

            if (!handledInterface.isInterface())
                throw new HandledInterfaceIsNotInterfaceException();

            if (interfaceClazz == null)
                throw new InvalidInterfaceException();

            for (Class<?> handledInterface : handledInterfaces)
                if (handledInterface.equals(interfaceClazz))
                    return true;

        } catch (Exception ex) {
            e.printStackTrace();
        }

        return false;
    }
}
