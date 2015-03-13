package com.asakusafw.lang.compiler.model.description;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Represents a serializable object.
 */
public class SerializableValueDescription implements ValueDescription {

    private final ReifiableTypeDescription valueType;

    private final byte[] serialized;

    /**
     * Creates a new instance.
     * @param valueType the original value type
     * @param serialized the serialized object
     */
    public SerializableValueDescription(ReifiableTypeDescription valueType, byte[] serialized) {
        this.valueType = valueType;
        this.serialized = serialized;
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static SerializableValueDescription of(Object value) {
        Class<?> type = value.getClass();
        byte[] bytes;
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(buffer)) {
            output.writeObject(value);
            output.close();
            bytes = buffer.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "failed to serialize a value: {0}", //$NON-NLS-1$
                    value));
        }
        return new SerializableValueDescription(ReifiableTypeDescription.of(type), bytes);
    }

    @Override
    public ValueKind getValueKind() {
        return ValueKind.SERIALIZABLE;
    }

    @Override
    public ReifiableTypeDescription getValueType() {
        return valueType;
    }

    /**
     * Returns the serialized object.
     * @return the serialized object
     */
    public byte[] getSerialized() {
        return serialized.clone();
    }

    @Override
    public Object resolve(ClassLoader classLoader) throws ReflectiveOperationException {
        try (InputStream buffer = new ByteArrayInputStream(serialized);
                ObjectInputStream input = new ResolveObjectInputStream(buffer, classLoader)) {
            return input.readObject();
        } catch (IOException e) {
            throw new ReflectiveOperationException(MessageFormat.format(
                    "failed to deserialize a value: {0}", //$NON-NLS-1$
                    this), e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + valueType.hashCode();
        result = prime * result + Arrays.hashCode(serialized);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SerializableValueDescription other = (SerializableValueDescription) obj;
        if (!valueType.equals(other.valueType)) {
            return false;
        }
        if (!Arrays.equals(serialized, other.serialized)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Serialized({0})", //$NON-NLS-1$
                valueType);
    }

    private static class ResolveObjectInputStream extends ObjectInputStream {

        private final ClassLoader loader;

        public ResolveObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
            super(in);
            this.loader = loader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            if (name.startsWith("[")) { //$NON-NLS-1$
                return resolveClassDesc(desc);
            }
            try {
                Class<?> loaded = loader.loadClass(name);
                return loaded;
            } catch (ClassNotFoundException e) {
                // may be primitive classes
                return super.resolveClass(desc);
            }
        }

        private Class<?> resolveClassDesc(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            assert desc != null;
            String name = desc.getName();
            int dimensions = 0;
            for (int i = 0, n = name.length(); i < n; i++) {
                if (name.charAt(i) == '[') {
                    dimensions++;
                } else {
                    break;
                }
            }
            // invalid descriptor
            if (name.length() == dimensions) {
                return super.resolveClass(desc);
            }
            // not "L...;"
            if (name.charAt(dimensions) != 'L' || name.charAt(name.length() - 1) != ';') {
                return super.resolveClass(desc);
            }
            String internalName = name.substring(dimensions + 1, name.length() - 1);
            Class<?> loaded = loader.loadClass(internalName.replace('/', '.'));
            for (int i = 0; i < dimensions; i++) {
                loaded = Array.newInstance(loaded, 0).getClass();
            }
            return loaded;
        }
    }
}
