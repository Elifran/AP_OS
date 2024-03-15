package aina.elifran.um5.ensam.ap_os;

import java.lang.reflect.Type;

public class data {
    private String Id;
    private Object Value;

    data(String id, Object value) {
        Id = id;
        Value = value;
    }
    data(String id) {
        Id = id;
    }
    data() {
    }
    public void setId(Object id){ Id = String.valueOf(id);}
    public void setValue(Object value){ Value = value;}
    public String getId() {
        return Id;
    }
    public Object getValue() {
        return Value;
    }
    public Type getType() {
        return Value.getClass();
    }
}
