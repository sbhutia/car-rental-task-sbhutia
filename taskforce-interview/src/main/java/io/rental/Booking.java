package io.rental;

import java.time.LocalDate;

public class Booking {

    private Renter renter;

    private Car car;

    private LocalDate startDate;

    private LocalDate endDate;

    public double getDailyCost() {
        return dailyCost;
    }

    public void setDailyCost(double dailyCost) {
        this.dailyCost = dailyCost;
    }

    private double dailyCost;

    private boolean isMaintenance = false;

    public Booking() {
    }

    public boolean isMaintenance() {
        return isMaintenance;
    }

    public void setMaintenance(boolean maintenance) {
        isMaintenance = maintenance;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Renter getRenter() {
        return renter;
    }

    public void setRenter(Renter renter) {
        this.renter = renter;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
