import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * Resources that will be shared between Visitors and Cashiers.
 * Cashiers generally only remove from their respective Line and Visitors only insert into their chosen Line.
 */
public class Line
{
    // Stats that are shared between all Lines
    private static int numServing, numWaitingTotal, numShopping;

    // Synchronizes between ALL Threads to allow stat updates
    private static Semaphore stats;

    // Unique id for labeling and the number of Visitors waiting in this Line
    private int lineNum, numWaitCurrLine;
    /**
     * Visitors stored in a Queue, FIFO data structure.
     * NOT using thread-safe variants from concurrent package for the purpose of Semaphore use.
     */
    private Queue<Visitor> cashLine;
    /**
     * Semaphores that control access.
     * Mutex that ensures mutual exclusion for each Line.
     * Full to count how "full" the individual Line is and to prevent removal from an empty Line.
     * Service only allows one Visitor to be serviced by the Cashier at a time:
     *      blocks the Visitor thread so that it does not exit before Cashier can process its items.
     */
    private Semaphore mutex, full, service;

    /**
     * Constructs a Line that can report stats about the number of Visitors being served (all Lines),
     *      number of Visitors waiting in total (all Lines), number of Visitors currently shopping, and
     *      number of Visitors currently waiting in this Line.
     * Semaphores initialized to control critical section access.
     * @param lineNum Unique identifier of this line. Used for output labeling.
     * @param numShopping The initial number of people shopping. Used for output labeling.
     */
    public Line(int lineNum, int numShopping){
        Line.numServing = 0;
        Line.numWaitingTotal = 0;
        Line.numShopping = numShopping;
        Line.stats = new Semaphore(1);

        this.lineNum = lineNum;
        numWaitCurrLine = 0;

        cashLine = new LinkedList<>();

        mutex = new Semaphore(1);
        full = new Semaphore(0);
        service = new Semaphore(0);
    }

    /**
     * Inserts a given Visitor, v, into this Line.
     * Primarily called by Visitors who enter the Line.
     * Special case: on poison, the Visitor number will be -1 signaling Cashier death.
     *      This unique Visitor is immediately inserted to the Queue with no regards to the stats or output.
     * Ensures mutual exclusion to the critical section which includes:
     *      Updating stats that must be passed to output
     *      Writing to the Line Queue
     *      Writing to output
     * The Queue has gained an element and the critical section is over so both semaphores are released.
     * @param v Visitor to insert into the Line
     */
    public void insert(Visitor v){
        try
        {
            mutex.acquire();
            // Do normal procedure if not poison
            if(v.getVNum() != -1){
                // Stat edits
                stats.acquire();
                numWaitCurrLine += 1;
                numWaitingTotal += 1;
                numShopping -= 1;
                stats.release();
                // Put the Visitor at the end of Line
                cashLine.offer(v);
                // Output results
                String newLine = "\n\t\t";
                System.out.println("\tVisitor " + v.getVNum() + " has entered line for Cashier " +
                        (lineNum+1) + "." + newLine + "Shopping: " + numShopping + newLine + "Waiting in Line " + (lineNum+1) + ": "
                        + numWaitCurrLine  + newLine + "Total Waiting: " +
                        numWaitingTotal + newLine + "Serving: " + numServing);
            }
            // Otherwise if poison, immediately put into Line because stats do not are about poison
            else
                cashLine.offer(v);
        }
        catch (InterruptedException e) { }
        finally
        {
            mutex.release();
            full.release();
        }
    }

    /**
     * Serves the Visitor at the head of the Line by peeking the Queue.
     * Does not remove from the Line; only peeks at the Visitor in front.
     * Done this way so that Visitors occupy the front of the Line
     *      and so that Visitor threads do not immediately exit before processing by Cashier.
     * @return Visitor at the front of the Line that must have its items processed.
     */
    public Visitor serve(){
        Visitor v = null;
        try
        {
            // Don't try to peek and serve nulls.
            // Serving is always precursor to removal, so acquire now to decrement permits before entering remove()
            full.acquire();
            mutex.acquire();
            // Check for poison
            if(cashLine.peek().getVNum() != -1){
                // Stats updates
                stats.acquire();
                numWaitCurrLine -= 1;
                numWaitingTotal -= 1;
                numServing += 1;
                stats.release();
                v = cashLine.peek();
                // Output stats
                String newLine = "\n\t\t\t\t\t";
                System.out.println("\t\t\tVisitor " + v.getVNum() + " is now being served by Cashier " + (lineNum+1) +
                        "." + newLine + "Shopping: " + numShopping + newLine + "Waiting in Line " + (lineNum+1) + ": "
                        + numWaitCurrLine + newLine + "Total Waiting: " + numWaitingTotal + newLine + "Total being Served: " + numServing);
            }
            // Otherwise if poison, just peek and then return
            else
                v = cashLine.peek();

        }
        catch (InterruptedException e) { }
        finally
        {
            mutex.release();
        }
        return v;
    }

    /**
     * Similar to serve, but now the head is removed and serving stat is updated.
     * Does not need to deal with poison.
     * @return Visitor that is removed from the front of the Line.
     */
    public Visitor remove(){
        Visitor v = null;
        try
        {
            mutex.acquire();
            stats.acquire();
            numServing -= 1;
            stats.release();
            v = cashLine.poll();
                String newLine = "\n\t\t\t\t\t\t\t\t";
                System.out.println("\t\t\t\t\t\tVisitor " + v.getVNum() + " is done being served by Cashier " + (lineNum+1) +
                        "." + newLine + "Shopping: " + numShopping + newLine + "Waiting in Line " + (lineNum+1) + ": "
                        + numWaitCurrLine + newLine + "Total Waiting: " + numWaitingTotal + newLine + "Total being Served: " + numServing);
        }
        catch (InterruptedException e) { }
        finally
        {
            mutex.release();
            // Allows Visitor to finish and kills thread.
            service.release();
        }
        return v;
    }

    /**
     * Visitor blocks using this and can only continue once Cashier reaches the end of remove().
     * @return false so that the Visitor thread may exit.
     */
    public boolean finish(){
        try
        {
            // Block until service is done (removal from Line)
            service.acquire();
            return false;
        }
        catch (InterruptedException e) { }
        return true;
    }

}
