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
import de.thjom.java.systemd.UnitNameMonitor;

public class MemoryCheck2 implements Runnable {

    private volatile boolean running;

    public MemoryCheck2() {
        this.running = true;
    }

    @Override
    public void run() {
        if (running) {
            try {
                Manager manager = Systemd.get(InstanceType.SYSTEM).getManager();

                Unit postfix = manager.getUnit("postfix.service");

                UnitNameMonitor miscMonitor = new UnitNameMonitor(manager);
                miscMonitor.addUnits(postfix);
                miscMonitor.addUnits("avahi-daemon.service");   // This one is loaded and running by default (enabled)
                miscMonitor.addUnits("foo.service");            // This one is loaded by default but not running (disabled)
                miscMonitor.addUnits("transient.service");      // This one shall pop in and out (not existing)
                miscMonitor.addDefaultHandlers();

                System.out.println("Press key to stop check");

                while (running) {
                    for (Unit unit : miscMonitor.getMonitoredUnits()) {
                        System.out.format("%s:\t%s\n", unit.getId(), StateTuple.of(unit));
                    }

                    try {
                        Thread.sleep(1000);
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
        MemoryCheck2 client = new MemoryCheck2();

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
