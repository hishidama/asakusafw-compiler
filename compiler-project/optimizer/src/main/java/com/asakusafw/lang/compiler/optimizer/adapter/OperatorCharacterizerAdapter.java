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
package com.asakusafw.lang.compiler.optimizer.adapter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.asakusafw.lang.compiler.model.graph.Operator;
import com.asakusafw.lang.compiler.optimizer.OperatorCharacterizer;
import com.asakusafw.lang.compiler.optimizer.OperatorEstimate;
import com.asakusafw.lang.compiler.optimizer.OperatorEstimator;
import com.asakusafw.lang.compiler.optimizer.OperatorEstimators;

/**
 * Adapter for {@link com.asakusafw.lang.compiler.optimizer.OperatorCharacterizer.Context}.
 */
public class OperatorCharacterizerAdapter
        extends ForwardingOptimizerContext
        implements OperatorCharacterizer.Context {

    private final OperatorEstimator.Context delegate;

    private final OperatorEstimator estimator;

    /**
     * Creates a new instance.
     * @param delegate the delegation target
     * @param estimator the estimation engine
     */
    public OperatorCharacterizerAdapter(OperatorEstimator.Context delegate, OperatorEstimator estimator) {
        super(delegate);
        this.delegate = delegate;
        this.estimator = estimator;
    }

    @Override
    public OperatorEstimate estimate(Operator operator) {
        Map<Operator, OperatorEstimate> results = estimate(Collections.singleton(operator));
        OperatorEstimate estimate = results.get(operator);
        assert estimate != null;
        return estimate;
    }

    @Override
    public Map<Operator, OperatorEstimate> estimate(Operator... operators) {
        return estimate(Arrays.asList(operators));
    }

    @Override
    public Map<Operator, OperatorEstimate> estimate(Collection<? extends Operator> operators) {
        return OperatorEstimators.apply(delegate, estimator, operators);
    }
}
