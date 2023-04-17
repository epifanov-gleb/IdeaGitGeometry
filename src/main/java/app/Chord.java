package app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import misc.Vector2d;

import java.util.Objects;

/**
 * Класс точки
 */
public class Chord {
    /**
     * Координаты конца 1
     */
    public Vector2d pos1;
    /**
     * Координаты конца 2
     */
    public Vector2d pos2;

    /**
     * Конструктор хорды
     *
     * @param pos1     конец 1
     * @param pos2     конец 2
     */
    @JsonCreator
    public Chord(@JsonProperty("pos1") Vector2d pos1, @JsonProperty("pos2") Vector2d pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    /**
     * Обнулить хорду
     *
     * @return положение
     */
/*
    public void clear() {
        this.pos1 = new Vector2d(0, 0);
        this.pos2 = new Vector2d(0, 0);
    }
*/

    /**
     * Получить конец 1
     *
     * @return конец 1
     */
    public Vector2d getPos1() {
        return pos1;
    }

    /**
     * Получить конец 2
     *
     * @return конец 2
     */
    public Vector2d getPos2() {
        return pos2;
    }

    /**
     * Получить длину хорды
     *
     * @return положение
     */
    public double getLength() {
        Vector2d tmp = Vector2d.subtract(pos1,pos2);
        return tmp.length();
    }

    /**
     * Строковое представление объекта
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Chord{" +
                "pos1=" + pos1 +
                ", pos2=" + pos2 +
                "}, длина " + String.format("%.2f", getLength()).replace(",", ".");
    }

    /**
     * Проверка двух объектов на равенство
     *
     * @param o объект, с которым сравниваем текущий
     * @return флаг, равны ли два объекта
     */
    @Override
    public boolean equals(Object o) {
        // если объект сравнивается сам с собой, тогда объекты равны
        if (this == o) return true;
        // если в аргументе передан null или классы не совпадают, тогда объекты не равны
        if (o == null || getClass() != o.getClass()) return false;
        // приводим переданный в параметрах объект к текущему классу
        Chord chord = (Chord) o;
        return (Objects.equals(pos1, chord.pos1) && Objects.equals(pos2, chord.pos2)) ||  (Objects.equals(pos2, chord.pos1) && Objects.equals(pos1, chord.pos2));
    }

    /**
     * Получить хэш-код объекта
     *
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(pos1, pos2);
    }
}
