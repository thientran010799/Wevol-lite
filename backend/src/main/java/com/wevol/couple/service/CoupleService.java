package com.wevol.couple.service;

import com.wevol.config.AppConstants;
import com.wevol.couple.dto.CoupleResponse;
import com.wevol.couple.dto.UpdateCoupleRequest;
import com.wevol.couple.entity.Couple;
import com.wevol.couple.repository.CoupleRepository;
import org.springframework.stereotype.Service;

@Service
public class CoupleService {

    private final CoupleRepository repo;

    public CoupleService(CoupleRepository repo) {
        this.repo = repo;
    }

    public CoupleResponse get() {
        Couple c = repo.findById(AppConstants.COUPLE_ID)
                .orElseThrow(() -> new IllegalStateException("Couple row missing"));
        return new CoupleResponse(c.getName(), c.getStartDate(), c.getAnniversary());
    }

    public CoupleResponse update(UpdateCoupleRequest req) {
        Couple c = repo.findById(AppConstants.COUPLE_ID)
                .orElseThrow(() -> new IllegalStateException("Couple row missing"));
        if (req.name() != null && !req.name().isBlank()) c.setName(req.name());
        c.setStartDate(req.startDate());
        c.setAnniversary(req.anniversary());
        repo.save(c);
        return new CoupleResponse(c.getName(), c.getStartDate(), c.getAnniversary());
    }
}
