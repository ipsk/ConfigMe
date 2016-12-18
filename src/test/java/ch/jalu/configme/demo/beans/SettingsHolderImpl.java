package ch.jalu.configme.demo.beans;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newBeanProperty;

/**
 * Holds the settings we want to use.
 */
public final class SettingsHolderImpl implements SettingsHolder {

    public static final Property<UserBase> USER_BASE =
        newBeanProperty(UserBase.class, "userdata", new UserBase());

    public static final Property<Country> COUNTRY =
        newBeanProperty(Country.class, "country", new Country());

    private SettingsHolderImpl() {
    }
}