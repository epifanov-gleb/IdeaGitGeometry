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
     * @param max_chord      хорда решения
     */
    private static void test(ArrayList<Circle> circles, Chord max_chord) {
        // ОСК от [-10.0,-10.0] до [10.0,10.0]
        CoordinateSystem2d cs = new CoordinateSystem2d(
                new Vector2d(-10.0, -10.0), new Vector2d(10.0, 10.0)
        );
        Task task = new Task(cs, circles);
        task.solve();
        // Проверяем, что решение правильное

      //  assert (max_chord == task.getMax_chord());

/*
        // проверяем, что координаты хорды принадлежат окружностям решения
        assert Math.pow(max_chord.pos1.x-circle1.center.x,2)+Math.pow(max_chord.pos1.y-circle1.center.y,2) == Math.pow(circle1.radius,2);
        assert Math.pow(max_chord.pos1.x-circle2.center.x,2)+Math.pow(max_chord.pos1.y-circle2.center.y,2) == Math.pow(circle2.radius,2);
        assert Math.pow(max_chord.pos2.x-circle1.center.x,2)+Math.pow(max_chord.pos2.y-circle1.center.y,2) == Math.pow(circle1.radius,2);
        assert Math.pow(max_chord.pos2.x-circle2.center.x,2)+Math.pow(max_chord.pos2.y-circle2.center.y,2) == Math.pow(circle2.radius,2);
*/





        //!!!!!!!!!!!!! добавить каких-нибудь проверок

    }

    /**
     * Первый тест
     */
    @Test
    public void test1() {
        ArrayList<Circle> circles = new ArrayList<>();

        circles.add(new Circle(new Vector2d(0, 0), 5));
        circles.add(new Circle(new Vector2d(5, 5), 3));
        circles.add(new Circle(new Vector2d(0, 0), 2));
        double tmp = Math.sqrt(43.56-37.12);
        Chord max_chord = new Chord(new Vector2d((6.6+tmp)/2,(6.6-tmp)/2), new Vector2d((6.6-tmp)/2,(6.6+tmp)/2));

        test(circles, max_chord);
    }
}


