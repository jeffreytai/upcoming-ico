package com.crypto.calendar;

import com.crypto.authentication.GoogleCalendarAuthentication;
import com.crypto.entity.UpcomingIco;
import com.crypto.slack.SlackWebhook;
import com.crypto.utils.DateUtils;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CalendarManager {

    /**
     * Logging
     */
    private static final Logger logger = LoggerFactory.getLogger(CalendarManager.class);

    /**
     * Name of Release calendar
     */
    private final String RELEASES_CALENDAR = "RELEASES";

    /**
     * Page token for calendar requests
     */
    private final String PAGE_TOKEN = null;

    /**
     * Date formatter
     */
    private final String DATE_FORMATTER = "yyyy-MM-dd";

    /**
     * Maximum number of event results to retrieve in one batch
     */
    private final Integer MAX_EVENT_RESULTS = 2500;

    /**
     * Time zone for calendar events
     */
    private final String TIMEZONE = "America/Los_Angeles";

    /**
     * Username to post in Slack as
     */
    private final String SLACK_USERNAME = "ico-calendar-event-alert";

    /**
     * Google Calendar service
     */
    private Calendar service;

    public CalendarManager() {
        try {
            this.service = GoogleCalendarAuthentication.getCalendarService();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void addEvents(Collection<UpcomingIco> upcomingIcos) {
        try {
            // Retrieve all accessible calendars
            CalendarList calendarList = service.calendarList().list().setPageToken(PAGE_TOKEN).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            // Find Release calendar
            Optional<CalendarListEntry> optionalReleaseCalendar =
                    items.stream()
                            .filter(i -> i.getSummary().equals(this.RELEASES_CALENDAR))
                            .findFirst();

            if (!optionalReleaseCalendar.isPresent()) {
                logger.error("{} calendar inaccessible", this.RELEASES_CALENDAR);
                return;
            }

            // Extract calendar id
            String calendarId = optionalReleaseCalendar.get().getId();

            // Retrieve all existing events
            Events calendarEvents =
                    service.events()
                            .list(calendarId)
                            .setPageToken(this.PAGE_TOKEN)
                            .setMaxResults(this.MAX_EVENT_RESULTS)
                            .execute();

            List<Event> calendarItems = calendarEvents.getItems();

            // Filter out past calendar events
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat(this.DATE_FORMATTER);

            Set<String> futureEventTitles =
                    calendarItems.stream()
                            .filter(item -> {
                                try {
                                    String start = item.getStart().getDate().toString();
                                    Date formattedStart = sdf.parse(start);
                                    return formattedStart.after(today);
                                } catch (ParseException | NullPointerException ex) {
                                    logger.error(item.getStart().toString());
                                }
                                return false;
                            })
                            .map(item -> item.getSummary())
                            .collect(Collectors.toSet());

            // Maintain a list of newly added entries
            List<String> newEntries = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormat.forPattern("d MMM y");

            // For each upcoming ICO, create an event if it doesn't already exist in the calendar
            for (UpcomingIco upcomingIco : upcomingIcos) {
                String projectName = upcomingIco.getProjectName();

                if (futureEventTitles.stream().anyMatch(title -> title.contains(projectName))) {
                    logger.info("Event already exists for {}", projectName);
                    continue;
                }

                try {
                    // Assign the appropriate dates including the year
                    String dateWithYear = upcomingIco.getStartDate() + " " + DateUtils.findSubsequentYear(upcomingIco.getStartDate());
                    DateTime startDate = formatter.parseDateTime(dateWithYear);
                    com.google.api.client.util.DateTime eventStartDate = new com.google.api.client.util.DateTime(startDate.toString());
                    EventDateTime start = new EventDateTime()
                            .setDateTime(eventStartDate)
                            .setTimeZone(this.TIMEZONE);

                    DateTime endDate = startDate.plusDays(1);
                    com.google.api.client.util.DateTime eventEndDate = new com.google.api.client.util.DateTime(endDate.toString());
                    EventDateTime end = new EventDateTime()
                            .setDateTime(eventEndDate)
                            .setTimeZone(this.TIMEZONE);

                    String description = String.format(
                            "URL: %s\n" +
                                    "Interest: %s\n" +
                                    "Category: %s\n" +
                                    "Goal: %s\n",
                            upcomingIco.getUrl(),
                            upcomingIco.getInterest(),
                            upcomingIco.getCategory(),
                            upcomingIco.getGoal()
                    );

                    // Apply event details
                    Event event = new Event()
                            .setSummary(projectName + " ICO")
                            .setDescription(description)
                            .setStart(start)
                            .setEnd(end);

                    event = service.events().insert(calendarId, event).execute();

                    newEntries.add(projectName);
                } catch (IllegalArgumentException ex) {
                    logger.info("Error creating event for {}", projectName);
                    continue;
                }
            }

            // Send slack message
            sendAlertMessage(newEntries);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Send message to slack about newly added entries
     * @param entries
     */
    private void sendAlertMessage(List<String> entries) {
        SlackWebhook slack = new SlackWebhook(this.SLACK_USERNAME);

        if (entries.size() > 0) {
            String message = String.format(
                    "Calendar events for the following ICOs have been added: %s",
                    String.join(", ", entries));

            slack.sendMessage(message);
        }

        slack.shutdown();
    }
}
