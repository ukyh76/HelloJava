package com.campus.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 教学项目使用的最小 JSON 编解码器，只处理本项目 API 所需的数据类型。
 */
final class Json {
    private Json() {
    }

    static Map<String, Object> parseObject(String source) {
        Object value = new Parser(source).parse();
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("JSON 根节点必须是对象");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, item) -> result.put(String.valueOf(key), item));
        return result;
    }

    static String stringify(Object value) {
        StringBuilder json = new StringBuilder();
        write(json, value);
        return json.toString();
    }

    private static void write(StringBuilder json, Object value) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof String || value instanceof Character || value instanceof Enum<?>) {
            quote(json, value.toString());
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value);
        } else if (value instanceof Map<?, ?> map) {
            json.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) json.append(',');
                first = false;
                quote(json, String.valueOf(entry.getKey()));
                json.append(':');
                write(json, entry.getValue());
            }
            json.append('}');
        } else if (value instanceof Iterable<?> iterable) {
            json.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) json.append(',');
                first = false;
                write(json, item);
            }
            json.append(']');
        } else {
            quote(json, value.toString());
        }
    }

    private static void quote(StringBuilder json, String value) {
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
                }
            }
        }
        json.append('"');
    }

    private static final class Parser {
        private final String source;
        private int position;

        private Parser(String source) {
            this.source = source == null ? "" : source;
        }

        private Object parse() {
            skipWhitespace();
            Object value = readValue();
            skipWhitespace();
            if (position != source.length()) {
                throw error("JSON 结尾存在多余内容");
            }
            return value;
        }

        private Object readValue() {
            skipWhitespace();
            if (position >= source.length()) throw error("JSON 内容不完整");
            return switch (source.charAt(position)) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", true);
                case 'f' -> readLiteral("false", false);
                case 'n' -> readLiteral("null", null);
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject() {
            Map<String, Object> result = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();
            if (take('}')) return result;
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                result.put(key, readValue());
                skipWhitespace();
                if (take('}')) return result;
                expect(',');
            }
        }

        private List<Object> readArray() {
            List<Object> result = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (take(']')) return result;
            while (true) {
                result.add(readValue());
                skipWhitespace();
                if (take(']')) return result;
                expect(',');
            }
        }

        private String readString() {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (position < source.length()) {
                char character = source.charAt(position++);
                if (character == '"') return result.toString();
                if (character != '\\') {
                    result.append(character);
                    continue;
                }
                if (position >= source.length()) throw error("字符串转义不完整");
                char escaped = source.charAt(position++);
                switch (escaped) {
                    case '"', '\\', '/' -> result.append(escaped);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> {
                        if (position + 4 > source.length()) throw error("Unicode 转义不完整");
                        result.append((char) Integer.parseInt(source.substring(position, position + 4), 16));
                        position += 4;
                    }
                    default -> throw error("不支持的字符串转义");
                }
            }
            throw error("字符串缺少结束引号");
        }

        private Object readNumber() {
            int start = position;
            if (take('-')) { }
            while (position < source.length() && Character.isDigit(source.charAt(position))) position++;
            boolean decimal = false;
            if (take('.')) {
                decimal = true;
                while (position < source.length() && Character.isDigit(source.charAt(position))) position++;
            }
            if (position < source.length() && (source.charAt(position) == 'e' || source.charAt(position) == 'E')) {
                decimal = true;
                position++;
                if (position < source.length() && (source.charAt(position) == '+' || source.charAt(position) == '-')) position++;
                while (position < source.length() && Character.isDigit(source.charAt(position))) position++;
            }
            if (start == position) throw error("无法识别 JSON 值");
            String number = source.substring(start, position);
            return decimal ? Double.parseDouble(number) : Long.parseLong(number);
        }

        private Object readLiteral(String literal, Object value) {
            if (!source.startsWith(literal, position)) throw error("无法识别 JSON 值");
            position += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (position < source.length() && Character.isWhitespace(source.charAt(position))) position++;
        }

        private boolean take(char expected) {
            if (position < source.length() && source.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (!take(expected)) throw error("期待字符 " + expected);
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + "，位置：" + position);
        }
    }
}
