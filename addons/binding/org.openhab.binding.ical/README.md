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

| Channel Type ID           | Item Type | Description                                           | Read/Write |
|---------------------------|-----------|-------------------------------------------------------|------------|
| nextEvent                 | Text      | Next event in the calendar.                           | R          |
| nextEventDescription      | Text      | Next event description in the calendar.               | R          |
| nextEventNotificationDate | DateTime  | Notification date for the next event in the calendar. | R          |
| nextEventStart            | DateTime  | Next event start in the calendar.                     | R          |
| nextEventEnd              | DateTime  | Next event end in the calendar.                       | R          |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

