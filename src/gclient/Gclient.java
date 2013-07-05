/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gclient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Damith
 *
 *
 */
class coinpile implements Observer, Comparable<coinpile> {

    public int value;
    public int position;
    public int ttl;

    public coinpile(int t, int v, int ps) {
        ttl = t;
        value = v;
        position = ps;
    }

    @Override
    public int compareTo(coinpile c) {
        return this.ttl - c.ttl;
    }

    @Override
    public void update(Observable o, Object o1) {
        if ((ttl / 1000) > 0) {
            ttl -= 1000;
        }
    }
}

class lifepack implements Observer, Comparable<lifepack> {

    public int value;
    public int position;
    public int ttl;

    public lifepack(int t, int ps) {
        ttl = t;
        //value = v;
        position = ps;
    }

    @Override
    public int compareTo(lifepack c) {
        return this.ttl - c.ttl;
    }

    @Override
    public void update(Observable o, Object o1) {
        if ((ttl / 1000) > 0) {
            ttl -= 1000;
        }
    }
}

public class Gclient extends Observable implements Observer, Runnable {

    ArrayList<Integer> bricks = new ArrayList<>();
    ArrayList<Integer> water = new ArrayList<>();
    ArrayList<Integer> stone = new ArrayList<>();
    int pnum, ppos, pdir, size;
    ArrayList<Integer> bfstack, pathstack, path;
    char[][] map;
    int[][] sap;
    Writer w;
    GServer listener;
    boolean brickshot, working;
    int[] recent;
    Queue<coinpile> coins;
    Queue<lifepack> lives;
    Dijkstra pathfinder;
    int prev, cur;

    public void shooter(){
        
    }
    
    public ArrayList<Integer> fillarray_I(ArrayList<Integer> ar, String st) {
        String[] temp = st.split("#")[0].split(";");
        for (int i = 0; i < temp.length; i++) {
            int xy = (Integer.parseInt(temp[i].split(",")[0])) * size + (Integer.parseInt(temp[i].split(",")[1]));
            ar.add(xy);
        }
        return ar;
    }

    public void decode(String msg) {
        brickshot = false;
        if (msg.charAt(0) == 'C' && msg.charAt(1)!='E') {
            if (coins == null) {
                coins = new PriorityQueue<>();
            }
            String[] tcm;
            tcm = msg.split(":");
            int cpos = Integer.parseInt(tcm[1].split(",")[0]) * size + Integer.parseInt(tcm[1].split(",")[1]);
            int ctl = Integer.parseInt(tcm[2]);
            int cval = Integer.parseInt(tcm[3].split("#")[0]);
            coinpile tc = new coinpile(ctl, cval, cpos);
            coins.add(tc);
            listener.addObserver(tc);
        } else {
        }

        if (msg.charAt(0) == 'L') {
            if (lives == null) {
                lives = new PriorityQueue<>();
            }
            String[] tcm;
            tcm = msg.split(":");
            int cpos = Integer.parseInt(tcm[1].split(",")[0]) * size + Integer.parseInt(tcm[1].split(",")[1]);
            int ctl = Integer.parseInt(tcm[2].split("#")[0]);
            //int cval = Integer.parseInt(tcm[3];
            lifepack tc = new lifepack(ctl, cpos);
            lives.add(tc);
            listener.addObserver(tc);
        }
        if (msg.charAt(0) == 'I' && msg.charAt(1) != 'N') {
            pnum = msg.charAt(3) - '0';

            bricks = fillarray_I(bricks, msg.split(":")[2]);

            stone = fillarray_I(stone, msg.split(":")[3]);

            water = fillarray_I(water, msg.split(":")[4]);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (bricks.contains((x * size + y))) {
                        map[x][y] = 'b';

                    } else if (stone.contains(x * size + y)) {
                        map[x][y] = 's';

                    } else if (water.contains(x * size + y)) {
                        map[x][y] = 'w';
                    } else {
                        map[x][y] = 'n';
                    }


                }

            }

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    System.out.print(map[x][y]);
                }
                System.out.println("");
            }
        }

        if (msg.charAt(0) == 'G' && msg.charAt(1) != 'A') {
            String str1 = msg.split(":")[pnum + 1];
            String[] dat = str1.split(";");
            ppos = Integer.parseInt(dat[1].split(",")[0]) * size + Integer.parseInt(dat[1].split(",")[1]);
            pdir = dat[2].charAt(0) - '0';
            do_stuff();

        }


    }

    public void do_stuff() {

        if (!working) {
            working = true;
            int nextTarget;
            if (coins == null || coins.isEmpty()) {
                System.out.println("waiting for coins");
                return;
            } else {
                coinpile c = coins.poll();
                nextTarget = c.position;
                System.out.println("moving for coin at " + c.position);
            }
            path = pathfinder.getPath(ppos, nextTarget);
        } else {
            if (path == null || path.isEmpty()) {
                working = false;
            } else {
                travelPath(path);
            }
        }
    }

    public void travelPath(ArrayList<Integer> route) {
        if (ppos == route.get(route.size() - 1)) {
            route.remove(route.size() - 1);
        }
        if (!route.isEmpty()) {
            int next = route.get(route.size() - 1);
            int dif = next - ppos;
            System.out.println("Moving to: " + next + " from :" + ppos);
            if (dif == 1) {

                //if(pdir==2)route.remove(route.size()-1);
                w.writeToPort(6000, "DOWN#");

            } else if (dif == -1) {
                //if(pdir==0)route.remove(route.size()-1);
                w.writeToPort(6000, "UP#");

            } else if (dif == size) {
                // if(pdir==1)route.remove(route.size()-1);
                w.writeToPort(6000, "RIGHT#");

            } else if (dif == (-1 * size)) {
                // if(pdir==3)route.remove(route.size()-1);
                w.writeToPort(6000, "LEFT#");

            }
        }
    }

    public void moveImmediate(int next) {
        int dif = next - ppos;

        if (dif == 1) {

            w.writeToPort(6000, "DOWN#");

        } else if (dif == -1) {

            w.writeToPort(6000, "UP#");

        } else if (dif == size) {

            w.writeToPort(6000, "RIGHT#");

        } else if (dif == (-1 * size)) {

            w.writeToPort(6000, "LEFT#");

        }

    }

    public Gclient() {
        size = 10;

        listener = new GServer();
        listener.addObserver(this);
        map = new char[10][10];
        pdir = 0;
        pathfinder = new Dijkstra(10, pdir, map);
        new Thread(listener).start();
        //x= val/100, y=val%100;
        w = new Writer();
//        Listener l = new Listener();
//        String response;
        w.writeToPort(6000, "JOIN#");

    }

    public static void main(String[] args) {

        Gclient gc = new Gclient();

    }

    @Override
    public void update(Observable o, Object o1) {
        String msg = "wait";
        //do_stuff();
        msg = listener.getInboundString();
        System.out.println(msg);
        decode(msg);
        //do_stuff();

        //System.out.println(listener.getInboundString());
        // System.out.println(o1.getClass());
    }

    @Override
    public void run() {
        while (true);
    }
}
/**
 * *******************depricated codes*************************
 */
