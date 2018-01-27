package com.crypto.ico;

import com.crypto.authentication.GoogleCalendarAuthentication;
import com.crypto.calendar.CalendarManager;
import com.crypto.entity.UpcomingIco;
import com.google.api.services.calendar.Calendar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IcoDrop {

    /**
     * Logging
     */
    private static final Logger logger = LoggerFactory.getLogger(IcoDrop.class);

    /**
     * Url to pulling upcoming icos
     */
    private static final String UPCOMING_ICO_URL = "https://icodrops.com/category/upcoming-ico/";

    /**
     * User agent for web requests
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36";

    /**
     * Google Calendar manager
     */
    private CalendarManager calendarManager;

    /**
     * Constructor
     */
    public IcoDrop() {
        this.calendarManager = new CalendarManager();
    }

    /**
     * Pull upcoming ICOs and add to Google Calendar as needed
     */
    public void process() {
        Collection<UpcomingIco> upcomingIcos = pullUpcomingIcos();

        calendarManager.addEvents(upcomingIcos);
    }

    /**
     * Grab all upcoming ICOs from ICODrops and create entities for each
     */
    public Collection<UpcomingIco> pullUpcomingIcos() {
        Map<String, UpcomingIco> upcomingIcoMap = new HashMap<>();

        try {
            // Connect to url
            Document icoDropDoc = Jsoup.connect(UPCOMING_ICO_URL).userAgent(USER_AGENT).get();
            Elements upcomingIcos = icoDropDoc.select("#upcoming_ico");

            // Extract necessary information and exclude redundant entries
            for (Element upcomingIco : upcomingIcos) {
                Element mainInfo = upcomingIco.getElementsByClass("ico-main-info").first();
                Element info = mainInfo.getElementsByTag("a").first();
                Element interest = upcomingIco.getElementsByClass("interest").first();
                Element categoryType = upcomingIco.getElementsByClass("categ_type").first();
                Element goal = upcomingIco.getElementById("categ_desctop");
                Element startDate = upcomingIco.getElementsByClass("date").first();

                UpcomingIco ico = new UpcomingIco(
                        info.text(),
                        info.attr("href"),
                        interest.text(),
                        categoryType.text(),
                        goal.text(),
                        startDate.text()
                );

                if (!upcomingIcoMap.containsKey(ico.getProjectName())) {
                    upcomingIcoMap.put(ico.getProjectName(), ico);
                }
            }
        } catch (IOException ex) {
            logger.error("Unable to retrieve upcoming ICOs");
        }

        return upcomingIcoMap.values();
    }
}
