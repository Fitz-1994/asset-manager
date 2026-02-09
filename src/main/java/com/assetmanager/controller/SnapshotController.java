package com.assetmanager.controller;

import com.assetmanager.dto.ReturnSeriesPoint;
import com.assetmanager.dto.SnapshotResponse;
import com.assetmanager.entity.AssetSnapshot;
import com.assetmanager.entity.User;
import com.assetmanager.mapper.AssetSnapshotMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/snapshots")
public class SnapshotController {

    private final AssetSnapshotMapper snapshotMapper;

    public SnapshotController(AssetSnapshotMapper snapshotMapper) {
        this.snapshotMapper = snapshotMapper;
    }

    @GetMapping
    public List<SnapshotResponse> list(@AuthenticationPrincipal User user,
                                        @RequestParam(required = false) String trigger_type,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from_date,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to_date,
                                        @RequestParam(defaultValue = "500") int limit) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        int limitVal = Math.min(limit, 2000);
        List<AssetSnapshot> list = snapshotMapper.findByUserIdOrderBySnapshotAtDesc(user.getId(), limitVal);
        if (trigger_type != null && !trigger_type.isEmpty())
            list = list.stream().filter(s -> trigger_type.equals(s.getTriggerType())).collect(Collectors.toList());
        if (from_date != null)
            list = list.stream().filter(s -> !s.getSnapshotAt().isBefore(from_date)).collect(Collectors.toList());
        if (to_date != null)
            list = list.stream().filter(s -> !s.getSnapshotAt().isAfter(to_date)).collect(Collectors.toList());
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/series")
    public List<ReturnSeriesPoint> getSeries(@AuthenticationPrincipal User user,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant base_date,
                                              @RequestParam(defaultValue = "365") int days) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Instant to = Instant.now();
        Instant from = to.minus(days, ChronoUnit.DAYS);
        List<AssetSnapshot> list = snapshotMapper.findByUserIdAndSnapshotAtBetweenOrderBySnapshotAtAsc(user.getId(), from, to);
        if (list.isEmpty()) return List.of();

        Instant base = base_date != null ? base_date : list.get(0).getSnapshotAt();
        double baseValue = 0;
        for (AssetSnapshot s : list) {
            if (!s.getSnapshotAt().isAfter(base)) {
                baseValue = s.getTotalValueCny() != null ? s.getTotalValueCny() : 0;
            }
        }
        if (baseValue == 0 && !list.isEmpty()) baseValue = list.get(0).getTotalValueCny() != null ? list.get(0).getTotalValueCny() : 0;

        double finalBaseValue = baseValue;
        List<ReturnSeriesPoint> out = new ArrayList<>();
        for (AssetSnapshot s : list) {
            double v = s.getTotalValueCny() != null ? s.getTotalValueCny() : 0;
            Double pct = finalBaseValue != 0 ? (v - finalBaseValue) / finalBaseValue * 100.0 : null;
            ReturnSeriesPoint p = new ReturnSeriesPoint();
            p.setDate(s.getSnapshotAt().atOffset(ZoneOffset.UTC).toLocalDate().toString());
            p.setTimestamp(s.getSnapshotAt());
            p.setValue(v);
            p.setReturn_pct(pct != null ? Math.round(pct * 10000.0) / 10000.0 : null);
            out.add(p);
        }
        return out;
    }

    @GetMapping("/{snapshotId}")
    public SnapshotResponse getWithDetails(@AuthenticationPrincipal User user, @PathVariable Long snapshotId) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        AssetSnapshot snap = snapshotMapper.findById(snapshotId);
        if (snap == null || !snap.getUserId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Snapshot not found");
        return toResponse(snap);
    }

    private SnapshotResponse toResponse(AssetSnapshot s) {
        SnapshotResponse r = new SnapshotResponse();
        r.setId(s.getId());
        r.setUser_id(s.getUserId());
        r.setSnapshot_at(s.getSnapshotAt());
        r.setTrigger_type(s.getTriggerType());
        r.setTotal_value_cny(s.getTotalValueCny());
        r.setCreated_at(s.getCreatedAt());
        return r;
    }
}
