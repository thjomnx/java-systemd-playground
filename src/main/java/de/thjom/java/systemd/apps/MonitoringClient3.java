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
import java.util.Optional;

import org.freedesktop.dbus.exceptions.DBusException;

import de.thjom.java.systemd.Manager;
import de.thjom.java.systemd.Systemd;
import de.thjom.java.systemd.Unit;
import de.thjom.java.systemd.Unit.StateTuple;
import de.thjom.java.systemd.UnitNameMonitor;

public class MonitoringClient3 implements Runnable {

    private volatile boolean running;

    public MonitoringClient3() {
        this.running = true;
    }

    @Override
    public void run() {
        if (running) {
            try {
                Manager manager = Systemd.get().getManager();

                UnitNameMonitor nameMonitor = new UnitNameMonitor(manager);
                nameMonitor.addUnits("cups.service");
                nameMonitor.addDefaultHandlers();
                nameMonitor.addListener((u, p) -> System.out.format("%s changed state to %s\n", u, StateTuple.of(u, p)));

                while (running) {
                    Optional<Unit> cups = nameMonitor.getMonitoredUnit("cups.service");

                    if (cups.isPresent()) {
                        System.out.format("%s: %s\n", cups.get(), StateTuple.of(cups.get()));
                    }

                    System.out.println("Press key to stop polling");

                    try {
                        Thread.sleep(10000);
                    }
                    catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                nameMonitor.removeDefaultHandlers();
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
        MonitoringClient3 client = new MonitoringClient3();

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
