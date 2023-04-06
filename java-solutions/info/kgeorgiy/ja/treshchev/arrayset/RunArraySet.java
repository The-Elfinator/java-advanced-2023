package info.kgeorgiy.ja.treshchev.arrayset;

import java.util.ArrayList;

public class RunArraySet {

    public static void main(String[] args) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        int[] arr = new int[]{804031843, -1587631165, -834402831, 1848061279, 614137574, 1860974621};
        for (int x : arr) {
            arrayList.add(x);
        }
        ArraySet<Integer> arraySet = new ArraySet<>(arrayList, (o1, o2) -> -o1.compareTo(o2));
        System.out.println(arraySet);
        System.out.println(arraySet.subSet(-1455245012, -1196600064));
//        System.out.println(arraySet.tailSet(100));
    }
}
