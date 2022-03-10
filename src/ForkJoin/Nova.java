package ForkJoin;

public class Nova extends Fractal
{
	private final double novaAttract = 1e-5;

	public Nova(double xFractal, double yFractal, double rFractal, double speed, int gridSize)
	{
		super(xFractal, yFractal, rFractal, speed, gridSize);
	}

	public Nova(double xFractal, double yFractal, double rFractal, double speed, int gridSize, double xJulia, double yJulia)
	{
		super(xFractal, yFractal, rFractal, speed, gridSize, xJulia, yJulia);
	}

	public double orbit(double px, double py)
	{
		Complex one = new Complex(1.0);

		Complex z = one;
		Complex c = new Complex(px, py);

		if (juliaMode)
		{
			z = c;
			c = new Complex(xJulia, yJulia);
		}

		Complex h = new Complex(px, py);
		int cycle = 1;
		int test = cycle;

		double convergence = 0.0;
		double divergence = 0.0;

		for (int iter = 1; iter <= MAX_ITER; iter++)
		{
			Complex t = z;

			Complex zFunction = Complex.sub(Complex.cube(z), one);
			Complex zDerived = Complex.lambda(3.0, Complex.sqr(z));

			Complex aux = Complex.sub(Complex.divide(zFunction, zDerived), c);

			z = Complex.sub(z, aux);

			double m = Complex.norm(Complex.sub(z, t));

			convergence += Math.exp(-1.0 / m);

			if (m < attract) return convergence;

			double n = Complex.norm(z);

			divergence += Math.exp(-n);

			if (n > BAILOUT) return divergence;

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
