package br.edu.ifba.inf008.interfaces.controller.util;

import java.lang.Thread;
import java.lang.StackTraceElement;

import br.edu.ifba.inf008.interfaces.controller.*;

public class ControllerUtil {

    private ControllerUtil() { }

    // ========== Getting and validating controllers ==========

    public static ICore tryGetAndValidateCore() {
        final ICore core = ICore.getInstance();

        if (core == null) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("An [" + ICore.class.getName() + "] was null when trying to execute [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "] from [" + stackTraces[3].getMethodName() + "].");

            return null;
        }

        return core;
    }

    public static IDatabaseController tryGetAndValidateDatabaseController() {
        final ICore core = ControllerUtil.tryGetAndValidateCore();
        return ControllerUtil.tryGetAndValidateDatabaseController(core);
    }

    public static IDatabaseController tryGetAndValidateDatabaseController(final ICore core) {
        final IDatabaseController database = core.getDatabaseController();

        if (database == null) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("An [" + IDatabaseController.class.getName() + "] was null when trying to execute [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "] from [" + stackTraces[3].getMethodName() + "].");

            return null;
        }

        return database;
    }

    public static IUIController tryGetAndValidateIUIController() {
        final ICore core = ControllerUtil.tryGetAndValidateCore();
        return ControllerUtil.tryGetAndValidateIUIController(core);
    }

    public static IUIController tryGetAndValidateIUIController(final ICore core) {
        final IUIController ui = core.getUIController();

        if (ui == null) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("An [" + IUIController.class.getName() + "] was null when trying to execute [ " + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "] from [" + stackTraces[3].getMethodName() + "].");

            return null;
        }

        return ui;
    }

    // ===================================================
}
