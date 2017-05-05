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

package de.thjom.java.systemd.apps;

import org.freedesktop.DBus.Properties.PropertiesChanged;
import org.freedesktop.dbus.exceptions.DBusException;

import de.thjom.java.systemd.Manager;
import de.thjom.java.systemd.Systemd;
import de.thjom.java.systemd.Unit;

public class MemoryCheck {

    public MemoryCheck() {
        // Do nothing
    }

    public void run() {
        try {
            Manager manager = Systemd.get().getManager();

            int i = 0;

            while (true) {
                System.out.println("iteration: " + i++);

                if (i % 50 == 0) {
                    System.gc();
                }

                Unit cronie = manager.getService("cronie");
                cronie.addHandler(PropertiesChanged.class, s -> System.out.println(s));

                System.out.println("hash -> " + System.identityHashCode(cronie));
            }
        }
        catch (DBusException e) {
            System.err.println(e);
        }
        finally {
            Systemd.disconnectAll();
        }
    }

    public static void main(final String[] args) {
        new MemoryCheck().run();
    }

}
