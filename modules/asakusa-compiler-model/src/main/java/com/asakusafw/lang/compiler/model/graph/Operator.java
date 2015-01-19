package com.asakusafw.lang.compiler.model.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.lang.compiler.model.description.TypeDescription;
import com.asakusafw.lang.compiler.model.description.ValueDescription;

/**
 * Represents an operator vertex.
 */
public abstract class Operator {

    final List<OperatorProperty> properties = new ArrayList<>();

    final Set<OperatorConstraint> constraints = EnumSet.noneOf(OperatorConstraint.class);

    /**
     * Returns the operator kind.
     * @return the operator kind
     */
    public abstract OperatorKind getOperatorKind();

    /**
     * Returns a copy of this operator.
     * The copy is not connected to any neighbors.
     * If this operator has operator graphs recursively, this performs as 'deep-copy'.
     * @return the created copy
     */
    public abstract Operator copy();

    /**
     * Copies operator attributes into the target operator.
     * @param copy the target operator
     * @param <T> the target operator type
     * @return the target operator
     */
    protected final <T extends Operator> T copyAttributesTo(T copy) {
        for (OperatorProperty property : properties) {
            switch (property.getPropertyKind()) {
            case INPUT: {
                OperatorInput p = (OperatorInput) property;
                copy.properties.add(new OperatorInput(copy, p.getName(), p.getDataType(), p.getGroup()));
                break;
            }
            case OUTPUT: {
                OperatorOutput p = (OperatorOutput) property;
                copy.properties.add(new OperatorOutput(copy, p.getName(), p.getDataType()));
                break;
            }
            case ARGUMENT: {
                OperatorArgument p = (OperatorArgument) property;
                copy.properties.add(new OperatorArgument(p.getName(), p.getValue()));
                break;
            }
            default:
                throw new AssertionError(property);
            }
        }
        copy.constraints.addAll(constraints);
        return copy;
    }

    /**
     * Disconnects from all neighbor operators.
     * @return this
     */
    public Operator disconnectAll() {
        for (OperatorPort port : getProperties(OperatorPort.class)) {
            port.disconnectAll();
        }
        return this;
    }

    /**
     * Returns the properties of this operator.
     * @return the properties
     */
    public List<? extends OperatorProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    /**
     * Returns the operator inputs.
     * @return the operator inputs
     */
    public List<OperatorInput> getInputs() {
        return getProperties(OperatorInput.class);
    }

    /**
     * Returns the operator outputs.
     * @return the operator outputs
     */
    public List<OperatorOutput> getOutputs() {
        return getProperties(OperatorOutput.class);
    }

    /**
     * Returns the operator arguments.
     * @return the operator arguments
     */
    public List<OperatorArgument> getArguments() {
        return getProperties(OperatorArgument.class);
    }

    /**
     * Returns the operator constraints.
     * @return the operator constraints
     */
    public Set<OperatorConstraint> getConstraints() {
        return Collections.unmodifiableSet(constraints);
    }

    /**
     * Returns an operator input.
     * @param name the input name
     * @return the found element, or {@code null} it is not found
     */
    public OperatorInput findInput(String name) {
        return findProperty(OperatorInput.class, name);
    }

    /**
     * Returns an operator output.
     * @param name the output name
     * @return the found element, or {@code null} it is not found
     */
    public OperatorOutput findOutput(String name) {
        return findProperty(OperatorOutput.class, name);
    }

    /**
     * Returns an operator argument.
     * @param name the argument name
     * @return the found element, or {@code null} it is not found
     */
    public OperatorArgument findArgument(String name) {
        return findProperty(OperatorArgument.class, name);
    }

    private <T> T findProperty(Class<T> type, String name) {
        for (OperatorProperty property : getProperties()) {
            if (property.getName().equals(name) && type.isInstance(property)) {
                return type.cast(property);
            }
        }
        return null;
    }

    private <T> List<T> getProperties(Class<T> type) {
        List<T> results = new ArrayList<>();
        for (OperatorProperty property : getProperties()) {
            if (type.isInstance(property)) {
                results.add(type.cast(property));
            }
        }
        return results;
    }

