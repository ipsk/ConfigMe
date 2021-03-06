package ch.jalu.configme;

import ch.jalu.configme.demo.beans.BeanPropertiesDemo;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test for {@link BeanPropertiesDemo}.
 */
class BeanDemoTest {

    @Test
    void shouldOutputExpectedText() {
        // given
        BeanPropertiesDemo beanDemo = new BeanPropertiesDemo();

        try {
            // Perform the actual test
            shouldOutputExpectedText(beanDemo);
        } finally {
            // Cleanup - delete the temporary file
            File file = beanDemo.getConfigFile();
            if (file != null) {
                file.delete();
            }
        }
    }

    private void shouldOutputExpectedText(BeanPropertiesDemo beanDemo) {
        // when
        String userInfo = beanDemo.generateUserInfo();

        // then
        String expectedText = "Saved locations of Richie: restaurant (47.5, 8.7), hospital (47.1, 8.8901)"
            + "\nNicknames of Bob: Bobby, Bobby boy"
            + "\nCountry 'Sweden' has neighbors: Norway, Finland, Denmark";
        assertThat(userInfo, equalTo(expectedText));
    }
}
