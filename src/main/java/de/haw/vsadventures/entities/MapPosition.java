package de.haw.vsadventures.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapPosition {
    String host;
    String name;

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

    @Override
    public String toString() {
        return "MapPosition{" +
                "host='" + host + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
