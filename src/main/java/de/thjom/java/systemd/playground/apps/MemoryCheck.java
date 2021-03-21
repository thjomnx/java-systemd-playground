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

package de.thjom.java.systemd.playground.apps;

import java.io.IOException;

import org.freedesktop.dbus.exceptions.DBusException;

import de.thjom.java.systemd.Manager;
import de.thjom.java.systemd.Systemd;
import de.thjom.java.systemd.Systemd.InstanceType;
import de.thjom.java.systemd.Unit;
import de.thjom.java.systemd.Unit.StateTuple;

public class MemoryCheck implements Runnable {

    private volatile boolean running;

    public MemoryCheck() {
        this.running = true;
    }

    @Override
    public void run() {
        if (running) {
            try {
                Manager manager = Systemd.get(InstanceType.SYSTEM).getManager();

                System.out.println("Press key to stop check");

                while (running) {
                    String unitName = "postfix.service";
                    Unit postfix = manager.getUnit(unitName);

                    System.out.format("%s:\t%s\n", unitName, StateTuple.of(postfix));

                    try {
                        Thread.sleep(50);
                    }
                    catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            catch (final DBusException e) {
                e.printStackTrace();
            }
            finally {
                Systemd.disconnectAll();
            }
        }
    }

    public static void main(final String[] args) {
        MemoryCheck client = new MemoryCheck();

        Thread t = new Thread(client);
        t.start();

        try {
            System.in.read();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        finally {
            client.running = false;
            t.interrupt();
        }
    }

}
