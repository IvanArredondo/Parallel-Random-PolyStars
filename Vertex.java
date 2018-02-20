package q1;

import java.util.Random;

public class Vertex {

	public Vertex next;
	public Vertex prev;
	private double x;
	private double y;

	public Vertex(Vertex next, Vertex prev, double x, double y) {
		this.next = next;
		this.prev = prev;
		this.setX(x);
		this.setY(y);
	}
	

	public int getX() {
		return (int)x;
	}

	public synchronized void setX(double x) {
		this.x = x;
	}

	public int getY() {
		return (int)y;
	}

	public synchronized void setY(double y) {
		this.y = y;
	}
	
	
}