//   void add_recent(int i) {
//        recent[0] = recent[1];
//        recent[1] = recent[2];
//        recent[2] = i;
//    }
//
//    boolean recent(int xy) {
//        boolean flag = false;
//        if (recent[0] == xy || recent[1] == xy || recent[2] == xy) {
//            flag = true;
//        }
//
//        return flag;
//    }  
//    public boolean shoot_brick(Writer w) {
//        String command = "wait";
//        int bcount = 0;
//        boolean flag = false;
//        boolean bs = false;
//        for (int x = -1; x < 2; x++) {
//            for (int y = -1; x < 2; x++) {
//                if ((x * 100 + y) == 0 || ((ppos % 100) + y) < 0 || ((ppos % 100) + y) > 9 || ((ppos / 100) + x) < 0 || ((ppos / 100) + x) > 9) {
//                    continue;
//                } else if (bricks.contains(ppos + (x * 100 + y))) {
//                    bcount++;
//                    if (x < 0 && y == 0 && pdir != 3) {
//                        command = "LEFT#";
//                        flag = true;
//                        break;
//                    } else if (x < 0 && y == 0 && pdir == 3) {
//                        command = "SHOOT#";
//                        flag = true;
//                        break;
//                    }
//                    if (x == 0 && y > 0 && pdir != 2) {
//                        command = "DOWN#";
//                        flag = true;
//                        break;
//                    } else if (x == 0 && y > 0 && pdir == 2) {
//                        command = "SHOOT#";
//                        flag = true;
//                        break;
//                    }
//                    if (x > 0 && y == 0 && pdir != 1) {
//                        command = "UP#";
//                        flag = true;
//                        break;
//                    } else if (x > 0 && y == 0 && pdir == 1) {
//                        command = "SHOOT#";
//                        flag = true;
//                        break;
//                    }
//                    if (x == 0 && y < 0 && pdir != 0) {
//                        command = "RIGHT#";
//                        flag = true;
//                        break;
//                    } else if (x == 0 && y < 0 && pdir == 0) {
//                        command = "SHOOT#";
//                        flag = true;
//                        break;
//                    }
//                }
//            }
//            if (flag) {
//                break;
//            }
//        }
//        if (flag) {
//            w.writeToPort(6000, command);
//            bs = true;
//        }
//        return bs;
//
//    }
/*
  public void BFS(int ps) {
        sap = new int[10][10];
        int n = 0;
        bfstack = new ArrayList<>();
        //bfstack.add(ps);
        sap[ps / size][ps % size] = 10;
        spread(ps, 0);


        do {
            int tm = bfstack.remove(0);
            n = spread(tm, sap[tm / size][tm % size]);
        } while (n < 10);

        sap[ps / size][ps % size] = 0;


        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                System.out.print(" " + sap[i][j]);
            }
            System.out.println("");
        }

       
    }
    public int spread(int pos, int num) {

        if (0 <= (pos / size - 1) && map[(pos - size) / 100][(pos - size) % size] == 0 && sap[(pos - size) / size][(pos - size) % size] == 0) {
            sap[(pos - size) / size][(pos - size) % size] = num + 1;
            bfstack.add(pos - size);
        }
        if (9 >= (pos + size) / size && map[(pos + size) / size][(pos + size) % size] == 0 && sap[(pos + size) / size][(pos + size) % size] == 0) {
            sap[(pos + size) / size][(pos + size) % size] = num + 1;
            bfstack.add(pos + size);
        }
        if (0 <= ((pos - 1) % size) && ((pos - 1) % size <= 9) && map[(pos - 1) / size][(pos - 1) % size] == 0 && sap[(pos - 1) / size][(pos - 1) % size] == 0) {
            sap[(pos - 1) / size][(pos - 1) % size] = num + 1;
            bfstack.add(pos - 1);
        }
        if (9 >= (pos + 1) % size && map[(pos + 1) / size][(pos + 1) % size] == 0 && sap[(pos + 1) / size][(pos + 1) % size] == 0) {
            sap[(pos + 1) / size][(pos + 1) % size] = num + 1;
            bfstack.add(pos + 1);
        }
        return num + 1;
    }
 */