    /**
     * Represents a kind of {@link Operator}.
     */
    public enum OperatorKind {

        /**
         * Core operators.
         * @see CoreOperator
         */
        CORE,

        /**
         * User operators.
         * @see UserOperator
         */
        USER,

        /**
         * Nested operators.
         * @see FlowOperator
         */
        FLOW,

        /**
         * External inputs.
         * @see ExternalInput
         */
        INPUT,

        /**
         * External outputs.
         * @see ExternalOutput
         */
        OUTPUT,
    }

    /**
     * Builds an operator.
     * @param <TOperator> the operator type
     * @param <TSelf> the actual builder type
     */
    public static abstract class AbstractBuilder<TOperator extends Operator, TSelf> {

        private final TOperator owner;

        /**
         * Creates a new instance.
         * @param owner the target operator
         */
        protected AbstractBuilder(TOperator owner) {
            this.owner = owner;
        }

        /**
         * Returns this object.
         * @return this
         */
        protected abstract TSelf getSelf();

        /**
         * Returns the built operator.
         * @return the built operator
         */
        public TOperator build() {
            return owner;
        }

        /**
         * Adds an input port to the building operator.
         * @param name the port name
         * @param dataType the data type on the port
         * @param upstreams the optional upstream ports to connect to the created input
         * @return this
         */
        public TSelf input(String name, TypeDescription dataType, OperatorOutput... upstreams) {
            return input(name, dataType, null, upstreams);
        }

        /**
         * Adds an input port to the building operator.
         * @param name the port name
         * @param dataType the data type on the port
         * @param group the grouping instruction
         * @param upstreams the optional upstream ports to connect to the created input
         * @return this
         */
        public TSelf input(String name, TypeDescription dataType, Group group, OperatorOutput... upstreams) {
            OperatorInput port = new OperatorInput(owner, name, dataType, group);
            owner.properties.add(port);
            for (OperatorOutput upstream : upstreams) {
                port.connect(upstream);
            }
            return getSelf();
        }

        /**
         * Adds an input port to the building operator.
         * @param name the port name
         * @param upstream the mandatory upstream port to connect to the created input
         * @param upstreams the optional upstream ports to connect to the created input
         * @return this
         */
        public TSelf input(String name, OperatorOutput upstream, OperatorOutput... upstreams) {
            return input(name, null, upstream, upstreams);
        }

        /**
         * Adds an input port to the building operator.
         * @param name the port name
         * @param group the grouping instruction
         * @param upstream the mandatory upstream port to connect to the created input
         * @param upstreams the optional upstream ports to connect to the created input
         * @return this
         */
        public TSelf input(String name, Group group, OperatorOutput upstream, OperatorOutput... upstreams) {
            OperatorOutput[] joint = new OperatorOutput[upstreams.length + 1];
            joint[0] = upstream;
            System.arraycopy(upstreams, 0, joint, 1, upstreams.length);
            return input(name, upstream.getDataType(), group, joint);
        }

        /**
         * Adds an output port to the building operator.
         * @param name the port name
         * @param dataType the data type on the port
         * @return this
         */
        public TSelf output(String name, TypeDescription dataType) {
            owner.properties.add(new OperatorOutput(owner, name, dataType));
            return getSelf();
        }

        /**
         * Adds an argument to the building operator.
         * @param name the argument name
         * @param value the argument value
         * @return this
         */
        public TSelf argument(String name, ValueDescription value) {
            owner.properties.add(new OperatorArgument(name, value));
            return getSelf();
        }

        /**
         * Adds constraints to the building operator.
         * @param constraints the constraints
         * @return this
         */
        public TSelf constraint(Collection<OperatorConstraint> constraints) {
            owner.constraints.addAll(constraints);
            return getSelf();
        }

        /**
         * Adds constraints to the building operator.
         * @param constraints the constraints
         * @return this
         */
        public TSelf constraint(OperatorConstraint... constraints) {
            Collections.addAll(owner.constraints, constraints);
            return getSelf();
        }
    }
}
