package q1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import java.util.Random;
import java.util.Vector;
import javax.imageio.*;


public class star {

	// The image constructed
	public static BufferedImage img;

	public Vertex head;
	public Vertex tail;

	// Image dimensions; you can modify these for bigger/smaller images
	public static int width = 1920;
	public static int height = 1080;

	static Random random = new Random();

	public static int m;
	public static int c;
	public static int size;

	public static double x_max = 0;
	public static double x_min = 0;
	public static double y_max = 0;
	public static double y_min = 0;

	public static void main(String[] args) {
		try {
			if (args.length<2)
				throw new Exception("Missing arguments, only "+args.length+" were specified!");
			// arg 0 is the max radius
			m = Integer.parseInt(args[0]);
			// arg 1 is count
			c = Integer.parseInt(args[1]);
			// arg 2 is a boolean


			// create an image and initialize it to all 0's
			img = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			for (int i=0;i<width;i++) {
				for (int j=0;j<height;j++) {
					img.setRGB(i,j,0);
				}
			}

			//Hardcoded poly
			star star = new star();
			star.addVertexAtEnd(-1.0, 5.0);
			star.addVertexAtEnd(1.0, 2.0);
			star.addVertexAtEnd(5.0, 0.0);
			star.addVertexAtEnd(1.0, -2.0);
			star.addVertexAtEnd(-4.0, -4.0);
			star.addVertexAtEnd(-3.0, -2.0);

			//            star.print();
			//            System.out.println(x_min + "," + x_max + "," + y_min+ "," + y_max);



			Runnable runnable = new Runnable() {

				@Override
				public void run() {

					Double newX;
					Double newY;
					for (int i = 0; i < c; i++) {
						int vertexNumber = random.nextInt(6);
						Vertex vertex = star.head;
						for (int j = 0; j < vertexNumber; j++) {
							vertex = vertex.next;
						}
						double r1 = random.nextInt(1001)/1000.0;
						double r2 = random.nextInt(1001)/1000.0;
						if(vertex == star.head) {
							newX = (1 - Math.sqrt(r1)) * star.tail.getX() + (Math.sqrt(r1) * (1 - r2)) * vertex.getX() + (Math.sqrt(r1) * r2) * vertex.next.getX();
							newY = (1 - Math.sqrt(r1)) * star.tail.getY() + (Math.sqrt(r1) * (1 - r2)) * vertex.getY() + (Math.sqrt(r1) * r2) * vertex.next.getY();
						}else if(vertex == star.tail) {
							newX = (1 - Math.sqrt(r1)) * vertex.prev.getX() + (Math.sqrt(r1) * (1 - r2)) * vertex.getX() + (Math.sqrt(r1) * r2) * star.head.getX();
							newY = (1 - Math.sqrt(r1)) * vertex.prev.getY() + (Math.sqrt(r1) * (1 - r2)) * vertex.getY() + (Math.sqrt(r1) * r2) * star.head.getY();
						}else {
							newX = (1 - Math.sqrt(r1)) * vertex.prev.getX() + (Math.sqrt(r1) * (1 - r2)) * vertex.getX() + (Math.sqrt(r1) * r2) * vertex.next.getX();
							newY = (1 - Math.sqrt(r1)) * vertex.prev.getY() + (Math.sqrt(r1) * (1 - r2)) * vertex.getY() + (Math.sqrt(r1) * r2) * vertex.next.getY();
						}
						vertex.setX(newX);
						vertex.setY(newY);

						try {
							Thread.sleep(30);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}


				}
			};

//			Thread[] threads = new Thread[m];
//			for (int i = 0; i < threads.length; i++) {
//				threads[i] = new Thread(runnable);
//				threads[i].start();
//			}
//
//			for (Thread thread : threads) {
//				thread.join();
//			}

			star.drawPoly();
			// Write out the image
			File outputfile = new File("outputimage.png");
			ImageIO.write(img, "png", outputfile);

		} catch (Exception e) {
			System.out.println("ERROR " +e);
			e.printStackTrace();
		}
	}

	public star() {
		size = 0;
	}


	public void addVertexAtStart(double x, double y) {
		Vertex tmp = new Vertex(head, null,(x*100)+ 960,(y*100) + 540);
		if(head != null ) {head.prev = tmp;}
		head = tmp;
		if(tail == null) { tail = tmp;}
		size++;
		System.out.println("adding: (" + x +", " + y + ")");

	}
	public void addVertexAtEnd(double x, double y) {
		Vertex tmp = new Vertex(null, tail, (x*100) + 960, (y*100) + 540);
		if(tail != null) {tail.next = tmp;}
		tail = tmp;
		if(head == null) { head = tmp;}
		size++;
		System.out.println("adding: (" + x +", " + y + ")");
	}

	public void iterateForward(){

		System.out.println("iterating forward..");
		Vertex tmp = head;
		while(tmp != null){
			System.out.println("(" + tmp.getX() +", " + tmp.getY() + ")");
			tmp = tmp.next;
		}
	}

	public void drawPoly() {
		Graphics2D g2 = img.createGraphics();
		g2.setColor(new Color(200, 100, 50));

		Vertex tmp = head;
		while(tmp != null){
			if(tmp == tail) {
				g2.drawLine(tail.getX(),tail.getY(), head.getX(), head.getY());
				
			}else if(tmp != tail) {
				g2.drawLine(tmp.getX(), tmp.getY(), tmp.next.getX(), tmp.next.getY());
			}
			tmp = tmp.next;
		}
		//g2.drawline
	}
}
