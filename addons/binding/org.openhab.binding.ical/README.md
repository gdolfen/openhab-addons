# iCal Binding

This binding integrates iCal-Calendars.

## Supported Things

Public available iCal-Calendars, reachable over http/https are supported.
The iCal-File can also be placed as static content under openhab/config/html.

## Discovery

There is no discovery available.
A calendar can be added through the (Paper) UI.

## Binding Configuration

There is no configuration for this binding.

## Thing Configuration

The following thing configurations are available:

| Name                            | Description                                                              |
|---------------------------------|--------------------------------------------------------------------------|
| url                             | The URL of the iCal-File.                                                |
| refreshInterval                 | Time for refreshing the iCal-File in seconds (default=86400s).           |
| nextEventNotificationDateOffset | Offset for the next event notification date in seconds (default=86400s). |

## Channels

| Channel Type ID             | Item Type | Description                                           | Read/Write |
|-----------------------------|-----------|-------------------------------------------------------|------------|
| currentEvent                | Text      | Current event in the calendar.                        | R          |
| currentEventDescription     | Text      | Current event description in the calendar.            | R          |
| currentEventStart           | DateTime  | Current event start in the calendar.                  | R          |
| currentEventEnd             | DateTime  | Current event end in the calendar.                    | R          |
| nextEvent                   | Text      | Next event in the calendar.                           | R          |
| nextEventDescription        | Text      | Next event description in the calendar.               | R          |
| nextEventNotificationDate   | DateTime  | Notification date for the next event in the calendar. | R          |
| nextEventStart              | DateTime  | Next event start in the calendar.                     | R          |
| nextEventEnd                | DateTime  | Next event end in the calendar.                       | R          |
| nextEventNotificationSwitch | Switch    | Switch is set ON after reaching notification date.    | R          |
| nextEventSwitch             | Switch    | Switch is set ON after reaching next start date.      | R          |

## Full Example

You can configure the things through Paper UI. Thus, you can find your device ID by looking into discovery results in Paper UI.

You could also specify an alternate ThingID using a .things file, specifying the url as a mandatory configuration parameter: 

```
ical:calendar:holiday [url="https://calendar.google.com/calendar/ical/de.german%23holiday%40group.v.calendar.google.com/public/basic.ics", refreshInterval=3600, nextEventNotificationDateOffset=86400] 
```

demo.items:

```
String   Holiday_NextEvent                 "Title [%s]"                                         <calendar>     { channel="ical:calendar:holiday:nextEvent" }
String   Holiday_NextEventDescription      "Description [%s]"                                   <calendar>     { channel="ical:calendar:holiday:nextEventDescription" }
DateTime Holiday_NextEventNotificationDate "Notification [%1$td.%1$tm.%1$tY, %1$tH:%1$tM Uhr]"  <calendar>     { channel="ical:calendar:holiday:nextEventNotificationDate" }
DateTime Holiday_NextEventStart            "Start [%1$td.%1$tm.%1$tY, %1$tH:%1$tM Uhr]"         <calendar>     { channel="ical:calendar:holiday:nextEventStart" }
DateTime Holiday_NextEventEnd              "End [%1$td.%1$tm.%1$tY, %1$tH:%1$tM Uhr]"           <calendar>     { channel="ical:calendar:holiday:nextEventEnd" }
```

demo.sitemap:

```
sitemap default label="Sitemap" {  
    Frame {
        Text item=Holiday_NextEvent
        Text item=Holiday_NextEventDescription
        Text item=Holiday_NextEventNotificationDate
        Text item=Holiday_NextEventStart
        Text item=Holiday_NextEventEnd
    }
}
```
