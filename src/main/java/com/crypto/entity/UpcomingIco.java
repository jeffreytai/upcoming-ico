package com.crypto.entity;

public class UpcomingIco {

    /**
     * Name of project
     */
    private String projectName;

    /**
     * Url to ICODrops page
     */
    private String url;

    /**
     * Level of interest
     */
    private String interest;

    /**
     * Category of project
     */
    private String category;

    /**
     * Target to fundraise in USD
     */
    private String goal;

    /**
     * Start of public sale
     */
    private String startDate;

    /**
     * Constructor
     * @param projectName
     * @param interest
     * @param category
     * @param goal
     * @param startDate
     */
    public UpcomingIco(String projectName, String url, String interest, String category, String goal, String startDate) {
        this.projectName = projectName;
        this.url = url;
        this.interest = interest;
        this.category = category;
        this.goal = goal;
        this.startDate = startDate;
    }

    /**
     * Getters and setters
     */

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}
