/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.fourthline.cling;

import android.util.Log;

import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

/**
 *  Runs a simple UPnP discovery procedure.
 *  UPnP discover procedure 测试代码.
 */
public class TestMain {
    private static final String TAG = "TestMain";

    public static void upnpDiscover(String[] args) throws Exception {

        // UPnP discovery is asynchronous, we need a callback
        RegistryListener listener = new RegistryListener() {

            public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
                System.out.println("Discovery started: " + device.getDisplayString());
                Log.i(TAG, "remoteDeviceDiscoveryStarted: ");
            }

            public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
                System.out.println("Discovery failed: " + device.getDisplayString() + " => " + ex);
                Log.i(TAG, "remoteDeviceDiscoveryFailed: ");
            }

            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
                System.out.println("Remote device available: " + device.getDisplayString());
                Log.i(TAG, "remoteDeviceAdded: ");
            }

            public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
                System.out.println("Remote device updated: " + device.getDisplayString());
                Log.i(TAG, "remoteDeviceUpdated: ");
            }

            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                System.out.println("Remote device removed: " + device.getDisplayString());
                Log.i(TAG, "remoteDeviceRemoved: ");
            }

            public void localDeviceAdded(Registry registry, LocalDevice device) {
                System.out.println("Local device added: " + device.getDisplayString());
                Log.i(TAG, "localDeviceAdded: ");
            }

            public void localDeviceRemoved(Registry registry, LocalDevice device) {
                System.out.println("Local device removed: " + device.getDisplayString());
                Log.i(TAG, "localDeviceRemoved: ");
            }

            public void beforeShutdown(Registry registry) {
                System.out.println("Before shutdown, the registry has devices: " + registry.getDevices().size());
                Log.i(TAG, "beforeShutdown: ");
            }

            public void afterShutdown() {
                System.out.println("Shutdown of registry complete!");
                Log.i(TAG, "afterShutdown: ");
            }

        };

        // This will create necessary network resources for UPnP right away
//        System.out.println("Starting Cling...");
        Log.i(TAG, "Starting Cling... upnp Discover.");
        UpnpService upnpService = new UpnpServiceImpl(listener);

        // Send a search message to all devices and services, they should respond soon
//        System.out.println("Sending SEARCH message to all devices...");
        Log.i(TAG, "Sending SEARCH message to all devices...");
        upnpService.getControlPoint().search(new STAllHeader());

        // Let's wait 10 seconds for them to respond
//        System.out.println("Waiting 10 seconds before shutting down...");
        Log.i(TAG, "Waiting 10 seconds before shutting down...");
        Thread.sleep(20 * 1000);

        // Release all resources and advertise BYEBYE to other UPnP devices
//        System.out.println("Stopping Cling...");
        Log.i(TAG, "Stopping Cling...");
        upnpService.shutdown();
    }
}

