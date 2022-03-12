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
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Mandelbrot extends JFrame
{
    final int width = 1600;
    final int height = width;
    final double ratio = (double) width / (double) height;
    final double bailout = 100.0;

    final int maxIter = 1 << 18;
    final int aliasing = 5;
    final int samples = aliasing * aliasing;

    double xCenter, yCenter, radius, gap, miniGap, close, delta;

    boolean julia = false;

    double xJulia, yJulia;

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    WritableRaster raster = image.getRaster();
    Graphics gfx;
    double[] mx;
    double[] my;

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

    private void process()
    {
        gap = (radius + radius) / (height - 1.0);
        miniGap = gap / aliasing;
        close = (miniGap / 3.0) * (miniGap / 3.0);
        delta = miniGap * (aliasing - 1.0) / 2.0;

        mx = mapping(xCenter - radius * ratio - delta, xCenter + radius * ratio + delta, width * aliasing);
        my = mapping(yCenter + radius + delta, yCenter - radius - delta, height * aliasing);


        ForkJoinPool pool = new ForkJoinPool();

        long time = System.currentTimeMillis();

        pool.invoke(new Tarea(0, false));

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

    private double[] fractal(double px, double py)
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

            if (m > bailout) return colorFunction(iter + smoothing(m));

            y = x * y;
            y = y + y + cy;
            x = a - b + cx;

            if (iter == test)
            {
                double tx = x - hx;
                double ty = y - hy;
                m = tx * tx + ty * ty;
                if (m < close) return new double[]{0.0, 0.0, 0.0};
                hx = x;
                hy = y;
                cycle++;
                test += cycle;
            }
        }
        return new double[]{0.0, 0.0, 0.0};
    }

    double smoothing(double modulus)
    {
        //return 1.0 + Math.log(Math.log(bailout) / Math.log(modulus)) / Math.log(2.0);
        return 1.0 - Math.log(Math.log(modulus)) / Math.log(2.0);
    }

    private double[] superPixel(int px, int py)
    {
        double[] color = new double[]{0.0, 0.0, 0.0};

        int sx = px * aliasing;
        int sy = py * aliasing;

        for (int y = sy; y < sy + aliasing; y++)
            for (int x = sx; x < sx + aliasing; x++)
            {
                double[] k = fractal(mx[x], my[y]);
                for (int i = 0; i < 3; i++) color[i] += k[i];
            }

        for (int i = 0; i < 3; i++) color[i] /= samples;

        return color;
    }

    final int[][] palette = {{0x00, 0x33, 0x99}, {0xed, 0x1c, 0x16}, {0xff, 0xcc, 0x00}, {0xf5, 0xf5, 0xf1}, {0xa4, 0xc6, 0x39}};

    private double[] colorPalette(double z)
    {
        double[] color = new double[3];
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
        double[] color = new double[3];
        double speed = 7.1;
        double s = 1.1;

        z *= speed * Math.PI / 180.0;

        for (int i = 0; i < 3; i++) color[i] = 155.0 + 100.0 * Math.sin(z + s * (i - 1));

        return color;
    }

    private double[] mapping(double start, double end, int steps)
    {
        return IntStream.range(0, steps).parallel().mapToDouble(i -> start + i * (end - start) / (steps - 1.0)).toArray();
    }

    class Tarea extends RecursiveAction
    {
        boolean suitable;
        int y;

        public Tarea(int y, boolean suitable)
        {
            this.y = y;
            this.suitable = suitable;
        }

        @Override
        public void compute()
        {
            if (suitable)
            {
                IntStream.range(0, width).forEach(x -> raster.setPixel(x, y, superPixel(x, y)));
                gfx.drawImage(image, 0, 0, null);
            }
            else
            {
                List<Tarea> tareas = new ArrayList<>();
                IntStream.range(0, height).forEach(y -> tareas.add(new Tarea(y, true)));
                invokeAll(tareas);
            }
        }
    }
}
