package ForkJoin;

public class Color
{
    double r, g, b;

    public Color(double r, double g, double b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void add(Color c)
    {
        this.r += c.r;
        this.g += c.g;
        this.b += c.b;
    }

    public Color multiply(double k)
    {
        return new Color(this.r * k, this.g * k, this.b * k);
    }
}
