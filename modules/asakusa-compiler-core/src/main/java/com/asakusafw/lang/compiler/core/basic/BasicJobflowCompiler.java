package com.asakusafw.lang.compiler.core.basic;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.lang.compiler.api.ExternalPortProcessor;
import com.asakusafw.lang.compiler.api.JobflowProcessor;
import com.asakusafw.lang.compiler.api.basic.ExternalPortContainer;
import com.asakusafw.lang.compiler.common.Diagnostic;
import com.asakusafw.lang.compiler.common.DiagnosticException;
import com.asakusafw.lang.compiler.core.CompilerParticipant;
import com.asakusafw.lang.compiler.core.JobflowCompiler;
import com.asakusafw.lang.compiler.core.adapter.ExternalPortProcessorAdapter;
import com.asakusafw.lang.compiler.core.adapter.JobflowProcessorAdapter;
import com.asakusafw.lang.compiler.model.graph.Jobflow;
import com.asakusafw.lang.compiler.model.info.BatchInfo;

/**
 * A basic implementation of {@link JobflowCompiler}.
 */
public class BasicJobflowCompiler implements JobflowCompiler {

    static final Logger LOG = LoggerFactory.getLogger(BasicJobflowCompiler.class);

    @Override
    public void compile(Context context, BatchInfo batch, Jobflow jobflow) {
        before(context, batch, jobflow);
        runOperatorGraphProcessor(context, batch, jobflow);
        runExternalPortProcessor(context, batch, jobflow);
        after(context, batch, jobflow);
    }

    private void runOperatorGraphProcessor(Context context, BatchInfo batch, Jobflow jobflow) {
        JobflowProcessorAdapter adapter = new JobflowProcessorAdapter(context);
        JobflowProcessor processor = context.getTools().getJobflowProcessor();
        try {
            processor.process(adapter, jobflow);
        } catch (IOException e) {
            throw new DiagnosticException(Diagnostic.Level.ERROR, MessageFormat.format(
                    "error occurred while processing operator graph (jobflow={0})",
                    jobflow.getDescriptionClass().getName()));
        }
    }

    private void runExternalPortProcessor(Context context, BatchInfo batch, Jobflow jobflow) {
        ExternalPortContainer externals = context.getExternalPorts();
        if (externals.isEmpty()) {
            return;
        }
        ExternalPortProcessorAdapter adapter = new ExternalPortProcessorAdapter(context);
        ExternalPortProcessor processor = context.getTools().getExternalPortProcessor();
        try {
            processor.process(adapter, externals.getInputs(), externals.getOutputs());
        } catch (IOException e) {
            throw new DiagnosticException(Diagnostic.Level.ERROR, MessageFormat.format(
                    "error occurred while processing external I/Os (jobflow={0})",
                    jobflow.getDescriptionClass().getName()));
        }
    }

    private void before(Context context, BatchInfo batch, Jobflow jobflow) {
        CompilerParticipant participant = context.getTools().getParticipant();
        participant.beforeJobflow(context, batch, jobflow);
    }

    private void after(Context context, BatchInfo batch, Jobflow jobflow) {
        CompilerParticipant participant = context.getTools().getParticipant();
        participant.afterJobflow(context, batch, jobflow);
    }
}
