-- Apply 엔티티에 낙관적 락(Optimistic Locking)을 위한 version 컬럼 추가
ALTER TABLE apply ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
