/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ical.internal;

import static java.time.ZoneId.systemDefault;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.smarthome.core.library.types.OnOffType.*;
import static org.openhab.binding.ical.internal.ICalBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biweekly.Biweekly;
import biweekly.ICalendar;

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
    private List<VEventAdapter> events = new ArrayList<>();
    private Optional<VEventAdapter> nextEvent = Optional.empty();
    private Optional<VEventAdapter> currentEvent = Optional.empty();
    private Optional<ScheduledFuture<?>> job = Optional.empty();
    private Optional<ScheduledFuture<?>> notificationJob = Optional.empty();
    private Optional<ScheduledFuture<?>> startJob = Optional.empty();
    private Optional<ScheduledFuture<?>> endJob = Optional.empty();
    private Optional<ZonedDateTime> lastUpdate = Optional.empty();
    private CommandHandler commandHandler;

    public ICalHandler(Thing thing, ItemRegistry itemRegistry) {
        super(thing);
        this.commandHandler = new CommandHandler(itemRegistry);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (CHANNEL_NEXT_EVENT.equals(channelUID.getId())) {
                updateChannelNextEventSummery();
            } else if (CHANNEL_NEXT_EVENT_START.equals(channelUID.getId())) {
                updateChannelNextEventStart();
            } else if (CHANNEL_NEXT_EVENT_END.equals(channelUID.getId())) {
                updateChannelNextEventEnd();
            } else if (CHANNEL_NEXT_EVENT_DESCRIPTION.equals(channelUID.getId())) {
                updateChannelNextEventEnd();
            } else if (CHANNEL_NEXT_EVENT_NOTIFICATION_DATE.equals(channelUID.getId())) {
                updateChannelNextEventNotificationDate();
            } else if (CHANNEL_CURRENT_EVENT.equals(channelUID.getId())) {
                updateChannelCurrentEventSummery();
            } else if (CHANNEL_CURRENT_EVENT_START.equals(channelUID.getId())) {
                updateChannelCurrentEventStart();
            } else if (CHANNEL_CURRENT_EVENT_END.equals(channelUID.getId())) {
                updateChannelCurrentEventEnd();
            } else if (CHANNEL_CURRENT_EVENT_DESCRIPTION.equals(channelUID.getId())) {
                updateChannelCurrentEventDescription();
            } else if (CHANNEL_LAST_UPDATE.equals(channelUID.getId())) {
                updateChannelLastUpdate();
            }
        }
    }

    private void updateChannelNextEventSummery() {
        updateState(CHANNEL_NEXT_EVENT, nextEvent.flatMap(VEventAdapter::getSummary).map(StringType::new)
                .map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void updateChannelCurrentEventSummery() {
        updateState(CHANNEL_CURRENT_EVENT, currentEvent.flatMap(VEventAdapter::getSummary).map(StringType::new)
                .map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void updateChannelCurrentEventStart() {
        Optional<ZonedDateTime> dateTime = currentEvent.flatMap(VEventAdapter::getStart);
        updateState(CHANNEL_CURRENT_EVENT_START,
                dateTime.map(DateTimeType::new).map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void updateChannelLastUpdate() {
        updateState(CHANNEL_LAST_UPDATE, lastUpdate.map(DateTimeType::new).map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void updateChannelCurrentEventEnd() {
        Optional<ZonedDateTime> dateTime = currentEvent.flatMap(VEventAdapter::getEnd);
        updateState(CHANNEL_CURRENT_EVENT_END,
                dateTime.map(DateTimeType::new).map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void updateChannelCurrentEventDescription() {
        updateState(CHANNEL_CURRENT_EVENT_DESCRIPTION, currentEvent.flatMap(VEventAdapter::getDescription)
                .map(StringType::new).map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void updateChannelNextEventStart() {
        Optional<ZonedDateTime> dateTime = nextEvent.flatMap(VEventAdapter::getStart);
        updateState(CHANNEL_NEXT_EVENT_START,
                dateTime.map(DateTimeType::new).map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void updateChannelNextEventEnd() {
        Optional<ZonedDateTime> dateTime = nextEvent.flatMap(VEventAdapter::getEnd);
        updateState(CHANNEL_NEXT_EVENT_END, dateTime.map(DateTimeType::new).map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void updateChannelNextEventDescription() {
        updateState(CHANNEL_NEXT_EVENT_DESCRIPTION, nextEvent.flatMap(VEventAdapter::getDescription)
                .map(StringType::new).map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void updateChannelNextEventNotificationDate() {
        Optional<ZonedDateTime> dateTime = nextEvent.flatMap(VEventAdapter::getNotification);
        updateState(CHANNEL_NEXT_EVENT_NOTIFICATION_DATE,
                dateTime.map(DateTimeType::new).map(d -> (State) d).orElse(UnDefType.NULL));
    }

    private void scheduleNextEvent() {
        ZonedDateTime now = ZonedDateTime.now(systemDefault());
        notificationJob.ifPresent(j -> j.cancel(false));
        startJob.ifPresent(j -> j.cancel(false));
        endJob.ifPresent(j -> j.cancel(false));
        if (nextEvent.isPresent() && nextEvent.get().isNotificationAfter(now)) {
            notificationJob = Optional.of(scheduler.schedule(() -> updateNextEvent(),
                    nextEvent.get().getNotificationInSeconds(now), SECONDS));
        }
        if (nextEvent.isPresent() && nextEvent.get().isStartAfter(now)) {
            startJob = Optional
                    .of(scheduler.schedule(() -> startNextEvent(), nextEvent.get().getStartInSeconds(now), SECONDS));
        }
        if (currentEvent.isPresent() && currentEvent.get().isEndAfter(now)) {
            endJob = Optional
                    .of(scheduler.schedule(() -> updateNextEvent(), currentEvent.get().getEndInSeconds(now), SECONDS));
        } else if (nextEvent.isPresent() && nextEvent.get().isEndAfter(now)) {
            endJob = Optional
                    .of(scheduler.schedule(() -> updateNextEvent(), nextEvent.get().getEndInSeconds(now), SECONDS));
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);
        job = Optional
                .of(scheduler.scheduleAtFixedRate(() -> updateCalendar(), 0, getUpdateInterval(), TimeUnit.SECONDS));
    }

    @Override
    public void dispose() {
        job.ifPresent(j -> j.cancel(true));
        notificationJob.ifPresent(j -> j.cancel(true));
        startJob.ifPresent(j -> j.cancel(true));
        endJob.ifPresent(j -> j.cancel(true));
    }

    private void startNextEvent() {
        nextEvent.get().getDescription().ifPresent(d -> commandHandler.sendCommand(d));
        updateNextEvent();
    }

    private void updateNextEvent() {
        ZonedDateTime now = ZonedDateTime.now(systemDefault());
        nextEvent = events.stream().filter(e -> e.isStartAfter(now)).findFirst();
        currentEvent = events.stream().filter(e -> e.isEndAfter(now) && !e.isStartAfter(now)).findFirst();
        if (currentEvent.isPresent()) {
            updateState(CHANNEL_NEXT_EVENT_NOTIFICATION_SWITCH, OFF);
            updateState(CHANNEL_NEXT_EVENT_SWITCH, ON);
        } else if (nextEvent.isPresent() && !nextEvent.get().isNotificationAfter(now)) {
            updateState(CHANNEL_NEXT_EVENT_NOTIFICATION_SWITCH, ON);
            updateState(CHANNEL_NEXT_EVENT_SWITCH, OFF);
        } else {
            updateState(CHANNEL_NEXT_EVENT_NOTIFICATION_SWITCH, OFF);
            updateState(CHANNEL_NEXT_EVENT_SWITCH, OFF);
        }
        updateChannelCurrentEventSummery();
        updateChannelCurrentEventStart();
        updateChannelCurrentEventEnd();
        updateChannelCurrentEventDescription();
        updateChannelNextEventSummery();
        updateChannelNextEventStart();
        updateChannelNextEventEnd();
        updateChannelNextEventDescription();
        updateChannelNextEventNotificationDate();
        scheduleNextEvent();
    }

    private void updateCalendar() {
        try {
            logger.info("updateCalendar");
            URL url = new URL(getUrl());
            URLConnection connection = url.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                ICalendar iCalendar = Biweekly.parse(inputStream).first();
                events.clear();
                iCalendar.getEvents()
                        .forEach(e -> events.add(new VEventAdapter(e, getNextEventNotificationDateOffset())));
                Collections.sort(events);
                lastUpdate = Optional.of(ZonedDateTime.now(systemDefault()));
                updateChannelLastUpdate();
                updateNextEvent();
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

    public long getUpdateInterval() {
        Object value = thing.getConfiguration().get(CONFIG_PARAMETER_UPDATE);
        return Optional.ofNullable(value).map(BigDecimal.class::cast).map(BigDecimal::longValue)
                .orElse(ONE_DAY_IN_SECONDS);
    }
}
