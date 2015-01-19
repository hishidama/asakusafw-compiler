/**
 * Copyright 2011-2014 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.lang.compiler.model;

import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Represents a resource location.
 */
public class Location {

    /**
     * The default path segment separator character.
     */
    public static final char DEFAULT_SEGMENT_SEPARATOR = '/';

    private final Location parent;

    private final String name;

    /**
     * Creates a new instance.
     * @param parent the parent location
     * @param name the local resource name
     */
    public Location(Location parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * Returns the parent location
     * @return the parent location, or {@code null} if this does not have a parent
     */
    public Location getParent() {
        return parent;
    }

    /**
     * Returns the local resource name.
     * @return the local resource name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a child location.
     * @param lastName the child resource name
     * @return the child location
     */
    public Location append(String lastName) {
        return new Location(this, lastName);
    }

    /**
     * Returns a child location
     * @param suffix the child location which is relative from this
     * @return the child location
     */
    public Location append(Location suffix) {
        LinkedList<String> segments = new LinkedList<>();
        Location current = suffix;
        while (current != null) {
            segments.addFirst(current.name);
            current = current.parent;
        }
        current = this;
        for (String segment : segments) {
            current = new Location(current, segment);
        }
        return current;
    }

    /**
     * Returns a location from path string using {@link #DEFAULT_SEGMENT_SEPARATOR} as a segment separator.
     * @param pathString the path string
     * @return the location
     * @see #of(String, char)
     */
    public static Location of(String pathString) {
        return of(pathString, DEFAULT_SEGMENT_SEPARATOR);
    }

    /**
     * Returns a location from path string.
     * @param pathString the path string
     * @param separator the segment separator char
     * @return the location
     */
    public static Location of(String pathString, char separator) {
        String[] segments = pathString.split(Pattern.quote(String.valueOf(separator)));
        Location current = null;
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            current = new Location(current, segment);
        }
        if (current == null) {
            throw new IllegalArgumentException();
        }
        return current;
    }

    /**
     * Returns path string using {@link #DEFAULT_SEGMENT_SEPARATOR} as a segment separator.
     * @return the path string
     */
    public String toPath() {
        return toPath(DEFAULT_SEGMENT_SEPARATOR);
    }

    /**
     * Returns path string.
     * @param separator the segment separator char
     * @return the path string
     */
    public String toPath(char separator) {
        LinkedList<String> segments = new LinkedList<>();
        Location current = this;
        while (current != null) {
            segments.addFirst(current.name);
            current = current.parent;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(segments.removeFirst());
        for (String segment : segments) {
            buf.append(separator);
            buf.append(segment);
        }
        return buf.toString();
    }

    /**
     * Returns whether this location is a prefix of another location.
     * @param other target location
     * @return {@code true} if is prefix or same location, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean isPrefixOf(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
        }
        int thisSegments = count(this);
        int otherSegments = count(other);
        if (thisSegments > otherSegments) {
            return false;
        }
        Location current = other;
        for (int i = 0, n = otherSegments - thisSegments; i < n; i++) {
            current = current.getParent();
        }
        return this.equals(current);
    }

    private int count(Location location) {
        int count = 1;
        Location current = location.getParent();
        while (current != null) {
            count++;
            current = current.getParent();
        }
        return count;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        Location current = this;
        while (current != null) {
            result = prime * result + current.name.hashCode();
            current = current.parent;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Location other = (Location) obj;
        Location thisCur = this;
        Location otherCur = other;
        while (thisCur != null && otherCur != null) {
            if (thisCur == otherCur) {
                return true;
            }
            if (thisCur.name.equals(otherCur.name) == false) {
                return false;
            }
            thisCur = thisCur.parent;
            otherCur = otherCur.parent;
        }
        return thisCur == otherCur;
    }

    @Override
    public String toString() {
        return toPath();
    }
}
