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

    public List<Booking> getBookings(){
        return bookings;
    }

    public void addCar(Car car) {
        cars.add(car);
    }

    /* Story 1 find car to rent */
    public synchronized List<Car> matchingCars(Criteria criteria) {
        List<Car> results = new ArrayList<Car>();

        // if no criteria is set then return all cars as match
        if (criteria.getMake()==null && criteria.getModel()==null){
            return cars;
        }

        for (Car c : cars){
            // check whether make and model matches
            if (c.getMake().equals(criteria.getMake()) && c.getModel().equals(criteria.getModel())) {

                boolean carBooked = false;

                // check if car is already booked for the period
                for (Booking b : bookings) {
                    if (c.getRegistrationNumber().matches(b.getCar().getRegistrationNumber()) && isBooked(criteria, b)) {
                        carBooked = true;
                        break;
                    }
                }

                // if make and model matches and car is not booked then add to results
                if (!carBooked) {
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
        // check if booking is in the past
        if(newBooking.getStartDate().isBefore(LocalDate.now())){
            throw new BookingException("Booking is in the past");
        }

        // check that booking does not clash with existing ones
        for (Booking b : bookings){

            // check whether booking exists for the same car */
            if(b.getCar().getRegistrationNumber().equals(newBooking.getCar().getRegistrationNumber())){
                Criteria c = new Criteria();
                c.setFromDate(newBooking.getStartDate());
                c.setToDate(newBooking.getEndDate());
                if(isBooked(c, b)){
                    throw new BookingException ("A booking already exists for these dates");
                }
            }
        }

        // if checks have passed then add new booking
        bookings.add(newBooking);
    }

    /* Story 4 - car preparation */
    public List<Booking> upcomingRentals(){
        List<Booking> upcomingRentals = new ArrayList<Booking>();

        // find booking between tomorrow and 7 days from today
        for (Booking b : bookings) {
            if (b.getStartDate().isAfter(LocalDate.now()) && b.getStartDate().isBefore(LocalDate.now().plusDays(8))) {
                upcomingRentals.add(b);
            }
        }

        /* sort the results by date */
        upcomingRentals.sort(Comparator.comparing(Booking::getStartDate));

        return upcomingRentals;
    }

    /* Story 5 - car maintenance */
    public synchronized void registerCarMaintenance (Car car, LocalDate startDate, LocalDate endDate){
        // book the car for maintenance
        Booking maintenanceBooking = new Booking();
        maintenanceBooking.setCar(car);
        maintenanceBooking.setStartDate(startDate);
        maintenanceBooking.setEndDate(endDate);
        maintenanceBooking.setMaintenance(true);

        bookings.add(maintenanceBooking);
    }

    /* Story 6 - Rental Pricing */
    public synchronized List<Car> getMatchingCarsIncludingBlendedPrice(String make, String model, LocalDate startDate, LocalDate endDate) throws BookingException {
        List<Car> matchingCars = availableCars(make, model, startDate, endDate);

        // add blended price to the cars;
        Map<String, Double> blendedPrices = getBlendedPrice();
        for(Car car: matchingCars){
            car.setCostPerDay(blendedPrices.get(car.getRentalGroup()));
        }

        return matchingCars;
    }

    public Map<String, Double> getBlendedPrice(){
        List<Car> blendedPrices = new ArrayList<Car>();

        Map<String, Double> map = cars.stream().collect(groupingBy(Car::getRentalGroup, averagingDouble(Car::getCostPerDay)));

        return map;
    }

    // Utility function to check whether the car is booked between two dates
     private boolean isBooked (Criteria c, Booking b){

        LocalDate queryStartDate = c.getFromDate();
        LocalDate queryEndDate = c.getToDate();
        LocalDate startDate = b.getStartDate();
        LocalDate endDate = b.getEndDate();

        /* check that queried dates lies outside of booked dates */
        if(queryEndDate.isBefore(startDate) || queryStartDate.isAfter(endDate)){
            return false;
        }else{
            return true;
        }
    }

}
