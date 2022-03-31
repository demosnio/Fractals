package Beta;

public class Color
{
    public double r, g, b;

    public Color()
    {
        this.r = 0.0;
        this.g = 0.0;
        this.b = 0.0;
    }

    public Color(double r, double g, double b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    static Color add(Color color1, Color color2)
    {
        return new Color(color1.r + color2.r, color1.g + color2.g, color1.b + color2.b);
    }

    static Color sub(Color color1, Color color2)
    {
        return new Color(color1.r - color2.r, color1.g - color2.g, color1.b - color2.b);
    }

    static Color times(double k, Color color)
    {
        return new Color(k * color.r, k * color.g, k * color.b);
    }

    @Override
    public String toString()
    {
        return "Color{" + "r=" + r + ", g=" + g + ", b=" + b + "}";
    }
}
