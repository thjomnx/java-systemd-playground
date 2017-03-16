/*
 * Java-systemd implementation
 * Copyright (c) 2016 Markus Enax
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of either the GNU Lesser General Public License Version 2 or the
 * Academic Free Licence Version 2.1.
 *
 * Full licence texts are included in the COPYING file with this program.
 */

package de.thjom.java.systemd;

import org.freedesktop.DBus.Properties.PropertiesChanged;
import org.freedesktop.dbus.exceptions.DBusException;

public class MemoryCheck {

    public MemoryCheck() {
        // Do nothing
    }

    public void run() {
        try {
            Manager manager = Systemd.get().getManager();

            int i = 0;

            while (true) {
                System.out.println(i++);

                if (i % 50 == 0) {
                    System.gc();
                }

                Unit cronie = manager.getService("cronie");
                cronie.addHandler(PropertiesChanged.class, s -> System.out.println(s));

                System.out.println(cronie.hashCode());
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
