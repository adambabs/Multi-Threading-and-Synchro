
// Adam Babs
// 201 355 820
// A.M.Babs@student.liverpool.ac.uk

class Cubicle {
	
	private boolean full = true; // initially, cubicle contains a tablet
	private int tablet;
  
	// Constructor stores a value on the tablet
	public Cubicle (int value){
		tablet = value;
	}

	// Synchronized remove method to prevent simultaneous access to cubicle.
	// Returns value on tablet, zero if no tablet is present
	public synchronized int remove() {
		int val = 0;
		if (full)
			val = tablet;
		full = false;
		return val;
	}
}

class Box {
	
	// value inscribed from 3 tablets by each Apprentice
	private int grandTotal = 0;
	
	// list to represent the box 
	int[] box = new int[2];
	
	// takes two values, sum of 3 tablets and Apprentice's ID
	public synchronized void put(int x, int id){	
		
		// while box is full -wait
		while(box[0] != 0 && box[1] != 0) {
			try{ 
				System.out.println("Box is full, Apprentice " + id + " waits");
				wait();
			} 
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		
		// if both places in the box are empty, choose randomly which place to append
		if (box[0] == 0 && box[1] == 0){
			int placeInBox = (int)(Math.random()* 2);
			box[placeInBox] = x;
			System.out.println("Apprentice " + id + " puts the Parchment of Partial Enlightment into the box, value: " + box[placeInBox]);
		}
		
		// if first place is empty put the value on the first place
		else if (box[0] == 0 && box[1] !=0) {
			box[0] = x;
			System.out.println("Apprentice " + id + " puts the Parchment of Partial Enlightment into the box, value: " + box[0]);
		}
		
		// if the second place is empty put the value on the second place
		else if (box[0] != 0 && box[1] == 0) {
			box[1] = x;
			System.out.println("Apprentice " + id + " puts the Parchment of Partial Enlightment into the box, value: " + box[1]);
		}
		notify();
	}
	
	public synchronized int removeBox() {
		
		// while there is nothing to remove in the box -wait
		while(box[0] == 0 && box[1] == 0){
			try{
				System.out.println("Volumina checks if there is anything to remove but the box is empty");
				wait();
			} 
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		
		// if both places in the box are full, choose randomly which place to remove
		if (box[0] != 0 && box[1] != 0){
			int placeInBox = (int)(Math.random()* 2);
			grandTotal = box[placeInBox];
			box[placeInBox] = 0;
		}
		
		// if first place is full and second empty, choose the first one to remove
		else if (box[0] != 0 && box[1] == 0) {
			grandTotal = box[0];
			box[0] = 0;	
		}
		
		// if second place is full and second empty, choose the second one to remove 
		else if (box[0] == 0 && box[1] != 0) {
			grandTotal = box[1];
			box[1] = 0;
		}	
		
		notify();
		
		// Volumina removes one Parchement from the box and tells what is its' value
		System.out.println("Volumina removes the Parchement of Partial Enlightment from the box and saves it, value: " + grandTotal);
	
		return grandTotal;
	}
}

class Volumina extends Thread {
	
	private Box b; 
	int totalFinal;
	
	// Constructor. Volumina needs access to the box
	public Volumina(Box box) {
		b = box;
	}
	
	public void run() {
		
		// get 6 parchements- we have 6 Apprentices and each of them inscribes the tablets on the Parchment
		for (int i = 0; i<6; i++){
			
			// call removeBox synchronized method which returns the value which was put in the box and stores it in the variable totalFinal
			int oneParchement = b.removeBox();
			totalFinal = totalFinal + oneParchement;
			
			// Yield to allow fairer scheduling of threads
			Thread.yield();
		}
		System.out.println("Volumina announces a total of " + totalFinal); 
	}
}

class Apprentice extends Thread {
	
	private Cubicle row[];
	private int id;
	private Box b;
	
	// Constructor. Apprentice needs access to row of cubicles and to the box.
	// Each Apprentice has unique ID.
	public Apprentice(Cubicle cubrow[], int iden, Box box) {
		row = cubrow;
		id = iden;	
		b = box;
	}

    public void run(){
		int ncubs = row.length - 1;
		int tot = 0; 
		int value;
		
		// Get 3 tablets
		for (int i = 0; i < 3; i++) { 
	  
			int num = (int)(Math.random()*ncubs) + 1; // Select random cubicle

			// If cubicle is empty, move along until we find a full one
			while ((value = row[num].remove()) == 0) {
				num++;
				if (num > ncubs) num = 1;
			}
			
			// Announce the tablet value and add it to total 
			System.out.println("Apprentice " + id + " has retrieved a tablet and inscribes it on the Parchement of Partial Enlightment "); 
			tot += value;
			
			// Yield to allow fairer scheduling of threads
			Thread.yield();
			
		}
		
		// pass the total value from three tablets (Parchement of Partial Enlighment) 
		// and Apprentice ID to the 'put' synchronized method which puts the Parchement of Partial Enlighment into the box
		b.put(tot, id); 
	}
}

public class Assignment2 { 

	// We will have 18 cubicles and 6 Apprentices
	private static final int NUMCUBS = 18;
	private static final int NUMAPPS = 6;

	public static void main(String args[]) {
		
		// Instantiate the object of the Box class
		Box box = new Box();
		   
		// Set up array of cubicles. Have an extra one so that we can index from 1.
		// Initialise each tablet value to be same as cubicle no.
		Cubicle row[] = new Cubicle[NUMCUBS+1];
		for (int i = 1; i <= NUMCUBS; i++)
			row[i] = new Cubicle(i);

		// Set up array of Apprentices
		Apprentice apprentices[] = new Apprentice[NUMAPPS];
		for (int i = 0; i < NUMAPPS; i++)
			apprentices[i] = new Apprentice(row, i+1, box);
		
		// Set up the volumina thread
		Volumina volumina = new Volumina(box);
		
		// Start up the Apprentice threads to run concurrently
		// Start up the Volumina thread to run concurrently with Apprentice threads
		for (int i = 0; i < NUMAPPS; i++)
			apprentices[i].start();
			volumina.start();
	}
}