import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainClass {
    public static final int CARS_COUNT = 4;

    public static void main(String[] args) {
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        CountDownLatch countDownLatch = new CountDownLatch(CARS_COUNT);
        CountDownLatch countDownLatch1 = new CountDownLatch(CARS_COUNT);
        Lock lock = new ReentrantLock();
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10),countDownLatch,countDownLatch1,lock);
        }
        for (int i = 0; i < cars.length; i++) {
            Thread thread = new Thread(cars[i]);
            thread.start();
        }
    }
}

class Car implements Runnable {
    private static int CARS_COUNT;
    private static int WIN;

    static {
        CARS_COUNT=0;
        WIN = 0;
    }
    private final CountDownLatch countDownLatch1;
    private final CountDownLatch countDownLatch;
    private final Lock lock;
    private Race race;
    private int speed;
    private String name;
    public String getName() {
        return name;
    }
    public int getSpeed() {
        return speed;
    }
    public Car(Race race, int speed,CountDownLatch countDownLatch,CountDownLatch countDownLatch1,Lock lock) {
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
        this.countDownLatch=countDownLatch;
        this.countDownLatch1=countDownLatch1;
        this.lock=lock;
    }

    public void getWin(Lock lock){
        try{
            lock.lock();
            if (WIN==CARS_COUNT){
                System.out.println(getName()+" WIN");
            }
            WIN--;
            if (WIN==0){
                System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            countDownLatch.countDown();
            countDownLatch.await();
            Thread.sleep(500 + (int)(Math.random() * 800));
            System.out.println(this.name + " готов");
            WIN++;
            if (WIN==CARS_COUNT){
                System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
            }
            countDownLatch1.countDown();
            countDownLatch1.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }
        getWin(lock);
    }
}

abstract class Stage {
    protected int length;
    protected String description;
    public String getDescription() {
        return description;
    }
    public abstract void go(Car c);
}

class Road extends Stage {
    public Road(int length) {
        this.length = length;
        this.description = "Дорога " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            System.out.println(c.getName() + " начал этап: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
            System.out.println(c.getName() + " закончил этап: " + description);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Tunnel extends Stage {
    Semaphore semaphore = new Semaphore(MainClass.CARS_COUNT/2);
    public Tunnel() {
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            try {
                System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                semaphore.acquire();
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " закончил этап: " + description);
                semaphore.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Race {
    private ArrayList<Stage> stages;
    public ArrayList<Stage> getStages() { return stages; }
    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
    }
}


