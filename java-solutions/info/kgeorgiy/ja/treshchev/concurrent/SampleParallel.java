package info.kgeorgiy.ja.treshchev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleParallel {

    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(i + 1);
        }
        System.out.println(Arrays.toString(list.toArray()));
        ParallelMapper myMapper = new ParallelMapperImpl(6);
        Thread threadMap = new Thread(() -> {
            try {
                List<Integer> res = myMapper.map(a -> a + 1, list);
                System.out.println(Arrays.toString(res.toArray()));
            } catch (InterruptedException e) {
                System.err.println("Interrupted exception caught in threadMap: " + e.getMessage());
            }
        });
        Thread threadStop = new Thread(myMapper::close);
        Thread threadAfterClose = new Thread(() -> {
            try {
                List<Integer> res = myMapper.map(a -> a + 1, list);
                System.out.println(Arrays.toString(res.toArray()));
            } catch (InterruptedException e) {
                System.err.println("Interrupted exception caught in threadMap: " + e.getMessage());
            }
        });

        threadMap.start();
        threadStop.start();
//        threadAfterClose.start();

        try {
            threadMap.join();
            threadStop.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
