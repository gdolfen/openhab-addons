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
import static java.util.Optional.ofNullable;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

import biweekly.component.VEvent;
import biweekly.util.ICalDate;

/**
 * The {@link VEventAdapter} class defines an Event in the ICalendar
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class VEventAdapter implements Comparable<VEventAdapter> {
    private VEvent event;
    private long offsetInSeconds;

    public VEventAdapter(VEvent event, long offsetInSeconds) {
        super();
        this.event = event;
        this.offsetInSeconds = offsetInSeconds;
    }

    public Optional<ZonedDateTime> getNotification() {
        return getStart().map(v -> v.minusSeconds(offsetInSeconds));
    }

    public Optional<ZonedDateTime> getStart() {
        return ofNullable(event.getDateStart()).map(v -> v.getValue()).map(this::toZonedDateTime);
    }

    public Optional<ZonedDateTime> getEnd() {
        Optional<ZonedDateTime> end = ofNullable(event.getDateEnd()).map(v -> v.getValue()).map(this::toZonedDateTime);
        return end.isPresent() ? end.get().isAfter(getStart().get()) ? end : getStart().map(v -> v.plusMinutes(1))
                : getStart().map(v -> v.plusDays(1));
    }

    public Optional<String> getSummary() {
        return ofNullable(event.getSummary()).map(v -> v.getValue());
    }

    public Optional<String> getDescription() {
        return ofNullable(event.getDescription()).map(v -> v.getValue());
    }

    private ZonedDateTime toZonedDateTime(ICalDate iCalDate) {
        return iCalDate.toInstant().atZone(systemDefault());
    }

    public boolean isAfter(ZonedDateTime other) {
        return isStartAfter(other) || isNotificationAfter(other) || isEndAfter(other);
    }

    public boolean isStartAfter(ZonedDateTime other) {
        Optional<ZonedDateTime> dateTime = getStart();
        return dateTime.isPresent() && dateTime.get().isAfter(other);
    }

    public boolean isEndAfter(ZonedDateTime other) {
        Optional<ZonedDateTime> dateTime = getEnd();
        return dateTime.isPresent() && dateTime.get().isAfter(other);
    }

    public boolean isNotificationAfter(ZonedDateTime other) {
        Optional<ZonedDateTime> dateTime = getNotification();
        return dateTime.isPresent() && dateTime.get().isAfter(other);
    }

    @Override
    public int compareTo(VEventAdapter other) {
        if (getStart().isPresent() && other.getStart().isPresent()) {
            return getStart().get().compareTo(other.getStart().get());
        }
        return 0;
    }

    public long getNotificationInSeconds(ZonedDateTime now) {
        return getNotification().get().toEpochSecond() - now.toEpochSecond();
    }

    public long getStartInSeconds(ZonedDateTime now) {
        return getStart().get().toEpochSecond() - now.toEpochSecond();
    }

    public long getEndInSeconds(ZonedDateTime now) {
        return getEnd().get().toEpochSecond() - now.toEpochSecond();
    }
}
