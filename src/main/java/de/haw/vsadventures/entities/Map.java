package de.haw.vsadventures.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Map {
    private MapPosition object;

    public MapPosition getObject() {
        return object;
    }

    public void setObject(MapPosition object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "Map{" +
                "object=" + object +
                '}';
    }
}
