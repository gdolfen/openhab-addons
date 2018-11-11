/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.ical.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ICalBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class ICalBindingConstants {

    private static final String BINDING_ID = "ical";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CALENDAR = new ThingTypeUID(BINDING_ID, "calendar");

    // List of all Channel ids
    public static final String CHANNEL_NEXT_EVENT = "nextEvent";
    public static final String CHANNEL_NEXT_EVENT_START = "nextEventStart";
    public static final String CHANNEL_NEXT_EVENT_END = "nextEventEnd";
    public static final String CHANNEL_NEXT_EVENT_DESCRIPTION = "nextEventDescription";
    public static final String CHANNEL_NEXT_EVENT_NOTIFICATION_DATE = "nextEventNotificationDate";

    public static final String CONFIG_PARAMETER_URL = "url";
    public static final String CONFIG_PARAMETER_REFRESH = "refreshInterval";
    public static final String CONFIG_PARAMETER_NEXT_EVENT_NOTIFICATION_DATE_OFFSET = "nextEventNotificationDateOffset";
}
