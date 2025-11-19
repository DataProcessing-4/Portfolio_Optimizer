-- 주식 포트폴리오 분석 시스템 - 수정된 MySQL DDL
-- 교수님 데이터 + 정적 주가 + 선택적 일별 데이터 구조
-- 생성일: 2024-11-12
-- 데이터베이스 생성




-- =================================================
-- 4. 삭제 확인 메시지
-- =================================================

SELECT 'All tables have been dropped successfully!' as status;
SELECT 'Database schema has been reset.' as message;

CREATE DATABASE IF NOT EXISTS portfolio_analysis 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE portfolio_analysis;

-- 1. 주식 종목 통합 정보 테이블 (교수님 데이터 + 정적 주가)
CREATE TABLE stock (
    ticker VARCHAR(10) PRIMARY KEY COMMENT '티커 심볼',
    stock_name VARCHAR(100) NOT NULL COMMENT '종목명',
    industry VARCHAR(50) COMMENT '업종',
    accounting_year INT COMMENT '회계년도',
    settlement_month TINYINT COMMENT '결산월',
    
    -- 교수님 제공 재무 데이터
    total_assets BIGINT COMMENT '총자산',
    total_debt BIGINT COMMENT '총부채',
    total_equity BIGINT COMMENT '총자본',
    revenue BIGINT COMMENT '매출액',
    operating_profit BIGINT COMMENT '영업이익',
    net_income BIGINT COMMENT '당기순이익',
    
    -- 교수님 제공 주당 지표
    eps DECIMAL(10,2) COMMENT '주당순이익(수정_EPS)',
    bps DECIMAL(10,2) COMMENT '주당순자산가치(수정_BPS)',
    sps DECIMAL(10,2) COMMENT '주당매출(수정_SPS)',
    cfps DECIMAL(10,2) COMMENT '주당현금흐름(수정_CFPS)',
    ebitdaps DECIMAL(10,2) COMMENT '주당EBITDA(수정_EBITDAPS)',
    
    -- 계산된 재무비율
    roe DECIMAL(10,4) COMMENT '자기자본이익률 (net_income/total_equity*100)',
    debt_ratio DECIMAL(10,4) COMMENT '부채비율 (total_debt/total_equity*100)',
    
    -- 정적 주가 데이터 (기준일자 기준)
    reference_date DATE COMMENT '기준 일자',
    close_price DECIMAL(10,2) COMMENT '기준일 종가',
    market_cap BIGINT COMMENT '기준일 시가총액',
    per DECIMAL(10,4) COMMENT '주가수익비율 (close_price/eps)',
    pbr DECIMAL(10,4) COMMENT '주가순자산비율 (close_price/bps)',
    
    -- 메타 데이터
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    -- 인덱스
    INDEX idx_stock_name (stock_name),
    INDEX idx_industry (industry),
    INDEX idx_factors (per, pbr, roe),
    INDEX idx_reference_date (reference_date),
    INDEX idx_financial_ratios (roe, debt_ratio)
) ENGINE=InnoDB COMMENT='교수님 제공 재무데이터 + 정적 주가정보 통합 저장';

