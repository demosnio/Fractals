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
    final int threshold = 200;

    final int width = threshold * 16;
    final int height = threshold * 9;
    final double ratio = (double) width / (double) height;
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    WritableRaster raster = image.getRaster();
    final double bailout = 10000.0;
    final double bailoutLog = Math.log10(bailout);
    final int maxIter = 1 << 18;
    final int aliasing = 7;
    final int samples = aliasing * aliasing;
//    final int[][] palette = {{0x00, 0x33, 0x99}, {0xed, 0x1c, 0x16}, {0xff, 0xcc, 0x00}, {0xf5, 0xf5, 0xf1}, {0xa4, 0xc6, 0x39}};
    final int[][] palette = {{0xff, 0xff, 0xff}};
    double xCenter, yCenter, radius, gap, miniGap, close, delta;
    boolean julia = false;
    double xJulia, yJulia;
    Graphics gfx;
    double[] xPixel;
    double[] yPixel;
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

        delta = (gap - miniGap) / 2.0;

        xPixel = mapping(xCenter - radius * ratio - delta, xCenter + radius * ratio + delta, width * aliasing);
        yPixel = mapping(yCenter + radius + delta, yCenter - radius - delta, height * aliasing);

        delta = gap / 2.0;

        xLight = mapping(xCenter - radius * ratio - delta, xCenter + radius * ratio + delta, 1 + width * aliasing);
        yLight = mapping(yCenter + radius + delta, yCenter - radius - delta, 1 + height * aliasing);


        ForkJoinPool pool = new ForkJoinPool();

        long time = System.currentTimeMillis();

        pool.invoke(new Task(0, 0, width, height, true));

//        gfx.drawImage(image, 0, 0, null);

        System.out.println(System.currentTimeMillis() - time);

        try
        {
            ImageIO.write(image, "png", new File("mandelbrot_" + getFilename() + ".png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
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

//        double e = 0.0;

        for (int iter = 0; iter < maxIter; iter++)
        {
            double a = x * x;
            double b = y * y;

            double m = a + b;
/*
            e += Math.exp(-m);
            if (m > bailout) return e;
*/
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

    private double[] superPixel(int px, int py)
    {
        double[] color = new double[]{0.0, 0.0, 0.0};

        int sx = px * aliasing;
        int sy = py * aliasing;

        for (int y = sy; y < sy + aliasing; y++)
            for (int x = sx; x < sx + aliasing; x++)
            {
                double[] k = colorPalette(fractal(xPixel[x], yPixel[y]));
                for (int i = 0; i < 3; i++) color[i] += k[i];
            }

        for (int i = 0; i < 3; i++) color[i] /= samples;

        return color;
    }

    private double[] normalize(double x, double y, double z)
    {
        double m = Math.sqrt(x * x + y * y + z * z);

        return new double[]{x / m, y / m, z / m};
    }

    private double[] lightPixel(int px, int py)
    {
        double[] color = new double[]{0.0, 0.0, 0.0};

        int sx = px * aliasing;
        int sy = py * aliasing;

        double[][] heightGrid = new double[1 + aliasing][1 + aliasing];
        double[][][] colorsGrid = new double[1 + aliasing][1 + aliasing][3];

        for (int y = 0; y <= aliasing; y++)
            for (int x = 0; x <= aliasing; x++)
            {
                heightGrid[x][y] = fractal(xLight[x + sx], yLight[y + sy]);
                System.arraycopy(colorPalette(heightGrid[x][y]), 0, colorsGrid[x][y], 0, 3);
//                System.arraycopy(new double[]{255.0, 255.0, 255.0}, 0, colorsGrid[x][y], 0, 3);
            }

        double p = 0.3;
        double q = 1.0 - p;

        boolean lighting = true;
        double light1 = 1.0;
        double light2 = 1.0;

        for (int y = 0; y < aliasing; y++)
            for (int x = 0; x < aliasing; x++)
            {
                if (lighting)
                {
                    double z1 = heightGrid[x][y];
                    double z2 = heightGrid[x][y + 1];
                    double z3 = heightGrid[x + 1][y + 1];
                    double z4 = heightGrid[x + 1][y];

                    double[] t1 = normalize(z1 - z4, z2 - z1, miniGap);
                    double[] t2 = normalize(z2 - z3, z3 - z4, miniGap);
/*
                    double[] lightDirection = normalize(-1.0, 0.0, 0.0);

                    double l1 = 0.0;
                    double l2 = 0.0;

                    for (int i = 0; i < 3; i++)
                    {
                        l1 += t1[i] * lightDirection[i];
                        l2 += t2[i] * lightDirection[i];
                    }

                    light1 = q + l1 * p;
                    light2 = q + l2 * p;
*/
                    light1 = q - t1[0] * p;
                    light2 = q - t2[0] * p;
                }
                for (int i = 0; i < 3; i++)
                {
                    color[i] += colorsGrid[y][x][i] * light1;
                    color[i] += colorsGrid[y][x + 1][i] * light1;
                    color[i] += colorsGrid[y + 1][x][i] * light1;

                    color[i] += colorsGrid[y][x + 1][i] * light2;
                    color[i] += colorsGrid[y + 1][x + 1][i] * light2;
                    color[i] += colorsGrid[y + 1][x][i] * light2;
                }
            }

        for (int i = 0; i < 3; i++)
            color[i] /= (aliasing) * (aliasing) * 6;

        return color;
    }

    private double[] colorPalette(double z)
    {
        double[] color = {0.0, 0.0, 0.0};

        if (z < 0.0) return color;

        double speed = 7.1;

        z *= speed * Math.PI / 180.0;

        int t = (int) Math.floor(z);
        z -= t;
        t = t % palette.length;
        int s = (t + 1) % palette.length;
        for (int i = 0; i < 3; i++) color[i] = palette[t][i] + z * (palette[s][i] - palette[t][i]);

        return color;
    }

    private double[] colorFunction(double z)
    {
        double[] color = {1.0, 0.0, 0.0};

        if (z < 0.0) return color;

        double speed = 7.1;

        z *= speed * Math.PI / 180.0;

        double s = Math.PI / 36.0;
        for (int i = 0; i < 3; i++) color[i] = 155.0 + 100.0 * Math.sin(z + s * (i - 1));

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

                for (int w = 0; w < width; w += threshold)
                    for (int h = 0; h < height; h += threshold)
                        tasks.add(new Task(w, h, threshold, threshold, false));

                invokeAll(tasks);
            }
            else
            {
                computeArea();
            }
        }

        private void computeArea()
        {
            IntStream.range(x, x + width).forEach(p -> IntStream.range(y, y + height).forEach(q -> raster.setPixel(p, q, lightPixel(p, q))));
            gfx.drawImage(image, 0, 0, null);
        }
    }
}
