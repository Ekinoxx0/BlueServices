package io.blueservices.entreprises;

public class PlatformRun {
	
	public final long runnerId;
	public final long runTime = System.currentTimeMillis();
	private long paidAt = -1;
	private final int paidAmount;
	
	public PlatformRun(long runnerId, PlatformCompany comp) {
		this.runnerId = runnerId;
		this.paidAmount = comp.getPrimePrice();
	}
	
	public boolean isPaid() {
		return paidAt > 0;
	}
	
	public void pay() {
		this.paidAt = System.currentTimeMillis();
	}
	
	public void cancel() {
		this.paidAt = -1;
	}
	
	public long whenPaid() {
		return this.paidAt;
	}
	
	public int howMuch() {
		return this.paidAmount;
	}
}
