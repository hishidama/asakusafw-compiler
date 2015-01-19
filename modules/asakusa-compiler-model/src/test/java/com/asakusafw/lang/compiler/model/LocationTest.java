package com.asakusafw.lang.compiler.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link Location}.
 */
public class LocationTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Location location = new Location(null, "a");
        assertThat(location.toString(), location.getParent(), is(nullValue()));
        assertThat(location.toString(), location.getName(), is("a"));
        assertThat(location.toString(), location.toPath(), is("a"));
    }

    /**
     * nested location.
     */
    @Test
    public void nested() {
        Location location = new Location(new Location(new Location(null, "a"), "b"), "c");
        assertThat(location.getParent(), is(notNullValue()));
        assertThat(location.getName(), is("c"));
        assertThat(location.toPath(','), is("a,b,c"));
    }

    /**
     * convert location and path.
     */
    @Test
    public void convert() {
        Location location = Location.of("a,b,c", ',');
        assertThat(location.toPath(':'), is("a:b:c"));
    }

    /**
     * nested location.
     */
    @Test
    public void append() {
        Location location = Location.of("a").append("b").append(Location.of("c,d", ','));
        assertThat(location.getParent(), is(notNullValue()));
        assertThat(location.toPath(':'), is("a:b:c:d"));
    }

    /**
     * comparing locations.
     */
    @Test
    public void compare() {
        Location a = Location.of("a");
        Location ab = Location.of("a/b");
        Location abc = Location.of("a/b/c");
        Location bc = Location.of("b/c");
        Location abc2 = Location.of("a/b/c");
        Location abc3 = abc.getParent().append("c");

        assertThat(a.isPrefixOf(a), is(true));
        assertThat(a.isPrefixOf(ab), is(true));
        assertThat(a.isPrefixOf(abc), is(true));
        assertThat(ab.isPrefixOf(abc), is(true));
        assertThat(a.isPrefixOf(bc), is(false));
        assertThat(abc.isPrefixOf(ab), is(false));
        assertThat(abc.isPrefixOf(abc2), is(true));
        assertThat(abc.isPrefixOf(abc3), is(true));

        assertThat(abc, is(equalTo(abc)));
        assertThat(abc, is(equalTo(abc2)));
        assertThat(abc, is(equalTo(abc3)));
        assertThat(abc, is(not(equalTo(ab))));
        assertThat(abc, is(not(equalTo(bc))));
    }
}
