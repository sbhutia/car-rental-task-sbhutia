package io.rental;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CarRentalTest {

    private static final Car CAR1 = new Car("VW", "Golf", "XX11 1UR", "B2", 90);
    private static final Car CAR2 = new Car("VW", "Passat", "XX12 2UR",  "C1", 110);
    private static final Car CAR3 = new Car("VW", "Polo", "XX13 3UR",  "A1", 65);
    private static final Car CAR4 = new Car("VW", "Polo", "XX14 4UR",  "A1", 70);

    private static final Renter RENTER1 = new Renter("Hydrogen", "Joe", "HYDRO010190JX8NM", LocalDate.of(1990, 1, 1));
    private static final Renter RENTER2 = new Renter("Calcium", "Sam", "CALCI010203SX8NM", LocalDate.of(2003, 2, 1));
    private static final Renter RENTER3 = new Renter("Neon", "Maisy", "NEONN010398MX8NM", LocalDate.of(1998, 3, 1));
    private static final Renter RENTER4 = new Renter("Carbon", "Greta", "CARBO010497GX8NM", LocalDate.of(1997, 4, 1));

    @Test
    public void testListCarsAvailableToRentGivesMoreThanOneCar() {
        CarRentalCompany carRentalCompany = new CarRentalCompany();
        carRentalCompany.addCar(CAR1);
        carRentalCompany.addCar(CAR2);
        carRentalCompany.addCar(CAR3);
        carRentalCompany.addCar(CAR4);

        Criteria criteria = new Criteria();
        List<Car> carsAvailable = carRentalCompany.matchingCars(criteria);

        assertThat(carsAvailable.size()).isGreaterThan(1);
    }

    /* Test for Story 1 - Find Car to Rent */
    @Test
    public void testFindCarToRent() throws BookingException {
        CarRentalCompany carRentalCompany = new CarRentalCompany();
        addSampleBookings(carRentalCompany);

        // test for car already booked for the period
        List<Car> availableCars = carRentalCompany.availableCars(CAR1.getMake(), CAR1.getModel(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(15));
        assertThat(availableCars.size()).isEqualTo(0);

        // test for car that is available during the period
        availableCars = carRentalCompany.availableCars(CAR1.getMake(), CAR1.getModel(), LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));
        assertThat(availableCars.size()).isEqualTo(1);
    }

    /* Test for Story 3 - Booking A Car*/
    @Test
    public void bookCar() throws BookingException {
        CarRentalCompany carRentalCompany = new CarRentalCompany();
        addSampleBookings(carRentalCompany);

        // add a new booking - successful
        assertThat(carRentalCompany.getBookings().size()).isEqualTo(3);

        Booking booking = new Booking();
        booking.setCar(CAR1);
        booking.setRenter(RENTER1);
        booking.setStartDate(LocalDate.now().plusDays(10));
        booking.setEndDate(LocalDate.now().plusDays(15));
        carRentalCompany.addBooking(booking);

        assertThat(carRentalCompany.getBookings().size()).isEqualTo(4);

        // add same booking again - unsuccessful
        try{
            carRentalCompany.addBooking(booking);
        }catch (BookingException e){
            System.out.println(e.getMessage());
        }
        assertThat(carRentalCompany.getBookings().size()).isEqualTo(4);
    }

    /* Test for Story 4 - Find Upcoming Rentals*/
    @Test
    public void findUpcomingRentals() throws BookingException {
        CarRentalCompany carRentalCompany = new CarRentalCompany();

        addSampleBookings(carRentalCompany);

        assertThat(carRentalCompany.upcomingRentals().size()).isEqualTo(2);

        // adding new booking at the end with intermediate start date
        Booking booking = new Booking();
        booking.setCar(CAR4);
        booking.setRenter(RENTER4);
        booking.setStartDate(LocalDate.now().plusDays(2));
        booking.setEndDate(booking.getStartDate().plusDays(4));
        carRentalCompany.addBooking(booking);

        List<Booking>  upcomingRentals = carRentalCompany.upcomingRentals();

        //test that the bookings are sorted by start date
        assertThat(upcomingRentals.size()).isEqualTo(3);

        for(int i = 0; i < upcomingRentals.size()-1; i++) {
            assertThat(upcomingRentals.get(i).getStartDate().isBefore(upcomingRentals.get(i+1).getStartDate())).isTrue();
        }

    }

    /* Test for Story 5 - Book car for maintenance */
    @Test
    public void bookCarForMaintenance() throws BookingException {
        CarRentalCompany carRentalCompany = new CarRentalCompany();

        addSampleBookings(carRentalCompany);

        assertThat(carRentalCompany.getBookings().size()).isEqualTo(3);

        // add new booking as maintenance
        Booking maintenanceBooking = new Booking();
        maintenanceBooking.setCar(CAR4);
        maintenanceBooking.setStartDate(LocalDate.now().plusDays(20));
        maintenanceBooking.setEndDate(LocalDate.now().plusDays(25));
        maintenanceBooking.setMaintenance(true);

        carRentalCompany.addBooking(maintenanceBooking);

        assertThat(carRentalCompany.getBookings().size()).isEqualTo(4);
    }

    /* Test for Story 6 - ShowBlendedPricing */
    @Test
    public void showBlendedPricing() throws BookingException {
        CarRentalCompany carRentalCompany = new CarRentalCompany();

        addSampleBookings(carRentalCompany);

        Map<String,Double> blendedPrices = carRentalCompany.getBlendedPrice();

        assertThat(blendedPrices.size()).isEqualTo(3);

        List<Car> carsWithBlendedPrices = carRentalCompany.getMatchingCarsIncludingBlendedPrice("VW", "Polo", LocalDate.now().plusDays(30), LocalDate.now().plusDays(35));

        assertThat(carsWithBlendedPrices.size()).isEqualTo(2);

        assertThat(carsWithBlendedPrices.get(0).getCostPerDay()).isEqualTo(67.5);

        //make booking with blended price
        assertThat(carRentalCompany.getBookings().size()).isEqualTo(3);

        Booking bookingWithBlendedPrice = new Booking();
        bookingWithBlendedPrice.setCar(CAR3);
        bookingWithBlendedPrice.setRenter(RENTER4);
        bookingWithBlendedPrice.setStartDate(LocalDate.now().plusDays(30));
        bookingWithBlendedPrice.setEndDate(LocalDate.now().plusDays(35));
        bookingWithBlendedPrice.setDailyCost(blendedPrices.get(CAR3.getRentalGroup()));
        carRentalCompany.addBooking(bookingWithBlendedPrice);

        assertThat(carRentalCompany.getBookings().size()).isEqualTo(4);

    }

    public void addSampleBookings(CarRentalCompany carRentalCompany){

        try{
            carRentalCompany.addCar(CAR1);
            carRentalCompany.addCar(CAR2);
            carRentalCompany.addCar(CAR3);
            carRentalCompany.addCar(CAR4);

            Booking booking;

            booking = new Booking();
            booking.setCar(CAR1);
            booking.setRenter(RENTER1);
            booking.setStartDate(LocalDate.now());
            booking.setEndDate(booking.getStartDate().plusDays(7));

            carRentalCompany.addBooking(booking);

            booking = new Booking();
            booking.setCar(CAR2);
            booking.setRenter(RENTER2);
            booking.setStartDate(LocalDate.now().plusDays(1));
            booking.setEndDate(booking.getStartDate().plusDays(7));

            carRentalCompany.addBooking(booking);

            booking = new Booking();
            booking.setCar(CAR3);
            booking.setRenter(RENTER3);
            booking.setStartDate(LocalDate.now().plusDays(3));
            booking.setEndDate(booking.getStartDate().plusDays(7));

            carRentalCompany.addBooking(booking);
        } catch (BookingException e) {
            System.out.println("An error occured - " + e.getMessage());
        }
    }
}
