package ch.jalu.configme.properties;

import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.properties.types.PrimitivePropertyType;
import ch.jalu.configme.resource.PropertyReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static ch.jalu.configme.TestUtils.isErrorValueOf;
import static ch.jalu.configme.TestUtils.isValidValueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;

/**
 * Test for {@link ArrayProperty}.
 */
@ExtendWith(MockitoExtension.class)
class ArrayPropertyTest {

    @Mock
    private PropertyReader reader;

    @Test
    void shouldReturnNullFornonCollectionTypes() {
        // given
        ArrayProperty<String> property = new ArrayProperty<>(
            "singleton",
            new String[] {"multiline", "message"},
            PrimitivePropertyType.STRING, String[]::new);

        given(reader.getObject("singleton")).willReturn("hello");

        // when
        String[] result = property.getFromReader(reader, null);

        // then
        assertThat(result, nullValue());
    }

    @Test
    void shouldHandleNull() {
        // given
        // given
        ArrayProperty<String> property = new ArrayProperty<>(
            "singleton",
            new String[] {"multiline", "message"},
            PrimitivePropertyType.STRING, String[]::new);

        given(reader.getObject("singleton")).willReturn(null);

        // when
        String[] result = property.getFromReader(reader, null);

        // then
        assertThat(result, nullValue());
    }

    @Test
    void shouldReturnArrayFromResource() {
        // given
        Property<String[]> property = new ArrayProperty<>(
            "array",
            new String[] {"multiline", "message"},
            PrimitivePropertyType.STRING, String[]::new);
        given(reader.getObject("array")).willReturn(Arrays.asList("qwerty", "123"));

        // when
        PropertyValue<String[]> result = property.determineValue(reader);

        // then
        assertThat(result, isValidValueOf(new String[] {"qwerty", "123"}));
    }

    @Test
    void shouldReturnDefaultValue() {
        // given
        Property<String[]> property = new ArrayProperty<>(
            "array",
            new String[] {"multiline", "message c:"},
            PrimitivePropertyType.STRING, String[]::new);

        given(reader.getObject("array")).willReturn(null);

        // when
        PropertyValue<String[]> result = property.determineValue(reader);

        // then
        assertThat(result, isErrorValueOf(new String[] {"multiline", "message c:"}));
    }

    @Test
    void shouldReturnValueAsExportValue() {
        // given
        Property<String[]> property = new ArrayProperty<>(
            "array",
            new String[] {},
            PrimitivePropertyType.STRING, String[]::new);

        String[] givenArray = new String[] {"hello, chert", "how in hell?"};

        // when / then
        assertThat(property.toExportValue(givenArray), equalTo(givenArray));
    }
}
