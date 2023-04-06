import app.Chord;
import app.Circle;
import app.Task;
import misc.CoordinateSystem2d;
import misc.Vector2d;
import org.junit.Test;

import java.util.ArrayList;


/**
 * Класс тестирования
 */
public class UnitTest {

    /**
     * Тест
     *
     * @param circles        список окружностей
     * @param circle1        первая окружность решения
     * @param circle2        вторая окружность решения
     * @param max_chord      хорда решения
     */
    private static void test(ArrayList<Circle> circles, Circle circle1, Circle circle2, Chord max_chord) {
        Task task = new Task(new CoordinateSystem2d(10, 10, 20, 20), circles);
        task.solve();
        // Проверяем, что решение правильное
        assert ((circle1 == task.getCircle1() && circle2 == task.getCircle2()) || (circle1 == task.getCircle2() && circle2 == task.getCircle1()));
        assert (max_chord == task.getMax_chord());

/*
        // проверяем, что координаты хорды принадлежат окружностям решения
        assert Math.pow(max_chord.pos1.x-circle1.center.x,2)+Math.pow(max_chord.pos1.y-circle1.center.y,2) == Math.pow(circle1.radius,2);
        assert Math.pow(max_chord.pos1.x-circle2.center.x,2)+Math.pow(max_chord.pos1.y-circle2.center.y,2) == Math.pow(circle2.radius,2);
        assert Math.pow(max_chord.pos2.x-circle1.center.x,2)+Math.pow(max_chord.pos2.y-circle1.center.y,2) == Math.pow(circle1.radius,2);
        assert Math.pow(max_chord.pos2.x-circle2.center.x,2)+Math.pow(max_chord.pos2.y-circle2.center.y,2) == Math.pow(circle2.radius,2);
*/





        //!!!!!!!!!!!!! добавить каких-нибудь проверок

    }

//!!!!!!!!!!!!! найти решение нескольких вариантов
    //Пока тест неверный
    /**
     * Первый тест
     */
    @Test
    public void test1() {
        ArrayList<Circle> circles = new ArrayList<>();

        circles.add(new Circle(new Vector2d(1, 1), 10));
        circles.add(new Circle(new Vector2d(-1, 1), 10));
        circles.add(new Circle(new Vector2d(-1, -1), 5));
        circles.add(new Circle(new Vector2d(2, 1), 3));
        circles.add(new Circle(new Vector2d(1, 2), 5));
        circles.add(new Circle(new Vector2d(2, 2), 1));

        Circle circle1 = new Circle(new Vector2d(1,1),10);
        Circle circle2 = new Circle(new Vector2d(-1,1),10);
        Chord max_chord = new Chord(new Vector2d(1,1), new Vector2d(-1,1));

        test(circles, circle1, circle2, max_chord);
    }
}


