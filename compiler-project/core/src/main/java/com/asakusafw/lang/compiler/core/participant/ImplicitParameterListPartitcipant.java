/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.lang.compiler.core.participant;

import com.asakusafw.lang.compiler.core.BatchCompiler.Context;
import com.asakusafw.lang.compiler.core.basic.AbstractCompilerParticipant;
import com.asakusafw.lang.compiler.model.graph.Batch;
import com.asakusafw.lang.compiler.parameter.ImplicitParameterList;

/**
 * Computes implicitly required batch parameters.
 * @since 0.5.0
 */
public class ImplicitParameterListPartitcipant extends AbstractCompilerParticipant {

    @Override
    public void beforeBatch(Context context, Batch batch) {
        context.registerExtension(ImplicitParameterList.class, ImplicitParameterList.of(batch));
    }
}
