import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Joseph Spada
 * Configures the simulation and runs with the specified parameters.
 */
public class Store
{
    /**
     * Main method, prompt user for input, create and start threads, ensure proper exit.
     * @param args
     */
    public static void main(String[] args)
    {
        /**
         * m, number of Visitors.
         * n, number of Cashiers.
         * tick, simulation time scale.
         */
        int m, n, tick;
        // Prompt user for inputs and store to respective variables.
        Scanner scan = new Scanner(System.in);
        System.out.println("Number of visitors, M: ");
        m = scan.nextInt();

        System.out.println("Number of cashiers, N: ");
        n = scan.nextInt();

        System.out.println("Time scale (milliseconds, recommend 30): ");
        tick = scan.nextInt();
        scan.close();
        // Array of Line that will hold each Cashier's respective Line
        Line[] lines = new Line[n];
        for(int i = 0; i < n; i++){
            lines[i] = new Line(i, m);
        }
        // Stores Visitors and Cashiers for starting of threads
        ArrayList<Visitor> visitors = new ArrayList<>();
        ArrayList<Cashier> cashiers = new ArrayList<>();
        // Construct and store Cashiers.
        for (int i = 0; i < n; i++)
        {
            cashiers.add(new Cashier(i, lines[i], tick));
        }
        // Construct and store visitors with a destined line, Visitor number (id), and the time scale.
        for (int i = 0; i < m; i++)
        {
            visitors.add(new Visitor(lines[(int)(Math.random() * n)], i+1, tick));
        }
        // Cashier threads begin their work.
        for (Cashier c : cashiers)
        {
            c.start();
        }
        // Visitor threads begin their work.
        for (Visitor v : visitors)
        {
            v.start();
        }
        // Join all Visitors to the main thread once they have naturally completed their work.
        for(Visitor v: visitors){
            try{ v.join(); } catch(Exception e){}
        }
        /**
         * Cashiers will poison themselves by inducing an insert, only after all visitors are done.
         * Once poisoned, Cashiers will be joined to the main thread on exit.
         */
        for(Cashier c: cashiers){
            c.poison();
            try{ c.join(); } catch(Exception e){}
        }
    }
}
