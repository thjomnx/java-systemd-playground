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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import de.thjom.java.systemd.Signal;

public class CoverageAnalyzer {

    public static void analyzePath(final String service, final String object, final String iface) throws ReflectiveOperationException, IOException {
        File outFile = new File("/home/menax/Temp/java-systemd/coverage-analysis/java/" + iface + ".dump");
        Process busctl = new ProcessBuilder("/usr/bin/busctl", "introspect", service, object, iface).redirectOutput(outFile).start();

        try {
            busctl.waitFor();
        }
        catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<String> results = new ArrayList<>();

        for (String line : Files.readAllLines(outFile.toPath())) {
            String[] token = line.replaceAll("\\s+", " ").split(" ");

            String varName = token[0].replaceFirst("^\\.", "");
            String varType = token[1];

            if (varType.equals("method")) {
                if (!findMethod(varName, iface)) {
                    results.add(line);
                }
            }
            else if (varType.equals("property")) {
                if (!findProperty(varName, iface)) {
                    results.add(line);
                }
            }
            else if (varType.equals("signal")) {
                if (!findSignal(varName, iface)) {
                    results.add(line);
                }
            }
        }

        Files.write(outFile.toPath().getParent().resolve(iface + ".miss"), results, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static boolean findMethod(final String name, final String iface) throws ReflectiveOperationException {
        String className = "de.thjom.java.systemd" + iface.substring(iface.lastIndexOf('.'));
        Class<?> cls = Class.forName(className);

        for (Method method : cls.getMethods()) {
            if (method.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean findProperty(final String name, final String iface) throws ReflectiveOperationException {
        String className = "de.thjom.java.systemd" + iface.substring(iface.lastIndexOf('.'));
        Class<?> cls = Class.forName(className);

        for (Method method : cls.getMethods()) {
            if (method.getName().equalsIgnoreCase("is" + name) || method.getName().equalsIgnoreCase("get" + name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean findSignal(final String name, final String iface) throws ReflectiveOperationException {
        String className = "de.thjom.java.systemd.interfaces" + iface.substring(iface.lastIndexOf('.')) + "Interface";
        Class<?> cls = Class.forName(className);

        for (Class<?> nested : cls.getClasses()) {
            if (Signal.class.isAssignableFrom(nested) && nested.getSimpleName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public static void main(final String[] args) {
        try {
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1", "org.freedesktop.systemd1.Manager");

            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/cronie_2eservice", "org.freedesktop.systemd1.Unit");

            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/proc_2dsys_2dfs_2dbinfmt_misc_2eautomount", "org.freedesktop.systemd1.Automount");
//        analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/", "org.freedesktop.systemd1.Busname");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/sys_2dmodule_2dconfigfs_2edevice", "org.freedesktop.systemd1.Device");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/tmp_2emount", "org.freedesktop.systemd1.Mount");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/org_2ecups_2ecupsd_2epath", "org.freedesktop.systemd1.Path");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/init_2escope", "org.freedesktop.systemd1.Scope");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/cronie_2eservice", "org.freedesktop.systemd1.Service");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/system_2eslice", "org.freedesktop.systemd1.Slice");
//        analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/", "org.freedesktop.systemd1.Snapshot");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/dbus_2esocket", "org.freedesktop.systemd1.Socket");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/dev_2ddisk_2dby_5cx2duuid_2d2bf012d0_5cx2d4abf_5cx2d4405_5cx2db314_5cx2dfd62d3e94cc3_2eswap", "org.freedesktop.systemd1.Swap");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/multi_2duser_2etarget", "org.freedesktop.systemd1.Target");
            analyzePath("org.freedesktop.systemd1", "/org/freedesktop/systemd1/unit/systemd_2dtmpfiles_2dclean_2etimer", "org.freedesktop.systemd1.Timer");
        }
        catch (final ReflectiveOperationException | IOException e) {
            e.printStackTrace();
        }
    }

}
