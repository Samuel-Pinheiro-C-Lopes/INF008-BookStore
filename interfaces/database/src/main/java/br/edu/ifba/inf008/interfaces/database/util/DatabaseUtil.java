package br.edu.ifba.inf008.interfaces.database.util;

import br.edu.ifba.inf008.interfaces.database.*;

public class DatabaseUtil {

    private DatabaseUtil() {  }

    // ========== Try to get class ==========

    public static <T extends IEntity> Class<T> tryGetClazz(IDatabasePlugin<T> plugin) {
        final Class<T> clazz;

        try {
            clazz = plugin.getEntityClazz();
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] when trying to get clazz while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "] from [" + stackTraces[3].getMethodName() + "].");

            return null;
        }

        return clazz;
    }

    // =========================================

    // ========== Try to get instance ==========

    public static <T extends IEntity> T tryGetInstance(Class<T> clazz) {
        final T instance;

        try {
            instance = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] when trying to get instance while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "] from [" + stackTraces[3].getMethodName() + "].");

            return null;
        }

        return instance;
    }

    // =========================================
}
