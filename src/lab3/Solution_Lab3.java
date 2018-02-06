/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab3;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eranga
 */
public class Solution_Lab3 {
    
    static int riders = 0;
    static Semaphore mutex = new Semaphore(1);
    static Semaphore multiplex = new Semaphore(50);
    static Semaphore bus = new Semaphore(0);
    static Semaphore allAboard = new Semaphore(0);
    
    static class Bus extends Thread{
        
        String busId = "";
        
        Bus(String busId) {
            this.busId = busId;
        }
        
        public void run() {
            try {
                
                System.out.println(busId + " : acquiring mutex...");
                //bus gets the mutex preventing the later arrived riders
                mutex.acquire();
                System.out.println(busId + " : acquired mutex");
                
                //check whether there are waiting riders
                if (riders > 0) {
                    System.out.println(busId + " : releasing bus...");
                    //allowing riders to be boarded
                    bus.release();
                    System.out.println(busId + " : released bus");
                    
                    System.out.println(busId + " : acquiring allAboard...");
                    //waiting for the riders to be boarded
                    allAboard.acquire();
                    System.out.println(busId + " : acquired allAboard");
                }
                
                System.out.println(busId + " : releasing mutex...");
                //releasing the mutex after aboarding the riders
                mutex.release();
                System.out.println(busId + " : released");
                
                System.out.println(busId + " : departed");
                
            } catch (InterruptedException ex) {
                Logger.getLogger(Solution_Lab3.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    static class Rider extends Thread{
        
        String name = "";
        
        Rider(String name) {
            this.name = name;
        }
        
        public void run() {
            try {
                
                System.out.println(name + ": acquiring multiplex...");
                //check whether the capacity is satisfied
                multiplex.acquire();
                System.out.println(name + ": acquired the multiplex");
                
                System.out.println(name + ": acquiring mutex...");
                //check whether a bus has already arrived
                mutex.acquire();
                System.out.println(name + ": acquired the mutex");
                
                //number of riders are get increased after acquiring the mutex
                riders++;
                
                System.out.println(name + ": releasing mutex...");
                mutex.release();
                System.out.println(name + ": released mutex");
                
                System.out.println(name + ": acquiring bus...");
                //waiting till a bus comes
                bus.acquire();
                System.out.println(name + ": acquired bus");
                
                //giving the chance to another rider after acquiring the bus
                multiplex.release();
                
                System.out.println(name + ": boarded");
                
                //decreasing the number of waiting riders after get boarded
                riders--;
                
                if (riders == 0) {
                    //releasing allAboard mutex when all are boarded.
                    System.out.println("All the riders are boarded");
                    allAboard.release();
                } else {
                    System.out.println(name + ": releasing bus...");
                    //give chance to another rider to get boarded
                    bus.release();
                    System.out.println(name + ": released bus");
                }                
                
            } catch (InterruptedException ex) {
                Logger.getLogger(Solution_Lab3.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public static void main(String[] args) {
        
        //creating a therad which generates riders randomly
        Thread riders_creator = new Thread(
                new Runnable() {
            Random randomGenerator = new Random();
            int numberOfRiders = 0;
            @Override
            public void run() {
                
                while(true) {
                    try {
                        Thread.sleep(30 * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Solution_Lab3.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                    //creating riders at random time intervals
                    new Rider("Rider-" + numberOfRiders).start();
                    numberOfRiders++;
                }
                
            }
        });
        riders_creator.start();
        
        //starting buses periodically
        int busId = 0;
        while (true) {
            try {
                Thread.sleep(20 * 60 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Solution_Lab3.class.getName()).log(Level.SEVERE, null, ex);
            }
            new Bus("Bus-" + busId).start();
            busId++;
        }
    }
    
}
