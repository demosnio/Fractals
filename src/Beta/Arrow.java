package Beta;

public class Arrow
{
    double x, y, z;

    public Arrow(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    static Arrow product(Arrow a, Arrow b)
    {
        double x = a.y * b.z - b.y * a.z;
        double y = a.x * b.z - b.x * a.z;
        double z = a.x * b.y - b.x * a.y;

        return new Arrow(x, y, z);
    }

    static Arrow normalize(Arrow a)
    {
        double m = Math.sqrt(a.x * a.x + a.y * a.y + a.z * a.z);

        return new Arrow(a.x / m, a.y / m, a.z / m);
    }

    static double dot(Arrow a, Arrow b)
    {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
}
