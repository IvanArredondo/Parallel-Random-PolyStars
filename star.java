package q1;

import java.awt.Color; 
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.*;


public class star {

	// The image constructed
	public static BufferedImage img;

	public Vertex head;
	public Vertex tail;

	static Object lock = new Object();

	// Image dimensions; you can modify these for bigger/smaller images
	public static int width = 1920;
	public static int height = 1080;

	static Random random = new Random();

	public static int m;
	public static int c;
	public static int size;
	public static double scaleX;
	public static double scaleY;
	public static double scaleFactor;

	static AtomicInteger counter = new AtomicInteger(0);

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

					double newX;
					double newY;
					for (int i = 0; i < c; i++) {
						int vertexNumber = random.nextInt(6);
						Vertex vertex = star.head;
						for (int j = 0; j < vertexNumber; j++) {
							vertex = vertex.next;
						}
						synchronized (lock) {

							if(vertex == star.head) {
								while(star.tail.busy || vertex.busy || vertex.next.busy) {
									System.out.println("waiting");
									try {
										lock.wait();
									} catch (Exception e) {
										// TODO: handle exception
									}
								}
								star.tail.busy = true;
								vertex.next.busy = true;
							}else if(vertex == star.tail) {
								while(vertex.prev.busy || vertex.busy || star.head.busy) {
									System.out.println("waiting");
									try {
										lock.wait();
									} catch (Exception e) {
										// TODO: handle exception
									}
								}
								star.head.busy = true;
								vertex.prev.busy = true;
							}else {
								while(vertex.prev.busy || vertex.busy || vertex.next.busy) {
									System.out.println("waiting");
									try {
										lock.wait();
									} catch (Exception e) {
										// TODO: handle exception
									}
								}
								vertex.prev.busy = true;
								vertex.next.busy = true;
							}

							vertex.busy = true;

						}
						double r1 = random.nextDouble();
						double r2 = random.nextDouble();
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
						vertex.setNewVertex(newX, newY);
						synchronized (lock) {
							if(vertex == star.head) {
								star.tail.busy = false;
								vertex.next.busy = false;
							}else if(vertex == star.tail) {
								star.head.busy = false;
								vertex.prev.busy = false;
							}else {
								vertex.prev.busy = false;
								vertex.next.busy = false;
							}
							vertex.busy = false;
							lock.notify();
						}

						try {
							Thread.sleep(30);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					System.out.println("all done " + counter.getAndIncrement());
				}
			};

			Thread[] threads = new Thread[m];
			for (int i = 0; i < threads.length; i++) {
				threads[i] = new Thread(runnable);
				threads[i].start();
			}

			for (Thread thread : threads) {
				thread.join();
			}
			
			
			star.adjustSize();
			star.iterateForward();

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
		Vertex tmp = new Vertex(null, tail, x, y);
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
			System.out.println("(" + ((tmp.getX()*scaleFactor)) + ", " + ((tmp.getY()*scaleFactor)) + ")");
			tmp = tmp.next;
		}
	}
	
	public void adjustSize() {
		double maxX = -Double.MAX_VALUE;
		double minX = Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		Vertex tmp = head;
		while(tmp != null){
			if(tmp.getX() > maxX) {
				maxX = tmp.getX();
			}
			if(tmp.getX() < minX) {
				minX = tmp.getX();
			}
			if(tmp.getY() > maxY) {
				maxY = tmp.getY();
			}
			if(tmp.getY() < minY) {
				minY = tmp.getY();
			}
			tmp = tmp.next;
		}
		
		System.out.println("maxX: " + maxX);
		System.out.println("minX: " + minX);
		System.out.println("maxY: " + maxY);
		System.out.println("minY: " + minY);
		
		double differenceX = Math.abs(maxX) - Math.abs(minX);
		double differenceY = Math.abs(maxY) - Math.abs(minY);
		
		if(Math.abs(differenceX) >= Math.abs(differenceY)) {
			scaleFactor = 540.0/maxX;
		}else {
			scaleFactor =960.0/maxY;
		}
		
		
		
		System.out.println("difference x: " + differenceX);
		System.out.println("difference y: " + differenceY);
	}

	public void drawPoly() {
		Graphics2D g2 = img.createGraphics();
		g2.setColor(new Color(200, 100, 50));

		Vertex tmp = head;
		while(tmp != null){
			if(tmp == tail) {
				g2.drawLine((int)((tail.getX()*scaleFactor)+960.0),(int)((tail.getY()*scaleFactor) + 540.0), (int)((head.getX()*scaleFactor)+960.0), (int)((head.getY()*scaleFactor)+540.0));

			}else if(tmp != tail) {
				g2.drawLine((int)((tmp.getX()*scaleFactor)+960.0), (int)((tmp.getY()*scaleFactor)+540.0), (int)((tmp.next.getX()*scaleFactor)+960.0), (int)((tmp.next.getY()*scaleFactor)+540.0));
			}
			tmp = tmp.next;
		}
		//g2.drawline
	}
}
