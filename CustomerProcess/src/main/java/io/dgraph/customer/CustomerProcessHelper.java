package io.dgraph.customer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.dgraph.Transaction;
import io.dgraph.DgraphGrpc.DgraphStub;
import io.dgraph.DgraphProto.Mutation;
import io.dgraph.DgraphProto.Request;
import io.dgraph.DgraphProto.Response;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class CustomerProcessHelper {
	DgraphClient dgraphClient = null;

	//
	public CustomerProcessHelper() {
		ManagedChannel channel1 = ManagedChannelBuilder.forAddress("localhost", 9080).usePlaintext().build();
		DgraphStub stub1 = DgraphGrpc.newStub(channel1);
		//
		dgraphClient = new DgraphClient(stub1);
	}

	public Handler newCustomerHandler = new Handler() {

		@Override
		public void handle(Context ctx) throws Exception {
			System.out.println(ctx.body());
			ObjectMapper mapper = new ObjectMapper();
			JsonNode ctxObject = mapper.readTree(ctx.body());
			String customerName = ctxObject.get("name").toString();
			String customerTitle = ctxObject.get("title").toString();
			String customerUID ="";
			String processId="";
			Transaction txn = dgraphClient.newTransaction();

			String lastEvent = "CustomerCreated";
			try {

				String mutationString = "_:customer <dgraph.type> \"Customer\" .\n"
						+ "    _:customer <CustomerBase.name> " + customerName + " .\n"
						+ "    _:customer <CustomerBase.title> " + customerTitle + " .\n";
//						+ "    _:customer <CustomerBase.comments> \"Initialization of Customer Process\" .";
				Mutation mu1 = Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(mutationString)).build();

				Response response = txn.mutate(mu1);
				System.out.println(response);
				customerUID = response.getUidsMap().get("customer");

				mutationString = "_:newCustomerProcess <dgraph.type> \"Process\" .\n"
						+ "    _:newCustomerProcess <Process.customer> <" + customerUID + "> .\n"
						+ "    _:newCustomerProcess <Process.lastEventName> \"" + lastEvent + "\" .\n"
						+ "    _:newCustomerProcess <Process.name> \"Customer creation and distribution process\" .";
				System.out.println(mutationString);
				Mutation mu2 = Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(mutationString)).build();

				Response processCreationResponse = txn.mutate(mu2);
				System.out.println(processCreationResponse);
				processId=processCreationResponse.getUidsMap().get("newCustomerProcess");
				//
				txn.commit();

			} catch (Exception ex) {
				System.out.println(ex);
			} finally {
				txn.discard();
			}
			//respond with the process state
			Process process = new Process();
			process.setName("New Customer Creation");
			process.setLastEventName(lastEvent);
			process.setId(processId);
			//
			ctx.json(process);

		}
	};
	public Handler calculateNextStepHandler = new Handler() {

		@Override
		public void handle(Context ctx) throws Exception {
			System.out.println(ctx.body());
			ObjectMapper mapper = new ObjectMapper();
			JsonNode ctxObject = mapper.readTree(ctx.body());
			String processId = ctxObject.get("processId").toString();
			System.out.println(processId);
			Transaction txn = dgraphClient.newTransaction();

			String processQuery = "{query(func: uid(" + processId + ")) {\n" + "    uid\n" + "    Process.customerId\n"
					+ "    Process.lastEventName\n" + "    Process.name\n" + "  }\n" + "}\n" + "\n" + "";
			// get process detail with last event name
			Response processDetail = dgraphClient.newReadOnlyTransaction().query(processQuery);
			String processJson = processDetail.getJson().toStringUtf8();
			JsonNode processNode = mapper.readTree(processJson);
			String lastEventName = processNode.get("query").get(0).get("Process.lastEventName").asText();
			System.out.println(lastEventName);

			String nextStateDetailQuery = "{\n" + "  query(func: type(Transition)) @filter(eq(Transition.eventName, \""
					+ lastEventName + "\") ) @cascade @normalize{\n" + "    uid\n"
//					+ "    <Transition.fromState> @filter(eq(State.name, \"CreateCustomer\")) {\n"
                    + "    <Transition.fromState> {\n"					
					+ "    fromStateUID: uid\n fromState: State.name\n" + "  }\n" + "    <Transition.toState>{\n"
					+ "    nextState: State.name\n" + "  }\n" + "  }\n" + "}\n" + "";
			// get next event
			Response nextStateDetail = dgraphClient.newReadOnlyTransaction().query(nextStateDetailQuery);
			String nextStateJson = nextStateDetail.getJson().toStringUtf8();
			System.out.println(nextStateJson);
			JsonNode nextStateNode = mapper.readTree(nextStateJson);
			String nextState = nextStateNode.get("query").get(0).get("nextState").asText();
			System.out.println(nextState);
			String fromState = nextStateNode.get("query").get(0).get("fromState").asText();
			System.out.println(fromState);
			String fromStateUID = nextStateNode.get("query").get(0).get("fromStateUID").asText();
			System.out.println("from state uid is " +fromStateUID);
			String updatedFromStateUID="<"+fromStateUID+">";
			
			// update the process id table
			String mutationQuery = "query {\n" + "process as var(func: uid(" + processId + "))\n" + "}\n";
			Mutation mu1 = Mutation.newBuilder()
					.setSetNquads(ByteString.copyFromUtf8("uid(process) <Process.nextStep> \""+nextState+"\" .")).build();
			Mutation mu2 = Mutation.newBuilder()
					.setSetNquads(ByteString.copyFromUtf8("uid(process) <Process.history> "+updatedFromStateUID+" .")).build();
			System.out.println(mu2.toString());
			Request request = Request.newBuilder().setQuery(mutationQuery).addMutations(mu1).addMutations(mu2)
					.setCommitNow(true).build();
			System.out.println(request.getQuery() + "\n");
			Response processUpdateResponse = txn.doRequest(request);
			System.out.println(processUpdateResponse);

			//respond with the process state
			Process process = new Process();
			process.setName("New Customer Creation");
			process.setLastEventName(lastEventName);
			process.setNextStep(nextState);
			process.setId(processId);
			//
			ctx.json(process);
		}
	};
	
	//validate customer
	public Handler validateCustomerNameHandler= new Handler() {
		
		@Override
		public void handle(Context ctx) throws Exception {
			System.out.println(ctx.body());
			ObjectMapper mapper = new ObjectMapper();
			JsonNode ctxObject = mapper.readTree(ctx.body());
			String processId = ctxObject.get("processId").toString();
			String customerName = ctxObject.get("customerName").toString();
			System.out.println(processId + ":" + customerName);
			Transaction txn = dgraphClient.newTransaction();		
			
			//business logic
			String lastEventName="CustomerValidated";
			if(customerName.length()>1) {
				//update process with last event name
				String mutationQuery = "query {\n" + "process as var(func: uid(" + processId + "))\n" + "}\n";
				
				Mutation mu1 = Mutation.newBuilder()
						.setSetNquads(ByteString.copyFromUtf8("uid(process) <Process.lastEventName> \""+lastEventName+"\" .")).build();

				Request request = Request.newBuilder().setQuery(mutationQuery).addMutations(mu1)
						.setCommitNow(true).build();
				System.out.println(request.getQuery() + "\n");
				Response processUpdateResponse = txn.doRequest(request);
				System.out.println(processUpdateResponse);				
			}
			
			//respond with the process state
			Process process = new Process();
			process.setName("New Customer Creation");
			process.setLastEventName(lastEventName);
			process.setId(processId);
			//
			ctx.json(process);
		}
	};

}
