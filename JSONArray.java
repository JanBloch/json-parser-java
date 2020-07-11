import java.util.ArrayList;
import java.util.HashMap;

public class JSONArray implements JSONValue {

    private ArrayList<JSONValue> values = new ArrayList<JSONValue>();

    public JSONArray(String code) throws InvalidSyntaxException {
        parse(code);
    }

    public JSONObject getObject(int index) throws PropertyNotFoundException {
        if (index >= this.values.size())
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + this.values.size());
        if (values.get(index) instanceof JSONString)
            return (JSONObject) this.values.get(index);
        else throw new PropertyNotFoundException("Value at index " + index + " not of type string");
    }

    public JSONString getString(int index) throws PropertyNotFoundException {
        if (index >= this.values.size())
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + this.values.size());
        if (this.values.get(index) instanceof JSONString)
            return (JSONString) this.values.get(index);
        else throw new PropertyNotFoundException("Value at index " + index + " not of type string");
    }

    public JSONValue get(int index) throws PropertyNotFoundException {
        if (index < this.values.size())
            return this.values.get(index);
        else throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + this.values.size());
    }

    public int size() {
        return this.values.size();
    }

    private void parse(String code) throws InvalidSyntaxException {
        code = code.trim();
        code = code.substring(1, code.length() - 1);
        code = code.replace("\n", "");
        code = code.replace("\r", "");
        boolean stringOpen = false;
        int level = 0;
        int lastIndex = -1;

        ArrayList<String> values = new ArrayList<String>();
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
                        String val = code.substring(lastIndex + 1, i).trim();
                        values.add(val);
                        lastIndex = i;
                    }
                    break;
            }
        }
        if (lastIndex != code.length() - 1) {
            String val = code.substring(lastIndex + 1).trim();
            values.add(val);
        }

        for (int i = 0; i < values.size(); i++) {
            values.set(i, values.get(i).trim());
            String valueString = values.get(i);
            valueString = JSONObject.unescape(valueString);
            JSONValue value = new JSONString("");
            char startChar = valueString.charAt(0);
            char endChar = valueString.charAt(valueString.length() - 1);
            if (startChar == '{' && endChar == '}')
                value = new JSONObject(valueString);
            else if (startChar == '"' && endChar == '"')
                value = new JSONString(valueString.substring(1, valueString.length() - 1));
            else if (startChar == '[' && endChar == ']')
                value = new JSONArray(valueString.substring(1, valueString.length() - 1));
            else throw new InvalidSyntaxException();
            this.values.add(value);
        }

    }

    @Override
    public String getFormattedJSONString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < this.values.size(); i++) {
            sb.append(this.values.get(i).getFormattedJSONString());
            if (i < this.values.size() - 1)
                sb.append(",\n");
        }
        sb.append(']');
        return sb.toString();
    }
}
