package app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.humbleui.jwm.MouseButton;
import io.github.humbleui.skija.*;
import lombok.Getter;
import misc.CoordinateSystem2d;
import misc.CoordinateSystem2i;
import misc.Vector2d;
import misc.Vector2i;
import panels.PanelControl;
import panels.PanelLog;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static app.Colors.*;

/**
 * Класс задачи
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Task {
    /**
     * Текст задачи
     */
    public static final String TASK_TEXT = """
            ПОСТАНОВКА ЗАДАЧИ:
            На плоскости задано множество окружностей.
            Найти такую пару пересекающихся окружностей,
            что длина отрезка, проведенного от одной точки
            пересечения этих двух окружностей до другой,
            максимальна. В качестве ответа: выделить эту
            пару окружностей, нарисовать отрезок между
            найденными точками пересечения.""";
    /**
     * Вещественная система координат задачи
     */
    @Getter
    private final CoordinateSystem2d ownCS;

    /**
     * Список окружностей
     */
    @Getter
    private final ArrayList<Circle> circles;

    /**
     * Размер точки
     */
    private static final int POINT_SIZE = 1;

    /**
     * Первая окружность
     */
    @Getter
    @JsonIgnore
    private Circle circle1;

    /**
     * Вторая окружность
     */
    @Getter
    @JsonIgnore
    private Circle circle2;

    /**
     * Максимальная хорда
     */
    @Getter
    @JsonIgnore
    private Chord max_chord;

    /**
     * Запоминаем значение центра рисуемой окружности
     */
    @Getter
    @JsonIgnore
    private Vector2d new_center;

    /**
     * был ли задан центр рисуемой окружности
     */
    protected boolean last_new_center = false;

    /**
     * Задача
     *
     * @param ownCS   СК задачи
     * @param circles массив окружностей
     */
    @JsonCreator
    public Task(
            @JsonProperty("ownCS") CoordinateSystem2d ownCS,
            @JsonProperty("circles") ArrayList<Circle> circles
    ) {
        this.ownCS = ownCS;
        this.circles = circles;
/*
//!!!!!!!!!!! Проверить, как загружаются тесты
        this.circle1 = circle1;
        this.circle2 = circle2;
        this.max_chord = max_chord;
*/
    }

    /**
     * Порядок разделителя сетки, т.е. раз в сколько отсечек
     * будет нарисована увеличенная
     */
    private static final int DELIMITER_ORDER = 10;

    /**
     * последнее движение мыши
     */
    protected Vector2i lastMove = new Vector2i(0, 0);
    /**
     * было ли оно внутри панели
     */
    protected boolean lastInside = false;
    /**
     * коэффициент колёсика мыши
     */
    private static final float WHEEL_SENSITIVE = 0.001f;
    /**
     * последняя СК окна
     */
    protected CoordinateSystem2i lastWindowCS;

    /**
     * Флаг, есть ли пересечение
     */
    private boolean cross = false;

    /**
     * Флаг, решена ли задача
     */
    private boolean solved;

    /**
     * Очистить задачу
     */
    public void clear() {
        circles.clear();
        solved = false;
    }

    /**
     * Решить задачу
     */
    public void solve() {
        // очищаем результирующие окружности и хорду до 0
        circle1 = new Circle(new Vector2d(0, 0), 0);
        circle2 = new Circle(new Vector2d(0, 0), 0);
        max_chord = new Chord(new Vector2d(0, 0), new Vector2d(0, 0));
        cross = false;

        // перебираем окружности
        for (int i = 0; i < circles.size(); i++) {
            for (int j = i + 1; j < circles.size(); j++) {
                // сохраняем окружности
                Circle a = circles.get(i);
                Circle b = circles.get(j);
                // если окружности совпадают
                if (a.equals(b)) {
                    cross = true;
                    // сравниваем с сохранённой хордой
                    if (max_chord.getLength() <= a.radius * 2) {
                        max_chord = new Chord(new Vector2d(a.centre.x, a.centre.y - a.radius), new Vector2d(a.centre.x, a.centre.y + a.radius));
                        circle1 = a;
                        circle2 = b;
                        continue;
                    }
                }
                // вычисляем расстояние между центрами
                Vector2d tmp = Vector2d.subtract(a.centre, b.centre);
                // если окружности не касаются
                if (tmp.length() > a.radius + b.radius) {
                    continue;
                }
                // внешнее касание в одной точке
                if (tmp.length() == a.radius + b.radius) {
                    cross = true;
                    // сравниваем с сохранённой хордой
                    if (max_chord.getLength() < 0) {
                        max_chord = new Chord(Vector2d.sum(b.centre, Vector2d.mul(tmp, b.radius / (a.radius + b.radius))),
                                Vector2d.sum(b.centre, Vector2d.mul(tmp, b.radius / (a.radius + b.radius))));
                        circle1 = a;
                        circle2 = b;
                        continue;
                    }
                }
                // одна окружность лежит внутри другой
                Vector2d tmp_ab_c = Vector2d.subtract(a.centre, b.centre);
                if (tmp_ab_c.length() + Math.min(a.radius, b.radius) < Math.max(a.radius, b.radius)) {
                    continue;
                }
                // внутреннее касание в одной точке
                if (tmp_ab_c.length() + Math.min(a.radius, b.radius) == Math.max(a.radius, b.radius)) {
                    cross = true;
                    // сравниваем с сохранённой хордой
                    if (max_chord.getLength() < 0) {
                        Vector2d tmp_vector = (a.radius < b.radius) ? (Vector2d.subtract(a.centre, b.centre)) : (Vector2d.subtract(b.centre, a.centre));
                        max_chord = new Chord(Vector2d.mul(tmp_vector, (a.radius + b.radius + Math.min(a.radius, b.radius)) / (a.radius + b.radius)),
                                Vector2d.mul(tmp_vector, (a.radius + b.radius + Math.min(a.radius, b.radius)) / (a.radius + b.radius)));
                        circle1 = a;
                        circle2 = b;
                        continue;
                    }
                }
                // если пересекаются в двух точках (в остальных случаях)
                cross = true;
                double x_c2 = b.centre.x - a.centre.x;
                double y_c2 = b.centre.y - a.centre.y;
                double atmp = (Math.pow(a.radius, 2) + Math.pow(x_c2, 2) + Math.pow(y_c2, 2) - Math.pow(b.radius, 2)) / (2 * x_c2);
                double btmp = y_c2 / x_c2;
                double k1 = Math.pow(btmp, 2) + 1;
                double k2 = atmp * btmp * (-1);
                double k3 = Math.pow(atmp, 2) - Math.pow(a.radius, 2);
                double y_1 = (k2 * (-1) + Math.sqrt(Math.pow(k2, 2) - k1 * k3)) / k1;
                double y_2 = (k2 * (-1) - Math.sqrt(Math.pow(k2, 2) - k1 * k3)) / k1;
                double x_1 = atmp - btmp * y_1;
                double x_2 = atmp - btmp * y_2;
                double x1 = x_1 + a.centre.x;
                double x2 = x_2 + a.centre.x;
                double y1 = y_1 + a.centre.y;
                double y2 = y_2 + a.centre.y;
                // сравниваем с сохранённой хордой
                Chord tmpChord = new Chord(new Vector2d(x1, y1), new Vector2d(x2, y2));
                if (max_chord.getLength() < tmpChord.getLength()) {
                    max_chord = tmpChord;
                    circle1 = a;
                    circle2 = b;
                }
            }
        }
        // задача решена
        solved = true;
    }

    /**
     * Отмена решения задачи
     */
    public void cancel() {
        solved = false;
    }

    /**
     * проверка, решена ли задача
     *
     * @return флаг
     */
    public boolean isSolved() {
        return solved;
    }

    /**
     * проверка, есть ли пересечения
     *
     * @return флаг
     */
    public boolean isCross() {
        return cross;
    }
    /**
     * Получить положение курсора мыши в СК задачи
     *
     * @param x        координата X курсора
     * @param y        координата Y курсора
     * @param windowCS СК окна
     * @return вещественный вектор положения в СК задачи
     */
    @JsonIgnore
    public Vector2d getRealPos(int x, int y, CoordinateSystem2i windowCS) {
        return ownCS.getCoords(x, y, windowCS);
    }
    /**
     * Рисование курсора мыши
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     * @param font     шрифт
     * @param pos      положение курсора мыши
     */
    public void paintMouse(Canvas canvas, CoordinateSystem2i windowCS, Font font, Vector2i pos) {
        // создаём перо
        try (var paint = new Paint().setColor(TASK_GRID_COLOR)) {
            // сохраняем область рисования
            canvas.save();
            // рисуем перекрестие
            canvas.drawRect(Rect.makeXYWH(0, pos.y - 1, windowCS.getSize().x, 2), paint);
            canvas.drawRect(Rect.makeXYWH(pos.x - 1, 0, 2, windowCS.getSize().y), paint);
            // смещаемся немного для красивого вывода текста
            canvas.translate(pos.x + 3, pos.y - 5);
            // положение курсора в пространстве задачи (переворачиваем y)
            Vector2d realPos = getRealPos(pos.x, lastWindowCS.getMax().y - pos.y, lastWindowCS);
            // выводим координаты
            canvas.drawString(realPos.toString(), 0, 0, font, paint);
            // восстанавливаем область рисования
            canvas.restore();
        }
    }

    /**
     * Рисование сетки
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void renderGrid(Canvas canvas, CoordinateSystem2i windowCS) {
        // сохраняем область рисования
        canvas.save();
        // получаем ширину штриха (т.е. по факту толщину линии)
        float strokeWidth = 0.03f / (float) ownCS.getSimilarity(windowCS).y + 0.5f;
        // создаём перо соответствующей толщины
        try (var paint = new Paint().setMode(PaintMode.STROKE).setStrokeWidth(strokeWidth).setColor(TASK_GRID_COLOR)) {
            // перебираем все целочисленные отсчёты нашей СК по оси X
            for (int i = (int) (ownCS.getMin().x); i <= (int) (ownCS.getMax().x); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(i, 0, ownCS);
                // каждый DELIMITER_ORDER штрих увеличенного размера
                float strokeHeight = i % DELIMITER_ORDER == 0 ? 5 : 2;
                // рисуем вертикальный штрих (переворачиваем y)
                canvas.drawLine(windowPos.x, windowCS.getMax().y - windowPos.y, windowPos.x, windowCS.getMax().y - windowPos.y + strokeHeight, paint);
                canvas.drawLine(windowPos.x, windowCS.getMax().y - windowPos.y, windowPos.x, windowCS.getMax().y - windowPos.y - strokeHeight, paint);
            }
            // перебираем все целочисленные отсчёты нашей СК по оси Y
            for (int i = (int) (ownCS.getMin().y); i <= (int) (ownCS.getMax().y); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(0, i, ownCS);
                // каждый DELIMITER_ORDER штрих увеличенного размера
                float strokeHeight = i % DELIMITER_ORDER == 0 ? 5 : 2;
                // рисуем горизонтальный штрих
                canvas.drawLine(windowPos.x, windowCS.getMax().y - windowPos.y, windowPos.x + strokeHeight, windowCS.getMax().y - windowPos.y, paint);
                canvas.drawLine(windowPos.x, windowCS.getMax().y - windowPos.y, windowPos.x - strokeHeight, windowCS.getMax().y - windowPos.y, paint);
            }
        }
        // восстанавливаем область рисования
        canvas.restore();
    }


    /**
     * Клик мыши по пространству задачи
     *
     * @param pos         положение мыши
     * @param mouseButton кнопка мыши
     */
    public void click(Vector2i pos, MouseButton mouseButton) {
        if (lastWindowCS == null) return;
        // переворачиваем y
        Vector2i pos1 = new Vector2i(pos.x, lastWindowCS.getMax().y - pos.y);
        // получаем положение на экране
        Vector2d taskPos = ownCS.getCoords(pos1, lastWindowCS);
        // если левая кнопка, запоминаем центр (он будет прорисован) и ждём правую,
        // если снова левая, то обновляем значение центра (старый центр не будет прорисован, т.к. его нет в глобальной переменной),
        // если правая при известном центре - рисуем окружность,
        // если центр задан не был, то ничего не происходит
        if (mouseButton.equals(MouseButton.PRIMARY)) {
            new_center = taskPos;
            last_new_center = true;
        } else if (mouseButton.equals(MouseButton.SECONDARY) && last_new_center) {
                last_new_center = false;
                Vector2d tmpR = Vector2d.subtract(taskPos,new_center);
                addCircle(new_center, tmpR.length());
                PanelControl.solve.text = "Решить";
        }
    }

    /**
     * Добавить окружность
     *
     * @param center положение центра
     * @param radius радиус
     */
    public void addCircle(Vector2d center, double radius) {
        solved = false;
        Circle newCircle = new Circle(center, radius);
        circles.add(newCircle);
        PanelLog.info("окружность " + newCircle + " добавлена в задачу");
    }


    /**
     * Добавить случайные окружности
     *
     * @param cnt кол-во случайных окружностей
     */
    public void addRandomCircles(int cnt) {
        // повторяем заданное количество раз
        for (int i = 0; i < cnt; i++) {
            // получаем случайные координаты центра
            Vector2d pos = ownCS.getRandomCoords();
            //получаем случайный радиус
            double tmpR = ThreadLocalRandom.current().nextDouble(0, Math.min(ownCS.getSize().x, ownCS.getSize().y) / 2);
            addCircle(pos, tmpR);
        }
    }

    /**
     * Составление массива отрезков для рисования окружности в координатах окна
     * У движка есть готовый метод рисования набора отрезков canvas.drawLines(). Этому методу передать массив вещественных чисел float размером в четыре раза большим, чем кол-во линий. В этом массиве все данные идут подряд: сначала x координата первой точки, потом y координата, потом x координата второй точки, потом y координата, следующие четыре элемента точно также описывают второй отрезок и т.д.
     *
     * @param centre центр окружности
     * @param rad    радиус
     * @return набор точек окружности
     */
    public float[] arrCircle(Vector2d centre, double rad) {


        // радиус вдоль оси x
        float radX = (float) (rad);
        // радиус вдоль оси y
        float radY = (float) (rad);
        // кол-во отсчётов цикла
        int loopCnt = 100;
        // создаём массив координат опорных точек
        float[] points = new float[loopCnt * 4];
        // запускаем цикл
        for (int i = 0; i < loopCnt; i++) {
            // координаты первой точки в СК окна
            double tmpXold = centre.x + radX * Math.cos(2 * Math.PI / loopCnt * i);
            double tmpYold = centre.y + radY * Math.sin(2 * Math.PI / loopCnt * i);
            Vector2i tmp = lastWindowCS.getCoords(tmpXold, tmpYold, ownCS);
            // записываем x
            points[i * 4] = (float) tmp.x;
            // записываем y
            points[i * 4 + 1] = lastWindowCS.getMax().y - (float) tmp.y;
            // координаты второй точки в СК окна
            tmp = lastWindowCS.getCoords(centre.x + radX * Math.cos(2 * Math.PI / loopCnt * (i + 1)), centre.y + radY * Math.sin(2 * Math.PI / loopCnt * (i + 1)), ownCS);
            // записываем x
            points[i * 4 + 2] = (float) tmp.x;
            // записываем y
            points[i * 4 + 3] = lastWindowCS.getMax().y - (float) tmp.y;
        }
        return points;


    }

    /**
     * Рисуем центр
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    private void paint_point(Canvas canvas, CoordinateSystem2i windowCS, Vector2d c) {
        canvas.save();
        // создаём перо
        try (var paint = new Paint()) {
            paint.setColor(CIRCLE_COLOR);
            // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
            // а в классическом представлении - вверх
            Vector2i windowPos = windowCS.getCoords(c.x, c.y, ownCS);
            // рисуем точку
            canvas.drawRect(Rect.makeXYWH(windowPos.x - POINT_SIZE, lastWindowCS.getMax().y - (windowPos.y - POINT_SIZE), POINT_SIZE * 2, POINT_SIZE * 2), paint);
            canvas.restore();
        }
    }




        /**
         * Рисование задачи
         *
         * @param canvas   область рисования
         * @param windowCS СК окна
         */
    private void renderTask(Canvas canvas, CoordinateSystem2i windowCS) {
        canvas.save();
        // создаём перо
        try (var paint = new Paint()) {
            paint.setColor(CIRCLE_COLOR);
            // рисуем центр новой окружности, для которой ожидается радиус
            if (last_new_center){
                paint_point(canvas,windowCS,new_center);
            }
            for (Circle c : circles) {
                // рисуем центр
                paint_point(canvas,windowCS,c.centre);
                // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                // а в классическом представлении - вверх
                Vector2i windowPos = windowCS.getCoords(c.centre.x, c.centre.y, ownCS);
                // рисуем окружность
                float[] points = arrCircle(c.centre, c.radius);
                canvas.drawLines(points, paint);
                }
            // решение обрисовываем другим цветом
            if (solved && cross) {
                paint.setColor(SOLVER_COLOR);
                float[] tmp1 = arrCircle(circle1.centre, circle1.radius);
                canvas.drawLines(tmp1, paint);
                float[] tmp2 = arrCircle(circle2.centre, circle2.radius);
                canvas.drawLines(tmp2, paint);
                Vector2i a = windowCS.getCoords(max_chord.pos1.x, max_chord.pos1.y, ownCS);
                Vector2i b = windowCS.getCoords(max_chord.pos2.x, max_chord.pos2.y, ownCS);
                canvas.drawLine(a.x, lastWindowCS.getMax().y - a.y, b.x, lastWindowCS.getMax().y - b.y, paint);
            }
        }
        canvas.restore();
    }

    /**
     * Рисование всего комплекса
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void paint(Canvas canvas, CoordinateSystem2i windowCS) {
        // Сохраняем последнюю СК
        lastWindowCS = windowCS;
        // рисуем координатную сетку
        renderGrid(canvas, lastWindowCS);
        // рисуем задачу
        renderTask(canvas, windowCS);

    }

    /**
     * Масштабирование области просмотра задачи
     *
     * @param delta  прокрутка колеса
     * @param center центр масштабирования
     */
    public void scale(float delta, Vector2i center) {
        if (lastWindowCS == null) return;
        // переворачиваем y
        int tmp = lastWindowCS.getMax().y - center.y;
        center.y = tmp;
        // получаем координаты центра масштабирования в СК задачи
        Vector2d realCenter = ownCS.getCoords(center, lastWindowCS);
        // выполняем масштабирование
        ownCS.scale(1 + delta * WHEEL_SENSITIVE, realCenter);
    }

}