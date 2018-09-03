package ch.jalu.configme.beanmapper.leafvaluehandler;

import ch.jalu.configme.utils.TypeInformation;

import javax.annotation.Nullable;

/**
 * A leaf value handler is used in {@link ch.jalu.configme.beanmapper.MapperImpl} to get "simple" values from their
 * read values to the desired type.
 * <p>
 * The bean mapper handles complex types such as maps and collections, and recursively calls the bean mapping process
 * accordingly. This class complements the mapper by halting this process and providing "leaf values" to be set into
 * Java beans.
 * <p>
 * Default implementation is provided by {@link StandardLeafValueHandlers#getDefaultLeafValueHandler}.
 */
public interface LeafValueHandler {

    /**
     * Converts the given value to the given class, if possible. Returns null otherwise.
     * This method <b>must</b> either return {@code null} or an object of the class type.
     *
     * @param typeInformation the required type
     * @param value the value to get
     * @return value of the given type, or null if not applicable
     */
    @Nullable
    Object convert(TypeInformation typeInformation, @Nullable Object value);

    /**
     * Converts the given value to a type more suitable for exporting. Used by the mapper in
     * when {@link ch.jalu.configme.properties.Property#toExportValue} is called on a bean property.
     * Returns null if the leaf value handler cannot handle the value.
     *
     * @param value the value to get to an export value, if possible
     * @return value to use in the export, or null if not applicable
     */
    @Nullable
    Object toExportValue(@Nullable Object value);

}
