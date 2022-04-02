package Beta;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

public class Mandelbrot extends JFrame
{
    final int threshold = 120;

    final int width = threshold * 16 << 1;
    final int height = threshold * 9 << 1;
    final double ratio = (double) width / (double) height;

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    WritableRaster raster = image.getRaster();

    final double bailout = 10000.0;
    final double bailoutLog = Math.log10(bailout);

    final int maxIter = 1 << 18;
    final double maxIterLog = Math.log10(maxIter);

    final int aliasing = 5;

    boolean lightOn = true;

    final int[][] palette = {{0x00, 0x33, 0x99}, {0xed, 0x1c, 0x16}, {0xff, 0xcc, 0x00}, {0xf5, 0xf5, 0xf1}, {0xa4, 0xc6, 0x39}};
    final int[][] white = {{0xff, 0xff, 0xff}};

    double xCenter, yCenter, radius, gap, miniGap, close;
    boolean julia = false;
    double xJulia, yJulia;

    Graphics gfx;

    double[] xLight;
    double[] yLight;

    public Mandelbrot(double xCenter, double yCenter, double radius)
    {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.radius = radius;

        startUp();
        process();
    }

    public Mandelbrot(double xCenter, double yCenter, double radius, double xJulia, double yJulia)
    {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.radius = radius;

        this.julia = true;

        this.xJulia = xJulia;
        this.yJulia = yJulia;

        startUp();
        process();
    }

    private void startUp()
    {
        setTitle("Mandelbrot BETA");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(null);
        getContentPane().setPreferredSize(new Dimension(width, height));
        pack();
        Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(((int) s.getWidth() - getWidth()) / 2, ((int) s.getHeight() - getHeight()) / 2);
        setVisible(true);

        gfx = getContentPane().getGraphics();
    }

    private String getFilename()
    {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS"));
    }

    private double[] mapping(double start, double end, int steps)
    {
        return IntStream.range(0, steps).parallel().mapToDouble(i -> start + i * (end - start) / (steps - 1.0)).toArray();
    }

