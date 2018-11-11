/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ical.internal;

import java.util.Comparator;

import biweekly.component.VEvent;

/**
 * The {@link VEventComparator} compares classes of type {@link VEvent}.
 *
 * @author Guido Dolfen - Initial contribution
 */
public class VEventComparator implements Comparator<VEvent> {
    @Override
    public int compare(VEvent o1, VEvent o2) {
        return o1.getDateStart().getValue().compareTo(o2.getDateStart().getValue());
    }
}
