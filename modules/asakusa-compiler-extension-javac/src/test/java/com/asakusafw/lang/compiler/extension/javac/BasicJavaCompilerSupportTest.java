package com.asakusafw.lang.compiler.extension.javac;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.lang.compiler.api.CompilerOptions;
import com.asakusafw.lang.compiler.api.mock.MockResourceProcessorContext;
import com.asakusafw.lang.compiler.common.DiagnosticException;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.testing.FileDeployer;

/**
 * Test for {@link BasicJavaCompilerSupport}.
 */
public class BasicJavaCompilerSupportTest {

    /**
     * temporary deployer.
     */
    @Rule
    public final FileDeployer deployer = new FileDeployer();

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File source = deployer.getFile("source");
        File target = deployer.getFile("target");

        BasicJavaCompilerSupport compiler = new BasicJavaCompilerSupport(
                source,
                Collections.<File>emptyList(),
                target);
        assertThat(compiler.getSourcePath(), is(source));
        assertThat(compiler.getDestinationPath(), is(target));
        assertThat(compiler.getClassPath(), is(empty()));

        put(compiler, "com.example.Hello", new String[] {
                "package com.example;",
                "import java.util.concurrent.Callable;",
                "",
                "public class Hello implements Callable<String> {",
                "    public String call() { return \"Hello, world!\"; }",
                "}",
        });
        compiler.process(context());
        try (URLClassLoader loader = loader(target)) {
            Class<?> built = loader.loadClass("com.example.Hello");
            assertThat(built, is(typeCompatibleWith(Callable.class)));
            assertThat(built.asSubclass(Callable.class).newInstance().call(), is((Object) "Hello, world!"));
        }
    }

    /**
     * using class path.
     * @throws Exception if failed
     */
    @Test
    public void classpath() throws Exception {
        File lib = deployer.copy("example.jar", "classpath/example.jar");
        File source = deployer.getFile("source");
        File target = deployer.getFile("target");

        JavaCompilerSupport compiler = new BasicJavaCompilerSupport(
                source,
                Arrays.asList(lib),
                target);

        put(compiler, "com.example.Inherit", new String[] {
                "package com.example;",
                "public class Inherit extends com.example.Hello {}",
        });
        compiler.process(context());
        try (URLClassLoader loader = loader(lib, target)) {
            Class<?> built = loader.loadClass("com.example.Inherit");
            assertThat(built.getSuperclass().getName(), is("com.example.Hello"));
        }
    }

    /**
     * empty sources.
     * @throws Exception if failed
     */
    @Test
    public void empty_sources() throws Exception {
        File source = deployer.getFile("source");
        File target = deployer.getFile("target");
        JavaCompilerSupport compiler = new BasicJavaCompilerSupport(
                source,
                Collections.<File>emptyList(),
                target);
        compiler.process(context());
        assertThat(target.exists(), is(false));
    }

    /**
     * occur compile error.
     * @throws Exception if failed
     */
    @Test(expected = DiagnosticException.class)
    public void compile_error_diagnostic() throws Exception {
        File source = deployer.getFile("source");
        File target = deployer.getFile("target");

        JavaCompilerSupport compiler = new BasicJavaCompilerSupport(
                source,
                Collections.<File>emptyList(),
                target);
        put(compiler, "com.example.Hello", "?");
        compiler.process(context());
    }

    /**
     * occur fatal error.
     * @throws Exception if failed
     */
    @Test(expected = DiagnosticException.class)
    public void compile_error_bootclasspath() throws Exception {
        File source = deployer.getFile("source");
        File target = deployer.getFile("target");
        JavaCompilerSupport compiler = new BasicJavaCompilerSupport(source, Collections.<File>emptyList(), target)
            .withBootClassPath(Collections.<File>emptyList());
        put(compiler, "com.example.Hello", new String[] {
                "package com.example;",
                "public class Hello {}",
        });
        compiler.process(context());
    }

    /**
     * occur fatal error.
     * @throws Exception if failed
     */
    @Test(expected = DiagnosticException.class)
    public void compile_error_compliant_version() throws Exception {
        File source = deployer.getFile("source");
        File target = deployer.getFile("target");
        JavaCompilerSupport compiler = new BasicJavaCompilerSupport(source, Collections.<File>emptyList(), target)
            .withCompliantVersion("INVALID");
        put(compiler, "com.example.Hello", new String[] {
                "package com.example;",
                "public class Hello {}",
        });
        compiler.process(context());
    }

    private MockResourceProcessorContext context() {
        return new MockResourceProcessorContext(
                new CompilerOptions("testing", "rw", Collections.<String, String>emptyMap()),
                getClass().getClassLoader());
    }

    private URLClassLoader loader(File... files) {
        List<URL> urls = new ArrayList<>();
        for (File file : files) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
        }
        return URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
    }

    private void put(JavaSourceExtension sources, String className, String... lines) {
        try (PrintWriter writer = new PrintWriter(sources.addJavaFile(new ClassDescription(className)))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
