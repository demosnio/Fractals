package ForkJoin;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class FractalForkJoin extends JFrame
{
	final int[][] softy = {{0x55, 0xac, 0xee}, {0x29, 0x2f, 0x33}, {0x66, 0x75, 0x7f}, {0xe1, 0xe8, 0xed}, {0xff, 0xff, 0xff}};
	final int[][] beauty = {{0x00, 0x33, 0x99}, {0xed, 0x1c, 0x16}, {0xff, 0xcc, 0x00}, {0xf5, 0xf5, 0xf1}, {0xa4, 0xc6, 0x39}};
	final int[][] white = {{0xff, 0xff, 0xff}};
	final int[][] testing = {{0xff, 0xff, 0xff}, {0x33, 0x66, 0x99}, {0xff, 0xcc, 0x00}, {0xcc, 0x00, 0x00}, {0x3f, 0x00, 0x3f}};

	int[][] palette = beauty;

	final BufferedImage image = new BufferedImage(Fractal.width, Fractal.height, BufferedImage.TYPE_INT_RGB);
	final WritableRaster raster = image.getRaster();
	final Graphics gfx;

	final String theTitle = "Fractal Fork Join Pool";

	final boolean autoSave = true;

	boolean lighting = true;

	Fractal fractal;

	public FractalForkJoin()
	{
		setTitle(theTitle);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(null);
		getContentPane().setPreferredSize(new Dimension(Fractal.width, Fractal.height));
		pack();
		Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(((int) s.getWidth() - getWidth()) / 2, ((int) s.getHeight() - getHeight()) / 2);
		setVisible(true);

		addKeyListener(thisKeyListener());
		getContentPane().addMouseListener(thisMouseListener());

		gfx = getContentPane().getGraphics();

		fractal = new Mandelbrot(-0.765, 0.0, 1.25, 0.16384, 1);
//		fractal = new Mandelbrot(-1.5216777662315921, 0.0, 5.135673858195456E-8, 0.0536870912, 1);
//		fractal = new Mandelbrot(-1.7687469863891603, 0.0015658140182495117, 3.814697265625E-5, 0.07, 1);
//		fractal = new Mandelbrot(0.0, 0.0, 1.55, 0.07, 1, -0.74543, 0.11301);
//		fractal = new Nova(-0.6, 0.0, 1.0, 0.15, 1);
//		fractal = new Nova(-0.5758192670706195, 0.0, 5.774199962615967E-8, 0.15, 1);
//		fractal = new Nova(0.0, 0.0, 1.0, 0.15, 1, -0.610286814534393804834, 0.0);
//		fractal = new Nova(-1.3569865541160104, 0.0, 7.450580596923828E-9, 0.15, 1);
//		fractal = new JuliaFour(0.0, 0.0, 1.75, 0.07, 1);
//		fractal = new JuliaFour(0.0, 0.0, 1.7, 0.07, 1, 0.579, 0.0);
//		fractal = new JuliaFour(0.0, 0.7, 0.14347, 0.07, 1, 0.579, 0.0);
//		fractal = new Magnet(0.0, 1.3, 2.25, 0.5, 1);
//		fractal = new Magnet(-0.67401123046875, -0.3787109375000002, 0.03515625, 0.5, 1);
//		fractal = new Quartet(0.0, 0.0, 2.5, 0.5, 1);

		fractalUpdate();
	}

	public static void main(String[] args)
	{
		new FractalForkJoin();
	}

	String getFilename()
	{
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss"));
	}

	void fractalUpdate()
	{
		fractal.update();

		ForkJoinPool fjPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		for (int h = 0; h < Fractal.height; h += Fractal.THRESHOLD)
			for (int w = 0; w < Fractal.width; w += Fractal.THRESHOLD)
				fjPool.submit(new FractalTask(w, h, Fractal.THRESHOLD, Fractal.THRESHOLD));
//				fjPool.execute(new FractalTask(w, h, Fractal.THRESHOLD, Fractal.THRESHOLD));

		if (autoSave)
		{
			try
			{
				long elapsed = System.currentTimeMillis();
				setTitle(theTitle + " ... Please wait");
				fjPool.shutdown();
				fjPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
				setTitle(theTitle);
				System.out.println(System.currentTimeMillis() - elapsed);
				String filename = getFilename();
				imageSave(filename + ".png");
				parameterSave(filename + ".txt");
			}
			catch (InterruptedException ie)
			{
				System.err.println("Something went wrong updating fractal");
			}
		}
	}

	Color coloring(double result)
	{
		Color color = new Color(0.0, 0.0, 0.0);

		if (result >= 0)
		{
			result *= fractal.speed;
			double floorResult = Math.floor(result);
			result -= floorResult;
			int t = (int) floorResult % palette.length;
			int s = (t + 1) % palette.length;
			color.r = palette[t][0] + result * (palette[s][0] - palette[t][0]);
			color.g = palette[t][1] + result * (palette[s][1] - palette[t][1]);
			color.b = palette[t][2] + result * (palette[s][2] - palette[t][2]);
		}
		return color;
	}

	double[] normalize(double x, double y, double z)
	{
		double m = Math.sqrt(x * x + y * y + z * z);

		return new double[]{x / m, y / m, z / m};
	}

	public void imageSave(String filename)
	{
		try
		{
			ImageIO.write(image, "png", new File(filename));
		}
		catch (IOException exception)
		{
			System.err.println("Something went wrong saving image");
		}
	}

	public void parameterSave(String filename)
	{
		try (PrintWriter pw = new PrintWriter(filename))
		{
			pw.println("xFractal = " + fractal.xFractal + ";");
			pw.println("yFractal = " + fractal.yFractal + ";");
			pw.println("rFractal = " + fractal.rFractal + ";");
			pw.println("speed = " + fractal.speed + ";");
			pw.println("gridSize = " + fractal.gridSize + ";");
		}
		catch (FileNotFoundException exception)
		{
			System.err.println("Something went wrong saving parameters");
		}
	}

	KeyListener thisKeyListener()
	{
		return new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				//KeyEvent ke = (KeyEvent) EventQueue.getCurrentEvent();
				String keyText = KeyEvent.getKeyText(e.getKeyCode());

				switch (keyText)
				{
					case "Escape" -> {
						setVisible(false);
						dispose();
						System.exit(0);
					}
					case "1", "2", "3", "4", "5", "6", "7", "8", "9" -> fractal.gridSize = Integer.parseInt(keyText);
					case "P" -> fractal.speed *= 1.25;
					case "Q" -> fractal.speed /= 1.25;
					case "R" -> fractal.yFractal = -fractal.yFractal;
					case "Y" -> fractal.yFractal = 0.0;
					case "Plus", "NumPad +" -> fractal.rFractal -= 16 * fractal.pixelSize;
					case "Minus", "NumPad -" -> fractal.rFractal += 16 * fractal.pixelSize;
					case "Left" -> fractal.xFractal -= 8 * fractal.pixelSize;
					case "Right" -> fractal.xFractal += 8 * fractal.pixelSize;
					case "Up" -> fractal.yFractal += 8 * fractal.pixelSize;
					case "Down" -> fractal.yFractal -= 8 * fractal.pixelSize;
					case "S" -> {
						String filename = getFilename();
						imageSave(filename + ".png");
						parameterSave(filename + ".txt");
					}
					case "A" -> fractal.compute = Fractal.ADDITION;
					case "D" -> fractal.compute = Fractal.DISTANCE;
					case "E" -> fractal.compute = Fractal.EXPONENT;
					case "N" -> fractal.compute = Fractal.SMOOTH;
					case "O" -> fractal.compute = Fractal.ORBITERS;
					case "L" -> lighting = !lighting;
					case "F1" -> palette = softy;
					case "F2" -> palette = beauty;
					case "F3" -> palette = white;
					case "F4" -> palette = testing;
					default -> {
						System.out.println("\"" + keyText + "\"");
						return;
					}
				}
				fractalUpdate();
			}
		};
	}

	MouseListener thisMouseListener()
	{
		return new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me)
			{
				fractal.xFractal = fractal.xOrigin + fractal.pixelSize * me.getX();
				fractal.yFractal = fractal.yOrigin - fractal.pixelSize * me.getY();
				fractal.rFractal = (me.getButton() == MouseEvent.BUTTON1) ? (fractal.rFractal / 8.0) : (fractal.rFractal * 8.0);
				fractalUpdate();
			}

			@Override
			public void mouseEntered(MouseEvent me)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent me)
			{
				setCursor(Cursor.getDefaultCursor());
			}
		};
	}

	class FractalTask implements Runnable
	{
		final int ox;
		final int oy;
		final int width;
		final int height;

		public FractalTask(int ox, int oy, int width, int height)
		{
			this.ox = ox;
			this.oy = oy;
			this.width = width;
			this.height = height;
		}

		@Override
		public void run()
		{
			double xStart = fractal.xOrigin + ox * fractal.pixelSize;
			double yStart = fractal.yOrigin - oy * fractal.pixelSize;

			int xPoints = 1 + fractal.gridSize * width;
			int yPoints = 1 + fractal.gridSize * height;

			double[][] heightGrid = new double[xPoints][yPoints];

			double dy = yStart;
			for (int y = 0; y < yPoints; y++)
			{
				double dx = xStart;
				for (int x = 0; x < xPoints; x++)
				{
					//heightGrid[x][y] = nova(dx, dy, dx, dy);
					heightGrid[x][y] = fractal.orbit(dx, dy);
					dx += fractal.gridGap;
				}
				dy -= fractal.gridGap;
			}

			Color[][] colors = new Color[xPoints][yPoints];

			for (int y = 0; y < yPoints; y++)
				for (int x = 0; x < xPoints; x++)
					colors[x][y] = coloring(heightGrid[x][y]);

			double p = 0.3;
			double q = 1.0 - p;

			double[] pixelData = new double[3 * width * height];
			int pixelIndex = 0;

			for (int yy = 0; yy < yPoints - 1; yy += fractal.gridSize)
			{
				for (int xx = 0; xx < xPoints - 1; xx += fractal.gridSize)
				{
					Color color = new Color(0.0, 0.0, 0.0);
					int k = 0;

					for (int y = 0; y < fractal.gridSize; y++)
					{
						int beta = yy + y;
						for (int x = 0; x < fractal.gridSize; x++)
						{
							int alfa = xx + x;

							double light1 = 1.0;
							double light2 = 1.0;

							if (lighting)
							{
								double z1 = heightGrid[alfa][beta];
								double z2 = heightGrid[alfa][beta + 1];
								double z3 = heightGrid[alfa + 1][beta + 1];
								double z4 = heightGrid[alfa + 1][beta];

								double[] t1 = normalize(z1 - z4, z2 - z1, fractal.gridGap);
								double[] t2 = normalize(z2 - z3, z3 - z4, fractal.gridGap);

								light1 = q + t1[0] * p;
								light2 = q + t2[0] * p;
							}

							color.add(colors[alfa][beta].multiply(light1));
							k++;

							color.add(colors[alfa][beta + 1].multiply(light1));
							k++;

							color.add(colors[alfa + 1][beta].multiply(light1));
							k++;

							color.add(colors[alfa][beta + 1].multiply(light2));
							k++;

							color.add(colors[alfa + 1][beta + 1].multiply(light2));
							k++;

							color.add(colors[alfa + 1][beta].multiply(light2));
							k++;
						}
					}
					pixelData[pixelIndex++] = color.r / k;
					pixelData[pixelIndex++] = color.g / k;
					pixelData[pixelIndex++] = color.b / k;
				}
			}
			raster.setPixels(ox, oy, width, height, pixelData);
			gfx.drawImage(image, 0, 0, null);
		}
	}
}