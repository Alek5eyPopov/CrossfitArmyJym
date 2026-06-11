package com.crossfitarmyjym.app.data.model;

public class AdminStats {

    private final int users;
    private final int activeUsers;
    private final int groups;
    private final int classes;
    private final int confirmedBookings;
    private final int attended;
    private final int results;

    public AdminStats(int users, int activeUsers, int groups, int classes,
                      int confirmedBookings, int attended, int results) {
        this.users = users;
        this.activeUsers = activeUsers;
        this.groups = groups;
        this.classes = classes;
        this.confirmedBookings = confirmedBookings;
        this.attended = attended;
        this.results = results;
    }

    public int getUsers() { return users; }
    public int getActiveUsers() { return activeUsers; }
    public int getGroups() { return groups; }
    public int getClasses() { return classes; }
    public int getConfirmedBookings() { return confirmedBookings; }
    public int getAttended() { return attended; }
    public int getResults() { return results; }
}
