import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.io.*;

public class project {
    private static Window window;
    public static void main(String[] args) {
        window = new Window("Route Finder"); // Create window
    }
}

class Window{
    Window(String title) {
        JFrame windowFrame = new JFrame();
        windowFrame.setTitle(title); // Set title of window

        // Input text box, fills left side
        JTextArea area=new JTextArea(); // Create text area
        area.setEditable(true); // Allow editing

        JScrollPane scroll = new JScrollPane (area); // Create scrollbar
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Always have a vertical scroll bar
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Show horizontal bar if order extends past box
        scroll.setBounds(5,5,430,550); // Set size and position


        // Result text box at bottom
        JTextArea results = new JTextArea("Result box"); // Create box
        results.setBounds(445,455, 655,100); // Set box location and size
        results.setLineWrap(true); // Wrap text when it reaches the edge
        results.setEditable(false); // Prevent editing results box
        results.setFont(new Font( "SansSerif", Font.BOLD, 17)); // Set font
        results.setCursor(new Cursor(Cursor.TEXT_CURSOR)); // Set cursor to text cursor when over results box


        // Button to start finding route
        Button routeButton = new Button("Get route");
        routeButton.setFont(new Font( "TimesRoman", Font.BOLD, 22)); // Set font
        routeButton.setBounds(1110,455, 200,100); // Set button location and size
        routeButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Set cursor to hand cursor when over button

        JPanel map = new JPanel(); // Create JPanel for map
        map.setBounds(445,5,865,440); // Set map location and size
        map.setBackground(new Color(67, 67, 67)); // Set colour

        // Add to frame
        windowFrame.add(map); // JPanel for map
        windowFrame.add(results); // area showing results
        windowFrame.add(scroll); // Add input box
        windowFrame.add(routeButton); // button to start finding route

        windowFrame.setSize(1330,600); // Set window size
        windowFrame.setLayout(null); // No set layout
        windowFrame.setResizable(false); // Prevent resizing window
        windowFrame.getContentPane().setBackground(new Color(222, 184, 135)); // Set background colour
        windowFrame.setVisible(true); // Show window

        // When get route button is pressed
        routeButton.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e){
                String input = "0,Apache pizza,0,53.381189,-6.592900\n";  // Set order 0 to be apache pizza
                input = input + area.getText(); //get the text from the input

                // Split input string into separate string when theres a new line and put each into array index
                String[] lines = input.split("\\n");

                int numOrders = lines.length; // Number of orders

                // 2d array, row for each order, 6 columns
                String[][] orders = new String[numOrders][6];

                // Get every order, split into 5 parts, place each part into row in order 2d array
                for(int i=0;i<numOrders;i++){
                    // new array, get order into 5 parts using split by comma
                    String orderSplit[] = lines[i].split(",");

                    // put each part into a row in the orders array
                    for(int x=0;x<5;x++){
                        //  Order No  |  Address  |  Time waiting  |  North  |  East  |  Completed
                        orders[i][x] = orderSplit[x]; // Add piece of info into orders index
                        System.out.print(orders[i][x] + "    "); // Prints out all orders
                    }

                    orders[i][5]="false"; // Set all orders as not being completed
                    System.out.println();
                }

                // Route finder - nearest neighbour
                String route[] = new String[numOrders]; // array to track what order orders are completed
                int orderNo=0;

                // Starting location, Apache Pizza Maynooth
                double curLat = 53.381189; // Starting latitude/ North
                double curLong = -6.592900; // Starting Longitude/ West

                double nextLat = 0.0; // Location for next delivery
                double nextLong = 0.0;


                // nearest neighbour route
                for(int x=0;x<numOrders;x++) {
                    double shortest = 999.9; // Track shortest distance to house, default is high so first order will replace it
                    for(int i=0;i<numOrders;i++) { // Go through every order, find shortest distance from current Location
                        if(orders[i][5]=="false") { // If order isn't completed
                            double lat2 = Double.parseDouble(orders[i][3]); // Get location
                            double long2 = Double.parseDouble(orders[i][4]);

                            double distance = getDistance(curLat,curLong,lat2,long2); // Get distance between points
//                                System.out.println(distance);

                            // If distance is shorter than current shortest distance
                            if (distance < shortest) {
                                shortest = distance;
                                orderNo = Integer.parseInt(orders[i][0]); // Save order chosen
//                                System.out.println("Order "+ orderNo  + " is current closest, distance:  " + distance);

                                nextLat = lat2; // Save location of order
                                nextLong = long2;
                            }
                        }
                    }
                    route[x] = Integer.toString(orderNo); // Save route to string
                    System.out.println("Next order is " + orderNo);
                    orders[orderNo][5] = "true"; // Mark order as completed

                    curLat = nextLat; // New starting location
                    curLong = nextLong;
                }

                // 2-OPT,  Based on pseudocode from https://en.wikipedia.org/wiki/2-opt
                int swaps = 99; // Track if there are still improvements

                while(swaps!=0) { // until no improvement
                    double bestAngMin = getAngMin(route, orders); // get angry minutes of route
                    String newRoute[] = new String[numOrders]; // New array to swap order of deliveries
                    swaps = 0; // reset swap count

                    for (int i = 1; i < numOrders - 1; i++) { // i = 1, can't change row 0 which is starting point
                        for (int k = i + 1; k < numOrders; k++) {
                            newRoute = optSwap(route, i, k); // Swap points and save as new route
                            double angryMin = getAngMin(newRoute,orders); // Get new routes angry minutes

                            if ((angryMin < bestAngMin)) {
                                route = newRoute; // Set current route to new route
                                bestAngMin = angryMin;
                                swaps++; // Increase number of swaps
                            }
                        }
                    }
                }


                // Getting route, setting results box to show route
                String path = route[1]; // set as 1 to ignore order 0(start point) Do before for loop to get commas right
                for(int i = 2;i< numOrders;i++){
                    path = path +  "," + route[i]; // Add each order onto path with comma before
                }
                results.setText(path); // Set text in output box to be route taken

                System.out.println("\nDelivery order is: ");
                System.out.println(path);
            }

