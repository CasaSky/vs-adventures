package de.haw.vsadventures.entities;

import java.util.List;

public class LocationPosition {

    private String host;
    private String name;
    private List<Integer> tasks;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getTasks() {
        return tasks;
    }

    public void setTasks(List<Integer> tasks) {
        this.tasks = tasks;
    }
}