-- 2. 일별 주가 데이터 테이블 (포트폴리오 최적화 구현 시에만 사용)
CREATE TABLE `stock_price` (
  `ticker` varchar(10) NOT NULL,
  `price_date` date NOT NULL,
  `open_price` decimal(12,2) DEFAULT NULL,
  `high_price` decimal(12,2) DEFAULT NULL,
  `low_price` decimal(12,2) DEFAULT NULL,
  `close_price` decimal(12,2) NOT NULL,
  `volume` bigint DEFAULT NULL,
  `shares_outstanding` bigint DEFAULT NULL,
  `market_cap` bigint DEFAULT NULL,
  `daily_return` decimal(9,6) DEFAULT NULL,
  PRIMARY KEY (`ticker`,`price_date`),
  KEY `idx_stock_price_date` (`price_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. 사용자 세션 관리 테이블
CREATE TABLE user_session (
    session_id VARCHAR(50) PRIMARY KEY COMMENT '세션 ID',
    user_ip VARCHAR(45) COMMENT '사용자 IP (IPv6 지원)',
    user_agent TEXT COMMENT '브라우저 정보',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 접근시간',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성 여부',
    
    INDEX idx_last_accessed (last_accessed),
    INDEX idx_active (is_active)
) ENGINE=InnoDB COMMENT='사용자별 세션 기반 상태 관리';

-- 4. 멀티팩터 스크리닝 결과 테이블
CREATE TABLE multifactor_screening (
    screening_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '스크리닝 ID',
    session_id VARCHAR(50) NOT NULL COMMENT '세션 ID',
    ticker VARCHAR(10) NOT NULL COMMENT '티커 심볼',
    per_score DECIMAL(8,6) COMMENT 'PER 점수',
    pbr_score DECIMAL(8,6) COMMENT 'PBR 점수',
    roe_score DECIMAL(8,6) COMMENT 'ROE 점수',
    per_weight DECIMAL(5,4) DEFAULT 0.3333 COMMENT 'PER 가중치',
    pbr_weight DECIMAL(5,4) DEFAULT 0.3333 COMMENT 'PBR 가중치',
    roe_weight DECIMAL(5,4) DEFAULT 0.3334 COMMENT 'ROE 가중치',
    composite_score DECIMAL(10,6) COMMENT '종합 점수',
    ranking INT COMMENT '순위',
    is_selected BOOLEAN DEFAULT FALSE COMMENT '상위 50개 선별 여부',
    screening_date DATE NOT NULL COMMENT '스크리닝 일자',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    
    INDEX idx_ranking (ranking),
    INDEX idx_session_ranking (session_id, ranking),
    INDEX idx_selected (is_selected, ranking),
    INDEX idx_screening_date (screening_date),
    
    FOREIGN KEY (session_id) REFERENCES user_session(session_id) ON DELETE CASCADE,
    FOREIGN KEY (ticker) REFERENCES stock(ticker) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='PER, PBR, ROE 기반 종목 평가 및 상위 50개 선별';

-- 5. 사용자 선택 자산 테이블
CREATE TABLE user_selected_assets (
    selection_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '선택 ID',
    session_id VARCHAR(50) NOT NULL COMMENT '세션 ID',
    ticker VARCHAR(10) NOT NULL COMMENT '티커 심볼',
    selection_order TINYINT COMMENT '선택 순서',
    selected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '선택 일시',
    
    UNIQUE KEY uk_session_ticker (session_id, ticker),
    INDEX idx_session_order (session_id, selection_order),
    
    FOREIGN KEY (session_id) REFERENCES user_session(session_id) ON DELETE CASCADE,
    FOREIGN KEY (ticker) REFERENCES stock(ticker) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='사용자가 선택한 5-10개 종목 관리';

-- 6. 상관관계 분석 결과 테이블
CREATE TABLE correlation_analysis (
    correlation_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '상관관계 ID',
    session_id VARCHAR(50) NOT NULL COMMENT '세션 ID',
    ticker1 VARCHAR(10) NOT NULL COMMENT '첫번째 티커',
    ticker2 VARCHAR(10) NOT NULL COMMENT '두번째 티커',
    correlation_3m DECIMAL(8,6) COMMENT '3개월 상관계수',
    correlation_6m DECIMAL(8,6) COMMENT '6개월 상관계수',
    correlation_1y DECIMAL(8,6) COMMENT '1년 상관계수',
    analysis_start_date DATE COMMENT '분석 시작일',
    analysis_end_date DATE COMMENT '분석 종료일',
    analysis_date DATE NOT NULL COMMENT '분석 수행일',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    
    UNIQUE KEY uk_session_tickers (session_id, ticker1, ticker2, analysis_date),
    INDEX idx_correlation_1y (correlation_1y),
    INDEX idx_tickers (ticker1, ticker2),
    INDEX idx_analysis_date (analysis_date),
    
    FOREIGN KEY (session_id) REFERENCES user_session(session_id) ON DELETE CASCADE,
    FOREIGN KEY (ticker1) REFERENCES stock(ticker) ON DELETE CASCADE,
    FOREIGN KEY (ticker2) REFERENCES stock(ticker) ON DELETE CASCADE,
    
    CONSTRAINT chk_different_tickers CHECK (ticker1 != ticker2)
) ENGINE=InnoDB COMMENT='종목간 피어슨 상관계수 분석 (다중 기간)';

-- 7. 포트폴리오 설정 테이블
CREATE TABLE portfolio_settings (
    setting_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '설정 ID',
    session_id VARCHAR(50) NOT NULL COMMENT '세션 ID',
    roe_weight DECIMAL(5,4) DEFAULT 0.3334 COMMENT 'ROE 가중치',
    pbr_weight DECIMAL(5,4) DEFAULT 0.3333 COMMENT 'PBR 가중치',
    per_weight DECIMAL(5,4) DEFAULT 0.3333 COMMENT 'PER 가중치',
    top_n_count TINYINT DEFAULT 5 COMMENT '상위 N개 추천수 (1-10)',
    min_correlation_threshold DECIMAL(5,4) DEFAULT 0.7 COMMENT '최소 상관관계 임계값',
    max_debt_ratio DECIMAL(5,4) DEFAULT 2.0 COMMENT '최대 부채비율',
    risk_free_rate DECIMAL(5,4) DEFAULT 0.03 COMMENT '무위험 수익률',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    UNIQUE KEY uk_session_settings (session_id),
    
    FOREIGN KEY (session_id) REFERENCES user_session(session_id) ON DELETE CASCADE,
    
    CONSTRAINT chk_weights_sum CHECK (roe_weight + pbr_weight + per_weight = 1.0),
    CONSTRAINT chk_top_n_range CHECK (top_n_count BETWEEN 1 AND 10)
) ENGINE=InnoDB COMMENT='팩터 가중치 및 포트폴리오 최적화 설정';

-- 8. 효율적 경계 데이터 테이블
CREATE TABLE efficient_frontier (
    frontier_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '경계 ID',
    session_id VARCHAR(50) NOT NULL COMMENT '세션 ID',
    expected_return DECIMAL(8,6) NOT NULL COMMENT '기대수익률',
    risk_std_dev DECIMAL(8,6) NOT NULL COMMENT '위험(표준편차)',
    sharpe_ratio DECIMAL(8,6) COMMENT '샤프 비율',
    portfolio_weights JSON COMMENT '포트폴리오 비중 JSON',
    portfolio_type ENUM('efficient_frontier', 'max_sharpe', 'min_variance') COMMENT '포트폴리오 유형',
    point_order INT COMMENT '경계선상 점 순서',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    
    INDEX idx_session_type (session_id, portfolio_type),
    INDEX idx_sharpe_ratio (sharpe_ratio DESC),
    INDEX idx_risk_return (risk_std_dev, expected_return),
    
    FOREIGN KEY (session_id) REFERENCES user_session(session_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='효율적 경계 그래프를 위한 최적화 결과';

-- 9. 최적 포트폴리오 추천 테이블
CREATE TABLE optimal_portfolio (
    portfolio_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '포트폴리오 ID',
    session_id VARCHAR(50) NOT NULL COMMENT '세션 ID',
    ticker VARCHAR(10) NOT NULL COMMENT '티커 심볼',
    weight DECIMAL(8,6) NOT NULL COMMENT '포트폴리오 내 비중',
    portfolio_type ENUM('max_sharpe', 'min_variance', 'efficient_portfolio') NOT NULL COMMENT '포트폴리오 유형',
    portfolio_rank TINYINT COMMENT '포트폴리오 순위 (1-5)',
    expected_return DECIMAL(8,6) COMMENT '기대수익률',
    portfolio_risk DECIMAL(8,6) COMMENT '포트폴리오 위험도',
    sharpe_ratio DECIMAL(8,6) COMMENT '샤프 비율',
    portfolio_name VARCHAR(100) COMMENT '포트폴리오 명',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    
    INDEX idx_session_type_rank (session_id, portfolio_type, portfolio_rank),
    INDEX idx_sharpe_desc (sharpe_ratio DESC),
    INDEX idx_weight (weight DESC),
    
    FOREIGN KEY (session_id) REFERENCES user_session(session_id) ON DELETE CASCADE,
    FOREIGN KEY (ticker) REFERENCES stock(ticker) ON DELETE CASCADE,
    
    CONSTRAINT chk_weight_range CHECK (weight >= 0 AND weight <= 1),
    CONSTRAINT chk_portfolio_rank CHECK (portfolio_rank BETWEEN 1 AND 5)
) ENGINE=InnoDB COMMENT='최대샤프지수, 최소분산 등 최적 포트폴리오 조합';

-- 10. 주식 용어 카테고리 테이블
CREATE TABLE stock_term_category (
    category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카테고리 ID',
    category_name VARCHAR(50) NOT NULL COMMENT '카테고리명',
    parent_category_id INT COMMENT '상위 카테고리 ID',
    description VARCHAR(200) COMMENT '설명',
    sort_order INT DEFAULT 0 COMMENT '정렬순서',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    
    UNIQUE KEY uk_category_name (category_name),
    INDEX idx_parent_category (parent_category_id),
    INDEX idx_sort_order (sort_order),
    
    FOREIGN KEY (parent_category_id) REFERENCES stock_term_category(category_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='트리 구조의 주식 용어 카테고리';

-- 11. 주식 용어 테이블
CREATE TABLE stock_term (
    term_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '용어 ID',
    category_id INT NOT NULL COMMENT '카테고리 ID',
    term_name VARCHAR(100) NOT NULL COMMENT '용어명',
    term_english VARCHAR(100) COMMENT '영문 용어명',
    definition TEXT NOT NULL COMMENT '용어 정의',
    detailed_explanation TEXT COMMENT '상세 설명',
    related_terms JSON COMMENT '연관 용어 목록',
    example_text TEXT COMMENT '예시 설명',
    image_path VARCHAR(500) COMMENT '이미지 경로',
    reference_url VARCHAR(500) COMMENT '참고 URL',
    difficulty_level ENUM('beginner', 'intermediate', 'advanced') DEFAULT 'beginner' COMMENT '난이도',
    view_count INT DEFAULT 0 COMMENT '조회수',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    UNIQUE KEY uk_term_name (term_name),
    INDEX idx_category_name (category_id, term_name),
    INDEX idx_difficulty (difficulty_level),
    INDEX idx_view_count (view_count DESC),
    FULLTEXT INDEX ft_term_search (term_name, definition, detailed_explanation),
    
    FOREIGN KEY (category_id) REFERENCES stock_term_category(category_id) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='주식 투자 관련 용어 사전';

-- 12. 분석 히스토리 테이블
CREATE TABLE analysis_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '히스토리 ID',
    session_id VARCHAR(50) NOT NULL COMMENT '세션 ID',
    analysis_type ENUM('screening', 'correlation', 'optimization') NOT NULL COMMENT '분석 유형',
    analysis_params JSON COMMENT '분석 매개변수',
    result_summary JSON COMMENT '결과 요약',
    execution_time_ms INT COMMENT '실행 시간(밀리초)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    
    INDEX idx_session_type (session_id, analysis_type),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (session_id) REFERENCES user_session(session_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='분석 실행 이력 및 성능 모니터링';

-- 13. 일별 수익률 계산 뷰 (stock_price 테이블 사용 시에만)
CREATE OR REPLACE VIEW daily_returns AS
SELECT 
    ticker,
    price_date,
    close_price,
    prev_close,
    CASE 
        WHEN prev_close IS NOT NULL AND prev_close != 0 
        THEN ROUND((close_price - prev_close) / prev_close * 100, 6)
        ELSE NULL 
    END as daily_return_pct
FROM (
    SELECT 
        ticker,
        price_date,
        close_price,
        LAG(close_price) OVER (PARTITION BY ticker ORDER BY price_date) as prev_close
    FROM stock_price
) temp_table
WHERE prev_close IS NOT NULL;

-- 교수님 데이터 적재를 위한 샘플 INSERT
/*
-- 교수님 CSV 데이터 적재 예시
LOAD DATA LOCAL INFILE 'kse_fin_data.csv'
INTO TABLE stock
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(ticker, stock_name, settlement_month, accounting_year, total_assets, 
 total_debt, total_equity, revenue, operating_profit, net_income, 
 eps, bps, sps, cfps, ebitdaps)
SET 
    roe = ROUND((net_income / total_equity) * 100, 4),
    debt_ratio = ROUND((total_debt / total_equity) * 100, 4),
    reference_date = '2024-11-01',
    created_at = NOW();

-- 주가 데이터 추가 후 PER, PBR 계산
UPDATE stock 
SET 
    per = ROUND(close_price / eps, 4),
    pbr = ROUND(close_price / bps, 4),
    market_cap = close_price * 발행주식수
WHERE close_price IS NOT NULL;
*/

-- 기본 데이터 삽입 (주식 용어)
# INSERT INTO stock_term_category (category_name, description, sort_order) VALUES
# ('분산투자', '포트폴리오 분산투자 관련 용어', 1),
# ('팩터', '투자 팩터 및 지표 관련 용어', 2),
# ('그래프', '차트 및 그래프 분석 관련 용어', 3),
# ('위험관리', '투자 위험 관리 관련 용어', 4);
#
# INSERT INTO stock_term (category_id, term_name, term_english, definition, detailed_explanation) VALUES
# (1, '상관계수', 'Correlation Coefficient', '두 변수 간의 선형 관계의 강도를 나타내는 지표', '상관계수는 -1에서 1 사이의 값을 가지며, 1에 가까울수록 양의 상관관계를 의미합니다.'),
# (2, 'PER', 'Price to Earnings Ratio', '주가수익비율로 주가를 주당순이익으로 나눈 값', 'PER이 낮을수록 저평가된 주식으로 볼 수 있으나, 성장성도 함께 고려해야 합니다.'),
# (2, 'PBR', 'Price to Book Ratio', '주가순자산비율로 주가를 주당순자산가치로 나눈 값', 'PBR이 1보다 낮으면 장부가치보다 저평가된 상태를 의미합니다.'),
# (2, 'ROE', 'Return on Equity', '자기자본이익률로 당기순이익을 자기자본으로 나눈 값', 'ROE가 높을수록 자기자본을 효율적으로 활용하여 수익을 창출한다는 의미입니다.');

-- 완료 메시지
SELECT 'Portfolio Analysis Database Created Successfully!' as status,
       '교수님 데이터 적재 준비 완료' as note;
