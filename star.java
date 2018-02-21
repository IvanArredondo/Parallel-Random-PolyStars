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

	boolean furthestIsX = false;

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

			m = Integer.parseInt(args[0]);	//the number of threads to be made

			c = Integer.parseInt(args[1]);	//the number of times each thread will alter the star

			// create an image and initialize it to all 0's, taken from assigment 1
			img = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			for (int i=0;i<width;i++) {
				for (int j=0;j<height;j++) {
					img.setRGB(i,j,0);
				}
			}

			star star = new star();
			star.addVertexAtEnd(-1.0, 5.0);	//harcoded beginning star
			star.addVertexAtEnd(1.0, 2.0);
			star.addVertexAtEnd(5.0, 0.0);
			star.addVertexAtEnd(1.0, -2.0);
			star.addVertexAtEnd(-4.0, -4.0);
			star.addVertexAtEnd(-3.0, -2.0);

			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					double newX;
					double newY;
					for (int i = 0; i < c; i++) {
						int vertexNumber = random.nextInt(6);	//decide which vertex the thread will modify
						Vertex vertex = star.head;
						for (int j = 0; j < vertexNumber; j++) {	//iterate through tht doubly linked list to get the vertex
							vertex = vertex.next;	
						}
						synchronized (lock) {	//make sure that the vertex, or its neighbors are not currently being modified
							if(vertex == star.head) {	//need to do this because not a circular doubly linked list, thus need to make the previous node of head point to tail
								while(star.tail.busy || vertex.busy || vertex.next.busy) {	//if one of the vertices are being modified, wait 
									try {
										lock.wait(); //since I call wait(), this seems to not be considered spinning
									} catch (Exception e) {
										// TODO: handle exception
									}
								}
								star.tail.busy = true;	//once the vertices free up, set their busy flags to true;
								vertex.next.busy = true;
							}else if(vertex == star.tail) { //need to do this because not a circular doubly linked list, thus need to make the next node of tail point to head
								while(vertex.prev.busy || vertex.busy || star.head.busy) { //if one of the vertices are being modified, wait 
									try {
										lock.wait();
									} catch (Exception e) {
										// TODO: handle exception
									}
								}
								star.head.busy = true;
								vertex.prev.busy = true;
							}else {
								while(vertex.prev.busy || vertex.busy || vertex.next.busy) {	//if the random vertex is neither the head or tail
									try {
										lock.wait();
									} catch (Exception e) {
										// TODO: handle exception
									}
								}
								vertex.prev.busy = true;
								vertex.next.busy = true;
							}

							vertex.busy = true;	//in either of the three cases, set the random vertex have a busy flag of true
						}
						//the best case scenario, since theres 6 vertices, is that 2 threads are modifying at once 
						double r1 = random.nextDouble();
						double r2 = random.nextDouble();
						if(vertex == star.head) {//if its the head, the previous is the tail
							//this equation gets a random uniform point in a triangle of 3 points. From section 4.2 of : http://www.cs.princeton.edu/~funk/tog02.pdf
							newX = (1 - Math.sqrt(r1)) * star.tail.getX() + (Math.sqrt(r1) * (1 - r2)) * vertex.getX() + (Math.sqrt(r1) * r2) * vertex.next.getX();
							newY = (1 - Math.sqrt(r1)) * star.tail.getY() + (Math.sqrt(r1) * (1 - r2)) * vertex.getY() + (Math.sqrt(r1) * r2) * vertex.next.getY();
						}else if(vertex == star.tail) {	//if its the tail, the next node is the head
							newX = (1 - Math.sqrt(r1)) * vertex.prev.getX() + (Math.sqrt(r1) * (1 - r2)) * vertex.getX() + (Math.sqrt(r1) * r2) * star.head.getX();
							newY = (1 - Math.sqrt(r1)) * vertex.prev.getY() + (Math.sqrt(r1) * (1 - r2)) * vertex.getY() + (Math.sqrt(r1) * r2) * star.head.getY();
						}else {
							newX = (1 - Math.sqrt(r1)) * vertex.prev.getX() + (Math.sqrt(r1) * (1 - r2)) * vertex.getX() + (Math.sqrt(r1) * r2) * vertex.next.getX();
							newY = (1 - Math.sqrt(r1)) * vertex.prev.getY() + (Math.sqrt(r1) * (1 - r2)) * vertex.getY() + (Math.sqrt(r1) * r2) * vertex.next.getY();
						}
						vertex.setNewVertex(newX, newY);
						synchronized (lock) {	//to set all the busy flags to false, and notify the waiting threads that their vertices are free now
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
							Thread.sleep(30);	//sleep as per instructions
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			};

			Thread[] threads = new Thread[m];	//create an array of m threads
			for (int i = 0; i < threads.length; i++) {	//create m threads and start them 
				threads[i] = new Thread(runnable);
				threads[i].start();
			}

			for (Thread thread : threads) {	//for all the threads in the array, join them
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
		Vertex tmp = new Vertex(head, null,x,y);
		if(head != null ) {head.prev = tmp;}	//if theres a head, make the previous node of the head point to you, and make head be you
		head = tmp;
		if(tail == null) { tail = tmp;}
		size++;	//increase number of nodes counter
		System.out.println("adding: (" + x +", " + y + ")");

	}
	public void addVertexAtEnd(double x, double y) {	
		Vertex tmp = new Vertex(null, tail, x, y);
		if(tail != null) {tail.next = tmp;}	//if theres a tail, make the next node of the current tail be you, and make tail now point to you
		tail = tmp;
		if(head == null) { head = tmp;}	//if theres not head, be the head
		size++;
	}

	public void iterateForward(){
		Vertex tmp = head;
		while(tmp != null){	//iterate until theres no nodes left
			System.out.println("(" + ((tmp.getX()*scaleFactor)) + ", " + ((tmp.getY()*scaleFactor)) + ")");
			tmp = tmp.next;
		}
	}

	public void adjustSize() {	//not really working
		double maxX = -Double.MAX_VALUE;
		double minX = Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;

		double overallX;
		double overallY;
		double overall;

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
		if(Math.abs(maxX)> Math.abs(minX)) {
			overallX = Math.abs(maxX);
		}else {
			overallX = Math.abs(minX);
		}
		if(Math.abs(maxY)> Math.abs(minY)) {
			overallY = Math.abs(maxY);
		}else {
			overallY = Math.abs(minY);
		}

		if(overallX > overallY) {
			overall = overallX;
			scaleFactor = 960.0/overall;
			furthestIsX = true;
		}else {
			overall = overallY;
			scaleFactor = 540.0/overall;
			furthestIsX = false;
		}
	}

	public void drawPoly() {
		Graphics2D g2 = img.createGraphics();
		g2.setColor(new Color(200, 100, 50));

		Vertex tmp = head;
		while(tmp != null){
			if(tmp == tail) {	//if its the las element, connect it to the first element
				g2.drawLine((int)((tail.getX()*scaleFactor)+960.0),(int)((tail.getY()*scaleFactor) + 540.0), (int)((head.getX()*scaleFactor)+960.0), (int)((head.getY()*scaleFactor)+540.0));

			}else if(tmp != tail) {
				g2.drawLine((int)((tmp.getX()*scaleFactor)+960.0), (int)((tmp.getY()*scaleFactor)+540.0), (int)((tmp.next.getX()*scaleFactor)+960.0), (int)((tmp.next.getY()*scaleFactor)+540.0));
			}
			tmp = tmp.next;
		}
		//g2.drawline
	}
}
