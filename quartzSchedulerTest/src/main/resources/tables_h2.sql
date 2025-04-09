-- ============================================================================
-- Quartz 2.3.2 H2 Database Schema DDL
-- Quartz Scheduler가 사용하는 내부 테이블 정의
-- ============================================================================

/**
 * Job 상세 정보를 저장하는 테이블.
 * 각 Job은 이름과 그룹으로 식별되며, 실행 클래스명과 추가 설정을 포함함.
 */
CREATE TABLE IF NOT EXISTS QRTZ_JOB_DETAILS (
                                                SCHED_NAME VARCHAR(120) NOT NULL,        -- 스케줄러 인스턴스 이름
    JOB_NAME VARCHAR(200) NOT NULL,          -- Job 이름
    JOB_GROUP VARCHAR(200) NOT NULL,         -- Job 그룹 이름
    DESCRIPTION VARCHAR(250),                -- 설명 (선택)
    JOB_CLASS_NAME VARCHAR(250) NOT NULL,    -- 실행할 Job 클래스의 FQCN
    IS_DURABLE BOOLEAN NOT NULL,             -- Trigger 없이도 유지 여부
    IS_NONCONCURRENT BOOLEAN NOT NULL,       -- 동시 실행 금지 여부
    IS_UPDATE_DATA BOOLEAN NOT NULL,         -- JobDataMap 갱신 여부
    REQUESTS_RECOVERY BOOLEAN NOT NULL,      -- 복구 요청 플래그
    JOB_DATA BLOB,                           -- 직렬화된 JobDataMap
    PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
    );

-- ----------------------------------------------------------------------------

/**
 * 등록된 Trigger의 공통 정보 저장 테이블.
 * Cron, Simple, Calendar 등 다양한 트리거 타입의 공통 속성을 관리.
 */
CREATE TABLE IF NOT EXISTS QRTZ_TRIGGERS (
                                             SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250),
    NEXT_FIRE_TIME BIGINT,                  -- 다음 실행 시간 (timestamp)
    PREV_FIRE_TIME BIGINT,                  -- 마지막 실행 시간
    PRIORITY INTEGER,                       -- 우선순위
    TRIGGER_STATE VARCHAR(16) NOT NULL,     -- 상태 (WAITING, PAUSED 등)
    TRIGGER_TYPE VARCHAR(8) NOT NULL,       -- SIMPLE, CRON 등
    START_TIME BIGINT NOT NULL,
    END_TIME BIGINT,
    CALENDAR_NAME VARCHAR(200),
    MISFIRE_INSTR SMALLINT,                 -- Misfire 처리 방식
    JOB_DATA BLOB,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
    REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME, JOB_NAME, JOB_GROUP)
    );

-- ----------------------------------------------------------------------------

/**
 * SimpleTrigger의 반복 설정을 저장하는 테이블.
 */
CREATE TABLE IF NOT EXISTS QRTZ_SIMPLE_TRIGGERS (
                                                    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT NOT NULL,           -- 반복 횟수
    REPEAT_INTERVAL BIGINT NOT NULL,        -- 반복 간격 (ms)
    TIMES_TRIGGERED BIGINT NOT NULL,        -- 실행된 횟수
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    );

-- ----------------------------------------------------------------------------

/**
 * CronTrigger의 cron 표현식을 저장하는 테이블.
 */
CREATE TABLE IF NOT EXISTS QRTZ_CRON_TRIGGERS (
                                                  SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    );

-- ----------------------------------------------------------------------------

/**
 * Quartz 2.2+에서 도입된 Properties 기반 트리거 저장용 테이블.
 */
CREATE TABLE IF NOT EXISTS QRTZ_SIMPROP_TRIGGERS (
                                                     SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512),
    STR_PROP_2 VARCHAR(512),
    STR_PROP_3 VARCHAR(512),
    INT_PROP_1 INTEGER,
    INT_PROP_2 INTEGER,
    LONG_PROP_1 BIGINT,
    LONG_PROP_2 BIGINT,
    DEC_PROP_1 NUMERIC(13,4),
    DEC_PROP_2 NUMERIC(13,4),
    BOOL_PROP_1 BOOLEAN,
    BOOL_PROP_2 BOOLEAN,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    );

