import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Part_A {

	private static ShipPort shipPort = new ShipPort();
	private static TrainPort trainPort = new TrainPort();
	private static TrainStorageUnit trainStorageUnit = new TrainStorageUnit();	
	private static TrailerPort trailerPort = new TrailerPort();
	private static TrailerStorageUnit trailerStorageUnit = new TrailerStorageUnit();	

	private static Queue<Ship> shipQueue = new LinkedList<>(); // Queue for ship that waiting the port
	private static Queue<Ship> dock1 = new LinkedList<>();
	private static Queue<Ship> dock2 = new LinkedList<>();

	private static Queue<Train> trainQueue = new LinkedList<>(); //train waiting 
	private static LinkedList<Train> trainServe = new LinkedList<>(); // Train can park at 1

	private static Queue<Train> storageQueueTrain = new LinkedList <>(); // storage queue
	private static Queue<Train> storageServeTrain = new LinkedList <>();

	private static Queue<Trailer> trailerQueue = new LinkedList<>(); //train waiting 
	private static LinkedList<Trailer> trailerServe = new LinkedList<>(); // Train can park at 1

	private static Queue<Trailer> storageQueueTrailer = new LinkedList <>(); // storage queue
	private static Queue<Trailer> storageServeTrailer = new LinkedList <>();

	private static Train train;
	private static Trailer trailer;
	private static Gate gate = new Gate(false);


	private static Lock lock = new ReentrantLock();

	private static Condition portIsEmpty = lock.newCondition();

	private static Condition trainArrived = lock.newCondition();
	private static Condition trainServeIsFull = lock.newCondition();
	private static Condition trainDockIsFull = lock.newCondition();
	private static Condition trainLoaded = lock.newCondition();
	private static Condition storageQueueTrainIsFull = lock.newCondition();
	private static Condition trainArrivedCrossSection = lock.newCondition();
	private static Condition trainPassedCrossSection = lock.newCondition();
	private static Condition trainCargoArrived = lock.newCondition();
	private static Condition trainCargoParked = lock.newCondition();
	private static Condition trainCargoUnloaded = lock.newCondition();
	private static Condition trainPortIsEmpty = lock.newCondition();
	private static Condition trainStorageQueueIsEmpty = lock.newCondition();

	private static Condition trailerArrived = lock.newCondition();
	private static Condition trailerServeIsFull = lock.newCondition();
	private static Condition trailerDockIsFull = lock.newCondition();
	private static Condition trailerPortIsEmpty = lock.newCondition();
	private static Condition trailerLoaded = lock.newCondition();
	private static Condition trailerArrivedCrossSection = lock.newCondition();
	private static Condition trailerPassedCrossSection = lock.newCondition();
	private static Condition storageQueueTrailerIsFull = lock.newCondition();
	private static Condition trailerCargoArrived = lock.newCondition();
	private static Condition trailerStorageIsEmpty = lock.newCondition();
	private static Condition trailerCargoParked = lock.newCondition();
	private static Condition trailerCargoUnloaded = lock.newCondition();

	private static Condition gateOpen = lock.newCondition();
	private static boolean trainPassingCrossSection = false;

	private static boolean trainLoadedCondition = false;

	public static void main(String[] args) {

		ExecutorService executor = Executors.newFixedThreadPool(28);// create thread pool

		executor.execute(new ShipArrivalTask());
		executor.execute(new ShipParkingTask());
		executor.execute(new ShipDepartTask());

		executor.execute(new TrainArrivalTask());
		executor.execute(new TrainParkingTask());
		executor.execute(new TrainLoadingTask());
		executor.execute(new TrainDepartTask());
		executor.execute(new TrainArriveCrossSectionTask());
		executor.execute(new TrainPassCrossSectionTask());

		executor.execute(new TrainCargoArrivalTask());
		executor.execute(new TrainCargoParkingTask());
		executor.execute(new TrainCargoUnloadTask());
		executor.execute(new TrainCargoDepartTask());

		executor.execute(new TrailerArrivalTask());
		executor.execute(new TrailerParkingTask());
		executor.execute(new TrailerLoadingTask());
		executor.execute(new TrailerDepartTask());
		executor.execute(new TrailerArriveCrossSectionTask());
		executor.execute(new TrailerPassCrossSectionTask());

		executor.execute(new TrailerCargoArrivalTask());
		executor.execute(new TrailerCargoParkingTask());
		executor.execute(new TrailerCargoUnloadTask());
		executor.execute(new TrailerCargoDepartTask());

		executor.shutdown();
	}

	//Ship Task
	public static class ShipArrivalTask implements Runnable {

		public void run() {
			try {

				Ship ship;
				// Declare Random that used to generate the random number
				// nextInt(max) function return number between 0 to (max-1)
				Random ran = new Random();
				int id = 0; // Generate id for each ship

				while (true) {

					
					id++;
					int cargos = (ran.nextInt(11) + 20); // Generate number of cargos between 20-30 for each ship
					ship = new Ship(id, cargos);
					shipPort.ShipArrival(ship); // execute ShipArrival function at Port class

					// Let thread delay between 20 to 25 seconds
					Thread.sleep((ran.nextInt(6) + 20) * 1000);
					
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

		}

	}

	public static class ShipParkingTask implements Runnable {

		public void run() {
			try {

				while (true) {
					if (!shipQueue.isEmpty()) {
						shipPort.ShipParking();
					}
					Thread.sleep(0);
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static class ShipDepartTask implements Runnable {

		public void run() {
			try {
				while (true) {

					if(!dock1.isEmpty() && dock1.peek().getCargos() == 0){
						// if all the cargos unloaded at dock1
						shipPort.ShipDepart(1);
					}

					else if(!dock2.isEmpty() && dock2.peek().getCargos() == 0){
						// if all the cargos unloaded at dock2
						shipPort.ShipDepart(2);
					}

					// Let thread delay between 20 to 25 seconds
					Random ran = new Random();
					Thread.sleep((ran.nextInt(6) + 10) * 1000);
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

		}
	}

	public static class TrainArrivalTask implements Runnable {

		public void run() {
			try {
				Train train;
				int id, emptyCargo;

				id = 0;
				emptyCargo = 0;

				while(true) {
					id++;
					train = new Train(id, emptyCargo); //create a trailer

					//only 2 trains available
					if(id <= 2) {
						train = new Train(id, emptyCargo);
						trainPort.TrainArrive(train);
					} else {
						break;
					}

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}



	public static class TrainParkingTask implements Runnable {

		public void run() {
			try {
				while(true) {
					trainPort.TrainParking();

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}

	}

	public static class TrainLoadingTask implements Runnable {

		public void run() {
			try {
				while(true) {
					trainPort.TrainLoading();

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}

	}

	public static class TrainDepartTask implements Runnable {

		public void run() {
			try {
				while(true) {
					trainPort.TrainDepart();

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}

	}

	public static class TrainArriveCrossSectionTask implements Runnable {

		public void run() {
			try {
				while(true) {
					trainPort.TrainArriveCrossSection(gate);

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}

	}

	public static class TrainPassCrossSectionTask implements Runnable {

		public void run() {
			try {
				while(true) {
					trainPort.TrainPassCrossSection(gate);

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}

	}

	public static class TrailerArrivalTask implements Runnable{

		public void run() {

			try {
				Trailer trailer;
				int id, emptyCargo;

				id = 0;
				emptyCargo = 0;

				while(true) {
					id++;
					trailer = new Trailer(id, emptyCargo); //create a trailer

					//only 3 trailers available
					if(id <= 3) {
						trailer = new Trailer(id, emptyCargo);
						trailerPort.TrailerArrive(trailer);
					} else {
						break;
					}

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}


	public static class TrailerParkingTask implements Runnable{

		public void run() {
			try {
				while(true) {
					trailerPort.TrailerParking();

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static class TrailerLoadingTask implements Runnable{
		public void run() {
			try {
				while(true) {
					trailerPort.TrailerLoading();

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}
	}

	public static class TrailerDepartTask implements Runnable{
		public void run() {
			try {
				while(true) {
					trailerPort.TrailerDepart();
					Thread.sleep(0);
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static class TrailerArriveCrossSectionTask implements Runnable {

		public void run() {
			try {
				while(true) {
					trailerPort.TrailerArriveCrossSection(gate);

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}

	}

	public static class TrailerPassCrossSectionTask implements Runnable {

		public void run() {
			try {
				while(true) {
					trailerPort.TrailerPassCrossSection();

					Thread.sleep(0);
				}
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}

	}

	public static class TrainCargoArrivalTask implements Runnable{

		public void run() {
			try {
				while(true) {
					trainStorageUnit.cargoArrival();		
					Thread.sleep(0);
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}
	}

	public static class TrainCargoParkingTask implements Runnable{

		public void run() {
			try {
				while(true) {
					trainStorageUnit.cargoParking();
					Thread.sleep(0);
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}

	}

	public static class TrainCargoUnloadTask implements Runnable{

		public void run() {
			try {
				while(true) {
					trainStorageUnit.cargoUnloading();
					Thread.sleep(0);
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}


		}

	}

	public static class TrainCargoDepartTask implements Runnable{

		public void run() {
			try {
				while(true) {
					trainStorageUnit.cargoDepart();
					Thread.sleep(0);
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}


		}
	}



	public static class TrailerCargoArrivalTask implements Runnable{

		public void run() {
			try {
				while(true) {
					trailerStorageUnit.cargoArrival();	
					Thread.sleep(0);
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}
	}



	public static class TrailerCargoParkingTask implements Runnable{

		public void run() {
			try {
				while(true) {
					trailerStorageUnit.cargoParking();
					Thread.sleep(0);
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}


		}
	} 



	public static class TrailerCargoUnloadTask implements Runnable{

		public void run()  {
			try {
				while(true) {
					trailerStorageUnit.cargoUnloading();
					Thread.sleep(0);
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}

		}
	} 




	public static class TrailerCargoDepartTask implements Runnable{

		public void run() {
			try {
				while(true) {
					trailerStorageUnit.cargoDepart();
					Thread.sleep(0);
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}



	public static class ShipPort{

		//Ship Function
		public void ShipArrival(Ship ship) {
			lock.lock();	

			try {
				shipQueue.add(ship); // add ship to the Queue for waiting the port
				System.out.println("Ship-" + ship.getID() + ": Arrived at port.");
			} finally {
				lock.unlock();
			}
		}

		public void ShipParking() {
			lock.lock();

			try {
				// When all the dock is full
				while ((dock1.size() == 1) && (dock2.size() == 1)) {
					System.out.println("Ship-" + shipQueue.peek().getID() + ": Waiting to park at port.");
					portIsEmpty.await(); // Wait for port is available for parking
				}

				// When the dock 1 is empty
				if (dock1.isEmpty()) {
					System.out.println("Ship-" + shipQueue.peek().getID() + ": Parked in the Train Dock successful.");
					dock1.add(shipQueue.poll());
					trainDockIsFull.signal();
				} else if (dock2.isEmpty()) { // when the dock 2 is empty
					System.out.println("Ship-" + shipQueue.peek().getID() + ": Parked in the Trailer Dock successful.");
					dock2.add(shipQueue.poll());
					trailerDockIsFull.signal();
				}

			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} finally {
				lock.unlock();
			}
		}

		public void ShipDepart(int dock) {
			lock.lock();

			try {
				if (dock == 1) {
					System.out.println("Ship-" + dock1.peek().getID() + ": Departs from Dock 1.");
					dock1.poll();
					portIsEmpty.signal(); // Signal port is available for parking
				} else if (dock == 2) {
					System.out.println("Ship-" + dock2.peek().getID() + ": Departs from Dock 2.");
					dock2.poll();
					portIsEmpty.signal(); // Signal port is available for parking
				}				


			}finally {
				lock.unlock();
			}
		}

	}

	public static class TrainPort{

		//Train Function
		public void TrainArrive(Train train) {
			lock.lock();

			try {
				trainQueue.add(train); //add train to queue
				System.out.println("Train-" + train.getID() + ": Arrived at dock.");
				trainArrived.signal();
			}finally {
				lock.unlock();
			}

		}

		public void TrainParking() {
			lock.lock();

			try {

				while(trainQueue.isEmpty()) {
					System.out.println("Train port queue is empty so far...");
					trainArrived.await(); // Wait for train arrived
				}

				// Train check if train exist in port or not
				while (trainServe.size() == 1) {
					System.out.println("Train-" + trainQueue.peek().getID() + ": Waiting to park at dock. [Next to park]");
					trainPortIsEmpty.await(); // Wait for train port is empty to park
				}

				// If the train is waiting to park
				if (!trainQueue.isEmpty()) {
					System.out.println("Train-" + trainQueue.peek().getID() + ": Parked in train dock successful");
					trainServe.add(trainQueue.poll()); // park train into train serve

					trainServeIsFull.signal(); // signal the train has been parked
				}

			} catch (InterruptedException ex) {
				ex.printStackTrace();
				
			} finally {
				lock.unlock();
			}

		}

		public void TrainLoading() {
			lock.lock();

			boolean trainLoadStatus = true;

			int shipUnloading, trainLoading;




			try {
				trainLoadedCondition = false;

				while(trainServe.isEmpty()) {
					trainServeIsFull.await(); // wait for train park at port
				}

				while(dock1.isEmpty()) {
					System.out.println("Train-" + trainServe.peek().getID() + ": Waiting for ship to park at train dock...");
					trainDockIsFull.await(); // wait for ship park at dock
				}

				// check for ship there or not
				if(!dock1.isEmpty()) {

					while(trainLoadStatus = true) {

						// check if train got slot to put cargo or not
						if(trainServe.peek().getTrainCargo() < train.MAX_CARGO_SLOT && dock1.peek().getCargos() > 0) {
							System.out.print("Train Dock Ship-" + dock1.peek().getID() + ": Unloading to train-" + trainServe.peek().getID());

							Thread.sleep(500); // 2 second for 4 cargos to load, (0.5 * 4 = 2s)

							shipUnloading = dock1.peek().getCargos();
							shipUnloading --;
							trainLoading = trainServe.peek().getTrainCargo();
							trainLoading ++;

							dock1.peek().setCargos(shipUnloading);
							trainServe.peek().setTrainCargo(trainLoading);
							System.out.print("  [" + dock1.peek().getCargos() + " cargo(s) left]	");
							System.out.println();
						}else {
							trainLoadStatus = false;
							trainLoadedCondition = true;
							trainLoaded.signal();
							break;
						}
					}
				}

			}catch (InterruptedException ex) {
				ex.printStackTrace();

			} finally {
				lock.unlock();
			}
		}

		public void TrainDepart() {
			lock.lock();

			try {
				while(trainLoadedCondition == false) {
				trainLoaded.await(); // wait train is loaded with cargos
				

				if(!trainServe.isEmpty() && trainLoadedCondition == true) {
					if(trainServe.peek().getTrainCargo() == 4) {
						System.out.println("Train-" + trainServe.peek().getID() + ": Train is now full.");
					} else {
						System.out.println("Train-" + trainServe.peek().getID() + ": No more cargo on ship");
					}

					System.out.println("Train-" + trainServe.peek().getID() + ": Departing from dock...");

					Thread.sleep(4000); // 4 seconds for train to reach gate (cross section)
					trainArrivedCrossSection.signal();
				}
				}
				

			}catch (InterruptedException ex) {
				ex.printStackTrace();

			} finally {
				lock.unlock();
			}

		}

		public void TrainArriveCrossSection(Gate gate){
			lock.lock();

			try {
				trainArrivedCrossSection.await(); // wait for train to arrive cross section

				if(!trainServe.isEmpty()) {
					System.out.println("Train-" + trainServe.peek().getID() + ": Passing cross section");
					gate.setGateClosed(true);
					Thread.sleep(1000); // 1 second for train to cross

					trainPassedCrossSection.signal();
				}

			}catch(InterruptedException ex) {
				ex.printStackTrace();

			} finally {
				lock.unlock();
			}
		}

		public void TrainPassCrossSection(Gate gate){
			lock.lock();

			try {
				trainPassedCrossSection.await();
				System.out.println("Train-" + trainServe.peek().getID() + ": Crossed the section...");
				gateOpen.signal();
				gate.setGateClosed(false);

				storageQueueTrain.add(trainServe.poll()); // add train to train storage queue
				trainPortIsEmpty.signal(); // signal train port is empty
				storageQueueTrainIsFull.signal(); // signal storage queue is available
			} catch(InterruptedException ex) {
				ex.printStackTrace();
				
			}finally {
				lock.unlock();
			}
		}
	}

	public static class TrailerPort{
		public void TrailerArrive(Trailer trailer) {
			lock.lock();

			try {
				trailerQueue.add(trailer);
				System.out.println("Trailer-" + trailer.getID() + ": Arrived at dock.");
				trailerArrived.signal(); // Signal that trailer has arrived
			}finally {
				lock.unlock();
			}
		}

		public void TrailerParking() {
			lock.lock();

			try {
				while(trailerQueue.isEmpty()) {
					System.out.println("Trailer port queue is empty so far...");
					trailerArrived.await(); // wait for trailer to arrive
				}

				// Trailer check if trailer already parked at port or not
				while(trailerServe.size() == 1) {
					System.out.println("Trailer-" + trailerQueue.peek().getID() + ": Waiting to park at dock. [Next to park]");
					trailerPortIsEmpty.await(); // wait for trailer port is empty
				}

				// If the trailer is waiting to park
				if(!trailerQueue.isEmpty()) {
					System.out.println("Trailer-" + trailerQueue.peek().getID() + ": Parked in trailer dock successful");
					trailerServe.add(trailerQueue.poll()); // park trailer into trailer serve

					trailerServeIsFull.signal(); // signal that trailer has been parked
				}

			} catch(InterruptedException ex) {
				ex.printStackTrace();

			} finally {
				lock.unlock();
			}
		}

		public void TrailerLoading() {
			lock.lock();

			boolean trailerLoadStatus = true;

			int shipUnloading, trailerLoading;		

			try {

				while(trailerServe.isEmpty()) {
					trailerServeIsFull.await(); // wait for trailer to be parked
				}

				while(dock2.isEmpty()) {
					System.out.println("Trailer-" + trailerServe.peek().getID() + ": Waiting for ship to park at trailer dock...");
					trailerDockIsFull.await(); // wait for trailer dock is full
				}

				while(trailerServe.peek().getTrailerCargo() > 0) {
					trailerCargoUnloaded.await(); //wait for cargo in trailer is unloaded
				}

				// check for trailer availability
				if(!dock2.isEmpty()) {
					//					System.out.println("Trailer-" + trailerServe.peek().getID() + ": Ready to load from ship-" + dock2.peek().getID() + ".");

					while(trailerLoadStatus = true) {
						//check if trailer got slot to put cargos
						if(trailerServe.peek().getTrailerCargo() < trailer.MAX_CARGO_SLOT && dock2.peek().getCargos() > 0) {
							System.out.print("Trailer Dock Ship-" + dock2.peek().getID() + ": Unloading to trailer-" + trailerServe.peek().getID());

							Thread.sleep(500); //1 second to load cargos to trailer (0.5s + 0.5s, two cargos 1s in total)

							shipUnloading = dock2.peek().getCargos();
							shipUnloading --;
							trailerLoading = trailerServe.peek().getTrailerCargo();
							trailerLoading++;


							dock2.peek().setCargos(shipUnloading);
							trailerServe.peek().setTrailerCargo(trailerLoading);
							System.out.print("  [" + dock2.peek().getCargos() + " cargo(s) left]	");
							System.out.println();
						}else {
							trailerLoadStatus = false;
							trailerLoaded.signal(); //signal that cargos are loaded into trailer serve
							break;
						}
					}
				}
			}catch(InterruptedException ex) {
				ex.printStackTrace();

			} finally {
				lock.unlock();
			}
		}

		public void TrailerDepart() {
			lock.lock();

			try {
				// Wait for trailer loaded with cargos (only checked for first time for now)
				trailerLoaded.await();


				if(trailerServe.peek().getTrailerCargo() == 2) {
					System.out.println("Trailer-" + trailerServe.peek().getID() + ": Trailer is now full");
				} else {
					System.out.println("Trailer-" + trailerServe.peek().getID() + ": No more cargo on ship");
				}

				System.out.println("Trailer-" + trailerServe.peek().getID() + ": Departing from dock...");


				Thread.sleep(2000); // 2 seconds for trailer to reach gate (cross section)
				trailerArrivedCrossSection.signal(); // signal that train arrived cross section
				
			} catch(InterruptedException ex) {
				ex.printStackTrace();

			} finally {
				lock.unlock();
			}
		}

		public void TrailerArriveCrossSection(Gate gate) {
			lock.lock();

			try {
				trailerArrivedCrossSection.await(); // wait for train to arrive cross section

				while(gate.gateClosed) {
					System.out.println("Trailer-" + trailerServe.peek().getID() + ": Waiting for gate to open...");
					gateOpen.await();
				}

				System.out.println("Trailer-" + trailerServe.peek().getID() + ": Passing cross section");
				System.out.println("Trailer-" + trailerServe.peek().getID() + ": Crossed the section...");

				trailerPassedCrossSection.signal();

			}catch(InterruptedException ex) {
				ex.printStackTrace();

			} finally {
				lock.unlock();
			}
		}

		public void TrailerPassCrossSection(){
			lock.lock();

			try {
				trailerPassedCrossSection.await();
				
				storageQueueTrailer.add(trailerServe.poll()); //add trailer into storage queue
				trailerPortIsEmpty.signal(); //signal trailer port is empty
				storageQueueTrailerIsFull.signal(); // signal storage queue is available
			} catch(InterruptedException ex) {
				ex.printStackTrace();

			}finally {
				lock.unlock();
			}
		}

	}

	public static class TrainStorageUnit{

		public void cargoArrival() {
			lock.lock();

			try {
				storageQueueTrainIsFull.await(); // wait for train available in storage queue


				//check if train is waiting at storage area or not
				if(!storageQueueTrain.isEmpty()) {
					System.out.println("Train-" + storageQueueTrain.peek().getID()  + ": Arrived at storage field");
					trainCargoArrived.signal(); // signal that cargo arrived
				}

			}catch (InterruptedException ex) {
				ex.printStackTrace();

			}finally {
				lock.unlock();
			}


		}
		/**
		@Override
		public void cargoArrival(Trailer trailer) {

		}*/

		public void cargoParking() {
			lock.lock();

			try {
				trainCargoArrived.await(); // wait train cargos to arrive

				//check if storage area is full or no
				while (storageServeTrain.size() == 1) {
					System.out.println("Train-" + storageQueueTrain.peek().getID() + ": Waiting to park at storage field");
					trainStorageQueueIsEmpty.await(); // Wait for storage queue is empty to park
				}

				// train with cargos available in storage queue
				if (!storageQueueTrain.isEmpty()) {
					System.out.println("Train-" + storageQueueTrain.peek().getID() + ": Parked in storage field successful");
					storageServeTrain.add(storageQueueTrain.poll()); //train in queue parked into storage serve
					trainCargoParked.signal(); // signal that train with cargo parked
				} else // if no any train is waiting to park
					System.out.println("Storage field is empty so far...");

			}catch (InterruptedException ex) {
				ex.printStackTrace();

			}finally {
				lock.unlock();
			}

		}

		public void cargoUnloading() {
			lock.lock();

			Random ran = new Random();
			boolean trainUnloadStatus = true;

			int trainUnload;

			try {
				trainCargoParked.await(); // wait for train with cargo parked


				//if there is serve train at storage
				if(!storageServeTrain.isEmpty()) {
					System.out.println("Train-" + storageServeTrain.peek().getID() + ": Ready to unload to storage field.");

					trainUnload = storageServeTrain.peek().getTrainCargo();

					while(trainUnloadStatus = true) {

						//if the train still have cargo
						if(storageServeTrain.peek().getTrainCargo() > 0) {
							System.out.println("Train-" + storageServeTrain.peek().getID() + ": Unloading to storage field.");
							Thread.sleep(500);// unload 2 second, 4 cargos (4* 0.5s = 2s)

							trainUnload --;
							storageServeTrain.peek().setTrainCargo(trainUnload);

						}else {
							//train finishes unload

							trainUnloadStatus = false;
							trainCargoUnloaded.signalAll(); //signal that train unloaded cargos into storage field
							break;

						}

					}

				}

			}catch (InterruptedException ex) {
				ex.printStackTrace();

			}finally {
				lock.unlock();
			}


		}

		public void cargoDepart() {
			lock.lock();

			Random ran = new Random();

			try {
				trainCargoUnloaded.await(); // wait for train unloaded cargos in storage field

				//no more cargos inside train serve
				if(storageServeTrain.peek().getTrainCargo() == 0) {
					System.out.println("Train-" + storageServeTrain.peek().getID() + ": Train is now empty.");
					System.out.println("Train-" + storageServeTrain.peek().getID() + ": Departing from storage field...");
					Thread.sleep((ran.nextInt(2)+3) * 1000); // 3-4 seconds to return to port
					System.out.println("Train-" + storageServeTrain.peek().getID() + ": Back to dock");
					trainQueue.add(storageServeTrain.poll()); // put back to train queue
					trainStorageQueueIsEmpty.signal(); // signal that queue of train storage is empty
					trainArrived.signal();
				}

			}catch (InterruptedException ex) {
				ex.printStackTrace();

			}finally {
				lock.unlock();
			}

		}

	}

	public static class TrailerStorageUnit{

		public void cargoArrival() {
			lock.lock();

			try {
				storageQueueTrailerIsFull.await(); // wait for trailer arrive at storage unit queue


				//check if trailer is waiting at storage area or not
				if(!storageQueueTrailer.isEmpty()) {
					System.out.println("Trailer-" + storageQueueTrailer.peek().getID()  + ": Arrived at storage field");
					trailerCargoArrived.signal();
				}

			}catch (InterruptedException ex) {
				ex.printStackTrace();
				storageQueueTrailerIsFull.signal();
			}finally {
				lock.unlock();
			}


		}
		/**
		@Override
		public void cargoArrival(Trailer trailer) {

		}*/

		public void cargoParking() {
			lock.lock();

			try {
				trailerCargoArrived.await(); // wait for trailer arrive to storage unit

				//check if storage area queue is full or no
				while (storageServeTrailer.size() == 1) {
					System.out.println("Trailer-" + storageQueueTrailer.peek().getID() + ": Waiting to park at trailer storage field");
					trailerStorageIsEmpty.await(); // Wait for train is not full
				}

				//checks if storage field is empty or not
				if (!storageQueueTrailer.isEmpty()) {
					System.out.println("Trailer-" + storageQueueTrailer.peek().getID() + ": Parked in trailer storage field successful");
					storageServeTrailer.add(storageQueueTrailer.poll());
					trailerCargoParked.signal();
				} else // if no any trailer is waiting to park
					System.out.println("Trailer storage field is empty so far...");

			}catch (InterruptedException ex) {
				ex.printStackTrace();

			}finally {
				lock.unlock();
			}

		}

		public void cargoUnloading() {
			lock.lock();

			boolean trailerUnloadStatus = true;

			int trailerUnload;

			try {
				trailerCargoParked.await(); // wait for trailer with cargo to be parked


				//if there is trailer at storage
				if(!storageServeTrailer.isEmpty()) {
					System.out.println("Trailer-" + storageServeTrailer.peek().getID() + ": Ready to unload to trailer storage field.");

					while(trailerUnloadStatus = true) {

						//if the trailer still have cargo
						if(storageServeTrailer.peek().getTrailerCargo() > 0) {
							System.out.println("Trailer-" + storageServeTrailer.peek().getID() + ": Unloading to trailer storage field.");
							Thread.sleep(500); // 1 second for trailer to load to storage field (0.5s + 0.5s, 2 cargos in total)
							trailerUnload = storageServeTrailer.peek().getTrailerCargo();
							trailerUnload --;
							storageServeTrailer.peek().setTrailerCargo(trailerUnload);
						}else {
							//trailer finishes unload

							trailerUnloadStatus = false;
							trailerCargoUnloaded.signalAll();
							break;

						}

					}

				}

			}catch (InterruptedException ex) {
				ex.printStackTrace();

			}finally {
				lock.unlock();
			}


		}

		public void cargoDepart() {
			lock.lock();

			Random ran = new Random();

			try {
				trailerCargoUnloaded.await(); // wait for trailer with cargos is unloaded

				//no more cargos in trailer serve
				if(storageServeTrailer.peek().getTrailerCargo() == 0) {
					System.out.println("Trailer-" + storageServeTrailer.peek().getID() + ": Trailer is now empty.");
					System.out.println("Trailer-" + storageServeTrailer.peek().getID() + ": Departing from trailer storage field...");
					Thread.sleep((ran.nextInt(2)+2) * 1000); // 2-3 seconds
					System.out.println("Trailer-" + storageServeTrailer.peek().getID() + ": Back to dock.");
					trailerQueue.add(storageServeTrailer.poll()); // put back to trailer queue
					trailerStorageIsEmpty.signal();
					trailerArrived.signal();
				}

			}catch (InterruptedException ex) {
				ex.printStackTrace();
				//trailerCargoUnloaded.signal();

			}finally {
				lock.unlock();
			}

		}

	}

	/*Objects in our program*/
	public static class Gate{

		private boolean gateClosed = false;

		public Gate(boolean gateClosed){
			this.gateClosed = gateClosed;
		}

		public boolean GateClosed(){
			return gateClosed;
		}

		public void setGateClosed(boolean gateClosed) {
			this.gateClosed = gateClosed;
		}
		//gate status

	}

	public static class Ship {

		private int shipID;
		private int shipCargo;

		public Ship(int shipID, int shipCargo) {
			this.shipID = shipID;
			this.shipCargo = shipCargo;
		}

		public int getID() {
			return shipID;
		}

		public int getCargos() {
			return shipCargo;
		}

		public void setCargos(int shipCargo) {
			this.shipCargo = shipCargo;
		}
	}

	public static class Train {

		private int trainID;
		private static final int MAX_CARGO_SLOT = 4;
		private int trainCargo = 0;

		public Train(int trainID, int trainCargo){
			this.trainID = trainID;
			this.trainCargo = trainCargo;

		}

		public int getID() {
			return trainID;
		}

		public int getTrainCargo() {
			return trainCargo;
		}

		public void setTrainCargo(int trainCargo) {
			this.trainCargo = trainCargo;
		}
	}

	public static class Trailer{
		private int trailerID;
		private static final int MAX_CARGO_SLOT = 2;
		private int trailerCargo = 0;

		public Trailer(int trailerID, int trailerCargo) {
			this.trailerID = trailerID;
			this.trailerCargo = trailerCargo;
		}

		public int getID() {
			return trailerID;
		}

		public int getTrailerCargo() {
			return trailerCargo;
		}

		public void setTrailerCargo(int trailerCargo) {
			this.trailerCargo = trailerCargo;
		}
	}

}