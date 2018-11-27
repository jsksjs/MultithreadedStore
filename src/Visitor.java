/**
 * @author Joseph Spada
 * Visitor threads that will insert themselves into a Line.
 * Thread exits on proper removal from the Line.
 */
public class Visitor extends Thread implements Runnable
{
    // Visitor id, number of items this has, max items this should have, and time scale.
    private int vNum, numItems, maxItems, tick;
    // Signals need for this Thread to be alive.
    private boolean shop;
    // Line this Visitor will eventually enter.
    private Line own;

    /**
     * Constructs Visitor with its Line, an identifier, and a time scale.
     * @param own Line that this Visitor will enter.
     * @param vNum Number attributed to this particular Visitor. Used for output labeling.
     * @param tick Scaling factor of time events, in ms.
     */
    public Visitor(Line own, int vNum, int tick){
        this.vNum = vNum;
        numItems = 0;
        maxItems = (int)(Math.random() * (6)+1);
        this.tick = tick;

        shop = true;

        this.own = own;
    }

    /**
     * Getter for id.
     * @return identifier of this Visitor.
     */
    public int getVNum(){
        return vNum;
    }

    /**
     * Getter for number of items this Visitor has.
     * @return number of items this Visitor has.
     */
    public int getNumItems(){
        return numItems;
    }

    /**
     * Starts the thread's work cycle.
     * Checks to see if this thread should should work, then proceeds.
     * Once it is told it cannot work, it prints an exit message.
     */
    public void run(){
        while(shop)
        {
            // Gather items.
            if(numItems < maxItems){
                // Start with 0 items, get an item every time cycle.
                for(int i = 0; i < maxItems; i++){
                    try
                    {
                        Thread.sleep((int) (Math.random() * 26 + 50) * tick);
                    }
                    catch (InterruptedException e) { }
                    numItems += 1;
                }
            }
            // Once all items gathered, enter Line and begin finish.
            else{
                own.insert(this);
                shop = own.finish();
            }
        }
        // Exit message.
        System.out.println("@Visitor " + vNum + " exits.");
    }
}
