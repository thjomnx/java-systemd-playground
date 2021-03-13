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

import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged;
import org.freedesktop.dbus.exceptions.DBusException;

import de.thjom.java.systemd.Manager;
import de.thjom.java.systemd.Systemd;
import de.thjom.java.systemd.Systemd.InstanceType;
import de.thjom.java.systemd.Unit;
import de.thjom.java.systemd.UnitNameMonitor;
import de.thjom.java.systemd.UnitTypeMonitor;
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

                fooMonitor.addHandler(PropertiesChanged.class, s -> {
                    if (fooMonitor.monitorsUnit(Unit.extractName(s.getPath()))) {
                        System.out.println("MonitoringClient.run().fooMonitor.addHandler().handle(): " + s);
                    }
                });

                UnitTypeMonitor serviceMonitor = new UnitTypeMonitor(manager);
                serviceMonitor.addMonitoredTypes(MonitoredType.SERVICE);
                serviceMonitor.addDefaultHandlers();
                serviceMonitor.addListener((u, p) -> System.out.format("%s changed state to %s\n", u, Unit.StateTuple.of(u, p)));

                serviceMonitor.addHandler(PropertiesChanged.class, s -> {
                    if (serviceMonitor.monitorsUnit(Unit.extractName(s.getPath()))) {
                        System.out.println("MonitoringClient.run().serviceMonitor.addHandler().handle(): " + s);
                    }
                });

                while (running) {
                    System.out.println("Press key to stop polling");

                    try {
                        Thread.sleep(60000);
                    }
                    catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
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
