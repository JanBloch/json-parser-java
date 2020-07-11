public class JSONString implements JSONValue{
    private String str;
    public JSONString(String str){
        this.str = str;
    }
    public String getString(){
        return str;
    }
    public void setString(String value){
        str = value;
    }
    public String toString(){
        return "\"" + JSONObject.escape(getString()) + "\"";
    }
    public String getFormattedJSONString(){
        return this.toString();
    }
}
