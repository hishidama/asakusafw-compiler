package com.asakusafw.lang.compiler.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test for {@link PropertyName}.
 */
public class PropertyNameTest {

    /**
     * trivial case.
     */
    @Test
    public void simple() {
        PropertyName name = PropertyName.of("simple");
        assertThat(name.isEmpty(), is(false));
        assertThat(name.getWords(), is(words("simple")));
    }

    /**
     * lower snake case name.
     */
    @Test
    public void lower_snake_case() {
        PropertyName name = PropertyName.of("abc_de_fgh");
        assertThat(name.getWords(), is(words("abc,de,fgh")));
    }

    /**
     * upper snake case name.
     */
    @Test
    public void upper_snake_case() {
        PropertyName name = PropertyName.of("ABC_DE_FGH");
        assertThat(name.getWords(), is(words("abc,de,fgh")));
    }

    /**
     * lower camel case name.
     */
    @Test
    public void lower_camel_case() {
        PropertyName name = PropertyName.of("abcDeFgh");
        assertThat(name.getWords(), is(words("abc,de,fgh")));
    }

    /**
     * upper camel case name.
     */
    @Test
    public void upper_camel_case() {
        PropertyName name = PropertyName.of("AbcDeFgh");
        assertThat(name.getWords(), is(words("abc,de,fgh")));
    }

    /**
     * lower snake case name w/ trimming.
     */
    @Test
    public void trim_snake() {
        PropertyName name = PropertyName.of("_abc_de_fgh_");
        assertThat(name.getWords(), is(words("abc,de,fgh")));
    }

    /**
     * lower snake case name w/ trimming.
     */
    @Test
    public void trim_camel() {
        PropertyName name = PropertyName.of("_abcDeFgh_");
        assertThat(name.getWords(), is(words("abc,de,fgh")));
    }

    /**
     * empty name.
     */
    @Test
    public void empty_name() {
        PropertyName name = PropertyName.of("_");
        assertThat(name.isEmpty(), is(true));
    }

    /**
     * builds snake case name.
     */
    @Test
    public void to_snake() {
        PropertyName name = new PropertyName(words("hello,world"));
        assertThat(name.toName(), is("hello_world"));
    }

    /**
     * builds camel case name.
     */
    @Test
    public void to_camel() {
        PropertyName name = new PropertyName(words("hello,world"));
        assertThat(name.toMemberName(), is("helloWorld"));
    }

    /**
     * manipulates name.
     */
    @Test
    public void manipulate() {
        PropertyName name = new PropertyName(words("hello"));
        PropertyName edit = name.addFirst("get").addLast("option");
        assertThat(edit, is(not(name)));
        assertThat(edit.getWords(), is(words("get,hello,option")));
    }

    private List<String> words(String sequence) {
        List<String> results = new ArrayList<>();
        for (String w : sequence.split(",")) {
            if (w.isEmpty() == false) {
                results.add(w);
            }
        }
        return results;
    }
}
