package ForkJoin;

public class Mandelbrot extends Fractal
{
	public Mandelbrot(double xFractal, double yFractal, double rFractal, double speed, int gridSize)
	{
		super(xFractal, yFractal, rFractal, speed, gridSize);
	}

	public Mandelbrot(double xFractal, double yFractal, double rFractal, double speed, int gridSize, double xJulia, double yJulia)
	{
		super(xFractal, yFractal, rFractal, speed, gridSize, xJulia, yJulia);
	}

	public double orbit(double px, double py)
	{
		Complex z = new Complex(px, py);
		Complex c = new Complex(px, py);

		if (juliaMode)
			c = new Complex(xJulia, yJulia);

		Complex h = new Complex(px, py);
		int cycle = 1;
		int test = cycle;

		Complex d = new Complex();
		Complex one = new Complex(1.0);

		double tmp = 0.0;
		double last;
		double minimal = Double.MAX_VALUE;

		for (int iter = 1; iter < MAX_ITER; iter++)
		{
			z = Complex.add(Complex.sqr(z), c);
			double m = Complex.norm(z);

			switch (compute)
			{
				case ADDITION -> {
					last = tmp;
					tmp += Math.abs(z.imag) / Math.sqrt(m);
					if (m > BAILOUT)
					{
						last /= iter - 1;
						tmp /= iter;
						tmp = last + (tmp - last) * smoothing(m);
						return 1e3 * tmp;
					}
				}
				case DISTANCE -> {
					d = Complex.add(Complex.lambda(2.0, Complex.multiply(d, z)), one);
					if (m > BAILOUT)
					{
						tmp = (2.0 * Math.log(m)) * m / Complex.modulus(d);
						return Math.log(1.0 + 1.0 / tmp);
					}
				}
				case EXPONENT -> {
					tmp += Math.exp(-m);
					if (m > BAILOUT)
						return tmp;
				}
				case ORBITERS -> {
					// if (Math.abs(z.imag) < minimal) minimal = Math.abs(z.imag);
					// if (Math.abs(z.real) < minimal) minimal = Math.abs(z.real);
					double uno = Math.abs(1.0 - m);
					if (uno < minimal) minimal = uno;
					if (m > BAILOUT)
					{
						double dos = Math.abs(1.0 - minimal);
						return dos;
					}
				}
				default -> {
					if (m > BAILOUT)
						return iter + smoothing(m);
				}
			}

			if (iter == test)
			{
				if (Complex.norm(Complex.sub(z, h)) < attract)
					return -1.0;
				h = z;
				cycle++;
				test += cycle;
			}
		}
		return -1.0;
	}
}

