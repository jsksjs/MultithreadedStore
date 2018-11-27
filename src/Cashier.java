/**
 * @author Joseph Spada
 * Cashier threads that will consume from a Line.
 * Can only insert to the queue in the instance of requested thread death/exit.
 */
public class Cashier extends Thread implements Runnable
{
    // Cashier id and time scale.
    private int cNum, tick;
    // Visitor currently being serviced.
    private Visitor v;
    // Line this Cashier is servicing.
    private Line own;
    // Signals need for this Thread to be alive.
    private boolean work;

    /**
     * Constructs Cashier with an identifier, a line, and a time scale.
     * Also allows the instance to continuously run once started by setting work to true.
     * @param cNum Number attributed to this particular Cashier. Used for output labeling.
     * @param own  Line that this Cashier is responsible for and can access.
     * @param tick Scaling factor of time events, in ms.
     */
    public Cashier(int cNum, Line own, int tick){
        this.cNum = cNum;
        this.tick = tick;
        this.own = own;
        work = true;
    }

    /**
     * Poisons cashiers once all Visitor threads have been joined to main.
     * Done by inserting to each cashier's line a unique customer that signals thread death.
     */
    public void poison(){
        own.insert(new Visitor(own, -1, -1));
    }

    /**
     * Starts the thread's work cycle.
     * Checks to see if this thread should should work, then proceeds.
     */
    public void run(){
        while(work){
            // Serve a Visitor from the line to begin checkout.
            v = own.serve();
            // Checks for the poison Visitor that signals thread death.
            if(v.getVNum() != -1){
                // Iterates through the Visitor's items, sleeping to simulate item checkout.
                for(int i = v.getNumItems(); i > 0; i--){
                    try
                    {
                        Thread.sleep((int)(Math.random() * 11 + 10) * tick);
                    }
                    catch (InterruptedException e) { }
                }
                // Once all items processed, remove Visitor from line.
                own.remove();
            }
            else
                // Once poison Visitor found, thread can die.
                work = false;
        }
        // Exit message in crit section so that it is always printed before exit.
        System.out.println("@Cashier " + (cNum+1) + " exits.");
    }
}
