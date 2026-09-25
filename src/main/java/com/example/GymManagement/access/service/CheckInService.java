package com.example.GymManagement.access.service;

import com.example.GymManagement.access.dto.CheckInRecordResponseDto;
import com.example.GymManagement.access.dto.CheckInResultDto;
import com.example.GymManagement.access.dto.InsideNowResponseDto;
import com.example.GymManagement.access.entity.CheckInRecord;
import com.example.GymManagement.access.entity.EntryType;
import com.example.GymManagement.access.repository.CheckInRecordRepository;
import com.example.GymManagement.booking.dto.BookingResponseDto;
import com.example.GymManagement.booking.entity.Booking;
import com.example.GymManagement.booking.service.BookingService;
import com.example.GymManagement.config.ResourceNotFoundException;
import com.example.GymManagement.user.entity.Role;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CheckInService {

    /**
     * Physical floor capacity of the SLIIT gym: maximum 20 people inside at once.
     * Up to 10 people can pre-book a slot in advance, leaving the remaining capacity for walk-ins.
     */
    public static final int DEFAULT_WALK_IN_CAPACITY = 20;
    public static final int MAX_GYM_CAPACITY = 20;
    public static final int MAX_PRE_BOOKED_SLOTS = 10;

    private final CheckInRecordRepository checkInRecordRepository;
    private final UserRepository userRepository;
    private final MedicalClearanceService medicalClearanceService;
    private final GymAccessCodeService gymAccessCodeService;
    private final BookingService bookingService;
    private final EntityManager entityManager;
    private final Map<Long, BookingResponseDto> bookingMetadataCache = new ConcurrentHashMap<>();

    public CheckInService(
            CheckInRecordRepository checkInRecordRepository,
            UserRepository userRepository,
            MedicalClearanceService medicalClearanceService,
            GymAccessCodeService gymAccessCodeService,
            BookingService bookingService,
            EntityManager entityManager
    ) {
        this.checkInRecordRepository = checkInRecordRepository;
        this.userRepository = userRepository;
        this.medicalClearanceService = medicalClearanceService;
        this.gymAccessCodeService = gymAccessCodeService;
        this.bookingService = bookingService;
        this.entityManager = entityManager;
    }

    @Transactional
    public CheckInResultDto scanQr(Long userId, String scannedCode) {
        // 1. Check the scanned QR code matches the active gym door code
        if (!gymAccessCodeService.isValidCode(scannedCode)) {
            throw new IllegalArgumentException("Invalid QR code");
        }

        // 2. Process the entry or exit for this user (not a manual staff check-in)
        return processEntryOrExit(userId, null, false);
    }

    @Transactional
    public CheckInResultDto manualCheckInOrOut(Long studentUserId, Long staffUserId) {
        // 1. Load the acting staff/admin member from the database
        User staffUser = userRepository.findById(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found with id: " + staffUserId));

        // 2. Process the entry or exit without a QR code check, recording the staff member
        return processEntryOrExit(studentUserId, staffUser, true);
    }

    @Transactional(readOnly = true)
    public CheckInRecordResponseDto getCurrentStatus(Long userId) {
        // 1. Find the user's open check-in record
        LocalDateTime now = LocalDateTime.now();
        CheckInRecord openRecord = checkInRecordRepository
                .findFirstByUser_UserIdAndExitTimeIsNullOrderByEntryTimeDesc(userId)
                .filter(record -> !record.isStale(now))
                .orElseThrow(() -> new ResourceNotFoundException("You are not currently checked into the gym."));

        // 2. Map and return the open check-in record DTO
        return toResponseDto(openRecord, now);
    }

    @Transactional(readOnly = true)
    public List<CheckInRecordResponseDto> getMyHistory(Long userId) {
        // 1. Retrieve all check-in records for the logged-in user, newest first
        LocalDateTime now = LocalDateTime.now();
        return checkInRecordRepository.findByUser_UserIdOrderByEntryTimeDesc(userId)
                .stream()
                .map(record -> toResponseDto(record, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public InsideNowResponseDto getInsideNow() {
        // 1. Fetch all open records and filter out any stale records
        LocalDateTime now = LocalDateTime.now();
        List<CheckInRecord> activeOpenRecords = checkInRecordRepository.findByExitTimeIsNullOrderByEntryTimeAsc()
                .stream()
                .filter(record -> !record.isStale(now))
                .toList();

        // 2. Convert each active record into a CheckInRecordResponseDto
        List<CheckInRecordResponseDto> recordDtos = new ArrayList<>();
        for (CheckInRecord record : activeOpenRecords) {
            recordDtos.add(toResponseDto(record, now));
        }

        // 3. Query BookingService for current slot capacity (capped at the 20-person gym maximum) and reserved places
        Optional<Integer> slotCapacityOpt = bookingService.getCapacityAt(now);
        Integer capacityNow = slotCapacityOpt
                .map(cap -> Math.min(cap, MAX_GYM_CAPACITY))
                .orElse(MAX_GYM_CAPACITY);
        int reservedNotArrived = Math.min(
                bookingService.getReservedPlacesNotYetArrived(now),
                MAX_PRE_BOOKED_SLOTS
        );

        // 4. Build and return the InsideNowResponseDto summary
        InsideNowResponseDto response = new InsideNowResponseDto();
        response.setPeopleInside(recordDtos.size());
        response.setCapacityNow(capacityNow);
        response.setReservedNotArrived(reservedNotArrived);
        response.setRecords(recordDtos);
        return response;
    }

    @Transactional
    public CheckInRecordResponseDto forceCheckout(Long recordId) {
        // 1. Load the target check-in record or throw 404
        LocalDateTime now = LocalDateTime.now();
        CheckInRecord record = checkInRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in record not found with id: " + recordId));

        // 2. Close the record using its domain close(now) method
        record.close(now);
        CheckInRecord saved = checkInRecordRepository.save(record);

        // 3. Return the updated DTO
        return toResponseDto(saved, now);
    }

    @Transactional
    public int autoCloseStaleRecords() {
        // 1. Find all open records where isStale(now) is true and auto-close them
        LocalDateTime now = LocalDateTime.now();
        List<CheckInRecord> openRecords = checkInRecordRepository.findByExitTimeIsNullOrderByEntryTimeAsc();
        int closedCount = 0;
        for (CheckInRecord record : openRecords) {
            if (record.isStale(now)) {
                record.autoClose(now);
                checkInRecordRepository.save(record);
                closedCount++;
            }
        }
        return closedCount;
    }

    private CheckInResultDto processEntryOrExit(Long userId, User staffUser, boolean isManual) {
        // 1. Load the user fresh from the database and verify the account is active
        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (!user.isActive()) {
            throw new AccessDeniedException("User account is inactive.");
        }

        // 2. Check if the user already has an open check-in record
        Optional<CheckInRecord> existingOpenOpt = checkInRecordRepository
                .findFirstByUser_UserIdAndExitTimeIsNullOrderByEntryTimeDesc(userId);

        if (existingOpenOpt.isPresent()) {
            CheckInRecord existingOpen = existingOpenOpt.get();
            if (existingOpen.isStale(now)) {
                // 2a. If the open record is stale (>4 hours or before today), auto-close it and proceed as a new entry
                existingOpen.autoClose(now);
                checkInRecordRepository.saveAndFlush(existingOpen);
            } else {
                // 2b. If the open record is not stale, this scan is an EXIT
                existingOpen.close(now);
                CheckInRecord closedRecord = checkInRecordRepository.save(existingOpen);
                return buildResult("EXITED", "Goodbye! You have been checked out.", toResponseDto(closedRecord, now));
            }
        }

        // 3. Verify the student has a valid, unexpired medical clearance
        if (!medicalClearanceService.hasValidClearance(userId)) {
            throw new AccessDeniedException("A valid medical clearance is required");
        }

        // 4. Verify current time falls within the role's allowed access window (inclusive)
        Role role = user.getRole();
        if (role != null && role.getAccessStartTime() != null && role.getAccessEndTime() != null) {
            LocalTime currentTime = now.toLocalTime();
            if (currentTime.isBefore(role.getAccessStartTime()) || currentTime.isAfter(role.getAccessEndTime())) {
                throw new AccessDeniedException("Outside your access hours");
            }
        }

        // 5. Count non-stale people currently inside and enforce the hard 20-person gym floor limit
        int inside = countNonStalePeopleInside(now);
        if (inside >= MAX_GYM_CAPACITY) {
            // When 20 people are already inside and the 21st person scans, reject with a clear sorry message
            throw new IllegalArgumentException(
                    "Sorry, the gym has reached its maximum capacity of 20 people right now. Please try again later or book a slot."
            );
        }

        // 6. Check if the student has a booking covering the current time
        Optional<BookingResponseDto> bookingOpt = bookingService.findBookingCoveringNow(userId, now);
        if (bookingOpt.isPresent()) {
            BookingResponseDto bookingDto = bookingOpt.get();
            bookingMetadataCache.put(bookingDto.getBookingId(), bookingDto);

            // 6a. Create the check-in record linked to the booking
            CheckInRecord bookedRecord = new CheckInRecord();
            bookedRecord.setUser(user);
            bookedRecord.setBooking(entityManager.getReference(Booking.class, bookingDto.getBookingId()));
            bookedRecord.setEntryTime(now);
            bookedRecord.setEntryType(isManual ? EntryType.MANUAL : EntryType.BOOKED);
            bookedRecord.setCheckedInBy(staffUser);

            CheckInRecord savedBooked = checkInRecordRepository.save(bookedRecord);

            // 6b. If the booking status is CONFIRMED, mark it as ATTENDED in Member D's BookingService
            if ("CONFIRMED".equalsIgnoreCase(String.valueOf(bookingDto.getStatus()))) {
                bookingService.markAttended(bookingDto.getBookingId());
            }

            String welcomeMsg = "Welcome, booking for " + bookingDto.getSessionTitle();
            return buildResult("ENTERED", welcomeMsg, toResponseDto(savedBooked, now));
        }

        // 7. Walk-in capacity check (no booking found): protect reserved places (up to 10 pre-booked slots) within the 20-person gym limit
        Optional<Integer> slotCapacityOpt = bookingService.getCapacityAt(now);
        if (slotCapacityOpt.isPresent()) {
            int effectiveCapacity = Math.min(slotCapacityOpt.get(), MAX_GYM_CAPACITY);
            int reservedNotArrived = Math.min(
                    bookingService.getReservedPlacesNotYetArrived(now),
                    MAX_PRE_BOOKED_SLOTS
            );
            if (inside + reservedNotArrived >= effectiveCapacity) {
                throw new IllegalArgumentException("Sorry, the gym is full right now. Please book a slot.");
            }
        } else {
            if (inside >= DEFAULT_WALK_IN_CAPACITY) {
                throw new IllegalArgumentException("Sorry, the gym is full right now. Please book a slot.");
            }
        }

        // 8. Save the walk-in (or manual) check-in record
        CheckInRecord walkInRecord = new CheckInRecord();
        walkInRecord.setUser(user);
        walkInRecord.setBooking(null);
        walkInRecord.setEntryTime(now);
        walkInRecord.setEntryType(isManual ? EntryType.MANUAL : EntryType.WALK_IN);
        walkInRecord.setCheckedInBy(staffUser);

        CheckInRecord savedWalkIn = checkInRecordRepository.save(walkInRecord);
        return buildResult("ENTERED", "Welcome to the gym!", toResponseDto(savedWalkIn, now));
    }

    private int countNonStalePeopleInside(LocalDateTime now) {
        // 1. Count all open records that are not stale
        return (int) checkInRecordRepository.findByExitTimeIsNullOrderByEntryTimeAsc()
                .stream()
                .filter(record -> !record.isStale(now))
                .count();
    }

    private CheckInResultDto buildResult(String outcome, String message, CheckInRecordResponseDto recordDto) {
        // 1. Package the check-in/out outcome, message, and record DTO
        CheckInResultDto result = new CheckInResultDto();
        result.setOutcome(outcome);
        result.setMessage(message);
        result.setRecord(recordDto);
        return result;
    }

    private CheckInRecordResponseDto toResponseDto(CheckInRecord record, LocalDateTime now) {
        // 1. Populate basic user and timestamp details
        CheckInRecordResponseDto dto = new CheckInRecordResponseDto();
        dto.setRecordId(record.getRecordId());
        dto.setUserId(record.getUser().getUserId());
        dto.setUserName(record.getUser().getName());
        dto.setUniversityId(record.getUser().getUniversityId());
        dto.setEntryTime(record.getEntryTime());
        dto.setExitTime(record.getExitTime());
        dto.setEntryType(record.getEntryType());
        dto.setAutoClosed(record.isAutoClosed());
        dto.setDurationMinutes(record.getDurationMinutes());
        dto.setInsideNow(record.isInside());
        dto.setCheckedInByName(record.getCheckedInBy() != null ? record.getCheckedInBy().getName() : null);

        // 2. If linked to a booking, populate bookingId, sessionTitle, and calculate overstayed status
        if (record.getBooking() != null) {
            Long bookingId = record.getBooking().getBookingId();
            dto.setBookingId(bookingId);

            BookingResponseDto bookingMeta = resolveBookingMetadata(
                    bookingId,
                    record.getUser().getUserId(),
                    record.getEntryTime()
            );
            if (bookingMeta != null) {
                dto.setSessionTitle(bookingMeta.getSessionTitle());
                if (bookingMeta.getEndTime() != null) {
                    LocalDateTime effectiveEndCheck = record.getExitTime() != null ? record.getExitTime() : now;
                    dto.setOverstayed(effectiveEndCheck.isAfter(bookingMeta.getEndTime().plusMinutes(10)));
                }
            }
        } else {
            dto.setOverstayed(false);
        }

        return dto;
    }

    private BookingResponseDto resolveBookingMetadata(Long bookingId, Long userId, LocalDateTime entryTime) {
        // 1. Check in-memory cache first
        BookingResponseDto cached = bookingMetadataCache.get(bookingId);
        if (cached != null) {
            return cached;
        }
        // 2. Query BookingService using the record's entryTime if not yet cached
        Optional<BookingResponseDto> lookedUp = bookingService.findBookingCoveringNow(userId, entryTime);
        if (lookedUp.isPresent() && bookingId.equals(lookedUp.get().getBookingId())) {
            bookingMetadataCache.put(bookingId, lookedUp.get());
            return lookedUp.get();
        }
        return null;
    }
}