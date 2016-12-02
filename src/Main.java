import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Math.*;

/**
 * Created by esselesse on 18.11.2016.
 */
public class Main {

    static List<Snippet> snippetList = new ArrayList<Snippet>();
    static List<Snippet> interestingSnippets = new ArrayList<Snippet>();
    static List<Snippet> redlineSnippets = new ArrayList<Snippet>();

    public static void main(String[] args) {
        while (true){
            System.out.println("Make new snippet? Y/N");
            Scanner sc = new Scanner(System.in);
            String ch = "";
            int i = 0;
            double x0, x1, y0, y1;
            Snippet snippet;
            if (sc.hasNextLine()) {
                ch = sc.nextLine();
            }

            if (ch.equalsIgnoreCase("N")){
                System.out.println("Last snippet is RED");
                int length = snippetList.size();
                snippet = snippetList.get(length-1);
                snippetList.remove(length-1);


                // проверка: сравниваем все отрезки, которые находятся в зоне "красного угла обзора", с самим красным отрезком:
                // если угол начала некрасного отрезка <= углу конца красного отрезка, или угол конца некрасного >= углу начала красного => этот некрасный отрезок, возможно, заслоняет часть красного
                // в таком случае проводим нормали в начало и конец этого некрасного отрезка. если обе нормали больше красных нормалей, => можно забыть про этот черный отрезок, если хотя бы одна меньше или равна, то
                // некрасный отрезок хотя бы в одной точке уже перекрывает красный отрезок


                for (Snippet s: snippetList) {

                    if (s.kickedAngleFrom <= snippet.kickedAngleTo || s.kickedAngleTo >= snippet.kickedAngleFrom || s.kickedAngleFrom >= abs(snippet.kickedAngleTo - 180) || s.kickedAngleTo <= abs(snippet.kickedAngleFrom - 180))
                        interestingSnippets.add(s);
                }

                if (snippet.kickedAngleFrom>snippet.kickedAngleTo) {
                    redlineSnippets.add(new Snippet(snippet.x0, snippet.y0, (snippet.x1*snippet.y0-snippet.x0*snippet.y1)/(snippet.y0-snippet.y1), 0));
                    redlineSnippets.add(new Snippet((snippet.x1*snippet.y0-snippet.x0*snippet.y1)/(snippet.y0-snippet.y1), 0, snippet.x1, snippet.y1));
                }
                else
                    redlineSnippets.add(snippet);

                Snippet temp;

                // преобразуем отрезки в угловые сектора с глубинами и разделим осью OX все отрезки на "до и после"

                for (int j = 0; j < interestingSnippets.size(); j++) {
                    temp = interestingSnippets.get(j);
                    if (temp.kickedAngleFrom>temp.kickedAngleTo){
                        interestingSnippets.add(new Snippet(temp.x0, temp.y0, (temp.x1*temp.y0-temp.x0*temp.y1)/(temp.y0-temp.y1), 0));
                        interestingSnippets.add(new Snippet((temp.x1*temp.y0-temp.x0*temp.y1)/(temp.y0-temp.y1), 0, temp.x1, temp.y1));
                        interestingSnippets.remove(j);
                        j--;
                    }
                }

                for (Snippet s: interestingSnippets) {
                    for (int j = 0; j < redlineSnippets.size(); j++) {
                        temp = redlineSnippets.get(j);

                        // здесь проецируем края черного отрезка на красную прямую (не отрезок).
                        // если хоть одна длина от камеры до точек проекции на красную прямую больше, чем расстояние от камеры до края черного отрезка, =>
                        // он (возможно, частично) заслоняет красный отрезок (тк до этого мы отбирали отрезки, которые хоть как-то интерферируют углами обзора с красным отрезком

                        double projectionToRed0;
                        double projectionToRed1;
                        double projectionToBlack0;
                        double projectionToBlack1;
                        // проекции надо считать в красной зоне, иначе дебилизм.
                        // то есть только, если углы проекций внутри красной зоны

                        if(temp.kickedAngleTo <= s.kickedAngleFrom || s.kickedAngleTo <= temp.kickedAngleFrom)
                            continue;

                        if (abs(temp.supernormalAngle - s.kickedAngleFrom) > 180){
                            projectionToRed0 = temp.supernormal/cos(toRadians(s.kickedAngleFrom - temp.supernormalAngle + 360));
                        }
                        else projectionToRed0 = temp.supernormal/cos(toRadians(s.kickedAngleFrom - temp.supernormalAngle));

                        if (abs(temp.supernormalAngle - s.kickedAngleTo) > 180){
                            projectionToRed1 = temp.supernormal/cos(toRadians(s.kickedAngleTo - temp.supernormalAngle + 360));
                        }
                        else projectionToRed1 = temp.supernormal/cos(toRadians(s.kickedAngleTo - temp.supernormalAngle));

                        if (abs(s.supernormalAngle - temp.kickedAngleFrom) > 180){
                            projectionToBlack0 = s.supernormal/cos(toRadians(temp.kickedAngleFrom - s.supernormalAngle + 360));
                        }
                        else projectionToBlack0 = s.supernormal/cos(toRadians(temp.kickedAngleFrom - s.supernormalAngle));

                        if (abs(s.supernormalAngle - temp.kickedAngleTo) > 180){
                            projectionToBlack1 = s.supernormal/cos(toRadians(temp.kickedAngleTo - s.supernormalAngle + 360));
                        }
                        else projectionToBlack1 = s.supernormal/cos(toRadians(temp.kickedAngleTo - s.supernormalAngle));

                        double angleFrom0;
                        double angleFrom1;
                        int flag1 = 0;
                        int flag0 = 0;
                        double angleTo0;
                        double angleTo1;

                        if (abs(temp.kickedAngleFrom-s.kickedAngleFrom) > 180) {
                            if (temp.kickedAngleFrom > s.kickedAngleFrom) {
                                angleFrom0 = temp.kickedAngleFrom - 360;
                                angleFrom1 = s.kickedAngleFrom;
                                if (angleFrom0 > angleFrom1) {
                                    flag0 = 1;
                                }
                                else flag0 = -1;

                                angleFrom0 += 360;

                            } else {
                                angleFrom0 = temp.kickedAngleFrom + 360;
                                angleFrom1 = s.kickedAngleFrom;
                                if (angleFrom0 > angleFrom1) {
                                    flag0 = 1;
                                }
                                else flag0 = -1;

                                angleFrom0 -= 360;
                            }
                        }
                        else {
                            angleFrom0 = temp.kickedAngleFrom;
                            angleFrom1 = s.kickedAngleFrom;
                            if (angleFrom0 > angleFrom1) {
                                flag0 = 1;
                            }
                            else flag0 = -1;
                        }



                        if (abs(temp.kickedAngleTo-s.kickedAngleTo) > 180) {
                            if (temp.kickedAngleTo > s.kickedAngleTo) {
                                angleTo0 = temp.kickedAngleTo - 360;
                                angleTo1 = s.kickedAngleTo;
                                if (angleTo0 > angleTo1) {
                                    flag1 = 1;
                                }
                                else flag1 = -1;

                                angleTo0 += 360;

                            } else {
                                angleTo0 = temp.kickedAngleTo + 360;
                                angleTo1 = s.kickedAngleTo;
                                if (angleTo0 > angleTo1) {
                                    flag1 = 1;
                                }
                                else flag1 = -1;

                                angleTo0 -= 360;
                            }
                        }
                        else {
                            angleTo0 = temp.kickedAngleTo;
                            angleTo1 = s.kickedAngleTo;
                            if (angleTo0 > angleTo1) {
                                flag1 = 1;
                            }
                            else flag1 = -1;
                        }

                        double a0;
                        double a1;


                        if (flag0 == 1 && flag1 == 1){
                            if(projectionToBlack0 <= temp.normalLength0 && projectionToRed1 >= s.normalLength1) {
                                if (angleFrom0 > angleTo1){
                                    a0=angleTo1;
                                    a1=angleFrom0;
                                }
                                else {
                                    a1=angleTo1;
                                    a0=angleFrom0;
                                }

                                j = cut(a0, a1, temp.normalLength0 * (cos(toRadians(a0))), temp.normalLength0 * (sin(toRadians(a0))), projectionToRed1 * (cos(toRadians(a1))), projectionToRed1 * (sin(toRadians(a1))), j);
                            }
                            else if(projectionToBlack0 <= temp.normalLength0 && projectionToRed1 < s.normalLength1){
                                double A = ((temp.x1-temp.x0)*(s.y0-temp.y0)-(temp.y1-temp.y0)*(s.x0-temp.x0))/((temp.y1-temp.y0)*(s.x1-s.x0)-(temp.x1-temp.x0)*(s.y1-s.y0));
                                double x = s.x0+A*(s.x1-s.x0);
                                double y = s.y0+A*(s.y1-s.y0);

                                Snippet view = new Snippet(0,0,x,y);
                                if (angleFrom0 > angleTo1){
                                    a0=angleTo1;
                                    a1=angleFrom0;
                                }
                                else {
                                    a1=angleTo1;
                                    a0=angleFrom0;
                                }
                                j = cut(a0, view.kickedAngleTo, temp.normalLength0*(cos(toRadians(a0))), temp.normalLength0*(sin(toRadians(a0))), x, y, j);

                            }
                            else if(projectionToBlack0 > temp.normalLength0 && projectionToRed1 >= s.normalLength1){
                                double A = ((temp.x1-temp.x0)*(s.y0-temp.y0)-(temp.y1-temp.y0)*(s.x0-temp.x0))/((temp.y1-temp.y0)*(s.x1-s.x0)-(temp.x1-temp.x0)*(s.y1-s.y0));
                                double x = s.x0+A*(s.x1-s.x0);
                                double y = s.y0+A*(s.y1-s.y0);

                                Snippet view = new Snippet(0,0,x,y);
                                if (angleFrom0 > angleTo1){
                                    a0=angleTo1;
                                    a1=angleFrom0;
                                }
                                else {
                                    a1=angleTo1;
                                    a0=angleFrom0;
                                }
                                j = cut(view.kickedAngleTo, a1, x, y, projectionToRed1*(cos(toRadians(a1))), projectionToRed1*(sin(toRadians(a1))), j);
                            }
                        }
                        else if (flag0 == -1  && flag1 == 1){
                            if(projectionToRed0 >= s.normalLength0 && projectionToRed1 >= s.normalLength1){
                                if (angleFrom1 > angleTo1){
                                    a0=angleTo1;
                                    a1=angleFrom1;
                                }
                                else {
                                    a1=angleTo1;
                                    a0=angleFrom1;
                                }
                                j = cut(a0, a1, projectionToRed0*(cos(toRadians(a0))), projectionToRed0*(sin(toRadians(a0))), projectionToRed0*(cos(toRadians(a1))), projectionToRed0*(sin(toRadians(a1))), j);
                            }
                            else if(projectionToRed0 >= s.normalLength0 && projectionToRed1 < s.normalLength1){
                                double A = ((temp.x1-temp.x0)*(s.y0-temp.y0)-(temp.y1-temp.y0)*(s.x0-temp.x0))/((temp.y1-temp.y0)*(s.x1-s.x0)-(temp.x1-temp.x0)*(s.y1-s.y0));
                                double x = s.x0+A*(s.x1-s.x0);
                                double y = s.y0+A*(s.y1-s.y0);

                                Snippet view = new Snippet(0,0,x,y);
                                if (angleFrom1 > angleTo1){
                                    a0=angleTo1;
                                    a1=angleFrom1;
                                }
                                else {
                                    a1=angleTo1;
                                    a0=angleFrom1;
                                }
                                j = cut(a0, view.kickedAngleTo, projectionToRed0*(cos(toRadians(a0))), projectionToRed0*(sin(toRadians(a0))), x, y, j);

                            }
                            else if(projectionToRed0 < s.normalLength0 && projectionToRed1 >= s.normalLength1){
                                double A = ((temp.x1-temp.x0)*(s.y0-temp.y0)-(temp.y1-temp.y0)*(s.x0-temp.x0))/((temp.y1-temp.y0)*(s.x1-s.x0)-(temp.x1-temp.x0)*(s.y1-s.y0));
                                double x = s.x0+A*(s.x1-s.x0);
                                double y = s.y0+A*(s.y1-s.y0);

                                Snippet view = new Snippet(0,0,x,y);
                                if (angleFrom1 > angleTo1){
                                    a0=angleTo1;
                                    a1=angleFrom1;
                                }
                                else {
                                    a1=angleTo1;
                                    a0=angleFrom1;
                                }
                                j = cut(view.kickedAngleTo, a1, x, y, projectionToRed1*(cos(toRadians(a1))), projectionToRed1*(sin(toRadians(a1))), j);
                            }
                        }
                        else if (flag0 == 1  && flag1 == -1){
                            if(projectionToBlack0 <= temp.normalLength0 && projectionToBlack1 <= temp.normalLength1) {
                                if (angleFrom0 > angleTo0){
                                    a0=angleTo0;
                                    a1=angleFrom0;
                                }
                                else {
                                    a1=angleTo0;
                                    a0=angleFrom0;
                                }
                                j = cut(a0, a1, temp.normalLength0 * (cos(toRadians(a0))), temp.normalLength0 * (sin(toRadians(a0))), temp.normalLength1 * (cos(toRadians(a1))), temp.normalLength1 * (sin(toRadians(a1))), j);
                            }
                            else if(projectionToBlack0 <= temp.normalLength0 && projectionToBlack1 > temp.normalLength1){
                                double A = ((temp.x1-temp.x0)*(s.y0-temp.y0)-(temp.y1-temp.y0)*(s.x0-temp.x0))/((temp.y1-temp.y0)*(s.x1-s.x0)-(temp.x1-temp.x0)*(s.y1-s.y0));
                                double x = s.x0+A*(s.x1-s.x0);
                                double y = s.y0+A*(s.y1-s.y0);

                                Snippet view = new Snippet(0,0,x,y);
                                if (angleFrom0 > angleTo0){
                                    a0=angleTo0;
                                    a1=angleFrom0;
                                }
                                else {
                                    a1=angleTo0;
                                    a0=angleFrom0;
                                }
                                j = cut(a0, view.kickedAngleTo, temp.normalLength0*(cos(toRadians(a0))), temp.normalLength0*(sin(toRadians(a0))), x, y, j);

                            }
                            else if(projectionToBlack0 > temp.normalLength0 && projectionToBlack1 <= temp.normalLength1){
                                double A = ((temp.x1-temp.x0)*(s.y0-temp.y0)-(temp.y1-temp.y0)*(s.x0-temp.x0))/((temp.y1-temp.y0)*(s.x1-s.x0)-(temp.x1-temp.x0)*(s.y1-s.y0));
                                double x = s.x0+A*(s.x1-s.x0);
                                double y = s.y0+A*(s.y1-s.y0);

                                Snippet view = new Snippet(0,0,x,y);
                                if (angleFrom0 > angleTo0){
                                    a0=angleTo0;
                                    a1=angleFrom0;
                                }
                                else {
                                    a1=angleTo0;
                                    a0=angleFrom0;
                                }
                                j = cut(view.kickedAngleTo, a1, x, y, temp.normalLength1*(cos(toRadians(a1))), temp.normalLength1*(sin(toRadians(a1))), j);
                            }
                        }
                        else if (flag0 == -1  && flag1 == -1){
                            if(projectionToRed0 >= s.normalLength0 && projectionToBlack1 <= temp.normalLength1) {
                                if (angleFrom1 > angleTo0){
                                    a0=angleTo0;
                                    a1=angleFrom1;
                                }
                                else {
                                    a1=angleTo0;
                                    a0=angleFrom1;
                                }
                                j = cut(a0, a1, projectionToRed0 * (cos(toRadians(a0))), projectionToRed0 * (sin(toRadians(a0))), temp.normalLength1 * (cos(toRadians(a1))), temp.normalLength1 * (sin(toRadians(a1))), j);
                            }
                            else if(projectionToRed0 >= s.normalLength0 && projectionToBlack1 > temp.normalLength1){
                                double A = ((temp.x1-temp.x0)*(s.y0-temp.y0)-(temp.y1-temp.y0)*(s.x0-temp.x0))/((temp.y1-temp.y0)*(s.x1-s.x0)-(temp.x1-temp.x0)*(s.y1-s.y0));
                                double x = s.x0+A*(s.x1-s.x0);
                                double y = s.y0+A*(s.y1-s.y0);

                                Snippet view = new Snippet(0,0,x,y);
                                if (angleFrom1 > angleTo0){
                                    a0=angleTo0;
                                    a1=angleFrom1;
                                }
                                else {
                                    a1=angleTo0;
                                    a0=angleFrom1;
                                }
                                j = cut(a0, view.kickedAngleTo, projectionToRed0*(cos(toRadians(a0))), projectionToRed0*(sin(toRadians(a0))), x, y, j);

                            }
                            else if(projectionToRed0 < s.normalLength0 && projectionToBlack1 <= temp.normalLength1){
                                double A = ((temp.x1-temp.x0)*(s.y0-temp.y0)-(temp.y1-temp.y0)*(s.x0-temp.x0))/((temp.y1-temp.y0)*(s.x1-s.x0)-(temp.x1-temp.x0)*(s.y1-s.y0));
                                double x = s.x0+A*(s.x1-s.x0);
                                double y = s.y0+A*(s.y1-s.y0);

                                Snippet view = new Snippet(0,0,x,y);
                                if (angleFrom1 > angleTo0){
                                    a0=angleTo0;
                                    a1=angleFrom1;
                                }
                                else {
                                    a1=angleTo0;
                                    a0=angleFrom1;
                                }
                                j = cut(view.kickedAngleTo, a1, x, y, temp.normalLength1*(cos(toRadians(a1))), temp.normalLength1*(sin(toRadians(a1))), j);
                            }
                        }
                    }
                }


                if (!redlineSnippets.isEmpty())
                    System.out.println("RedLine is visible");
                else System.out.println("RedLine is invisible");

                break;
            }
            else if (ch.equalsIgnoreCase("Y")){
                System.out.println("Enter x0, y0 for snippet" + i);
                x0 = sc.nextDouble();
                y0 = sc.nextDouble();
                System.out.println("Enter x1, y1 for snippet" + i);
                x1 = sc.nextDouble();
                y1 = sc.nextDouble();

                snippetList.add(new Snippet(x0, y0, x1, y1));
            }
            else {
                System.out.println("Something went wrong");
                break;
            }
        }
    }


