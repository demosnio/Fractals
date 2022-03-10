package ForkJoin;

public class JuliaFour extends Fractal
{
	public JuliaFour(double xFractal, double yFractal, double rFractal, double speed, int gridSize)
	{
		super(xFractal, yFractal, rFractal, speed, gridSize);
	}

	public JuliaFour(double xFractal, double yFractal, double rFractal, double speed, int gridSize, double xJulia, double yJulia)
	{
		super(xFractal, yFractal, rFractal, speed, gridSize, xJulia, yJulia);
	}

	public double orbit(double px, double py)
	{
		Complex z = new Complex(px, py);
		Complex c = new Complex(px, py);

		if (juliaMode) c = new Complex(xJulia, yJulia);

		Complex h = new Complex(px, py);
		int cycle = 1;
		int test = cycle;

		Complex d = new Complex();
		Complex one = new Complex(1.0);

		double tmp = 0.0;
		double last;

		for (int iter = 1; iter < MAX_ITER; iter++)
		{
			Complex t = Complex.sqr(z);
			z = Complex.add(t, Complex.inverse(t));
			z = Complex.multiply(z, c);

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
