import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.Math.*;

/**
 * Created by esselesse on 18.11.2016.
 */
public class Snippet {
    public double x0;
    public double y0;
    public double x1;
    public double y1;
    public double normalLength0;
    public double normalLength1;
    public double zeronormal = 0;
    public double angle0;
    public double angle1;
    public double kickedAngleFrom;
    public double kickedAngleTo;
    public double wholeAngle;
    public double supernormal;
    public double snippetLength;
    public double supernormalAngle;
    public double xcoef;
    public double ycoef;
    public double freecoef;



    public Snippet(double x0, double y0, double x1, double y1){
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;

        xcoef = y1-y0;
        ycoef = x0-x1;
        freecoef = y0*(x1-x0) - x0*(y1-y0);

        if(ycoef<0){
            xcoef*=-1;
            ycoef*=-1;
            freecoef*=-1;
        }

        snippetLength = sqrt((x1-x0)*(x1-x0) + (y1-y0)*(y1-y0));

        // определяем длину нормалей, опущенных из (0,0) в указанные точки, по т. Пифагора

        this.normalLength0 = sqrt(x0*x0 + y0*y0);
        this.normalLength1 = sqrt(x1*x1 + y1*y1);
        // далее определяем углы прямых, проведенных из (0,0) в указанные точки, относительно OX
        // от 0 до 359.99 градусов
        if ((this.x0 / this.normalLength0) >= 0 && (this.y0 / this.normalLength0) >= 0)
            this.angle0 = toDegrees(asin((this.y0 / this.normalLength0)));
        else if ((this.x0 / this.normalLength0) >= 0 && (this.y0 / this.normalLength0) < 0)
            this.angle0 = toDegrees(asin((abs(this.x0) / this.normalLength0)))+270;
        else if ((this.x0 / this.normalLength0) < 0 && (this.y0 / this.normalLength0) >= 0)
            this.angle0 = toDegrees(acos((abs(this.y0) / this.normalLength0)))+90;
        else if ((this.x0 / this.normalLength0) < 0 && (this.y0 / this.normalLength0) < 0)
            this.angle0 = toDegrees(asin((abs(this.y0) / this.normalLength0)))+180;

        if ((this.x1 / this.normalLength1) >= 0 && (this.y1 / this.normalLength1) >= 0)
            this.angle1 = toDegrees(asin((this.y1 / this.normalLength1)));
        else if ((this.x1 / this.normalLength1) >= 0 && (this.y1 / this.normalLength1) < 0)
            this.angle1 = toDegrees(asin((abs(this.x1) / this.normalLength1)))+270;
        else if ((this.x1 / this.normalLength1) < 0 && (this.y1 / this.normalLength1) >= 0)
            this.angle1 = toDegrees(acos((abs(this.y1) / this.normalLength1)))+90;
        else if ((this.x1 / this.normalLength1) < 0 && (this.y1 / this.normalLength1) < 0)
            this.angle1 = toDegrees(asin((abs(this.y1) / this.normalLength1)))+180;



//векторизируем отрезки - определяем углы видимости против часовой стрелки относительно ОХ
            if (abs(this.angle1 - this.angle0) > 180) {
                double aAngle = this.angle1 > 180 ? abs(this.angle1 - 360) : this.angle1;
                double bAngle = this.angle0 > 180 ? abs(this.angle0 - 360) : this.angle0;
                this.wholeAngle = abs(aAngle + bAngle);
                this.wholeAngle = angleKiller(this.wholeAngle);
                this.angle0 = angleKiller(this.angle0);
                this.angle1 = angleKiller(this.angle1);
                if (this.angle0 > this.angle1) {
                    this.kickedAngleFrom = this.angle0;
                    this.kickedAngleTo = this.angle1;
                } else {
                    this.kickedAngleFrom = angle1;
                    double temp = this.normalLength1;
                    this.normalLength1 = this.normalLength0;
                    this.normalLength0 = temp;
                    this.kickedAngleTo = angle0;
                    temp=this.x0;
                    this.x0=this.x1;
                    this.x1=temp;
                    temp=this.y0;
                    this.y0=this.y1;
                    this.y1=temp;
                    this.zeronormal = -y0 * (x1 - x0) / (y1 - y0) + x0;
                }
            } else if (abs(this.angle1 - this.angle0) < 180) {
                this.wholeAngle = abs(this.angle1 - this.angle0);
                this.wholeAngle = angleKiller(this.wholeAngle);
                this.angle0 = angleKiller(this.angle0);
                this.angle1 = angleKiller(this.angle1);
                if (this.angle0 < this.angle1) {
                    this.kickedAngleFrom = this.angle0;
                    this.kickedAngleTo = this.angle1;
                } else {
                    this.kickedAngleFrom = angle1;
                    this.kickedAngleTo = angle0;
                    double temp = this.normalLength1;
                    this.normalLength1 = this.normalLength0;
                    this.normalLength0 = temp;
                    temp=this.x0;
                    this.x0=this.x1;
                    this.x1=temp;
                    temp=this.y0;
                    this.y0=this.y1;
                    this.y1=temp;
                }
            } else if (this.angle1 - this.angle0 == 0 || this.angle1 - this.angle0 == 180) {
                this.kickedAngleFrom = 0;
                this.kickedAngleTo = 0;
            }

            this.supernormal = this.normalLength0 * this.normalLength1 * sin(toRadians(this.wholeAngle)) / this.snippetLength;

            if(this.normalLength0 != 0 && this.normalLength1 != 0) {
                double tempo = this.supernormal / normalLength0;
                if (tempo>1)
                    tempo=(int)tempo;
                double alpha = toDegrees(acos(tempo));
                alpha = angleKiller(alpha);

                tempo = this.supernormal / normalLength1;
                if (tempo>1)
                    tempo=(int)tempo;
                double beta = toDegrees(acos(tempo));
                beta = angleKiller(beta);

                if (angleKiller(alpha + beta) <= this.wholeAngle + 0.0000000001 && angleKiller(alpha + beta) >= this.wholeAngle - 0.0000000001)
                    this.supernormalAngle = alpha + this.kickedAngleFrom;
                else if (angleKiller(beta - alpha) <= this.wholeAngle + 0.0000000001 && angleKiller(beta - alpha) >= this.wholeAngle - 0.0000000001)
                    this.supernormalAngle = this.kickedAngleFrom - alpha;
            }
            if (this.kickedAngleFrom == 360)
                this.kickedAngleFrom = 0;
            if (this.kickedAngleTo == 0)
                this.kickedAngleTo = 360;

    }

public double angleKiller(double angle){
    double newDouble;
    if (angle!=0){
        newDouble = new BigDecimal(angle).setScale(10, RoundingMode.HALF_EVEN).doubleValue();
        return newDouble;
    }
    else return angle;

}

}
