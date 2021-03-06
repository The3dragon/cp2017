package cp;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class ExecutorProducersConsumers
{
	private static class Product {
		private final String name;
		private final String attributes;
		public Product( String name, String attributes )
		{
			this.name = name;
			this.attributes = attributes;
		}
		
		public String toString()
		{
			return name + ". " + attributes;
		}
	}
	
	private static final BlockingDeque< Product > THE_LIST = new LinkedBlockingDeque<>();
	
	private static void produce( BlockingDeque< Product > list, String threadName, ExecutorService executor )
	{
		IntStream.range( 1, 2000 ).forEach( i -> {
				Product prod = new Product( "Water Bottle", "Liters: " + i + ". By thread: " + threadName );
				list.add( prod );
				executor.submit( () -> {
					consume( THE_LIST, "Consumer" + i );
				} );
//				new Thread( () -> {
//					consume( THE_LIST, "Consumer" + i );
//				} ).start();
				//System.out.println( threadName + " producing " + prod );
		} );
	}
	
	private static void consume( BlockingDeque< Product > list, String threadName )
	{
		try {
			Product prod = list.takeFirst();
			//System.out.println( threadName + " consuming " + prod.toString() );
		} catch( InterruptedException e ) {}
	}
	
	private static final int NUM_PRODUCERS = 3;
	
	public static void run()
	{
		ExecutorService executor = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() + 1 );
		CountDownLatch latch = new CountDownLatch( NUM_PRODUCERS );
		
		// Proposal 1: Before the consumer waits, it checks if something is in the list.
		// Proposal 2: Before the producer sends the signal, it checks if a consumer is waiting.
		IntStream.range( 0, NUM_PRODUCERS ).forEach(
		i -> {
			new Thread( () -> {
				produce( THE_LIST, "Producer" + i, executor );
				latch.countDown();
			} ).start();
		} );
		
		try {
			latch.await();
			executor.shutdown();
			executor.awaitTermination( 1L, TimeUnit.DAYS );
		} catch( InterruptedException e ) {}
	}
}
