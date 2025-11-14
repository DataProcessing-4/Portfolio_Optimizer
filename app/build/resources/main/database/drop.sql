DROP VIEW IF EXISTS daily_returns;

-- =================================================
-- 2. 외래키 종속성 순서에 따른 테이블 삭제
-- =================================================

-- 단계 1: 가장 하위 종속 테이블들 (다른 테이블을 참조하는 테이블)
DROP TABLE IF EXISTS analysis_history;
DROP TABLE IF EXISTS optimal_portfolio;
DROP TABLE IF EXISTS efficient_frontier;
DROP TABLE IF EXISTS correlation_analysis;
DROP TABLE IF EXISTS user_selected_assets;
DROP TABLE IF EXISTS multifactor_screening;
DROP TABLE IF EXISTS portfolio_settings;

-- 단계 2: 주가 데이터 테이블
DROP TABLE IF EXISTS stock_price;

-- 단계 3: 용어 사전 테이블들
DROP TABLE IF EXISTS stock_term;
DROP TABLE IF EXISTS stock_term_category;

-- 단계 4: 세션 관리 테이블
DROP TABLE IF EXISTS user_session;

-- 단계 5: 메인 주식 데이터 테이블
DROP TABLE IF EXISTS stock;