    private void process()
    {
        gap = (radius + radius) / (height - 1.0);
        miniGap = gap / aliasing;
        close = Math.pow(miniGap / 3.0, 2.0);

        double delta = gap / 2.0;

        xLight = mapping(xCenter - radius * ratio - delta, xCenter + radius * ratio + delta, 1 + width * aliasing);
        yLight = mapping(yCenter + radius + delta, yCenter - radius - delta, 1 + height * aliasing);


        ForkJoinPool pool = new ForkJoinPool();

        long time = System.currentTimeMillis();

        pool.invoke(new Task(0, 0, width, height, true));

        System.out.println(System.currentTimeMillis() - time);

        try
        {
            ImageIO.write(image, "png", new File("mandelbrot_" + getFilename() + ".png"));
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private double fractal(double px, double py)
    {
        double x = px;
        double y = py;

        double cx = (julia) ? xJulia : px;
        double cy = (julia) ? yJulia : py;

        double hx = px;
        double hy = py;

        int cycle = 1;
        int test = cycle;

        for (int iter = 0; iter < maxIter; iter++)
        {
            double a = x * x;
            double b = y * y;

            double m = a + b;

            if (m > bailout) return iter + smoothing(m);

            y = x * y;
            y = y + y + cy;
            x = a - b + cx;

            if (iter == test)
            {
                double tx = x - hx;
                double ty = y - hy;
                m = tx * tx + ty * ty;
                if (m < close) return -1.0;
                hx = x;
                hy = y;
                cycle++;
                test += cycle;
            }
        }
        return -1.0;
    }

    private double smoothing(double modulus)
    {
        return bailoutLog - Math.log(Math.log(modulus)) / Math.log(2.0);
    }

    private double[] lightArea(int px, int py, int width, int height)
    {
        double[] colors = new double[width * height * 3];

        int sx = px * aliasing;
        int sy = py * aliasing;

        int xPoints = width * aliasing;
        int yPoints = height * aliasing;

        double[][] heightGrid = new double[1 + xPoints][1 + yPoints];
        Color[][] colorsGrid = new Color[1 + xPoints][1 + yPoints];

        IntStream.rangeClosed(0, yPoints).forEach(y -> IntStream.rangeClosed(0, xPoints).forEach(x ->
        {
            heightGrid[x][y] = fractal(xLight[x + sx], yLight[y + sy]);
            colorsGrid[x][y] = colorPalette(heightGrid[x][y]);
        }));

        double p = 0.3;
        double q = 1.0 - p;

        int index = 0;
        for (int h = 0; h < yPoints; h += aliasing)
            for (int w = 0; w < xPoints; w += aliasing)
            {
                Color color = new Color();
                for (int y = 0; y < aliasing; y++)
                    for (int x = 0; x < aliasing; x++)
                    {
                        Color c1 = colorsGrid[w + x][h + y];
                        Color c2 = colorsGrid[w + x][h + y + 1];
                        Color c3 = colorsGrid[w + x + 1][h + y + 1];
                        Color c4 = colorsGrid[w + x + 1][h + y];
                        if (lightOn)
                        {
                            double z1 = heightGrid[w + x][h + y];
                            double z2 = heightGrid[w + x][h + y + 1];
                            double z3 = heightGrid[w + x + 1][h + y + 1];
                            double z4 = heightGrid[w + x + 1][h + y];

                            Arrow k1 = Arrow.normalize(new Arrow(z1 - z4, z1 - z2, miniGap));
                            color = Color.add(color, Color.times(q - p * k1.x, c1));

                            Arrow k2 = Arrow.normalize(new Arrow(z2 - z3, z2 - z1, miniGap));
                            color = Color.add(color, Color.times(q - p * k2.x, c2));

                            Arrow k3 = Arrow.normalize(new Arrow(z2 - z3, z4 - z3, miniGap));
                            color = Color.add(color, Color.times(q - p * k3.x, c3));

                            Arrow k4 = Arrow.normalize(new Arrow(z1 - z4, z3 - z4, miniGap));
                            color = Color.add(color, Color.times(q - p * k4.x, c4));
                        }
                        else
                        {
                            color = Color.add(color, c1);
                            color = Color.add(color, c2);
                            color = Color.add(color, c3);
                            color = Color.add(color, c4);
                        }
                    }
                color = Color.times(1.0 / (aliasing * aliasing * 4), color);
                colors[index++] = color.r;
                colors[index++] = color.g;
                colors[index++] = color.b;
            }
        return colors;
    }

    private Color colorPalette(double z)
    {
        Color color = new Color();

        if (z < 0.0) return color;

        double speed = 7.1;

        z *= speed * Math.PI / 180.0;

        int t = (int) Math.floor(z);
        z -= t;
        t = t % palette.length;
        int s = (t + 1) % palette.length;

        color.r = palette[t][0] + z * (palette[s][0] - palette[t][0]);
        color.g = palette[t][1] + z * (palette[s][1] - palette[t][1]);
        color.b = palette[t][2] + z * (palette[s][2] - palette[t][2]);

        return color;
    }

    private Color colorFunction(double z)
    {
        Color color = new Color();

        if (z < 0.0) return color;

        double speed = 7.1;

        z *= speed * Math.PI / 180.0;

        double s = Math.PI / 4.0;

        color.r = 155.0 + 100.0 * Math.sin(z - s);
        color.g = 155.0 + 100.0 * Math.sin(z);
        color.b = 155.0 + 100.0 * Math.sin(z + s);

        return color;
    }

    private Color colorExperiment(double z)
    {
        Color color = new Color();

        if (z <= 1.0) return color;

        Color color1 = new Color(0x00, 0xbf, 0xff); // deepskyblue
        Color color2 = new Color(0xff, 0x14, 0x93); // deeppink

        double t = Math.log10(z) / maxIterLog;

        if (t > 0.5) return color2;

        color = Color.add(color1, Color.times(t + t, Color.sub(color2, color1)));

        return color;
    }

    class Task extends RecursiveAction
    {
        boolean distribute;
        int x, y, width, height;

        public Task(int x, int y, int width, int height, boolean distribute)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.distribute = distribute;
        }

        @Override
        public void compute()
        {
            if (distribute)
            {
                List<Task> tasks = new ArrayList<>();

                for (int h = 0; h < height; h += threshold)
                    for (int w = 0; w < width; w += threshold)
                        tasks.add(new Task(w, h, threshold, threshold, false));

                invokeAll(tasks);
            }
            else
            {
                raster.setPixels(x, y, width, height, lightArea(x, y, width, height));
                gfx.drawImage(image, 0, 0, null);
            }
        }
    }
}
