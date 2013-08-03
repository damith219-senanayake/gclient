package gclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 *
 * @author Damith
 *
 *
 */
public class Gclient extends Observable implements Observer, Runnable {

    ArrayList<Integer> bricks = new ArrayList<>();
    ArrayList<Integer> water = new ArrayList<>();
    ArrayList<Integer> stone = new ArrayList<>();
    int pnum, ppos, pdir, size, moveCount;
    ArrayList<Integer> bfstack, pathstack, path;
    char[][] map;
    int[][] sap;
    Writer w;
    int nextTarget, targetPos, targetPlayer;
    GServer listener;
    boolean brickshot, working, hunting, gathering;
    int[] recent, playerPos, playerDir, playerHealth;
    boolean[] playerShot;
    ArrayList<coinpile> coins;
    ArrayList<lifepack> lives;
    Dijkstra pathfinder;
    int prev, cur;
    boolean prevCoin, prevLife, shooting;
    ArrayList<player> players;
    int deadcount = 0;

    public int TargetRow(int target) {
        int tt = (ppos / size) * size + target % size;
        return tt;

    }

    public int TargetColumn(int target) {
        int tt = (ppos % size) + (target / size) * size;
        return tt;
    }

    public void MoveToRowShoot(int Target) {
        if (ppos % size != Target % size) {
            path = pathfinder.getPath(ppos, TargetRow(Target), playerPos);
            travelPath(path);
        } else {
            if (ppos / size > Target / size) {
                if (pdir == 3) {
                    w.writeToPort(6000, "SHOOT#");
                } else {
                    w.writeToPort(6000, "LEFT#");
                }
            } else if (ppos / size < Target / size) {
                if (pdir == 1) {
                    w.writeToPort(6000, "SHOOT#");
                } else {
                    w.writeToPort(6000, "RIGHT#");
                }
            }
        }


    }

    public void MoveToColumnShoot(int Target) {
        if (ppos / size != Target / size) {
            path = pathfinder.getPath(ppos, TargetColumn(Target), playerPos);
            travelPath(path);
        } else {
            if (ppos % size > Target % size) {
                if (pdir == 0) {
                    w.writeToPort(6000, "SHOOT#");
                } else {
                    w.writeToPort(6000, "UP#");
                }
            } else if (ppos % size < Target % size) {
                if (pdir == 2) {
                    w.writeToPort(6000, "SHOOT#");
                } else {
                    w.writeToPort(6000, "DOWN#");
                }
            }
        }


    }

    public void shoot(int Target) {

        if (!working) {
            working = true;

        }
        if (working) {
            int rowdif = Math.abs(ppos % size - Target % size);
            int coldif = Math.abs(ppos / size - Target / size);
            if (rowdif < coldif) {
                MoveToRowShoot(Target);
            } else {
                MoveToColumnShoot(Target);
            }

        }


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


        if (msg.charAt(0) == 'C' && msg.charAt(1) != 'E') {
            if (coins == null) {
                coins = new ArrayList<>();
            }
            String[] tcm;
            tcm = msg.split(":");
            int cpos = Integer.parseInt(tcm[1].split(",")[0]) * size + Integer.parseInt(tcm[1].split(",")[1]);
            int ctl = Integer.parseInt(tcm[2]);
            int cval = Integer.parseInt(tcm[3].split("#")[0]);
            coinpile tc = new coinpile(ctl, cval, cpos);
            coins.add(tc);
            listener.addObserver(tc);
        } else if (msg.charAt(0) == 'C' && msg.charAt(1) == 'E') {
            try {
                Thread.sleep(900);
            } catch (InterruptedException ex) {
                w.writeToPort(6000, "SHOOT#");
            }
            w.writeToPort(6000, "SHOOT#");
        }

        if (msg.charAt(0) == 'S') {
            players = new ArrayList();
            String[] plinf = msg.split(":");
            playerPos = new int[plinf.length - 1];
            playerDir = new int[plinf.length - 1];
            playerHealth = new int[plinf.length - 1];
            for (int i = 0; i < (plinf.length - 1); i++) {
                int tepnum = Integer.parseInt(plinf[i + 1].split(";")[0].substring(1));
                int tepx = Integer.parseInt(plinf[i + 1].split(";")[1].split(",")[0]);
                int tepy = Integer.parseInt(plinf[i + 1].split(";")[1].split(",")[1]);
                int tepdr = Integer.parseInt(plinf[i + 1].split(";")[2].split("#")[0]);

                playerPos[i] = tepx * size + tepy;

                playerDir[i] = tepdr;
                playerShot = new boolean[playerPos.length];
                player tepl = new player();
                tepl.name = i;
                tepl.position = playerPos[i];
                tepl.health = 100;
                tepl.coins = 0;
                players.add(i, tepl);
            }
        }

        if (msg.charAt(0) == 'L') {
            if (lives == null) {
                lives = new ArrayList<>();
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

            String[] plinf = msg.split(":");

            for (int i = 0; i < (playerPos.length); i++) {
                int tepx = Integer.parseInt(plinf[i + 1].split(";")[1].split(",")[0]);
                System.out.println("Current x " + tepx);
                int tepy = Integer.parseInt(plinf[i + 1].split(";")[1].split(",")[1]);
                System.out.println("Current y " + tepy);
                int tepdr = Integer.parseInt(plinf[i + 1].split(";")[2].split("#")[0]);
                int teshot = Integer.parseInt(plinf[i + 1].split(";")[3]);
                int health = Integer.parseInt(plinf[i + 1].split(";")[4]);
                playerPos[i] = tepx * size + tepy;
                playerDir[i] = tepdr;
                playerHealth[i] = health;
                if (teshot == 0) {
                    playerShot[i] = false;
                } else {
                    playerShot[i] = true;
                }
            }
            ppos = playerPos[pnum];
            pdir = playerDir[pnum];
            refreshPlayers();
            coins = refreshCAtG(coins);
            lives = refreshLAtG(lives);
            do_stuff();

        }
    }

    public void refreshPlayers() {
        ArrayList<player> teps;
        teps = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            player p = players.get(i);
            if (p.health > 0) {
                teps.add(p);
            } else {
                playerPos[p.name] = 18;
            }
        }

        players = teps;
    }

