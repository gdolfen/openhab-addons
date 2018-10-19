/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.KeyControl;

/**
 * Handles Key Control Command Ok.
 *
 * @author Guido Dolfen
 */
@NonNullByDefault
public class KeyControlOk extends BaseChannelHandler<Void, Object> {

    private KeyControl getControl(ConnectableDevice device) {
        return device.getCapability(KeyControl.class);
    }

    @Override
    public void onReceiveCommand(@Nullable ConnectableDevice device, String channelId, LGWebOSHandler handler,
            Command command) {
        if (device == null) {
            return;
        }
        if (device.hasCapabilities(KeyControl.OK)) {
            getControl(device).ok(getDefaultResponseListener());
        }
    }
}
