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

import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.controlpoint.ControlPointImpl;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ProtocolFactoryImpl;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryImpl;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.RouterImpl;
//import org.seamless.util.Exceptions;

//import javax.enterprise.inject.Alternative;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of {@link UpnpService}, starts immediately on construction.
 * <p>
 * If no {@link UpnpServiceConfiguration} is provided it will automatically
 * instantiate {@link DefaultUpnpServiceConfiguration}. This configuration <strong>does not
 * work</strong> on Android! Use the {@link org.fourthline.cling.android.AndroidUpnpService}
 * application component instead.
 * </p>
 * <p>
 * Override the various <tt>create...()</tt> methods to customize instantiation of protocol factory,
 * router, etc.
 * </p>
 *
 * @author Christian Bauer
 */
//@Alternative
public class UpnpServiceImpl implements UpnpService {

    private static Logger log = Logger.getLogger(UpnpServiceImpl.class.getName());

    protected final UpnpServiceConfiguration configuration;
    protected final ControlPoint controlPoint;
    protected final ProtocolFactory protocolFactory;
    protected final Registry registry;
    protected final Router router;

    public UpnpServiceImpl() {
        this(new DefaultUpnpServiceConfiguration());
    }

    public UpnpServiceImpl(RegistryListener... registryListeners) {
//        this(new DefaultUpnpServiceConfiguration(), registryListeners);
        this(new AndroidUpnpServiceConfiguration(), registryListeners);
    }

    public UpnpServiceImpl(UpnpServiceConfiguration configuration, RegistryListener... registryListeners) {
        this.configuration = configuration;

        log.info(">>> Starting UPnP service...");

        log.info("Using configuration: " + getConfiguration().getClass().getName());

        // Instantiation order is important: Router needs to start its network services after registry is ready

        this.protocolFactory = createProtocolFactory();

        this.registry = createRegistry(protocolFactory);
        for (RegistryListener registryListener : registryListeners) {
            this.registry.addListener(registryListener);
        }

        // 初始化Router.
        this.router = createRouter(protocolFactory, registry);

        try {
            this.router.enable(); // 初始化路由器.
        } catch (RouterException ex) {
            throw new RuntimeException("Enabling network router failed: " + ex, ex);
        }

        // 创建控制点，仅一处调用.
        this.controlPoint = createControlPoint(protocolFactory, registry);

        log.info("<<< UPnP service started successfully");
    }

    // 仅一处调用，UpnpServiceImpl.
    protected ProtocolFactory createProtocolFactory() {
        return new ProtocolFactoryImpl(this);
    }

    // Registry 实例的由来，为 RegistryImpl 的实现.
    protected Registry createRegistry(ProtocolFactory protocolFactory) {
        return new RegistryImpl(this);
    }

    // 初始化Router.
    protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
        return new RouterImpl(getConfiguration(), protocolFactory);
    }

    // 仅一处调用.
    protected ControlPoint createControlPoint(ProtocolFactory protocolFactory, Registry registry) {
        return new ControlPointImpl(getConfiguration(), protocolFactory, registry);
    }

    public UpnpServiceConfiguration getConfiguration() {
        return configuration;
    }

    public ControlPoint getControlPoint() {
        return controlPoint;
    }

    public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    public Registry getRegistry() {
        return registry;
    }

    public Router getRouter() {
        return router;
    }

    synchronized public void shutdown() {
        shutdown(false);
    }

    protected void shutdown(boolean separateThread) {
        Runnable shutdown = new Runnable() {
            @Override
            public void run() {
                log.info(">>> Shutting down UPnP service...");
                shutdownRegistry();
                shutdownRouter();
                shutdownConfiguration();
                log.info("<<< UPnP service shutdown completed");
            }
        };
        if (separateThread) {
            // This is not a daemon thread, it has to complete!
            new Thread(shutdown).start();
        } else {
            shutdown.run();
        }
    }

    protected void shutdownRegistry() {
        getRegistry().shutdown();
    }

    protected void shutdownRouter() {
        try {
            getRouter().shutdown();
        } catch (RouterException ex) {
//            Throwable cause = Exceptions.unwrap(ex);
//            if (cause instanceof InterruptedException) {
//                log.log(Level.INFO, "Router shutdown was interrupted: " + ex, cause);
//            } else {
//                log.log(Level.SEVERE, "Router error on shutdown: " + ex, cause);
//            }
        }
    }

    protected void shutdownConfiguration() {
        getConfiguration().shutdown();
    }

}
