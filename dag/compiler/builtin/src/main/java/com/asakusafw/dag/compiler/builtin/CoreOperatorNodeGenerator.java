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
package com.asakusafw.dag.compiler.builtin;

import static com.asakusafw.dag.compiler.builtin.Util.*;

import java.util.function.Supplier;

import com.asakusafw.dag.compiler.codegen.OperatorNodeGenerator;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.model.graph.CoreOperator;
import com.asakusafw.lang.compiler.model.graph.CoreOperator.CoreOperatorKind;
import com.asakusafw.lang.compiler.model.graph.Operator;

/**
 * Generates classes for processing Asakusa core operators.
 * @since 0.4.0
 */
public abstract class CoreOperatorNodeGenerator implements OperatorNodeGenerator {

    /**
     * Returns the target operator kind.
     * @return the target operator kind
     */
    protected abstract CoreOperatorKind getOperatorKind();

    /**
     * Generates a class for processing the operator, and returns the generated class binary.
     * @param context the current context
     * @param operator the target operator
     * @param namer the class namer
     * @return the generated node info
     */
    protected abstract NodeInfo generate(
            Context context,
            CoreOperator operator,
            Supplier<? extends ClassDescription> namer);

    @Override
    public final ClassDescription getAnnotationType() {
        return getOperatorKind().getAnnotationType();
    }

    @Override
    public NodeInfo generate(Context context, Operator operator, Supplier<? extends ClassDescription> classNamer) {
        checkDependencies(context, operator);
        return generate(context, (CoreOperator) operator, classNamer);
    }
}
