package awsTest;

import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;


public class awsTest {

	static AmazonEC2 ec2;
	
	private static void init() throws Exception {
		
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch(Exception e) {
			throw new AmazonClientException("not valid location");
		}
		ec2 = AmazonEC2ClientBuilder.standard()
				.withCredentials(credentialsProvider)
				.withRegion("us-east-1")
				.build();
	}
	
	public static void main(String[] args) throws Exception {
		
		init();
		
		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);
		int number = 0;
		
		while(true) {
			System.out.println("1. list instance");
			System.out.println("Enter an integer : ");
		number = menu.nextInt();
		
		switch(number) {
		
		case 1 :
			listInstances();
			break;
		}
		}
		
	}

	private static void listInstances() {
		// TODO Auto-generated method stub
		
		//System.out.println(ec2.);
		System.out.println("Listing instances...");
		boolean done = false;
		
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);
			
			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
							"[id] %s, " +
							"[AMI] %s, " + 
							"[type] %s, " +
							"[state] %10s, " +
							"[monitoring state] %s",
							instance.getInstanceId(),
							instance.getImageId(),
							instance.getInstanceType(),
							instance.getState().getName(),
							instance.getMonitoring().getState()
							);
				}
				System.out.println();
			}
			
			request.setNextToken(response.getNextToken());
			
			if(response.getNextToken() == null)
				done = true;
		}
	}
	
}
