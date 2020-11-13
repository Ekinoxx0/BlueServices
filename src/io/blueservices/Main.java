package io.blueservices;
public class Main {
	
	public static BlueServices SERVICES;
	
	public static void main(String[] args) {
		SERVICES = new BlueServices();
		SERVICES.login();
	}
	
}
