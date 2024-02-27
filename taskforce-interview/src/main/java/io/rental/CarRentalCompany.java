package io.rental;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

public class CarRentalCompany {
    private List<Car> cars = new ArrayList<>();

    private List<Booking> bookings = new ArrayList<Booking>();

    public void addCar(Car car) {
        cars.add(car);
    }

    /* Story 1 find car to rent */
    public synchronized List<Car> matchingCars(Criteria criteria) {
        List<Car> results = new ArrayList<Car>();


        for (Car c : cars){
            /* check whether make and model matches */
            if (c.getMake().equals(criteria.getMake()) && c.getModel().equals(criteria.getModel())) {

                boolean carBooked = false;

                for (Booking b : bookings) {
                    if (c.getRegistrationNumber().matches(b.getCar().getRegistrationNumber()) && !isBooked(criteria, b)) {
                        carBooked = true;
                        break;
                    }
                }

                /* if make and model matches and car is not booked then add to results */
                if (carBooked = false) {
                    results.add(c);
                }
            }
        }

        return results;
    }

    /* Story 2 - find an available car to be rented */
    public synchronized List<Car> availableCars(String make, String model, LocalDate startDate, LocalDate endDate) throws BookingException {
        if (make == null || model == null || startDate == null || endDate == null){
            throw new BookingException("Invalid arguments");
        }

        Criteria criteria= new Criteria();
        criteria.setMake(make);
        criteria.setModel(model);
        criteria.setFromDate(startDate);
        criteria.setToDate(endDate);

        return matchingCars(criteria);
    }

    /* Story 3 - Booking a car */
    public synchronized void addBooking(Booking newBooking) throws BookingException {

        /* check that booking does not clash with existing ones */
        for (Booking b : bookings){
            /* check whether booking exists for the same car */

            if(b.getCar().getRegistrationNumber().equals(newBooking.getCar().getRegistrationNumber())){
                /* check whether there is a date clash */
                Criteria c = new Criteria();
                c.setFromDate(newBooking.getStartDate());
                c.setToDate(newBooking.getEndDate());
                if(isBooked(c, b)){
                    throw new BookingException ("A bookign already exists for these dates");
                }
            }
        }

        /* if checks have passed then add new booking */
        bookings.add(newBooking);
    }

    /* Story 4 - car preparation */
    public List<Booking> upcomingRentals(){
        List<Booking> upcomingRentals = new ArrayList<Booking>();

        for (Booking b : bookings) {
            if (b.getStartDate().isAfter(LocalDate.now()) && b.getEndDate().isBefore(LocalDate.now().plusDays(7))) {
                upcomingRentals.add(b);
            }
        }

        /* sort the results by date */
        upcomingRentals.sort(Comparator.comparing(Booking::getEndDate));
        return upcomingRentals;
    }

    /* Story 5 - car maintenance */
    public synchronized void registerCarMaintenance (Car car, LocalDate startDate, LocalDate endDate){


        /* book the car for maintenance */
        Booking maintenanceBooking = new Booking();
        maintenanceBooking.setCar(car);
        maintenanceBooking.setStartDate(startDate);
        maintenanceBooking.setEndDate(endDate);
        maintenanceBooking.setMaintenance(true);

        bookings.add(maintenanceBooking);
    }

    /* Story 6 - Rental Pricing */
    public List<Car> getBlendedPrice(){
        List<Car> blendedPrices = new ArrayList<Car>();

        Map<String, Double> map = cars.stream().collect(groupingBy(Car::getRentalGroup, averagingDouble(Car::getCostPerDay)));

        //blendedPrices = cars.stream().map(Car::setCostPerDay, map.get(Car::getRentalGroup));
        return null;
    }


    public void rentCar(Renter renter, Car car) {}

    public void returnCar(Renter renter, Car car) {}

    /* Utility function to check whether the car is booked between two dates */
     private boolean isBooked (Criteria c, Booking b){

        LocalDate queryStartDate = c.getFromDate();
        LocalDate queryEndDate = c.getToDate();
        LocalDate startDate = b.getStartDate();
        LocalDate endDate = b.getEndDate();

        if(queryStartDate.isBefore(startDate) && queryEndDate.isAfter(endDate)) {
            /* booking lies within queried dates */
            return true;
        }
        if (queryStartDate.isAfter(startDate) && queryStartDate.isBefore(endDate)) {
            /* start date lies within booking period */
            return true;
        }

        if (queryEndDate.isAfter(startDate) && queryEndDate.isBefore(endDate)){
            /* end date lies within booking period */
            return true;
        }

        /* no booking for the period */
        return false;
    }

}
