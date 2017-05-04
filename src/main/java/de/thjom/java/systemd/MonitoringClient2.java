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

import java.io.IOException;

import org.freedesktop.DBus.Properties.PropertiesChanged;
import org.freedesktop.dbus.exceptions.DBusException;

import de.thjom.java.systemd.Systemd.InstanceType;
import de.thjom.java.systemd.UnitTypeMonitor.MonitoredType;

public class MonitoringClient2 implements Runnable {

    private volatile boolean running;

    public MonitoringClient2() {
        this.running = true;
    }

    @Override
    public void run() {
        if (running) {
            try {
                Manager manager = Systemd.get(InstanceType.USER).getManager();

//                manager.subscribe();
//                manager.addConsumer(Reloading.class, s -> System.out.println(s));
//                manager.addConsumer(UnitNew.class, s -> System.out.println(s));
//                manager.addConsumer(UnitRemoved.class, s -> System.out.println(s));
//                manager.addConsumer(UnitFilesChanged.class, s -> System.out.println(s));

                UnitNameMonitor fooMonitor = new UnitNameMonitor(manager);
                fooMonitor.addUnits("foo.service");
                fooMonitor.addDefaultHandlers();

                fooMonitor.addConsumer(PropertiesChanged.class, s -> {
                    if (fooMonitor.monitorsUnit(Unit.extractName(s.getPath()))) {
                        System.out.println("MonitoringClient.run().fooMonitor.addConsumer().handle(): " + s);
                    }
                });

                UnitTypeMonitor serviceMonitor = new UnitTypeMonitor(manager);
                serviceMonitor.addMonitoredTypes(MonitoredType.SERVICE);
                serviceMonitor.addDefaultHandlers();

                serviceMonitor.addConsumer(PropertiesChanged.class, s -> {
                    if (serviceMonitor.monitorsUnit(Unit.extractName(s.getPath()))) {
                        System.out.println("MonitoringClient.run().serviceMonitor.addConsumer().handle(): " + s);
                    }
                });

                while (running) {
                    System.out.println("Press key to stop polling");

                    try {
                        Thread.sleep(60000);
                    }
                    catch (final InterruptedException e) {
                        // Ignore (occurs on key press)
                    }
                }

                fooMonitor.removeDefaultHandlers();
                serviceMonitor.removeDefaultHandlers();
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
        MonitoringClient2 client = new MonitoringClient2();

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
