/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gclient;

import java.util.*;

/**
 *
 *
 * @author dracus
 */
class node implements Comparable<node> {

    private int pos;
    public int dist;
    public int pred;

    public node(int p) {
        pos = p;
        dist = Integer.MAX_VALUE;
        pred = -1;
    }

    public int Pos() {
        return pos;
    }

    @Override
    public int compareTo(node t) {
        return this.dist - t.dist;
    }
}

public class Dijkstra {

    int dir, s, dest, sz, pos;
    char[][] map;
    int[] pred, dist;
    ArrayList<node> Q;

    public Dijkstra(int size, int dir, char[][] map) {
        sz = size;
        dest = 0;

        this.dir = dir;
        this.map = map;


        pred = new int[sz * sz];
        dist = new int[sz * sz];




    }

    node getMinNode(ArrayList<node> A) {
        node temp = A.get(0);
        for (int i = 1; i < A.size(); i++) {
            if (temp.dist > A.get(i).dist) {
                temp = A.get(i);
            }
        }

        return temp;
    }

    public int[] dijkstra(int s) {
        Q = new ArrayList();
        for (int i = 0; i < sz * sz; i++) {
            Q.add(i, new node(i));
            Q.get(i).dist = Integer.MAX_VALUE;
            dist[i] = Integer.MAX_VALUE;
        }

        Q.get(s).dist = 0;
        //System.out.println(Q.get(s).dist);
        dist[s] = 0;
        //System.out.println(dist[s]);

        while (!Q.isEmpty()) {
            node u = getMinNode(Q);
            removeNode(u, Q);
            if (u.dist == Integer.MAX_VALUE) {
                System.out.println("Unreachable");
                break;
            } else {
                if (u.Pos() / sz != 0) {
                    int v = u.Pos() - sz;
                    if (map[v / sz][v % sz] == 'n') {
                        int alt = dist[u.Pos()] + 1;
                        if (alt < dist[v]) {
                            dist[v] = alt;
                            pred[v] = u.Pos();
                            reOrderQ(Q, v, alt);
                        }
                    }
                }
                if (u.Pos() / sz < (sz - 1)) {
                    int v = u.Pos() + sz;
                    if (map[v / sz][v % sz] == 'n') {
                        int alt = dist[u.Pos()] + 1;
                        if (alt < dist[v]) {
                            dist[v] = alt;
                            pred[v] = u.Pos();
                            reOrderQ(Q, v, alt);
                        }
                    }
                }
                if (u.Pos() % sz != 0) {
                    int v = u.Pos() - 1;
                    if (map[v / sz][v % sz] == 'n') {
                        int alt = dist[u.Pos()] + 1;
                        if (alt < dist[v]) {
                            dist[v] = alt;
                            pred[v] = u.Pos();
                            reOrderQ(Q, v, alt);
                        }
                    }
                }
                if (u.Pos() % sz != (sz - 1)) {
                    int v = u.Pos() + 1;
                    if (map[v / sz][v % sz] == 'n') {
                        int alt = dist[u.Pos()] + 1;
                        if (alt < dist[v]) {
                            dist[v] = alt;
                            pred[v] = u.Pos();
                            reOrderQ(Q, v, alt);
                        }
                    }
                }
            }
        }

//        for (int i = 0; i < sz; i++) {
//            for (int j = 0; j < sz; j++) {
//                System.out.print(dist[i * sz + j] + ": ");
//            }
//            System.out.println("");
//        }
//        System.out.println("********");
//        for (int i = 0; i < sz; i++) {
//            for (int j = 0; j < sz; j++) {
//                System.out.print(pred[i * sz + j] + ": ");
//            }
//            System.out.println("");
//        }

        //Q=null;
        //dist=null;
        //System.gc();
        return pred;
    }

    void reOrderQ(ArrayList<node> A, int node, int dist) {
        for (int i = 0; i < A.size(); i++) {
            if (A.get(i).Pos() == node) {
                A.get(i).dist = dist;
                break;
            }
        }
    }

    void removeNode(node n, ArrayList<node> A) {
        for (int i = 0; i < A.size(); i++) {
            if (n.Pos() == A.get(i).Pos()) {
                A.remove(i);
                break;
            }
        }
    }

    public ArrayList<Integer> getPath(int source, int dest) {
        ArrayList<Integer> path = new ArrayList<>();
        int[] preds = dijkstra(source);
        int pr, cur, count = 0;

        cur = dest;
        pr = preds[cur];

        while (cur != source) {
            path.add(count, cur);
            cur = pr;
            pr = preds[cur];
            count++;
        }

        for (int i = 0; i < path.size(); i++) {
            System.out.print(path.get(i) + " --> ");
        }
        return path;
    }

    public static void main(String[] args) {
        Dijkstra d = new Dijkstra(10, 0, new char[10][10]);
        //d.dijkstra(75);
        d.getPath(75, 0);
    }
}
