package com.pierre.planner.trip;

import com.pierre.planner.activity.ActivityData;
import com.pierre.planner.activity.ActivityRequestPayload;
import com.pierre.planner.activity.ActivityResponse;
import com.pierre.planner.exception.ValidationException;
import com.pierre.planner.participant.ParticipantCreateResponse;
import com.pierre.planner.participant.ParticipantData;
import com.pierre.planner.participant.ParticipantRequestPayload;
import com.pierre.planner.link.LinkData;
import com.pierre.planner.link.LinkRequestPayload;
import com.pierre.planner.link.LinkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
        Trip newTrip = tripService.createTrip(payload);
        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        return tripService.getTripDetails(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        try {
            Trip updatedTrip = tripService.updateTrip(id, payload);
            return ResponseEntity.ok(updatedTrip);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> getConfirmTrip(@PathVariable UUID id) {
        try {
            Trip confirmedTrip = tripService.confirmTrip(id);
            return ResponseEntity.ok(confirmedTrip);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id) {
        List<ActivityData> activityList = tripService.getAllActivities(id);
        return ResponseEntity.ok(activityList);
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload) {
        try {
            ActivityResponse activityResponse = tripService.registerActivity(id, payload);
            return ResponseEntity.ok(activityResponse);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id) {
        List<ParticipantData> participantList = tripService.getAllParticipants(id);
        return ResponseEntity.ok(participantList);
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipants(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload) {
        try {
            ParticipantCreateResponse participantResponse = tripService.inviteParticipants(id, payload);
            return ResponseEntity.ok(participantResponse);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id) {
        List<LinkData> linkList = tripService.getAllLinks(id);
        return ResponseEntity.ok(linkList);
    }

    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payload) {
        try {
            LinkResponse linkResponse = tripService.registerLink(id, payload);
            return ResponseEntity.ok(linkResponse);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
