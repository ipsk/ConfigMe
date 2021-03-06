package ch.jalu.configme.properties;

import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.resource.PropertyReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.jalu.configme.TestUtils.isErrorValueOf;
import static ch.jalu.configme.TestUtils.isValidValueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

/**
 * Test for {@link DoubleProperty}.
 */
@ExtendWith(MockitoExtension.class)
class DoublePropertyTest {

    @Mock
    private PropertyReader reader;

    @Test
    void shouldReturnDoubleFromResource() {
        // given
        Property<Double> property = new DoubleProperty("test.path", 3.4);
        given(reader.getObject("test.path")).willReturn(-2508.346);

        // when
        PropertyValue<Double> result = property.determineValue(reader);

        // then
        assertThat(result, isValidValueOf(-2508.346));
    }

    @Test
    void shouldReturnDefaultValue() {
        // given
        Property<Double> property = new DoubleProperty("property.path", 5.9);
        given(reader.getObject("property.path")).willReturn(null);

        // when
        PropertyValue<Double> result = property.determineValue(reader);

        // then
        assertThat(result, isErrorValueOf(5.9));
    }

    @Test
    void shouldReturnValueAsExportValue() {
        // given
        Property<Double> property = new DoubleProperty("property.path", 4);
        double givenValue = 4.3456;

        // when
        Object exportValue = property.toExportValue(givenValue);

        // then
        assertThat(exportValue, equalTo(givenValue));
    }
}
