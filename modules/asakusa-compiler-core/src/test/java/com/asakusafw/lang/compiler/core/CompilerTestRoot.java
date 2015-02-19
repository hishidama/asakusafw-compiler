package com.asakusafw.lang.compiler.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.lang.compiler.api.BatchProcessor;
import com.asakusafw.lang.compiler.api.CompilerOptions;
import com.asakusafw.lang.compiler.api.DataModelLoader;
import com.asakusafw.lang.compiler.api.ExternalPortProcessor;
import com.asakusafw.lang.compiler.api.JobflowProcessor;
import com.asakusafw.lang.compiler.core.basic.BasicDataModelLoader;
import com.asakusafw.lang.compiler.core.dummy.DummyExternalPortProcessor;
import com.asakusafw.lang.compiler.core.dummy.DummyImporterDescription;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.model.description.Descriptions;
import com.asakusafw.lang.compiler.model.graph.ExternalInput;
import com.asakusafw.lang.compiler.model.graph.ExternalOutput;
import com.asakusafw.lang.compiler.model.graph.Jobflow;
import com.asakusafw.lang.compiler.model.graph.OperatorGraph;
import com.asakusafw.lang.compiler.model.info.BatchInfo;
import com.asakusafw.lang.compiler.model.info.ExternalInputInfo;
import com.asakusafw.lang.compiler.model.info.ExternalOutputInfo;
import com.asakusafw.lang.compiler.packaging.FileContainer;
import com.asakusafw.lang.compiler.packaging.FileContainerRepository;

/**
 * Test utilities for batch/jobflow compilers.
 */
public abstract class CompilerTestRoot {

    /**
     * temporary working dir.
     */
    @Rule
    public final TemporaryFolder root = new TemporaryFolder() {
        @Override
        protected void after() {
            for (Object object : closeables) {
                try {
                    if (object instanceof AutoCloseable) {
                        ((AutoCloseable) object).close();
                    }
                } catch (Exception e) {
                    // ignored
                }
            }
            closeables.clear();
            super.after();
        }
    };

    /**
     * Closeable objects.
     */
    public List<Object> closeables = new ArrayList<>();

    /**
     * compiler options.
     */
    public CompilerOptions options = new CompilerOptions(
            "testing",
            "working",
            Collections.<String, String>emptyMap());

    /**
     * embedded libraries.
     */
    public final Set<File> embedded = new LinkedHashSet<>();

    /**
     * attached libraries.
     */
    public final Set<File> attached = new LinkedHashSet<>();

    /**
     * data model loaders.
     */
    public List<DataModelLoader> dataModelLoaders = new ArrayList<>();

    /**
     * batch processors.
     */
    public List<BatchProcessor> batchProcessors = new ArrayList<>();

    /**
     * jobflow processors.
     */
    public List<JobflowProcessor> jobflowProcessors = new ArrayList<>();

    /**
     * external port processors.
     */
    public List<ExternalPortProcessor> externalPortProcessors = new ArrayList<>();

    /**
     * compiler participants.
     */
    public List<CompilerParticipant> compilerParticipants = new ArrayList<>();

    /**
     * Returns compiler context.
     * @param defaults {@code true} to use defaults
     * @return created context
     */
    public CompilerContext context(boolean defaults) {
        ProjectRepository project = project();
        closeables.add(project);
        ToolRepository tools = tools(defaults, project.getClassLoader());
        try {
            return new CompilerContext.Basic(options, project, tools, new FileContainerRepository(root.newFolder()));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns a new file container.
     * @return created container
     */
    public FileContainer container() {
        try {
            return new FileContainer(root.newFolder());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns a dummy batch info.
     * @param id the batch ID
     * @return info
     */
    public BatchInfo batchInfo(String id) {
        return new BatchInfo.Basic(
                id,
                new ClassDescription(id),
                null,
                Collections.<BatchInfo.Parameter>emptyList(),
                Collections.<BatchInfo.Attribute>emptyList());
    }

    /**
     * Returns a simple jobflow.
     * @param id the jobflow id
     * @return jobflow
     */
    public Jobflow jobflow(String id) {
        ExternalInput in = ExternalInput.newInstance("in", new ExternalInputInfo.Basic(
                Descriptions.classOf(DummyImporterDescription.class),
                "simple",
                Descriptions.classOf(String.class),
                ExternalInputInfo.DataSize.UNKNOWN));
        ExternalOutput out = ExternalOutput.newInstance("out", new ExternalOutputInfo.Basic(
                Descriptions.classOf(DummyImporterDescription.class),
                "simple",
                Descriptions.classOf(String.class)));
        in.getOperatorPort().connect(out.getOperatorPort());
        return new Jobflow(id, new ClassDescription(id), new OperatorGraph().add(in).add(out));
    }

    private ToolRepository tools(boolean defaults, ClassLoader classLoader) {
        ToolRepository.Builder builder = ToolRepository.builder(classLoader);
        for (DataModelLoader loader : dataModelLoaders) {
            builder.use(loader);
        }
        for (BatchProcessor processor : batchProcessors) {
            builder.use(processor);
        }
        for (JobflowProcessor processor : jobflowProcessors) {
            builder.use(processor);
        }
        for (ExternalPortProcessor processor : externalPortProcessors) {
            builder.use(processor);
        }
        for (CompilerParticipant participant : compilerParticipants) {
            builder.use(participant);
        }
        if (defaults) {
            if (dataModelLoaders.isEmpty()) {
                builder.use(new BasicDataModelLoader(classLoader));
            }
            if (externalPortProcessors.isEmpty()) {
                builder.use(new DummyExternalPortProcessor());
            }
        }
        return builder.build();
    }

    private ProjectRepository project() {
        ProjectRepository.Builder loader = ProjectRepository.builder(getClass().getClassLoader());
        for (File file : embedded) {
            loader.embed(file);
        }
        for (File file : attached) {
            loader.attach(file);
        }
        try {
            return loader.build();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
