package ForkJoin;

public class Complex
{
    double real;
    double imag;

    public Complex(double real, double imag)
    {
        this.real = real;
        this.imag = imag;
    }

    public Complex(double real)
    {
        this.real = real;
        this.imag = 0.0;
    }

    public Complex()
    {
        this.real = 0.0;
        this.imag = 0.0;
    }

    static Complex add(Complex a, Complex b)
    {
        return new Complex(a.real + b.real, a.imag + b.imag);
    }

    static Complex sub(Complex a, Complex b)
    {
        return new Complex(a.real - b.real, a.imag - b.imag);
    }

    static Complex multiply(Complex a, Complex b)
    {
        return new Complex(a.real * b.real - a.imag * b.imag, a.real * b.imag + a.imag * b.real);
    }

    static Complex divide(Complex a, Complex b)
    {
        double n = Complex.norm(b);
        Complex c = Complex.multiply(a, new Complex(b.real, -b.imag));

        return new Complex(c.real / n, c.imag / n);
    }

    static Complex inverse(Complex a)
    {
        double n = Complex.norm(a);

        return new Complex(a.real / n, -a.imag / n);
    }

    static Complex sqr(Complex a)
    {
        return new Complex((a.real + a.imag) * (a.real - a.imag), a.real * (a.imag + a.imag));
    }

    static Complex cube(Complex a)
    {
        return Complex.multiply(Complex.sqr(a), a);
    }

    static double norm(Complex a)
    {
        return a.real * a.real + a.imag * a.imag;
    }

    static double modulus(Complex a)
    {
        return Math.sqrt(Complex.norm(a));
    }

    static Complex lambda(double k, Complex a)
    {
        return new Complex(k * a.real, k * a.imag);
    }

    static Complex pow(Complex a, int n)
    {
        double r = Math.pow(Complex.modulus(a), n);
        double alfa = n * Math.atan2(a.imag, a.real);

        return new Complex(r * Math.cos(alfa), r * Math.sin(alfa));
    }

    static Complex sin(Complex a)
    {
        return new Complex(Math.sin(a.real) * Math.cosh(a.imag), Math.cos(a.real) * Math.sinh(a.imag));
    }
}
