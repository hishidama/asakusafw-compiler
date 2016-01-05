/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.lang.compiler.core.basic;

import com.asakusafw.lang.compiler.analyzer.BatchAnalyzer;
import com.asakusafw.lang.compiler.analyzer.ExternalPortAnalyzer;
import com.asakusafw.lang.compiler.analyzer.FlowGraphAnalyzer;
import com.asakusafw.lang.compiler.analyzer.FlowPartBuilder;
import com.asakusafw.lang.compiler.analyzer.FlowPartDriver;
import com.asakusafw.lang.compiler.analyzer.JobflowAnalyzer;
import com.asakusafw.lang.compiler.analyzer.adapter.BatchAdapter;
import com.asakusafw.lang.compiler.analyzer.adapter.JobflowAdapter;
import com.asakusafw.lang.compiler.core.AnalyzerContext;
import com.asakusafw.lang.compiler.core.ClassAnalyzer;
import com.asakusafw.lang.compiler.core.adapter.ExternalPortAnalyzerAdapter;
import com.asakusafw.lang.compiler.model.graph.Batch;
import com.asakusafw.lang.compiler.model.graph.Jobflow;

/**
 * Analyzes Asakusa DSL elements.
 */
public class BasicClassAnalyzer implements ClassAnalyzer {

    @Override
    public boolean isBatchClass(Context context, Class<?> aClass) {
        return BatchAdapter.isBatch(aClass);
    }

    @Override
    public boolean isJobflowClass(Context context, Class<?> aClass) {
        return JobflowAdapter.isJobflow(aClass);
    }

    @Override
    public Batch analyzeBatch(Context context, Class<?> batchClass) {
        return createBatchAnalyzer(context).analyze(batchClass);
    }

    @Override
    public Jobflow analyzeJobflow(Context context, Class<?> jobflowClass) {
        return createJobflowAnalyzer(context).analyze(jobflowClass);
    }

    /**
     * Creates a new {@link FlowPartDriver}.
     * @param context the current context
     * @return the created builder
     */
    public static FlowPartDriver newFlowPartDriver(AnalyzerContext context) {
        return new FlowPartDriver(createFlowGraphAnalyzer(context));
    }

    /**
     * Creates a new {@link FlowPartBuilder}.
     * @param context the current context
     * @return the created builder
     */
    public static FlowPartBuilder newFlowPartBuilder(AnalyzerContext context) {
        return new FlowPartBuilder(createFlowGraphAnalyzer(context));
    }

    static BatchAnalyzer createBatchAnalyzer(AnalyzerContext context) {
        return new BatchAnalyzer(createJobflowAnalyzer(context));
    }

    static JobflowAnalyzer createJobflowAnalyzer(AnalyzerContext context) {
        return new JobflowAnalyzer(createFlowGraphAnalyzer(context));
    }

    static FlowGraphAnalyzer createFlowGraphAnalyzer(AnalyzerContext context) {
        return new FlowGraphAnalyzer(createExternalPortAnalyzer(context));
    }

    static ExternalPortAnalyzer createExternalPortAnalyzer(AnalyzerContext context) {
        return new ExternalPortAnalyzerAdapter(context);
    }
}
