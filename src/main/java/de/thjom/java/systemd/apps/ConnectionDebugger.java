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

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

public class ConnectionDebugger {

    public static void main(final String[] args) {
        try {
            int i = 0;

            while (true) {
                DBusConnection conn = DBusConnection.getConnection(DBusConnection.SYSTEM);

                System.out.format("ConnectionDebugger.main() - %d - conn=%s\n", i++, conn);

                conn.disconnect();

                Thread.sleep(50);
            }
        }
        catch (final DBusException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
