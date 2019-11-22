package awsTest;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeImageAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ecr.model.DescribeImagesFilter;
import com.amazonaws.services.simplesystemsmanagement.model.transform.DescribeAvailablePatchesRequestMarshaller;


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
		int number;
		  
		while(true) {
			System.out.println("1. list instance");
			System.out.println("2. start instance");
			System.out.println("3. stop instance");
			System.out.println("4. reboot instance");
			System.out.println("5. create instance");
			System.out.println("6. image list");
			System.out.println("7. available zone");
			System.out.println("8. available region");
			System.out.println("Enter an integer : ");
		number = menu.nextInt();
		switch(number) {
		
		case 1 :
			listInstances();
			break;
		case 2:
			System.out.println("Enter Instance Id : ");
			startInstance(id_string.nextLine());
			break;
		case 3:
			System.out.println("Enter Instance Id : ");
			stopInstance(id_string.nextLine());
			break;
		case 4 :
			System.out.println("Enter Instance Id :");
			rebootInstance(id_string.nextLine());
			break;
		case 5 :
			System.out.println("Enter Instance ami_Image_Id :");
			String ami_Image_Id = id_string.nextLine();
			
			createInstance(ami_Image_Id);
			break;
		case 6 :
			getAmiImageList();
			break;
		case 7 :
			getAvailableZones();
			break;
		case 8 :
			getAvailableRegions();
			break;
		}
		}
		
	}

	private static void listInstances() {
		// TODO Auto-generated method stub
		
		//System.out.println(ec2.);
		System.out.println("Listing Instances..");
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
	
	private static void startInstance(String instance_Num) {
		
		List<String> list = getInstancesId();
		if(!list.contains(instance_Num)) {
			System.out.println("Not Valid Instance Id");
			return;
		}
		//Dry_Run --> 해당 인스턴스가 실제로 동작하는지를 먼저 테스트 : 과금을 하지않고 테스팅 가능
		DryRunSupportedRequest<StartInstancesRequest> dryRequest = () ->{
			StartInstancesRequest request = new StartInstancesRequest()
					.withInstanceIds(instance_Num);
			
			return request.getDryRunRequest();
		}; 
		
		DryRunResult dry_result = ec2.dryRun(dryRequest);
		
		if(!dry_result.isSuccessful()) {
			System.out.printf("Instance %s failed to start\n",instance_Num);
			throw dry_result.getDryRunResponse();
		}
		StartInstancesRequest request = new StartInstancesRequest()
												.withInstanceIds(instance_Num);
		ec2.startInstances(request);
		
		System.out.printf("Instance %s is successfully started!\n",instance_Num);
	}
	
	private static void stopInstance(String instance_Num) {
		
		List<String> list = getInstancesId();
		if(!list.contains(instance_Num)) {
			System.out.println("Not Valid Instance Id");
			return;
		}
		
		DryRunSupportedRequest<StopInstancesRequest> dryRequest = () ->{
			StopInstancesRequest request = new StopInstancesRequest()
					.withInstanceIds(instance_Num);
			
			return request.getDryRunRequest();
		}; 
		
		DryRunResult dry_result = ec2.dryRun(dryRequest);
		
		if(!dry_result.isSuccessful()) {
			System.out.printf("Instance %s failed to stop\n",instance_Num);
			throw dry_result.getDryRunResponse();
		}
		StopInstancesRequest request = new StopInstancesRequest()
												.withInstanceIds(instance_Num);
		ec2.stopInstances(request);
		
		System.out.printf("Instance %s is successfully stopped!\n",instance_Num);
	}
	
	private static void rebootInstance(String instance_Num) {
		List<String> list = getInstancesId();
		if(!list.contains(instance_Num)) {
			System.out.println("Not Valid Instance Id");
			return;
		}
		
		DryRunSupportedRequest<RebootInstancesRequest> dryRequest = () ->{
			RebootInstancesRequest request = new RebootInstancesRequest()
					.withInstanceIds(instance_Num);
			
			return request.getDryRunRequest();
		}; 
		
		DryRunResult dry_result = ec2.dryRun(dryRequest);
		
		if(!dry_result.isSuccessful()) {
			System.out.printf("Instance %s failed to reboot\n",instance_Num);
			throw dry_result.getDryRunResponse();
		}
		RebootInstancesRequest request = new RebootInstancesRequest()
												.withInstanceIds(instance_Num);
		ec2.rebootInstances(request);
		
		System.out.printf("Instance %s is successfully rebooted!\n",instance_Num);
		
	}
	
	private static void createInstance(String ami_Image_Id) {
		RunInstancesRequest run_request = new RunInstancesRequest().withImageId(ami_Image_Id)
												.withInstanceType(InstanceType.T2Micro)
												.withMaxCount(1).withMinCount(1);
		
		RunInstancesResult run_result = ec2.runInstances(run_request);
		
		String reservation_id = run_result.getReservation().getInstances().get(0).getInstanceId();
//		Tag tag = new Tag().withKey("Name").withValue(instance_Name);
//		
//		CreateTagsRequest tag_request = new CreateTagsRequest().withResources(ami_Image_Id).withTags(tag);
//		
//		CreateTagsResult tag_result = ec2.createTags(tag_request);
		
		System.out.printf("Instance is successfully created with ami-image : %s, name : %s\n"
							,ami_Image_Id,reservation_id);
		
	}
	private static List<String> getInstancesId() { //현재 생성된 instance들의 id값들을 가져옴

		List<String> list = new ArrayList<String>();
		
		boolean done = false;
	
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);
			
			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					list.add(instance.getInstanceId());
				}
			}
			
			request.setNextToken(response.getNextToken());
			
			if(response.getNextToken() == null)
				done = true;
		}
		return list;
	}
	
	private static void getAvailableRegions() {
		
		DescribeRegionsResult result = ec2.describeRegions();
		
		for(Region region : result.getRegions()) {
			System.out.printf(
					"[region] : %s " +
					"[endpoint] : %s\n",
					region.getRegionName(),region.getEndpoint()
					);
		}
	}
	
	private static void getAvailableZones() {
		
		DescribeAvailabilityZonesResult result 
						= ec2.describeAvailabilityZones();
		
		for(AvailabilityZone zone : result.getAvailabilityZones()) {
			System.out.printf(
					"[AvailabilityZone] : %s " +
					"[status] : %s " +
					"[region] : %s\n",
					zone.getZoneName(),zone.getState(),zone.getRegionName()
					);
		}
	}

	private static void getAmiImageList() { 
		
		System.out.println("Listing Images...");
		
		//private인 ami만 가져옴.
		Filter filter = new Filter().withName("is-public").withValues("false");
		DescribeImagesRequest request = new DescribeImagesRequest()
												.withFilters(filter);
		
		DescribeImagesResult response = ec2.describeImages(request);

		for(Image image : response.getImages()) {
			System.out.printf(
					"[ImageID] %s, " +
					"[Name] %s, " + 
					"[Owner] %s,",
					image.getImageId(),
					image.getName(),
					image.getOwnerId()			
					);
		}
	}
	
//	private static String getInstanceState(String instance_Num) { //해당 instance의 상태를 return
//		String result = null;
//		
//		return result;
//	}
}
