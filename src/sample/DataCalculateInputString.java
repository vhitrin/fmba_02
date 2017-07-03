package sample;

import javafx.beans.property.*;

public class DataCalculateInputString {

    private final StringProperty P1;
    private final FloatProperty V5;
    private final FloatProperty V6;
    private final FloatProperty V7;
    private final FloatProperty V8;
    private final FloatProperty V9;
    private final FloatProperty V10;

    public DataCalculateInputString(String p1, float v5, float v6, float v7, float v8, float v9, float v10) {
        this.P1 = new SimpleStringProperty(p1);
        this.V5 = new SimpleFloatProperty(v5);
        this.V6 = new SimpleFloatProperty(v6);
        this.V7 = new SimpleFloatProperty(v7);
        this.V8 = new SimpleFloatProperty(v8);
        this.V9 = new SimpleFloatProperty(v9);
        this.V10 = new SimpleFloatProperty(v10);
    }

    public String getP1() {
        return P1.get();
    }

    public StringProperty P1Property() {
        return P1;
    }

    public void setP1(String p1) {
        this.P1.set(p1);
    }

    public float getV5() {
        return V5.get();
    }

    public FloatProperty V5Property() {
        return V5;
    }

    public void setV5(float v5) {
        this.V5.set(v5);
    }

    public float getV6() {
        return V6.get();
    }

    public FloatProperty V6Property() {
        return V6;
    }

    public void setV6(float v6) {
        this.V6.set(v6);
    }

    public float getV7() {
        return V7.get();
    }

    public FloatProperty V7Property() {
        return V7;
    }

    public void setV7(float v7) {
        this.V7.set(v7);
    }

    public float getV8() {
        return V8.get();
    }

    public FloatProperty V8Property() {
        return V8;
    }

    public void setV8(float v8) {
        this.V8.set(v8);
    }

    public float getV9() {
        return V9.get();
    }

    public FloatProperty V9Property() {
        return V9;
    }

    public void setV9(float v9) {
        this.V9.set(v9);
    }

    public float getV10() {
        return V10.get();
    }

    public FloatProperty V10Property() {
        return V10;
    }

    public void setV10(float v10) {
        this.V10.set(v10);
    }
}