    public static int cut(double angle0, double angle1, double x0, double y0, double x1, double y1, int index){
        Snippet temp;
        double redlineAngle0;
        double redlineAngle1;
        if (redlineSnippets.isEmpty())
            return index;
        for (int i = 0; i < redlineSnippets.size(); i++) {

            temp = redlineSnippets.get(i);
            if(temp.kickedAngleTo < angle0 || angle1 < temp.kickedAngleFrom)
                continue;

            redlineAngle0 = temp.kickedAngleFrom;
            redlineAngle1 = temp.kickedAngleTo;
            if (abs(redlineAngle0-redlineAngle1)>180 && abs(angle1-angle0)<180 && abs(redlineAngle0-angle0)>180 && abs(redlineAngle1-angle1)<180)
                redlineAngle0 -= 360;
            else if (abs(redlineAngle0-redlineAngle1)>180 && abs(angle1-angle0)>180 && abs(redlineAngle0-angle0)<180 && abs(redlineAngle1-angle1)<180) {
                redlineAngle0 -= 360;
                angle0 -= 360;
            }
            else if (abs(redlineAngle0-redlineAngle1)<180 && abs(angle1-angle0)>180 && abs(redlineAngle0-angle0)<180 && abs(redlineAngle1-angle1)>180)
                angle1 += 360;
            else if (abs(redlineAngle0-redlineAngle1)>180 && abs(angle1-angle0)<180 && abs(redlineAngle0-angle0)<180 && abs(redlineAngle1-angle1)>180)
                redlineAngle1 += 360;

            if (angle0 <= redlineAngle0)
                if (angle1 < redlineAngle1 && angle1 >redlineAngle0){
                    redlineSnippets.add(new Snippet(x1, y1, temp.x1, temp.y1));
                    redlineSnippets.remove(i);
                    index = i < index ? index-1 : index ;
                    return index;
                }
                else { //angle1 < redlineAngle1 && angle1 >redlineAngle0
                    redlineSnippets.remove(i);
                    index = i < index ? index-1 : index ;
                    return index;
                }
            else { //angle0 <= redlineAngle0
                if (angle0 <= redlineAngle1 && angle1 >= redlineAngle1){
                    redlineSnippets.add(new Snippet(temp.x0, temp.y0, x0, y0));
                    redlineSnippets.remove(i);
                    index = i < index ? index-1 : index ;
                    return index;
                }
                else if (angle0 < redlineAngle1 && angle1 < redlineAngle1){
                    redlineSnippets.add(new Snippet(temp.x0, temp.y0, x0, y0));
                    redlineSnippets.add(new Snippet(x1, y1, temp.x1, temp.y1));
                    redlineSnippets.remove(i);
                    index = i < index ? index-1 : index ;
                    return index;
                }
            }
        }
        return index;
    }
}
