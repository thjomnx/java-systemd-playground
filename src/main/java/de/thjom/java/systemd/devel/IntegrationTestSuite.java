/*
 * Java-systemd implementation (playground)
 * Copyright (c) 2016 Markus Enax
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of either the GNU Lesser General Public License Version 2 or the
 * Academic Free Licence Version 3.0.
 *
 * Full licence texts are included in the COPYING file with this program.
 */

package de.thjom.java.systemd.devel;

import java.lang.reflect.Method;
import java.util.List;

import org.freedesktop.dbus.exceptions.DBusException;

import de.thjom.java.systemd.Automount;
import de.thjom.java.systemd.Device;
import de.thjom.java.systemd.Manager;
import de.thjom.java.systemd.Mount;
import de.thjom.java.systemd.Path;
import de.thjom.java.systemd.Scope;
import de.thjom.java.systemd.Service;
import de.thjom.java.systemd.Slice;
import de.thjom.java.systemd.Socket;
import de.thjom.java.systemd.Swap;
import de.thjom.java.systemd.Systemd;
import de.thjom.java.systemd.Target;
import de.thjom.java.systemd.Timer;

public class IntegrationTestSuite {

    private static void testManager(final Object testee, final Class<?> unitClass) throws ReflectiveOperationException {
        List<String> ignored = List.of(
                "getAutomount",
                "getBusName",
                "getDevice",
                "getMount",
                "getPath",
                "getScope",
                "getService",
                "getSlice",
                "getSocket",
                "getSwap",
                "getTarget",
                "getTimer",
                "getUnit"
        );

        for (Method method : unitClass.getMethods()) {
            String methodName = method.getName();

            if (ignored.contains(methodName)) {
                return;
            }

            testObject(testee, method);
        }
    }

    private static void testUnit(final Object unit, final Class<?> unitClass) throws ReflectiveOperationException {
        for (Method method : unitClass.getMethods()) {
            testObject(unit, method);
        }
    }

    private static void testObject(final Object testee, final Method method) throws ReflectiveOperationException {
        String methodName = method.getName();

        if (!method.getDeclaringClass().getName().contains("systemd")) {
            return;
        }

        if (methodName.equals("isAssignableFrom")) {
            return;
        }

        if (methodName.startsWith("get") || methodName.startsWith("is")) {
            Object obj;

            try {
                obj = method.invoke(testee);
            }
            catch (final Exception e) {
                System.err.format("%s: %s%n", method, e.getMessage());

                throw e;
            }

            System.out.format("%s: %s%n", methodName, obj);
        }
    }

    public static void main(final String[] args) {
        try {
            Manager manager = Systemd.get().getManager();

            testManager(manager, Manager.class);

            testUnit(manager.getAutomount("proc-sys-fs-binfmt_misc"), Automount.class);
            testUnit(manager.getDevice("sys-module-configfs"), Device.class);
            testUnit(manager.getMount("tmp"), Mount.class);
            testUnit(manager.getPath("systemd-ask-password-console"), Path.class);
            testUnit(manager.getScope("init"), Scope.class);
            testUnit(manager.getService("dbus"), Service.class);
            testUnit(manager.getSlice("system"), Slice.class);
            testUnit(manager.getSocket("dbus"), Socket.class);
            testUnit(manager.getSwap("dev-disk-by\\x2duuid-2bf012d0\\x2d4abf\\x2d4405\\x2db314\\x2dfd62d3e94cc3"), Swap.class);
            testUnit(manager.getTarget("basic"), Target.class);
            testUnit(manager.getTimer("systemd-tmpfiles-clean"), Timer.class);
        }
        catch (final DBusException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
        finally {
            Systemd.disconnectAll();
        }
    }

}
