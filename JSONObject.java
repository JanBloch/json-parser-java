import java.util.ArrayList;
import java.util.HashMap;

public class JSONObject implements JSONValue {

    private HashMap<String, JSONValue> properties = new HashMap<String, JSONValue>();

    public JSONObject(String code) throws InvalidSyntaxException {
        parse(code);
    }

    public JSONObject getObject(String key) throws PropertyNotFoundException {
        if (!properties.containsKey(key))
            throw new PropertyNotFoundException("Property [" + key + "] not found");
        if (properties.get(key) instanceof JSONString)
            return (JSONObject) properties.get(key);
        else throw new PropertyNotFoundException("Property [" + key + "] not of type object");
    }

    public JSONString getString(String key) throws PropertyNotFoundException {
        if (!properties.containsKey(key))
            throw new PropertyNotFoundException("Property [" + key + "] not found");
        if (properties.get(key) instanceof JSONString)
            return (JSONString) properties.get(key);
        else throw new PropertyNotFoundException("Property [" + key + "] not of type string");
    }

    public JSONValue get(String key) throws PropertyNotFoundException {
        if (properties.containsKey(key)) {
            return properties.get(key);
        } else throw new PropertyNotFoundException("Property [" + key + "] not found");
    }

    public int propertyCount() {
        return properties.size();
    }

    public String[] keys() {
        Object[] keys = this.properties.keySet().toArray();
        String[] out = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            out[i] = (String) keys[i];
        }
        return out;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < this.keys().length; i++) {
            builder.append('"')
                    .append(this.keys()[i])
                    .append('"')
                    .append(": ")
                    .append(this.properties.get(this.keys()[i]).toString());
            if (i < this.keys().length - 1)
                builder.append(",");
        }
        builder.append("}");
        return builder.toString();
    }

    public String getFormattedJSONString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < this.keys().length; i++) {
            builder.append('"')
                    .append(this.keys()[i])
                    .append('"')
                    .append(": ")
                    .append(this.properties.get(this.keys()[i]).getFormattedJSONString());
            if (i < this.keys().length - 1)
                builder.append(",\n");
        }
        builder.append("\n}");
        return builder.toString();
    }

    private void parse(String code) throws InvalidSyntaxException {
        code = code.trim();
        code = code.substring(1, code.length() - 1);
        code = code.replace("\n", "");
        code = code.replace("\r", "");
        boolean stringOpen = false;
        int level = 0;
        int lastIndex = -1;

        HashMap<String, String> properties = new HashMap<String, String>();

        for (int i = 0; i < code.length(); i++) {
            char chr = code.charAt(i);
            switch (chr) {
                case '"':
                    if ((i > 0 && code.charAt(i - 1) != '\\') || i == 0)
                        stringOpen = !stringOpen;
                    break;
                case '{':
                case '[':
                    if (!stringOpen)
                        level++;
                    break;
                case '}':
                case ']':
                    if (!stringOpen)
                        level--;
                    break;
                case ',':
                    if (!stringOpen && level == 0) {
                        String[] split = code.substring(lastIndex + 1, i).trim().split(":", 2);
                        if (split.length == 1)
                            throw new InvalidSyntaxException("Missing value for property [" + split[0] + "]");
                        if (split[0].charAt(0) == '"' && split[0].charAt(split[0].length() - 1) == '"') {
                            split[0] = split[0].trim();
                            split[0] = split[0].substring(1, split[0].length() - 1);
                        }
                        properties.put(split[0], split[1]);
                        lastIndex = i;
                    }
                    break;
            }
        }
        if (lastIndex != code.length() - 1) {
            String[] split = code.substring(lastIndex + 1).trim().split(":", 2);
            if (split.length == 1)
                throw new InvalidSyntaxException("Missing value for property [" + split[0] + "]");
            if (split[0].charAt(0) == '"' && split[0].charAt(split[0].length() - 1) == '"') {
                split[0] = split[0].trim();
                split[0] = split[0].substring(1, split[0].length() - 1);
            }
            properties.put(split[0], split[1]);
        }

        Object[] keys = properties.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            properties.put((String) keys[i], properties.get(keys[i]).trim());
            String valueString = properties.get(keys[i]);
            valueString = unescape(valueString);
            JSONValue value = new JSONString("");
            char startChar = valueString.charAt(0);
            char endChar = valueString.charAt(valueString.length() - 1);
            if (startChar == '{' && endChar == '}')
                value = new JSONObject(valueString);
            else if (startChar == '"' && endChar == '"')
                value = new JSONString(valueString.substring(1, valueString.length() - 1));
            else if (startChar == '[' && endChar == ']')
                value = new JSONArray(valueString);
            else throw new InvalidSyntaxException();
            this.properties.put((String) keys[i], value);
        }

    }

    public static String escape(String str) {
        return str.replace("\n", "\\n")
                .replace("\"", "\\\"")
                .replace("\t", "\\t");
    }

    public static String unescape(String str) {
        return str.replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\t", "\t");
    }
}
