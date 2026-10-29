package com.overload.service;

import com.overload.domain.*;
import com.overload.dto.Dtos.*;
import com.overload.exception.ResourceNotFoundException;
import com.overload.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WorkoutService {
    private final WorkoutSessionRepository sessions;
    private final WorkoutSetRepository sets;
    private final ExerciseVariantRepository variants;
    private final SessionFeedbackRepository feedback;
    private final UserRepository users;
    private final RegressionService regression;
    private final UUID userId;

    public WorkoutService(WorkoutSessionRepository sessions, WorkoutSetRepository sets, ExerciseVariantRepository variants,
                          SessionFeedbackRepository feedback, UserRepository users, RegressionService regression,
                          @Value("${app.default-user-id}") UUID userId) {
        this.sessions = sessions; this.sets = sets; this.variants = variants;
        this.feedback = feedback; this.users = users; this.regression = regression; this.userId = userId;
    }

    @Transactional
    public SessionResponse createSession(CreateSessionRequest req) {
        WorkoutSession s = new WorkoutSession();
        s.setUser(users.getReferenceById(userId));
        s.setSessionDate(req.sessionDate());
        s.setSplitType(req.splitType());
        s.setNotes(req.notes());
        return toResponse(sessions.save(s));
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(UUID id) { return toResponse(find(id)); }

    @Transactional(readOnly = true)
    public List<SessionResponse> listSessions() {
        return sessions.findByUserIdAndDeletedAtIsNullOrderBySessionDateDesc(userId, PageRequest.of(0, 30))
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deleteSession(UUID id) { find(id).setDeletedAt(LocalDateTime.now()); }

    @Transactional
    public LogSetResponse logSet(UUID sessionId, LogSetRequest req) {
        WorkoutSession session = find(sessionId);
        ExerciseVariant variant = variants.findById(req.variantId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise variant " + req.variantId() + " not found"));
        double added = req.addedWeight() == null ? 0 : req.addedWeight();
        double load = StrengthMath.setLoad(req.weight(), req.bodyweight(), added);
        boolean working = req.setType() != SetType.WARMUP;

        // Compare against history BEFORE saving the new set. Warm-ups never count.
        Double priorE1rm = null, priorHeaviest = null; Integer priorReps = null;
        SetDelta delta = new SetDelta(false, 0, 0, 0);
        if (working) {
            priorE1rm = sets.bestEstimatedOneRepMax(userId, variant.getId());
            priorHeaviest = sets.heaviestLoad(userId, variant.getId());
            priorReps = sets.bestRepsAtLoad(userId, variant.getId(), load);
            List<WorkoutSet> prev = sets.previousWorkingSets(userId, variant.getId(), sessionId, PageRequest.of(0, 1));
            if (!prev.isEmpty()) {
                WorkoutSet p = prev.get(0);
                double prevVol = StrengthMath.volume(p.effectiveLoad(), p.getReps());
                delta = new SetDelta(true, StrengthMath.round2(load - p.effectiveLoad()), req.reps() - p.getReps(),
                        StrengthMath.percentChange(prevVol, StrengthMath.volume(load, req.reps())));
            }
        }

        WorkoutSet set = new WorkoutSet();
        set.setSession(session); set.setVariant(variant);
        set.setSetNumber((int) sets.countBySessionIdAndVariantId(sessionId, variant.getId()) + 1);
        set.setWeight(req.weight()); set.setReps(req.reps()); set.setSetType(req.setType());
        set.setBodyweight(req.bodyweight()); set.setAddedWeight(added);
        sets.save(set);

        double e1rm = StrengthMath.estimateOneRepMax(load, req.reps());
        List<String> achievements = new ArrayList<>();
        if (working && priorE1rm != null) { // a first-ever lift sets a baseline, not a PR
            if (e1rm > priorE1rm + 0.005) achievements.add("ESTIMATED_1RM_PR");
            if (priorHeaviest != null && load > priorHeaviest) achievements.add("WEIGHT_PR");
            if (priorReps != null && req.reps() > priorReps) achievements.add("REP_PR");
        }
        return new LogSetResponse(toDto(set), delta, achievements);
    }

    @Transactional
    public SessionResponse saveFeedback(UUID sessionId, RegressionReason reason) {
        WorkoutSession s = find(sessionId);
        feedback.save(new SessionFeedback(s.getId(), reason));
        return toResponse(s);
    }

    @Transactional(readOnly = true)
    public RegressionCheck checkRegression(UUID sessionId, Long variantId) {
        WorkoutSession s = find(sessionId);
        return regression.evaluate(userId, s.getId(), s.getSessionDate(), variantId);
    }

    private WorkoutSession find(UUID id) {
        return sessions.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout session " + id + " not found"));
    }

    private SessionResponse toResponse(WorkoutSession s) {
        List<WorkoutSet> list = sets.findBySessionIdOrderByVariantIdAscSetNumberAsc(s.getId());
        RegressionReason reason = feedback.findById(s.getId()).map(SessionFeedback::getRegressionReason).orElse(null);
        return new SessionResponse(s.getId(), s.getSessionDate(), s.getSplitType(), s.getNotes(),
                list.stream().map(this::toDto).toList(), StrengthMath.round2(StrengthMath.volumeLoad(list)), reason);
    }

    private SetDto toDto(WorkoutSet s) {
        return new SetDto(s.getId(), s.getVariant().getId(), s.getVariant().getVariantName(), s.getSetNumber(),
                s.getWeight(), s.getReps(), s.getSetType(), s.isBodyweight(), s.getAddedWeight(),
                StrengthMath.estimateOneRepMax(s.effectiveLoad(), s.getReps()));
    }
}