    int getClosestCoinP() {
        int nex = 0;
        try {
            Collections.sort(coins);
            nex = coins.remove(0).position;
        } catch (Exception e) {
            nex = new Random().nextInt() % size * size;
        }
        return nex;

    }

    int getClosestLifeP() {
        int nex = 0;
        try {
            Collections.sort(lives);
            nex = lives.remove(0).position;
        } catch (Exception e) {
            nex = new Random().nextInt() % size * size;
        }
        return nex;

    }

    public void do_stuff() {
        if (!gathering) {
            hunt();
            moveCount--;
            if (moveCount <= 0) {
                moveCount = 50;
                gathering = true;
            }

        } else {
            gather();
            moveCount--;
            if (moveCount <= 0) {
                moveCount = 30;
                gathering = false;
            }
        }
        //shoot();


    }

    public void hunt() {
        int deadcount = 0;
        for (int i = 0; i < playerHealth.length; i++) {
            if (playerHealth[i] == 0) {
                deadcount++;
            }
        }

        if (deadcount == (playerHealth.length - 1)) {
            while (moveCount > 0) {
                moveCount--;
            }
        }

        if (playerPos.length >= 2) {
            player p = this.nextTargetPlayer();
            if (p == null) {
                moveCount = 0;
                hunting=true;
            } else {
                targetPlayer = p.name;
                targetPos = p.position;
            }
            if (!hunting) {
//                for (int i = 0; i < playerPos.length; i++) {
//
//
//
//                    if (i != pnum && playerHealth[i] != 0) {
//                        targetPlayer = i;
//                        targetPos = playerPos[i];
//                        System.out.println("Target Player= Player" + targetPlayer);
                        hunting = true;
//                        break;
//                    }
//                }
            }
            if (playerHealth[targetPlayer] == 0 && hunting) {
                hunting = false;
                path = pathfinder.getPath(ppos, playerPos[targetPlayer], playerPos);
                travelPath(path);
                return;
            }


            shoot(playerPos[targetPlayer]);

        }

    }

    public void gather() {
        if (playerHealth[pnum] < 100) {
            getLife();
        } else {
            getCoin();
        }
    }


    ArrayList<coinpile> refreshCAtG(ArrayList<coinpile> c) {
        ArrayList<coinpile> temp = new ArrayList<>();
        try {
            for (int i = 0; i < c.size(); i++) {
                coinpile cp = c.get(i);
                if (cp.ttl / 1000 > 0) {
                    temp.add(cp);
                }
            }
            c = null;
        } catch (Exception e) {
            temp = c;
        }


        return temp;
    }

