package ForkJoin;

public class Quartet extends Fractal
{
	public Quartet(double xFractal, double yFractal, double rFractal, double speed, int gridSize)
	{
		super(xFractal, yFractal, rFractal, speed, gridSize);
	}

	public Quartet(double xFractal, double yFractal, double rFractal, double speed, int gridSize, double xJulia, double yJulia)
	{
		super(xFractal, yFractal, rFractal, speed, gridSize, xJulia, yJulia);
	}

	public double orbit(double px, double py)
	{
		Complex one = new Complex(1.0);
		Complex two = new Complex(2.0);

		Complex z = new Complex(px, py);
		Complex c = new Complex(px, py);
		Complex h = c;

		if (juliaMode)
		{
			z = c;
			c = new Complex(xJulia, yJulia);
		}

		int cycle = 1;
		int test = cycle;

		double convergence = 0.0;
		double divergence = 0.0;

		for (int iter = 1; iter <= MAX_ITER; iter++)
		{
			Complex t = z;
/*
			Complex numerator = Complex.sub(Complex.add(Complex.sqr(z), c), one);
			Complex denominator = Complex.sub(Complex.add(Complex.lambda(2.0, z), c), two);

			z = Complex.sqr(Complex.divide(numerator, denominator));
*/
			z = Complex.sub(Complex.sin(z), c);
			c = Complex.inverse(Complex.lambda(50.0, z));

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
