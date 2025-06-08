package backend.logic;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Adapter class for converting between {@link LocalDate} and {@link String}
 * during XML marshalling and unmarshalling.
 *
 * This adapter ensures that {@code LocalDate} values are correctly formatted
 * using the ISO-8601 date format (yyyy-MM-dd) when serialized to XML,
 * and properly parsed back when deserialized.
 *
 * Used by JAXB for mapping XML string values to Java {@code LocalDate} objects.
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Converts a date string from XML into a {@link LocalDate} object.
     *
     * @param v The date string in ISO-8601 format.
     * @return A {@link LocalDate} parsed from the input string.
     * @throws Exception If the input cannot be parsed.
     */
    @Override
    public LocalDate unmarshal(String v) throws Exception {
        return LocalDate.parse(v, formatter);
    }

    /**
     * Converts a {@link LocalDate} object into a string suitable for XML output.
     *
     * @param v The {@link LocalDate} to format.
     * @return The formatted date string, or {@code null} if the input is {@code null}.
     * @throws Exception If the date cannot be formatted.
     */
    @Override
    public String marshal(LocalDate v) throws Exception {
        return v != null ? v.format(formatter) : null;
    }
}