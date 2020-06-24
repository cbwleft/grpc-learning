import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class HelloWorldServer {
  private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50052;
    server = ServerBuilder.forPort(port)
        .addService(new GreeterImpl())
        .build()
        .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        HelloWorldServer.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final HelloWorldServer server = new HelloWorldServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(Hello.HelloRequest req, StreamObserver<Hello.HelloReply> responseObserver) {
      Hello.HelloReply reply = Hello.HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }

    @Override
    public void testCancel(Hello.HelloRequest req, StreamObserver<Hello.HelloReply> responseObserver) {
      try {
        Thread.sleep(10000);
        Hello.HelloReply reply = Hello.HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
        System.out.println("expensive operation is done");
      } catch (InterruptedException e) {
        responseObserver.onError(new StatusException(Status.ABORTED));
        System.out.println("expensive operation is canceled");
      }
    }

    @Override
    public void testCancelPropagation(Hello.HelloRequest request, StreamObserver<Hello.HelloReply> responseObserver) {
      HelloWorldClient helloWorldClient = new HelloWorldClient("localhost", 50052);
      ListenableFuture<Hello.HelloReply> helloReplyListenableFuture = helloWorldClient.testCancel(request.getName());
      try {
        Hello.HelloReply helloReply = helloReplyListenableFuture.get();
        responseObserver.onNext(helloReply);
        responseObserver.onCompleted();
      } catch (InterruptedException e) {
        responseObserver.onError(new StatusException(Status.ABORTED));
        e.printStackTrace();
      } catch (ExecutionException e) {
        responseObserver.onError(new StatusException(Status.UNKNOWN));
        e.printStackTrace();
      }


    }
  }
}
