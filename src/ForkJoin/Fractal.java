package ForkJoin;

public class Fractal
{
    final static int THRESHOLD = 100;
    final static int width = THRESHOLD * 16;
    final static int height = THRESHOLD * 9;

    final int MAX_ITER = 1 << 16;
    final double BAILOUT = 1.0e6;

    final static int SMOOTH = 0;
    final static int ADDITION = 1 << 1;
    final static int DISTANCE = 1 << 2;
    final static int EXPONENT = 1 << 3;
    final static int ORBITERS = 1 << 4;

    double xFractal;
    double yFractal;
    double rFractal;

    double speed;
    int gridSize;

    int compute = SMOOTH;
    
    double attract;

    double pixelSize;
    double gridGap;
    double xOrigin;
    double yOrigin;

    boolean juliaMode = false;

    double xJulia;
    double yJulia;

    public Fractal(double xFractal, double yFractal, double rFractal, double speed, int gridSize)
    {
        this.xFractal = xFractal;
        this.yFractal = yFractal;
        this.rFractal = rFractal;

        this.speed = speed;
        this.gridSize = gridSize;
    }

    public Fractal(double xFractal, double yFractal, double rFractal, double speed, int gridSize, double xJulia, double yJulia)
    {
        this.xFractal = xFractal;
        this.yFractal = yFractal;
        this.rFractal = rFractal;

        this.speed = speed;
        this.gridSize = gridSize;

        this.juliaMode = true;

        this.xJulia = xJulia;
        this.yJulia = yJulia;
    }

    public void update()
    {
        pixelSize = 2.0 * rFractal / height;
        gridGap = pixelSize / gridSize;
        attract = gridGap * gridGap / 9.0;

        xOrigin = xFractal - rFractal * width / height;
        yOrigin = yFractal + rFractal;
    }

    double smoothing(double modulus)
    {
        return 1.0 + Math.log(Math.log(BAILOUT) / Math.log(modulus)) / Math.log(2.0);
    }

    public double orbit(double px, double py)
    {
        return 0.0;
    }
}
