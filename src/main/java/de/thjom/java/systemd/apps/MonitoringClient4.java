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

import java.io.IOException;

import org.freedesktop.dbus.exceptions.DBusException;

import de.thjom.java.systemd.Manager;
import de.thjom.java.systemd.Systemd;
import de.thjom.java.systemd.Unit;
import de.thjom.java.systemd.UnitNameMonitor;

public class MonitoringClient4 implements Runnable {

    private volatile boolean running;

    public MonitoringClient4() {
        this.running = true;
    }

    @Override
    public void run() {
        try {
            Manager manager = Systemd.get().getManager();

            Unit postfix = manager.getUnit("postfix.service");

            UnitNameMonitor nameMonitor = new UnitNameMonitor(manager);
            nameMonitor.addDefaultHandlers();
            nameMonitor.addUnits(postfix);
            nameMonitor.addUnits("avahi-daemon.service");
            nameMonitor.addUnits("foo.service");
            nameMonitor.addUnits("transient.service");

            nameMonitor.addListener(units -> units.forEach(System.out::println));
            nameMonitor.startPolling(5000L, 10000L);

            System.out.println("Press key to stop check");

            while (running) {
                try {
                    Thread.sleep(10000);
                }
                catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            nameMonitor.stopPolling();
            nameMonitor.removeDefaultHandlers();
            nameMonitor.reset();
        }
        catch (final DBusException e) {
            e.printStackTrace();
        }
        finally {
            Systemd.disconnectAll();
        }
    }

    public static void main(final String[] args) {
        MonitoringClient4 client = new MonitoringClient4();

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
