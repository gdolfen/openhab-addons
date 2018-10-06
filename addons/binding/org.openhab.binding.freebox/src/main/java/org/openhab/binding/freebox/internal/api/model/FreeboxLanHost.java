/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

import java.util.List;

/**
 * The {@link FreeboxLanHost} is the Java class used to map the "LanHost"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxLanHost {
    private String id;
    private String primaryName;
    private String hostType;
    private boolean primaryNameManual;
    private FreeboxLanHostL2Ident l2ident;
    private String vendorName;
    private boolean persistent;
    private boolean reachable;
    private long lastTimeReachable;
    private boolean active;
    private long lastActivity;
    private List<FreeboxLanHostName> names;
    private List<FreeboxLanHostL3Connectivity> l3connectivities;

    public String getMAC() {
        return (l2ident != null && l2ident.isMacAddress()) ? l2ident.getId() : null;
    }

    public String getId() {
        return id;
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public String getHostType() {
        return hostType;
    }

    public boolean isPrimaryNameManual() {
        return primaryNameManual;
    }

    public FreeboxLanHostL2Ident getL2ident() {
        return l2ident;
    }

    public String getVendorName() {
        return vendorName;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isReachable() {
        return reachable;
    }

    public long getLastTimeReachable() {
        return lastTimeReachable;
    }

    public boolean isActive() {
        return active;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public List<FreeboxLanHostName> getNames() {
        return names;
    }

    public List<FreeboxLanHostL3Connectivity> getL3connectivities() {
        return l3connectivities;
    }
}