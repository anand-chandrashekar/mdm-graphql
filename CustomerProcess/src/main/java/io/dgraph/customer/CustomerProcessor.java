package io.dgraph.customer;

import io.javalin.Javalin;
import io.javalin.http.Handler;


public class CustomerProcessor {

	public static void main(String[] args) {
		Javalin app = Javalin.create().start(5050);
		CustomerProcessHelper helper=new CustomerProcessHelper();
		
//      process a new customer		
		Handler newCustomerHandler=helper.newCustomerHandler;
		app.post("/processNewCustomer", newCustomerHandler);
//		calculate next step for a customer process		
		Handler calculateNextStep=helper.calculateNextStepHandler;
		app.post("/calculateNextStep", calculateNextStep);
//		validate customer name		
		Handler validateCustomerName=helper.validateCustomerNameHandler;
		app.post("/validateCustomerName", validateCustomerName);
	}
}
