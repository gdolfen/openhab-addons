package org.openhab.binding.ical.internal;

import java.util.Comparator;

import biweekly.component.VEvent;

public class VEventComparator implements Comparator<VEvent> {
    @Override
    public int compare(VEvent o1, VEvent o2) {
        return o1.getDateStart().getValue().compareTo(o2.getDateStart().getValue());
    }
}
