package com.autoshield.util;

import java.util.List;

/**
 * Small helper for building CSV content without pulling in an external
 * library — handles quoting/escaping fields that contain commas, quotes,
 * or newlines per the standard CSV rules.
 */
/**
 * Provides the CsvUtil functionality.
 */
public class CsvUtil {
/**
 * Executes the escape operation.
 */

    public static String escape(Object value) {
        if (value == null) return "";
        String s = value.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
/**
 * Executes the buildCsv operation.
 */

    public static String buildCsv(List<String> headers, List<List<Object>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");
        for (List<Object> row : rows) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) line.append(",");
                line.append(escape(row.get(i)));
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}