package thread;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

class CD {
	String name;
	boolean hasRented;

	public CD(String name) {
		this.name = name;
		this.hasRented = false;
	}
}

class CDs {
	CD cd;
	int count;

	public CDs(CD cd) {
		this.cd = cd;
		this.count = 10;
	}

	public CDs(CD cd, int count) {
		super();
		this.cd = cd;
		this.count = count;
	}
}

public class CDShop {
	String[] CDNames = { "Yesterday Once More", "Unchained melody", "My Heart Will Go On", "I will always love you",
			"Say You Say Me", "Don't cry", "Right Here Waiting", "Careless whisper", "More Than I Can Say",
			"Casablanca" };

	FileOutputStream fileOutputStream;

	public class PurchaseThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					purchaseCD();
					sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("Emergency purchase!");
					continue;
				}
			}
		}
	}

	public synchronized void purchaseCD() throws InterruptedException {
		date = new Date(System.currentTimeMillis());
		System.out.println("Start to purchase at " + formatter.format(date));
		for (int i = 0; i < 10; i++) {
			if (forSale[i].count < 10) {
				int purchaseCount = 10 - forSale[i].count;
				System.out.println("purchase " + purchaseCount + " CD \"" + forSale[i].cd.name + "\"");
				forSale[i].count = 10;
			}
		}
		printCDNums();
		notifyAll();
		Thread.sleep(100);
	}

	public class RentThread extends Thread {
		@Override
		public void run() {
			while (true) {
				int rand = rd.nextInt(10);
				rentCD(rand);
				try {
					sleep(200 + rd.nextInt(100));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				returnCD(rand);
			}

		}
	}

	public class SellThread extends Thread {
		@Override
		public void run() {
			while (true) {
				int rdKind = rd.nextInt(10);
				int rdCount = rd.nextInt(10) + 1;
				try {
					sellCD(rdKind, rdCount);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	public synchronized void printCDNums() {
		System.out.print("Current CD nums: ");
		for (int i = 0; i < 10; i++) {
			System.out.print(forSale[i].count + " ");
		}
		System.out.println("\n");
	}

	public CD[] forRent = new CD[10];
	public CDs[] forSale = new CDs[10];
	public Random rd = new Random();
	public Date date = new Date(System.currentTimeMillis());
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	public synchronized void sellCD(int kind, int count) throws InterruptedException {
		Thread.sleep(rd.nextInt(200));
		date = new Date(System.currentTimeMillis());
		System.out.println(Thread.currentThread().getName());
		System.out
				.println("try to buy " + count + " CD \"" + forSale[kind].cd.name + "\" at " + formatter.format(date));
		if (forSale[kind].count < count) {
			int rdNum = rd.nextInt(100);
			if (rdNum < 50) {
				System.out.println("The CD is not enough.");
				while (forSale[kind].count < count) {
					try {
						System.out.println(Thread.currentThread().getName() + " is waiting for purchase...");
						purchaseThread.interrupt();
						wait();
					} catch (InterruptedException e) {
						break;
					}
				}
			} else {
				System.out.println("Abandon to sale!");
				printCDNums();
				return;
			}
		}
		forSale[kind].count -= count;
		date = new Date(System.currentTimeMillis());
		System.out.println("Success at " + formatter.format(date) + "!");
		printCDNums();

	}

	public synchronized void rentCD(int kind) {
		System.out.println(Thread.currentThread().getName());
		try {
			Thread.sleep(rd.nextInt(200));
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		date = new Date(System.currentTimeMillis());
		System.out.println("Try to rent CD \"" + forRent[kind].name + "\" at " + formatter.format(date));
		if (forRent[kind].hasRented) {
			System.out.println("The CD has been rented!");
			int rand = rd.nextInt(100);
			if (rand < 50) {
				System.out.println(Thread.currentThread().getName() + " waiting for return...");
				while (forRent[kind].hasRented) {
					try {
						wait();
					} catch (InterruptedException e) {
						break;
					}
				}
			} else {
				System.out.println("Abandon to rent!");
				return;
			}
		}
		forRent[kind].hasRented = true;
		System.out.println("The CD is rented successfully.");
		printRentStatus();
	}

	public synchronized void returnCD(int kind) {
		System.out.println(Thread.currentThread().getName());
		if (!forRent[kind].hasRented) {
			System.out.println("The CD has been returned.");
		} else {
			date = new Date(System.currentTimeMillis());
			System.out.println("The CD \"" + forRent[kind].name + "\" is returned successfully at "
					+ formatter.format(date) + ".");
			forRent[kind].hasRented = false;
			notifyAll();
		}
	}

	public synchronized void printRentStatus() {
		System.out.print("current CD for rent: ");
		for (var i : forRent) {
			System.out.print(i.hasRented ? "0 " : "1 ");
		}
		System.out.println("\n");
	}

	public CDShop() {
		for (int i = 0; i < 10; i++) {
			forRent[i] = new CD(i + "");
			forSale[i] = new CDs(new CD(i + ""));
		}
		purchaseThread.setDaemon(true);
	}

	public CDShop.PurchaseThread purchaseThread = new PurchaseThread();

	public static void main(String[] args) throws FileNotFoundException {
		PrintStream ps = new PrintStream(new FileOutputStream("record.txt", true));
		System.setOut(ps);
		CDShop shop = new CDShop();
		CDShop.SellThread sellThread1 = shop.new SellThread();
		CDShop.SellThread sellThread2 = shop.new SellThread();
		CDShop.SellThread sellThread3 = shop.new SellThread();
		CDShop.RentThread rentThread1 = shop.new RentThread();
		CDShop.RentThread rentThread2 = shop.new RentThread();
		CDShop.RentThread rentThread3 = shop.new RentThread();
		shop.purchaseThread.start();
		sellThread1.setDaemon(true);
		sellThread1.setName("SellThread--1");
		sellThread1.start();
		sellThread2.setDaemon(true);
		sellThread2.setName("SellThread--2");
		sellThread2.start();
		sellThread3.setDaemon(true);
		sellThread3.setName("SellThread--3");
		sellThread3.start();
		rentThread1.setName("RentThread--1");
		rentThread1.setDaemon(true);
		rentThread1.start();
		rentThread2.setName("RentThread--2");
		rentThread2.setDaemon(true);
		rentThread2.start();
		rentThread3.setName("RentThread--3");
		rentThread3.setDaemon(true);
		rentThread3.start();

		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Program have finished!");
		}
	}
}