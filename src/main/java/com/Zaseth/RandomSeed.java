package com.Zaseth;

class RandomSeeder {
    private int rndSeed = 327680;
    private int finishedSeed = 0;

    public RandomSeeder() {}

    public RandomSeeder(int customSeed) {
        this.setSeed(customSeed);
    }

    public int Random(int value) {
        int rawSeed = (this.rndSeed * 1140671485 + 1280163) % 16777216;
        this.rndSeed = rawSeed;
        double divideSeed = rawSeed * 1.0 / 16777216;
        int roundedSeed = (int) Math.ceil(divideSeed * value);
        this.finishedSeed = roundedSeed;
        return roundedSeed;
    }

    public void setSeed(int value) {
        this.rndSeed = Math.abs(value);
    }

    public int getSeed() {
        return this.rndSeed;
    }

    public int getFinishedSeed() {
        return this.finishedSeed;
    }

    public int getDefaultSeed() {
        return 327680;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Finished seed: " + this.getFinishedSeed());
        sb.append("\r\nRandom seed value: " + this.getSeed());
        sb.append("\r\nDefault seed value: " + this.getDefaultSeed());
        return sb.toString();
    }

    public static void main(String[] args) {
        RandomSeeder rsd = new RandomSeeder();
        rsd.Random(50);
        System.out.println(rsd.toString());
    }
}