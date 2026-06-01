package com.wevol.couple.repository;

import com.wevol.couple.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CoupleRepository extends JpaRepository<Couple, UUID> {}
