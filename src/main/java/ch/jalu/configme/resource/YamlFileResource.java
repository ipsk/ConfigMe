package ch.jalu.configme.resource;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.exception.ConfigMeException;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.resource.PropertyPathTraverser.PathElement;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class YamlFileResource implements PropertyResource {

    private static final String INDENTATION = "    ";

    private final File file;
    private final IntUnaryOperator indentFunction;
    private Yaml yamlObject;

    public YamlFileResource(File file) {
        this(file, null);
    }

    public YamlFileResource(File file, @Nullable IntUnaryOperator indentFunction) {
        this.file = file;
        this.indentFunction = indentFunction;
    }

    @Override
    public PropertyReader createReader() {
        return new YamlFileReader(file, getCharset());
    }

    @Override
    public void exportProperties(ConfigurationData configurationData) {
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fos, getCharset())) {
            PropertyPathTraverser pathTraverser = new PropertyPathTraverser(configurationData);
            for (Property<?> property : configurationData.getProperties()) {
                final Object exportValue = getExportValue(property, configurationData);
                exportValue(writer, pathTraverser, property.getPath(), exportValue);
            }
            writer.append("\n");
            writer.flush();
        } catch (IOException e) {
            throw new ConfigMeException("Could not save config to '" + file.getPath() + "'", e);
        } finally {
            onWriteComplete();
        }
    }

    protected final File getFile() {
        return file;
    }

    /**
     * Exports the given value at the provided path.
     *
     * @param writer the file writer to write with
     * @param pathTraverser the path traverser (e.g. keeps track of which path elements are new)
     * @param path the path to export at
     * @param value the value to export
     * @throws IOException .
     */
    protected void exportValue(OutputStreamWriter writer, PropertyPathTraverser pathTraverser,
                               String path, Object value) throws IOException {
        if (value == null) {
            return;
        }

        if (value instanceof Map<?, ?> && !((Map) value).isEmpty()) {
            final String pathPrefix = path.isEmpty() ? "" : path + ".";

            for (Map.Entry<String, ?> entry : ((Map<String, ?>) value).entrySet()) {
                exportValue(writer, pathTraverser, pathPrefix + entry.getKey(), entry.getValue());
            }
        } else {
            List<PathElement> pathElements = pathTraverser.getPathElements(path);

            for (int i = 0; i < pathElements.size(); i++) {
                PathElement pathElement = pathElements.get(i);

                if (i == 0) {
                    writeIndentingBetweenLines(writer, pathElement.getIndentationLevel());
                }

                writeComments(writer, pathElement.getIndentationLevel(), pathElement.getComments());
                writer.append(this.getNewLineCheckingFileLength())
                    .append(indent(pathElement.getIndentationLevel()))
                    .append(pathElement.getName())
                    .append(":");
                writer.flush();
            }

            writer.append(" ")
                .append(toYamlIndented(value, pathElements.get(pathElements.size() - 1).getIndentationLevel()));
            writer.flush();
        }
    }

    /**
     * Writes the given comment lines as YAML comments at the given indentation level.
     *
     * @param writer the writer to write with
     * @param indentation the level at which the comment lines should be indented
     * @param comments the comment lines to write
     * @throws IOException .
     */
    protected void writeComments(OutputStreamWriter writer, int indentation, List<String> comments) throws IOException {
        if (comments.isEmpty()) {
            return;
        }
        String commentStart = indent(indentation) + "# ";

        for (int i = 0; i < comments.size(); i++) {
            writer.append(i == 0 ? getNewLineCheckingFileLength() : "\n").append(commentStart).append(comments.get(i));
        }

        writer.flush();
    }

    private void writeIndentingBetweenLines(Writer writer, int indent) throws IOException {
        if (indentFunction == null || isEmptyFile()) {
            return;
        }

        for (int i = 0; i < indentFunction.applyAsInt(indent); i++) {
            writer.append("\n");
        }
    }

    private String getNewLineCheckingFileLength() {
        return this.isEmptyFile() ? "" : "\n";
    }

    private boolean isEmptyFile() {
        return file.length() == 0;
    }

    /**
     * Returns the value in its YAML representation with an indentation of the given level. Proper indentation
     * should be applied to all lines except for the first one (such that this method's return value can simply
     * be appended to a properly indented property prefix like {@code name:}).
     *
     * @param value the value to convert to YAML
     * @param indent level of indentation to use
     * @return the value as YAML at the given indentation level
     */
    protected String toYamlIndented(@Nullable Object value, int indent) {
        String representation = toYaml(value);
        String[] lines = representation.split("\\n");
        return String.join("\n" + indent(indent), lines);
    }

    /**
     * Returns the YAML representation for the given value (belonging to the given value).
     * This method returns the YAML representation of the value only (does not include the key)
     * with no indentation (will be applied afterwards with the appropriate level).
     *
     * @param value the value to transform as YAML
     * @return the YAML representation of the value
     */
    protected String toYaml(@Nullable Object value) {
        if (value instanceof String) {
            return getYamlObject().dump(value);
        } else if (value instanceof Collection<?>) {
            List<?> list = collectionToList((Collection<?>) value);
            return list.isEmpty() ? "[]" : "\n" + getYamlObject().dump(list);
        } else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;

            return array.length == 0 ? "[]" : "\n" + getYamlObject().dump(array);
        }
        return getYamlObject().dump(value);
    }

    /**
     * Returns a String of whitespace for indentation in YAML at the given level.
     *
     * @param level the desired level of indentation
     * @return whitespace to prepend to a line for proper indentation
     */
    protected String indent(int level) {
        String result = "";
        for (int i = 0; i < level; i++) {
            result += INDENTATION;
        }
        return result;
    }

    /**
     * Called at the end of {@link #exportProperties}, regardless whether the execution was successful or not.
     */
    protected void onWriteComplete() {
        yamlObject = null;
    }

    /**
     * Returns the YAML instance with which values are converted to YAML.
     *
     * @return the YAML instance to use
     */
    protected Yaml getYamlObject() {
        if (yamlObject == null) {
            yamlObject = createNewYaml();
        }
        return yamlObject;
    }

    protected Yaml createNewYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        return new Yaml(options);
    }

    protected Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    private <T> Object getExportValue(Property<T> property, ConfigurationData configurationData) {
        return property.toExportValue(configurationData.getValue(property));
    }

    private static List<?> collectionToList(Collection<?> collection) {
        return collection instanceof List<?>
            ? (List<?>) collection
            : new ArrayList<>(collection);
    }
}
