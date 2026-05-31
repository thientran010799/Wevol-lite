package com.wevol.trip.service;

import com.wevol.config.AppConstants;
import com.wevol.exception.AppException;
import com.wevol.trip.dto.*;
import com.wevol.trip.entity.Trip;
import com.wevol.trip.entity.TripStop;
import com.wevol.trip.repository.TripRepository;
import com.wevol.trip.repository.TripStopRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final TripStopRepository stopRepository;

    public TripService(TripRepository tripRepository, TripStopRepository stopRepository) {
        this.tripRepository = tripRepository;
        this.stopRepository = stopRepository;
    }

    @Transactional(readOnly = true)
    public List<TripResponse> findAll() {
        return tripRepository.findByCoupleIdOrderByStartDateDescCreatedAtDesc(AppConstants.COUPLE_ID)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TripResponse findById(UUID id) {
        return toResponse(requireTrip(id));
    }

    public TripResponse create(CreateTripRequest request) {
        Trip trip = new Trip();
        trip.setCoupleId(AppConstants.COUPLE_ID);
        trip.setCreatedBy(AppConstants.USER_ID);
        applyTripFields(trip, request.name(), request.startDate(), request.endDate(),
                request.status(), request.budget(), request.currency(), request.notes());
        tripRepository.save(trip);
        return toResponse(trip);
    }

    public TripResponse update(UUID id, UpdateTripRequest request) {
        Trip trip = requireTrip(id);
        applyTripFields(trip, request.name(), request.startDate(), request.endDate(),
                request.status(), request.budget(), request.currency(), request.notes());
        tripRepository.save(trip);
        return toResponse(trip);
    }

    public void delete(UUID id) {
        tripRepository.delete(requireTrip(id));
    }

    public TripStopResponse addStop(UUID tripId, CreateTripStopRequest request) {
        Trip trip = requireTrip(tripId);
        int nextPosition = stopRepository.findMaxPositionByTripIdAndDayNumber(tripId, request.dayNumber()) + 1;
        TripStop stop = new TripStop();
        stop.setTrip(trip);
        stop.setDayNumber(request.dayNumber());
        stop.setPosition(nextPosition);
        applyStopFields(stop, request.time(), request.placeName(), request.placeType(),
                request.address(), request.notes(), request.cost(), request.currency());
        stopRepository.save(stop);
        return toStopResponse(stop);
    }

    public TripStopResponse updateStop(UUID tripId, UUID stopId, UpdateTripStopRequest request) {
        requireTrip(tripId);
        TripStop stop = stopRepository.findByIdAndTripId(stopId, tripId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Stop not found"));
        applyStopFields(stop, request.time(), request.placeName(), request.placeType(),
                request.address(), request.notes(), request.cost(), request.currency());
        stopRepository.save(stop);
        return toStopResponse(stop);
    }

    public void deleteStop(UUID tripId, UUID stopId) {
        requireTrip(tripId);
        TripStop stop = stopRepository.findByIdAndTripId(stopId, tripId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Stop not found"));
        stopRepository.delete(stop);
    }

    public void deleteDay(UUID tripId, int dayNumber) {
        requireTrip(tripId);
        stopRepository.deleteByTripIdAndDayNumber(tripId, dayNumber);
    }

    public void reorderStops(UUID tripId, ReorderStopsRequest request) {
        requireTrip(tripId);
        List<UUID> stopIds = request.stopIds();
        for (int i = 0; i < stopIds.size(); i++) {
            UUID stopId = stopIds.get(i);
            final int pos = i;
            stopRepository.findByIdAndTripId(stopId, tripId).ifPresent(stop -> {
                stop.setPosition(pos);
                stopRepository.save(stop);
            });
        }
    }

    private Trip requireTrip(UUID tripId) {
        return tripRepository.findByIdAndCoupleId(tripId, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Trip not found"));
    }

    private void applyTripFields(Trip trip, String name, LocalDate startDate, LocalDate endDate,
                                  String status, BigDecimal budget, String currency, String notes) {
        trip.setName(name.trim());
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        if (status != null) trip.setStatus(status);
        trip.setBudget(budget);
        if (currency != null) trip.setCurrency(currency);
        trip.setNotes(notes);
    }

    private void applyStopFields(TripStop stop, String time, String placeName, String placeType,
                                  String address, String notes, BigDecimal cost, String currency) {
        stop.setTime(time);
        stop.setPlaceName(placeName.trim());
        stop.setPlaceType(placeType);
        stop.setAddress(address);
        stop.setNotes(notes);
        stop.setCost(cost);
        if (currency != null) stop.setCurrency(currency);
    }

    private TripResponse toResponse(Trip trip) {
        List<TripStopResponse> stops = trip.getStops().stream().map(this::toStopResponse).toList();
        return new TripResponse(trip.getId(), trip.getCoupleId(), trip.getCreatedBy(),
                trip.getName(), trip.getStartDate(), trip.getEndDate(),
                trip.getStatus(), trip.getNotes(), trip.getBudget(), trip.getCurrency(),
                stops, trip.getCreatedAt(), trip.getUpdatedAt());
    }

    private TripStopResponse toStopResponse(TripStop stop) {
        return new TripStopResponse(stop.getId(), stop.getTrip().getId(),
                stop.getDayNumber(), stop.getTime(), stop.getPlaceName(), stop.getPlaceType(),
                stop.getAddress(), stop.getNotes(), stop.getCost(), stop.getCurrency(),
                stop.getPosition(), stop.getCreatedAt());
    }
}