    ArrayList<lifepack> refreshLAtG(ArrayList<lifepack> l) {
        ArrayList<lifepack> temp = new ArrayList<>();
        try {
            for (int i = 0; i < l.size(); i++) {
                lifepack cp = l.get(i);
                if (cp.ttl / 1000 > 0) {
                    temp.add(cp);
                }
            }
            l = null;
        } catch (Exception e) {
            temp = l;
        }
        return temp;
    }

    public void getCoin() {
        if (!working) {
            working = true;
            if (coins == null || coins.isEmpty()) {
                System.out.println("waiting for coins");
            } else {


                nextTarget = this.getClosestCoinP();
                System.out.println("moving for coin at " + nextTarget);
            }

        } else {
            if (playerPos == null) {

                System.out.println("Error!");
            } else {
                for (int i = 0; i < playerPos.length; i++) {
                    System.out.println(playerPos[i]);
                }
            }
            path = pathfinder.getPath(ppos, nextTarget, playerPos);
            if (path == null || path.isEmpty()) {
                working = false;
                prevLife = false;
                prevCoin = true;
            } else {
                travelPath(path);
            }
        }

    }

    public void getLife() {
        if (!working) {
            working = true;
            int nextTarget;
            if (lives == null || lives.isEmpty()) {
                System.out.println("waiting for lifepacks");

                return;
            } else {


                nextTarget = this.getClosestLifeP();
                System.out.println("moving for lifepack at " + nextTarget);
            }
            path = pathfinder.getPath(ppos, nextTarget, playerPos);
        } else {
            if (path == null || path.isEmpty()) {
                working = false;
                prevCoin = false;
                prevLife = true;

            } else {
                travelPath(path);
            }
        }

    }

    public void travelPath(ArrayList<Integer> route) {
        if (  route.size()>0 && ppos == route.get(route.size() - 1)) {
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
        prevLife = true;
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
        String msg;
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


    class coinpile implements Observer, Comparable<coinpile> {

        public int value;
        public int position;
        public int ttl;

        public coinpile(int t, int v, int ps) {
            ttl = t;
            value = v;
            position = ps;
        }

        public int getDist() {
            int dist = (Math.abs((this.position - ppos) / size) + Math.abs(this.position % size - ppos % size));
            return dist;
        }

        @Override
        public int compareTo(coinpile c) {
            return this.getDist() - c.getDist();
        }

        @Override
        public void update(Observable o, Object o1) {
            if ((ttl / 1000) > 0) {
                ttl -= 1000;
            }
        }
    }

    player nextTargetPlayer() {
        Collections.sort(players);
        player p = players.get(0);
        int count = 0;
        while (count < players.size() && (p.health == 0 || p.name == pnum)) {
            p = players.get(count);
            count++;

        }

        return p;

    }

    class player implements Comparable<player> {

        public int name;
        public int health;
        public int coins;
        public int position;

        int getDist() {
            return pathfinder.getPath(ppos, this.position, playerPos).size();

        }

        @Override
        public int compareTo(player p) {
            return this.getDist() - p.getDist();
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

        public int getDist() {
            int dist = (Math.abs((this.position - ppos) / size) + Math.abs(this.position % size - ppos % size));
            return dist;
        }

        @Override
        public int compareTo(lifepack c) {
            return this.getDist() - c.getDist();
        }

        @Override
        public void update(Observable o, Object o1) {
            if ((ttl / 1000) > 0) {
                ttl -= 1000;
            }
        }
    }
}
/**
 * *******************depricated codes*************************
 */
//    coinpile getTargetCoinpile(){
//        coinpile c;
//        c=coins.peek();
//        return c;
//    }
//Two internal classes that are used for keeping track of coinpiles and lifepacks
//    public void getCoin() {
//
//        if (!working) {
//            working = true;
//            int nextTarget;
//            if (coins == null || coins.isEmpty()) {
//                System.out.println("waiting for coins");
//                return;
//            } else {
//
//                coinpile c = coins.poll();
//                int count = 100;
//                while (c.ttl <= 0 && count > 0) {
//                    c = coins.poll();
//                    count--;
//                }
//                nextTarget = c.position;
//                System.out.println("moving for coin at " + c.position);
//            }
//            path = pathfinder.getPath(ppos, nextTarget);
//        } else {
//            if (path == null || path.isEmpty()) {
//                working = false;
//                prevLife = false;
//                prevCoin = true;
//            } else {
//                travelPath(path);
//            }
//        }
//
//    }
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