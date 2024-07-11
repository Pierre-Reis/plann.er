package com.pierre.planner.trip;

import com.pierre.planner.activity.ActivityData;
import com.pierre.planner.activity.ActivityRequestPayload;
import com.pierre.planner.activity.ActivityResponse;
import com.pierre.planner.activity.ActivityService;
import com.pierre.planner.exception.ValidationException;
import com.pierre.planner.link.LinkData;
import com.pierre.planner.link.LinkRequestPayload;
import com.pierre.planner.link.LinkResponse;
import com.pierre.planner.link.LinkService;
import com.pierre.planner.participant.ParticipantCreateResponse;
import com.pierre.planner.participant.ParticipantData;
import com.pierre.planner.participant.ParticipantRequestPayload;
import com.pierre.planner.participant.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LinkService linkService;

    public Trip createTrip(TripRequestPayload payload) {
        Trip newTrip = new Trip(payload);

        var currentDate = LocalDateTime.now();

        if (newTrip.getStartsAt().isAfter(newTrip.getEndsAt())) {
            throw new ValidationException("A data de início da viagem deve ser anterior à data de chegada");
        }

        if (newTrip.getStartsAt().isBefore(currentDate)) {
            throw new ValidationException("A data de início da viagem deve ser posterior à data atual");
        }

        this.tripRepository.save(newTrip);
        this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

        return newTrip;
    }

    public Optional<Trip> getTripDetails(UUID id) {
        return this.tripRepository.findById(id);
    }

    public Trip updateTrip(UUID id, TripRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.tripRepository.save(rawTrip);

            return rawTrip;
        }

        throw new ValidationException("Trip not found");
    }

    public Trip confirmTrip(UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.tripRepository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);

            return rawTrip;
        }

        throw new ValidationException("Trip not found");
    }

    public List<ActivityData> getAllActivities(UUID id) {
        return this.activityService.getAllActivitiesFromTrip(id);
    }

    public ActivityResponse registerActivity(UUID id, ActivityRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            var activityOccurs = LocalDateTime.parse(payload.occurs_at(), DateTimeFormatter.ISO_DATE_TIME);

            if (activityOccurs.isBefore(rawTrip.getStartsAt()) || activityOccurs.isAfter(rawTrip.getEndsAt())) {
                throw new ValidationException("A data da atividade deve estar entre as datas de início e fim da viagem");
            }

            return this.activityService.registerActivity(payload, rawTrip);
        }

        throw new ValidationException("Trip not found");
    }

    public List<ParticipantData> getAllParticipants(UUID id) {
        return this.participantService.getAllParticipantsFromTrip(id);
    }

    public ParticipantCreateResponse inviteParticipants(UUID id, ParticipantRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ParticipantCreateResponse participantResponse = this.participantService.registerParticipantToEvent(payload.email(), rawTrip);

            if (rawTrip.getIsConfirmed()) {
                this.participantService.triggerConfirmationEmailToParticipant(payload.email());
            }

            return participantResponse;
        }

        throw new ValidationException("Trip not found");
    }

    public List<LinkData> getAllLinks(UUID id) {
        return this.linkService.getAllLinksFromTrip(id);
    }

    public LinkResponse registerLink(UUID id, LinkRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();
            return this.linkService.registerLink(payload, rawTrip);
        }

        throw new ValidationException("Trip not found");
    }
}