            // Do 2optSwap,  Based on Pseudocode from https://en.wikipedia.org/wiki/2-opt
            public String[] optSwap(String route[], int i, int k){
                String newRoute[] = new String[route.length];
                newRoute[0] = "0"; // Set start of new route to order 0 ie apache pizza

                // 1. take route[0] to route[i-1] and add them in order to newRoute
                for(int x=1;x<i;x++){
                    newRoute[x] = route[x];
                }

                // 2. take route[i] to route[k] and add them in reverse order to newRoute
                int r = k;
                for(int x=i;x<=k;x++){
                    newRoute[x] = route[r];
                    r--;
                }

                // 3. take route[k+1] to end and add them in order to newRoute
                for(int x=k+1;x<route.length;x++){
                    newRoute[x] = route[x];
                }

                return newRoute; // return new route
            }


            // Get angry minutes for route
            public double getAngMin(String route[], String orders[][]){
                double length = 0.0;
                double angryMin = 0.0;
                double totAngryMin = 0.0;


                for(int i=0;i<orders.length-1;i++){
                    // Get order numbers
                    int order1 = Integer.parseInt(route[i]);
                    int order2 = Integer.parseInt(route[i+1]);

                    // Get location of orders
                    double lat1 = Double.parseDouble(orders[order1][3]); // get latitude
                    double lon1 = Double.parseDouble(orders[order1][4]); // get longitude

                    double lat2 = Double.parseDouble(orders[order2][3]);
                    double lon2 = Double.parseDouble(orders[order2][4]);

                    length = length + getDistance(lat1,lon1,lat2,lon2);

                    // delivery man is driving at 60km/hr so 1km = 1 minute
                    // If time waiting + time to get to house is > 30 minutes
                    if((Double.parseDouble(orders[i][2]) + length) >30.0){
                        angryMin = (Double.parseDouble(orders[i][2]) + length) - 30.0; // get minutes over 30
                        totAngryMin += angryMin;
                    }

                }
                return totAngryMin;
            }


            // Get distance between two points
            public double getDistance(double lat1,double lon1,double lat2,double lon2){
                double latDist = Math.toRadians(lat2 - lat1);
                double lonDist = Math.toRadians(lon2 - lon1);

                double d = Math.sin(latDist / 2) * Math.sin(latDist / 2) + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDist / 2) * Math.sin(lonDist / 2);

                double distance = 2 * 6371 * Math.asin(Math.sqrt(d));
                return distance;
            }
        });
    }
}