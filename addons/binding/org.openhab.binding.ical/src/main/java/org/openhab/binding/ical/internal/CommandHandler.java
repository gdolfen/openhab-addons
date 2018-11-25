/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ical.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.LocationItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.PlayerItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandHandler} is responsible for handling commands,
 * which should be send to other items by the binding.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class CommandHandler {
    private static void sendLocationItemCommand(LocationItem item, String command) {
        item.send(PointType.valueOf(command));
    }
    private static void sendNumberItemCommand(NumberItem item, String command) {
        item.send(DecimalType.valueOf(command));
    }
    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private Map<Class<?>, BiConsumer<Item, String>> itemHandler = new HashMap<>();

    private ItemRegistry itemRegistry;

    public CommandHandler(ItemRegistry itemRegistry) {
        super();
        this.itemRegistry = itemRegistry;
        itemHandler.put(ColorItem.class, (i, c) -> sendColorItemCommand((ColorItem) i, c));
        itemHandler.put(DateTimeItem.class, (i, c) -> sendDateTimeItemCommand((DateTimeItem) i, c));
        itemHandler.put(DimmerItem.class, (i, c) -> sendDimmerItemCommand((DimmerItem) i, c));
        itemHandler.put(LocationItem.class, (i, c) -> sendLocationItemCommand((LocationItem) i, c));
        itemHandler.put(NumberItem.class, (i, c) -> sendNumberItemCommand((NumberItem) i, c));
        itemHandler.put(PlayerItem.class, (i, c) -> sendPlayerItemCommand((PlayerItem) i, c));
        itemHandler.put(RollershutterItem.class, (i, c) -> sendRollershutterItemCommand((RollershutterItem) i, c));
        itemHandler.put(SwitchItem.class, (i, c) -> sendSwitchItemCommand((SwitchItem) i, c));
        itemHandler.put(StringItem.class, (i, c) -> sendStringItemCommand((StringItem) i, c));
    }

    private void sendColorItemCommand(ColorItem item, String command) {
        try {
            item.send(HSBType.valueOf(command));
        } catch (IllegalArgumentException e) {
            sendDimmerItemCommand(item, command);
        }
    }

    public void sendCommand(String command) {
        String[] splitted = command.split(" ");
        if (splitted.length == 3 && splitted[0].trim().equals("send")) {
            logger.info("Found send command " + command);
            String itemName = splitted[1].trim();
            try {
                Item item = itemRegistry.getItem(itemName);
                String type = splitted[2].trim();
                itemHandler.get(item.getClass()).accept(item, type);
            } catch (ItemNotFoundException e) {
                logger.error("Item " + itemName + " not found", e);
            }
        }
    }

    private void sendDateTimeItemCommand(DateTimeItem item, String command) {
        item.send(DateTimeType.valueOf(command));
    }

    private void sendDimmerItemCommand(DimmerItem item, String command) {
        try {
            item.send(PercentType.valueOf(command));
        } catch (IllegalArgumentException e) {
            sendSwitchItemCommand(item, command);
        }
    }

    private void sendPlayerItemCommand(PlayerItem item, String command) {
        try {
            item.send(PlayPauseType.valueOf(command));
        } catch (IllegalArgumentException e) {
            try {
                item.send(RewindFastforwardType.valueOf(command));
            } catch (IllegalArgumentException ex) {
                item.send(NextPreviousType.valueOf(command));
            }
        }
    }

    private void sendRollershutterItemCommand(RollershutterItem item, String command) {
        try {
            item.send(UpDownType.valueOf(command));
        } catch (IllegalArgumentException e) {
            try {
                item.send(StopMoveType.valueOf(command));
            } catch (IllegalArgumentException ex) {
                item.send(PercentType.valueOf(command));
            }
        }
    }

    private void sendStringItemCommand(StringItem item, String command) {
        item.send(StringType.valueOf(command));
    }

    private void sendSwitchItemCommand(SwitchItem item, String command) {
        item.send(OnOffType.valueOf(command));
    }
}
