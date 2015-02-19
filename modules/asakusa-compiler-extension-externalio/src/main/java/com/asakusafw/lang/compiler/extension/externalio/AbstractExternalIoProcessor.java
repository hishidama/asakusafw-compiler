package com.asakusafw.lang.compiler.extension.externalio;

import java.util.Collection;
import java.util.Set;

import com.asakusafw.lang.compiler.api.ExternalIoProcessor;
import com.asakusafw.lang.compiler.api.reference.ExternalInputReference;
import com.asakusafw.lang.compiler.api.reference.ExternalOutputReference;
import com.asakusafw.lang.compiler.model.description.Descriptions;
import com.asakusafw.lang.compiler.model.description.ValueDescription;
import com.asakusafw.lang.compiler.model.info.ExternalInputInfo;
import com.asakusafw.lang.compiler.model.info.ExternalOutputInfo;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * An abstract implementation of {@link ExternalIoProcessor}.
 * @param <TInput> the importer description type
 * @param <TOutput> the exporter description type
 */
public abstract class AbstractExternalIoProcessor<
            TInput extends ImporterDescription,
            TOutput extends ExporterDescription>
        implements ExternalIoProcessor {

    /**
     * Returns the external I/O module name.
     * @return the module name
     */
    protected abstract String getModuleName();

    /**
     * Returns the importer description type.
     * @return the importer description type
     */
    protected abstract Class<TInput> getInputDescriptionType();

    /**
     * Returns the exporter description type.
     * @return the exporter description type
     */
    protected abstract Class<TOutput> getOutputDescriptionType();

    /**
     * Returns the properties of the target importer description.
     * @param context  the current context
     * @param name the input name
     * @param description the target description
     * @return the extracted properties, or {@code null} if the input does not have any additional properties
     */
    protected abstract ValueDescription analyzeInputProperties(Context context, String name, TInput description);

    /**
     * Returns the properties of the target exporter description.
     * @param context  the current context
     * @param name the input name
     * @param description the target description
     * @return the extracted properties, or {@code null} if the output does not have any additional properties
     */
    protected abstract ValueDescription analyzeOutputProperties(Context context, String name, TOutput description);

    /**
     * Returns input paths, which will be used in main phase, of the target external input.
     * @param context  the current context
     * @param name the input name
     * @param info the structural information of the target input
     * @return the computed input paths
     */
    protected abstract Set<String> computeInputPaths(Context context, String name, ExternalInputInfo info);

    @Override
    public final boolean isSupported(Context context, Class<?> descriptionClass) {
        Class<TInput> input = getInputDescriptionType();
        Class<TOutput> output = getOutputDescriptionType();
        return input.isAssignableFrom(descriptionClass) || output.isAssignableFrom(descriptionClass);
    }

    @Override
    public final ExternalInputInfo analyzeInput(Context context, String name, Object description) {
        Class<TInput> type = getInputDescriptionType();
        if (type.isInstance(description) == false) {
            throw new IllegalArgumentException();
        }
        TInput desc = type.cast(description);
        ValueDescription properties = analyzeInputProperties(context, name, desc);
        return new ExternalInputInfo.Basic(
                Descriptions.classOf(desc.getClass()),
                getModuleName(),
                Descriptions.classOf(desc.getModelType()),
                convert(desc.getDataSize()),
                properties);
    }

    @Override
    public final ExternalOutputInfo analyzeOutput(Context context, String name, Object description) {
        Class<TOutput> type = getOutputDescriptionType();
        if (type.isInstance(description) == false) {
            throw new IllegalArgumentException();
        }
        TOutput desc = type.cast(description);
        ValueDescription properties = analyzeOutputProperties(context, name, desc);
        return new ExternalOutputInfo.Basic(
                Descriptions.classOf(desc.getClass()),
                getModuleName(),
                Descriptions.classOf(desc.getModelType()),
                properties);
    }

    @Override
    public ExternalInputReference resolveInput(Context context, String name, ExternalInputInfo info) {
        Collection<String> paths = computeInputPaths(context, name, info);
        return new ExternalInputReference(name, info, paths);
    }

    @Override
    public ExternalOutputReference resolveOutput(
            Context context,
            String name,
            ExternalOutputInfo info,
            Collection<String> internalOutputPaths) {
        return new ExternalOutputReference(name, info, internalOutputPaths);
    }

    private static ExternalInputInfo.DataSize convert(ImporterDescription.DataSize value) {
        if (value == null) {
            return ExternalInputInfo.DataSize.UNKNOWN;
        }
        switch (value) {
        case TINY:
            return ExternalInputInfo.DataSize.TINY;
        case SMALL:
            return ExternalInputInfo.DataSize.SMALL;
        case LARGE:
            return ExternalInputInfo.DataSize.LARGE;
        default:
            return ExternalInputInfo.DataSize.UNKNOWN;
        }
    }
}
