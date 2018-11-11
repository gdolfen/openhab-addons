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

import static org.openhab.binding.ical.internal.ICalBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;

/**
 * The {@link ICalHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class ICalHandler extends BaseThingHandler {
    private static final long ONE_DAY_IN_SECONDS = 24 * 60 * 60L;
    private final Logger logger = LoggerFactory.getLogger(ICalHandler.class);
    @Nullable
    private ICalendar iCalendar;
    @Nullable
    private ScheduledFuture<?> refreshJob;

    public ICalHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (CHANNEL_NEXT_EVENT.equals(channelUID.getId())) {
                updateChannelNextEvent();
            } else if (CHANNEL_NEXT_EVENT_START.equals(channelUID.getId())) {
                updateChannelNextEventStart();
            } else if (CHANNEL_NEXT_EVENT_END.equals(channelUID.getId())) {
                updateChannelNextEventEnd();
            } else if (CHANNEL_NEXT_EVENT_DESCRIPTION.equals(channelUID.getId())) {
                updateChannelNextEventEnd();
            } else if (CHANNEL_NEXT_EVENT_NOTIFICATION_DATE.equals(channelUID.getId())) {
                updateChannelNextEventNotificationDate();
            }
        }
    }

    private void updateChannelNextEvent() {
        updateState(CHANNEL_NEXT_EVENT,
                getNextEvent().map(VEvent::getSummary).map(s -> s.getValue()).map(StringType::new).orElse(null));
    }

    private void updateChannelNextEventStart() {
        updateState(CHANNEL_NEXT_EVENT_START, getNextEventStart().map(DateTimeType::new).orElse(null));
    }

    private void updateChannelNextEventEnd() {
        updateState(CHANNEL_NEXT_EVENT_END, getNextEventEnd().map(DateTimeType::new).orElse(
                getNextEventStart().map(v -> v.plusHours(23).plusMinutes(59)).map(DateTimeType::new).orElse(null)));
    }

    private void updateChannelNextEventDescription() {
        updateState(CHANNEL_NEXT_EVENT_DESCRIPTION,
                getNextEvent().map(VEvent::getDescription).map(s -> s.getValue()).map(StringType::new).orElse(null));
    }

    private void updateChannelNextEventNotificationDate() {
        updateState(CHANNEL_NEXT_EVENT_NOTIFICATION_DATE, getNextEventStart()
                .map(v -> v.minusSeconds(getNextEventNotificationDateOffset())).map(DateTimeType::new).orElse(null));
    }

    public Optional<ZonedDateTime> getNextEventEnd() {
        return getNextEvent().map(VEvent::getDateEnd).map(v -> v.getValue())
                .map(v -> v.toInstant().atZone(ZoneId.systemDefault()));
    }

    public Optional<ZonedDateTime> getNextEventStart() {
        return getNextEvent().map(VEvent::getDateStart).map(v -> v.getValue())
                .map(v -> v.toInstant().atZone(ZoneId.systemDefault()));
    }

    public Optional<VEvent> getNextEvent() {
        if (iCalendar != null) {
            List<VEvent> sortedEvents = new ArrayList<>(iCalendar.getEvents());
            Collections.sort(sortedEvents, new VEventComparator());
            final Date now = new Date();
            return sortedEvents.stream().filter(e -> e.getDateStart().getValue().after(now)).findFirst();
        }
        return Optional.empty();
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            updateCalendar();
            if (refreshJob == null || refreshJob.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::update, 10, getRefreshInterval(), TimeUnit.SECONDS);
            }
        });
        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    private void update() {
        updateCalendar();
        updateChannelNextEvent();
        updateChannelNextEventStart();
        updateChannelNextEventEnd();
        updateChannelNextEventDescription();
        updateChannelNextEventNotificationDate();
    }

    private void updateCalendar() {
        try {
            URL url = new URL(getUrl());
            URLConnection connection = url.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                iCalendar = Biweekly.parse(inputStream).first();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (MalformedURLException e) {
            logger.error("No valid URL: " + getUrl(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No valid URL: " + getUrl());
        } catch (IOException e) {
            logger.error("Could not open connection to " + getUrl(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not open connection to " + getUrl());
        }
    }

    public String getUrl() {
        return (String) thing.getConfiguration().get(CONFIG_PARAMETER_URL);
    }

    public long getNextEventNotificationDateOffset() {
        Object value = thing.getConfiguration().get(CONFIG_PARAMETER_NEXT_EVENT_NOTIFICATION_DATE_OFFSET);
        return Optional.ofNullable(value).map(BigDecimal.class::cast).map(BigDecimal::longValue)
                .orElse(ONE_DAY_IN_SECONDS);
    }

    public long getRefreshInterval() {
        Object value = thing.getConfiguration().get(CONFIG_PARAMETER_REFRESH);
        return Optional.ofNullable(value).map(BigDecimal.class::cast).map(BigDecimal::longValue)
                .orElse(ONE_DAY_IN_SECONDS);
    }
}
