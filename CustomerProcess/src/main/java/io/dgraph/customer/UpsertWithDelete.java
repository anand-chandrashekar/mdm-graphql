package io.dgraph.customer;

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

public class UpsertWithDelete {

	public static void main(String[] args) {
		DgraphClient dgraphClient = null;

		ManagedChannel channel1 = ManagedChannelBuilder.forAddress("localhost", 9080).usePlaintext().build();
		DgraphStub stub1 = DgraphGrpc.newStub(channel1);
		//
		dgraphClient = new DgraphClient(stub1);
		Transaction txn = dgraphClient.newTransaction();

		try {
			String mutationQuery = "query {\n" + "V as var(func: type(Org)) @filter(eq(OrgLocation, \"D\"))\n" + "}\n";
			Mutation mu1 = Mutation.newBuilder()
					.setDelNquads(ByteString.copyFromUtf8("uid(V) * * .")).build();

			Request request = Request.newBuilder().setQuery(mutationQuery).addMutations(mu1)
					.setCommitNow(true).build();
			System.out.println(request.getQuery() + "\n");
			Response processUpdateResponse = txn.doRequest(request);
			System.out.println(processUpdateResponse);	

		} catch (Exception ex) {
			System.out.println(ex);
		} finally {
			// Clean up. Calling this after txn.commit() is a no-op
			// and hence safe.
			txn.discard();
		}
	}

}
