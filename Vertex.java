package q1;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Vertex {	//classic doubly linked list

	public Vertex next;
	public Vertex prev;
	public volatile boolean busy;	//true if vertex is being modified
	private double x;
	private double y;

	public Vertex(Vertex next, Vertex prev, double x, double y) {
		this.next = next;
		this.prev = prev;
		this.x = x;
		this.y = y;
		this.busy = false;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	public synchronized void setNewVertex(double x, double y) {	//making sure that updating a vertex by only 1 thread
		this.x = x;
		this.y = y;
	}
}