-- ----------------------------------------------------------------------------

/**
 * 사용자 정의 트리거나 확장 트리거 등 Blob 타입 트리거를 위한 저장소.
 */
CREATE TABLE IF NOT EXISTS QRTZ_BLOB_TRIGGERS (
                                                  SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BLOB,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    );

-- ----------------------------------------------------------------------------

/**
 * Quartz Calendar를 저장하는 테이블.
 * 특정 날짜/시간 제외 조건 설정에 사용됨.
 */
CREATE TABLE IF NOT EXISTS QRTZ_CALENDARS (
                                              SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME VARCHAR(200) NOT NULL,
    CALENDAR BLOB NOT NULL,
    PRIMARY KEY (SCHED_NAME, CALENDAR_NAME)
    );

-- ----------------------------------------------------------------------------

/**
 * 일시 중지된 트리거 그룹 목록을 저장.
 */
CREATE TABLE IF NOT EXISTS QRTZ_PAUSED_TRIGGER_GRPS (
                                                        SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
    );

-- ----------------------------------------------------------------------------

/**
 * 현재 실행 중이거나 최근 실행된 트리거를 기록하는 테이블.
 * 클러스터 환경에서 상태 동기화에 사용됨.
 */
CREATE TABLE IF NOT EXISTS QRTZ_FIRED_TRIGGERS (
                                                   SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,    -- 실행 중인 인스턴스명
    FIRED_TIME BIGINT NOT NULL,
    SCHED_TIME BIGINT NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200),
    JOB_GROUP VARCHAR(200),
    IS_NONCONCURRENT BOOLEAN,
    REQUESTS_RECOVERY BOOLEAN,
    PRIMARY KEY (SCHED_NAME, ENTRY_ID)
    );

-- ----------------------------------------------------------------------------

/**
 * 스케줄러 인스턴스 상태를 기록.
 * 클러스터 환경에서 각 인스턴스의 heartbeat 체크에 사용됨.
 */
CREATE TABLE IF NOT EXISTS QRTZ_SCHEDULER_STATE (
                                                    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT NOT NULL,
    CHECKIN_INTERVAL BIGINT NOT NULL,
    PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
    );

-- ----------------------------------------------------------------------------

/**
 * 클러스터 락 테이블. 트랜잭션 동기화, 동시성 제어에 사용.
 */
CREATE TABLE IF NOT EXISTS QRTZ_LOCKS (
                                          SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY (SCHED_NAME, LOCK_NAME)
    );

-- ----------------------------------------------------------------------------

-- 인덱스 정의 (성능 최적화용)
CREATE INDEX IF NOT EXISTS IDX_QRTZ_T_J ON QRTZ_TRIGGERS(JOB_NAME, JOB_GROUP);
CREATE INDEX IF NOT EXISTS IDX_QRTZ_T_N_G ON QRTZ_TRIGGERS(TRIGGER_NAME, TRIGGER_GROUP);
CREATE INDEX IF NOT EXISTS IDX_QRTZ_T_C ON QRTZ_TRIGGERS(CALENDAR_NAME);
CREATE INDEX IF NOT EXISTS IDX_QRTZ_T_G ON QRTZ_TRIGGERS(TRIGGER_GROUP);
CREATE INDEX IF NOT EXISTS IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS(INSTANCE_NAME);
CREATE INDEX IF NOT EXISTS IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS(INSTANCE_NAME, REQUESTS_RECOVERY);
CREATE INDEX IF NOT EXISTS IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS(JOB_NAME, JOB_GROUP);
CREATE INDEX IF NOT EXISTS IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS(TRIGGER_NAME, TRIGGER_GROUP);
