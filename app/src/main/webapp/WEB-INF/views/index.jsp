<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portfolio Optimizer</title>
    <!-- ✅ 절대 경로로 CSS 참조 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/styles.css">
</head>
<body>
<header class="header">
    <div class="header-content">
        <div class="logo">📊 Portfolio Optimizer</div>
        <nav class="nav">
            <a class="nav-link active" data-section="dashboard">대시보드</a>
            <a class="nav-link" data-section="screening">스크리닝</a>
            <a class="nav-link" data-section="selection">자산 선택</a>
            <a class="nav-link" data-section="correlation">상관관계</a>
        </nav>
    </div>
</header>

<div class="container">
    <!-- Dashboard -->
    <section id="dashboard" class="section active">
        <div class="alert alert-info">
            <span>💡</span>
            <div>
                <strong>시스템 상태:</strong> 백엔드 API 연동 준비 완료
                <div style="margin-top: 0.25rem; font-size: 0.9rem;">
                    API 기본 URL: <code id="api-url">http://localhost:8282</code>
                </div>
            </div>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-label">전체 종목 수</div>
                <div class="stat-value" id="total-stocks">-</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">스크리닝 완료</div>
                <div class="stat-value" id="screening-count">-</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">선택한 종목</div>
                <div class="stat-value" id="selected-count">0</div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <div class="card-title">API 연결 테스트</div>
            </div>
            <div class="form-group">
                <label class="form-label">API Base URL</label>
                <input type="text" id="api-base-url" class="form-control" value="http://localhost:8282">
            </div>
            <button class="btn btn-primary" onclick="testConnection()">🔌 연결 테스트</button>
            <div id="connection-result" style="margin-top: 1rem;"></div>
        </div>
    </section>

    <!-- Screening -->
    <section id="screening" class="section">
        <div class="card">
            <div class="card-header">
                <div class="card-title">멀티팩터 스크리닝</div>
                <button class="btn btn-secondary btn-sm" onclick="resetWeights()">기본값으로 리셋</button>
            </div>

            <div class="alert alert-warning">
                <span>⚙️</span>
                <div><strong>가중치 설정:</strong> PER, PBR, ROE의 합계는 100%가 되어야 합니다.</div>
            </div>

            <div class="weight-controls">
                <div class="weight-item">
                    <div class="weight-label">PER 가중치</div>
                    <input type="number" id="per-weight" class="weight-input" value="33.33" min="0" max="100" step="0.01" oninput="updateWeights()">
                    <div style="text-align: center; margin-top: 0.5rem; color: var(--text-gray); font-size: 0.85rem;">%</div>
                </div>
                <div class="weight-item">
                    <div class="weight-label">PBR 가중치</div>
                    <input type="number" id="pbr-weight" class="weight-input" value="33.33" min="0" max="100" step="0.01" oninput="updateWeights()">
                    <div style="text-align: center; margin-top: 0.5rem; color: var(--text-gray); font-size: 0.85rem;">%</div>
                </div>
                <div class="weight-item">
                    <div class="weight-label">ROE 가중치</div>
                    <input type="number" id="roe-weight" class="weight-input" value="33.34" min="0" max="100" step="0.01" oninput="updateWeights()">
                    <div style="text-align: center; margin-top: 0.5rem; color: var(--text-gray); font-size: 0.85rem;">%</div>
                </div>
            </div>

            <div style="text-align: center; margin-bottom: 1.5rem;">
                <div style="font-size: 0.9rem; color: var(--text-gray);">합계:</div>
                <div id="weight-sum" style="font-size: 1.5rem; font-weight: 700; color: var(--primary-blue);">100.00%</div>
            </div>

            <div class="form-group">
                <label class="form-label">최대 부채비율</label>
                <input type="number" id="max-debt-ratio" class="form-control" value="2.0" step="0.1" min="0">
            </div>

            <button class="btn btn-primary" style="width: 100%;" onclick="performScreening()">🔍 스크리닝 실행</button>
            <div id="screening-result" style="margin-top: 1.5rem;"></div>
        </div>

        <div class="card" id="screening-results-card" style="display: none;">
            <div class="card-header">
                <div class="card-title">스크리닝 결과</div>
                <div><span class="badge badge-primary" id="result-count">0개 종목</span></div>
            </div>
            <div class="table-container">
                <table class="table">
                    <thead>
                    <tr>
                        <th>순위</th><th>티커</th><th>종목명</th><th>업종</th>
                        <th>PER</th><th>PBR</th><th>ROE</th><th>종합점수</th><th>선별</th>
                    </tr>
                    </thead>
                    <tbody id="screening-results-body"></tbody>
                </table>
            </div>
            <div class="pagination" id="screening-pagination"></div>
        </div>
    </section>

    <!-- Selection -->
    <section id="selection" class="section">
        <div class="card">
            <div class="card-header">
                <div class="card-title">주식 검색 및 선택</div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">검색 타입</label>
                    <select id="search-type" class="form-control">
                        <option value="keyword">종목명</option>
                        <option value="ticker">티커</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">검색어</label>
                    <input type="text" id="search-value" class="form-control" placeholder="검색어를 입력하세요">
                </div>
                <div class="form-group">
                    <label class="form-label">&nbsp;</label>
                    <button class="btn btn-primary" style="width: 100%;" onclick="searchStocks()">🔍 검색</button>
                </div>
            </div>
            <div id="search-results"></div>
        </div>

        <div class="card">
            <div class="card-header">
                <div class="card-title">선택한 종목 (5~10개)</div>
                <div>
                    <span class="badge badge-success" id="selected-assets-count">0개 선택</span>
                    <button class="btn btn-outline btn-sm" onclick="clearSelectedAssets()" style="margin-left: 1rem;">전체 초기화</button>
                </div>
            </div>
            <div class="table-container">
                <table class="table">
                    <thead>
                    <tr>
                        <th>순서</th><th>티커</th><th>종목명</th><th>업종</th>
                        <th>PER</th><th>PBR</th><th>ROE</th><th>액션</th>
                    </tr>
                    </thead>
                    <tbody id="selected-assets-body">
                    <tr>
                        <td colspan="8" style="text-align: center; color: var(--text-gray); padding: 2rem;">
                            선택된 종목이 없습니다. 위에서 종목을 검색하여 선택해주세요.
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </section>

    <!-- Correlation -->
    <section id="correlation" class="section">
        <div class="card">
            <div class="card-header">
                <div class="card-title">상관관계 분석 & 히트맵</div>
                <button class="btn btn-primary btn-sm" onclick="performCorrelationAnalysis()">📊 분석 실행</button>
            </div>
            <div class="alert alert-info">
                <span>ℹ️</span>
                <div>선택한 종목들 간의 상관관계를 분석하고 히트맵으로 시각화합니다.</div>
            </div>
            <div id="heatmap-result"></div>
        </div>

        <div class="card" id="heatmap-card" style="display: none;">
            <div class="card-header">
                <div class="card-title">상관관계 히트맵</div>
            </div>
            <div id="heatmap-display"></div>
        </div>

        <div class="card">
            <div class="card-header">
                <div class="card-title">분산투자 최적화</div>
                <button class="btn btn-primary btn-sm" onclick="optimizeDiversification()">🎯 최적화 실행</button>
            </div>
            <div class="alert alert-info">
                <span>ℹ️</span>
                <div>높은 상관관계 종목을 제거하고 분산 효과가 우수한 포트폴리오를 구성합니다.</div>
            </div>
            <div id="optimization-result"></div>
        </div>

        <div class="card" id="optimization-display-card" style="display: none;">
            <div class="card-header">
                <div class="card-title">최적화 결과</div>
            </div>
            <div id="optimization-display"></div>
        </div>
    </section>
</div>

<!-- ✅ 절대 경로로 JS 참조 -->
<script src="${pageContext.request.contextPath}/static/script.js"></script>
<script>console.log('Portfolio Optimizer - Frontend Loaded');</script>
</body>
</html>
