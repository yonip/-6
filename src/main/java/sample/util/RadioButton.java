package sample.util;

/**
 * Created by yonipedersen on 5/10/16.
 */
public class RadioButton<T> extends javafx.scene.control.RadioButton {
    private T obj;

    public RadioButton(T obj) {
        super(obj.getClass().getSimpleName());
        this.obj = obj;
    }

    public RadioButton(T obj, String name) {
        super(name);
        this.obj = obj;
    }

    public T get() {
        return obj;
    }
}
