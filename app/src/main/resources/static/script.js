// ========================================
// Global Variables
// ========================================
let API_BASE_URL = 'http://localhost:8282';
let currentScreeningResults = [];
let currentCorrelationData = null;
let selectedAssets = [];

// ========================================
// Navigation
// ========================================
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', function() {
        const sectionId = this.getAttribute('data-section');

        // Update nav links
        document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
        this.classList.add('active');

        // Update sections
        document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
        document.getElementById(sectionId).classList.add('active');

        // Load section data
        if (sectionId === 'selection') {
            loadSelectedAssets();
        }
    });
});

// ========================================
// API Functions
// ========================================
async function apiCall(endpoint, options = {}) {
    const url = API_BASE_URL + endpoint;

    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            credentials: 'include' // 세션 쿠키 포함
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API Call Error:', error);
        throw error;
    }
}

// ========================================
// Connection Test
// ========================================
async function testConnection() {
    const urlInput = document.getElementById('api-base-url');
    API_BASE_URL = urlInput.value.trim();

    const resultDiv = document.getElementById('connection-result');
    resultDiv.innerHTML = '<div class="spinner"></div>';

    try {
        // Test endpoint: 주식 목록 조회 (첫 페이지만)
        const response = await apiCall('/api/stocks?page=1&pageSize=10');

        resultDiv.innerHTML = `
                    <div class="alert alert-success">
                        <span>✅</span>
                        <div>
                            <strong>연결 성공!</strong><br>
                            총 ${response.totalElements || 0}개의 종목 데이터 확인됨
                        </div>
                    </div>
                `;

        // Update dashboard stats
        document.getElementById('total-stocks').textContent = response.totalElements || 0;
        document.getElementById('api-url').textContent = API_BASE_URL;

    } catch (error) {
        resultDiv.innerHTML = `
                    <div class="alert alert-error">
                        <span>❌</span>
                        <div>
                            <strong>연결 실패</strong><br>
                            ${error.message}
                        </div>
                    </div>
                `;
    }
}

// ========================================
// Weight Management
// ========================================
function updateWeights() {
    const per = parseFloat(document.getElementById('per-weight').value) || 0;
    const pbr = parseFloat(document.getElementById('pbr-weight').value) || 0;
    const roe = parseFloat(document.getElementById('roe-weight').value) || 0;

    const sum = per + pbr + roe;
    const sumElement = document.getElementById('weight-sum');
    sumElement.textContent = sum.toFixed(2) + '%';

    // Color coding
    if (Math.abs(sum - 100) < 0.01) {
        sumElement.style.color = 'var(--success)';
    } else {
        sumElement.style.color = 'var(--error)';
    }
}

function resetWeights() {
    document.getElementById('per-weight').value = 33.33;
    document.getElementById('pbr-weight').value = 33.33;
    document.getElementById('roe-weight').value = 33.34;
    updateWeights();
}

// ========================================
// Screening
// ========================================
async function performScreening() {
    const per = parseFloat(document.getElementById('per-weight').value) / 100;
    const pbr = parseFloat(document.getElementById('pbr-weight').value) / 100;
    const roe = parseFloat(document.getElementById('roe-weight').value) / 100;
    const maxDebtRatio = parseFloat(document.getElementById('max-debt-ratio').value);

    // Validate weights
    const sum = per + pbr + roe;
    if (Math.abs(sum - 1.0) > 0.001) {
        showAlert('screening-result', 'error', '가중치의 합이 100%가 되어야 합니다.');
        return;
    }

    const resultDiv = document.getElementById('screening-result');
    resultDiv.innerHTML = '<div class="spinner"></div>';

    try {
        const response = await apiCall('/api/screening/perform', {
            method: 'POST',
            body: JSON.stringify({
                perWeight: per.toFixed(4),
                pbrWeight: pbr.toFixed(4),
                roeWeight: roe.toFixed(4),
                maxDebtRatio: maxDebtRatio
            })
        });

        currentScreeningResults = response.screeningResults || [];

        resultDiv.innerHTML = `
                    <div class="alert alert-success">
                        <span>✅</span>
                        <div>
                            <strong>스크리닝 완료!</strong><br>
                            총 ${response.totalStocksAnalyzed}개 분석, 상위 ${response.selectedStocksCount}개 선별
                        </div>
                    </div>
                `;

        // Show results table
        displayScreeningResults(currentScreeningResults);
        document.getElementById('screening-count').textContent = response.selectedStocksCount;

    } catch (error) {
        resultDiv.innerHTML = `
                    <div class="alert alert-error">
                        <span>❌</span>
                        <div>
                            <strong>스크리닝 실패</strong><br>
                            ${error.message}
                        </div>
                    </div>
                `;
    }
}

function displayScreeningResults(results) {
    const card = document.getElementById('screening-results-card');
    const tbody = document.getElementById('screening-results-body');
    const countBadge = document.getElementById('result-count');

    card.style.display = 'block';
    countBadge.textContent = `${results.length}개 종목`;

    tbody.innerHTML = results.map(stock => `
                <tr>
                    <td><strong>${stock.ranking}</strong></td>
                    <td><span class="stock-symbol">${stock.ticker}</span></td>
                    <td>${stock.stockName}</td>
                    <td>${stock.industry || '-'}</td>
                    <td>${stock.per ? stock.per.toFixed(2) : '-'}</td>
                    <td>${stock.pbr ? stock.pbr.toFixed(2) : '-'}</td>
                    <td>${stock.roe ? stock.roe.toFixed(2) : '-'}%</td>
                    <td><strong>${stock.compositeScore ? stock.compositeScore.toFixed(4) : '-'}</strong></td>
                    <td>
                        ${stock.isSelected ?
        '<span class="badge badge-success">선별됨</span>' :
        '<span class="badge">-</span>'}
                    </td>
                </tr>
            `).join('');
}

// ========================================
// Stock Search
// ========================================
async function searchStocks() {
    const searchType = document.getElementById('search-type').value;
    const searchValue = document.getElementById('search-value').value.trim();

    if (!searchValue) {
        showAlert('search-results', 'warning', '검색어를 입력해주세요.');
        return;
    }

    const resultDiv = document.getElementById('search-results');
    resultDiv.innerHTML = '<div class="spinner"></div>';

    try {
        const response = await apiCall(
            `/api/stocks/search?searchType=${searchType}&searchValue=${encodeURIComponent(searchValue)}&page=1&pageSize=30`
        );

        const stocks = response.content || [];

        if (stocks.length === 0) {
            resultDiv.innerHTML = `
                        <div class="alert alert-info" style="margin-top: 1rem;">
                            <span>ℹ️</span>
                            <div>검색 결과가 없습니다.</div>
                        </div>
                    `;
            return;
        }

        resultDiv.innerHTML = `
                    <div class="card" style="margin-top: 1rem;">
                        <div class="card-header">
                            <div class="card-title">검색 결과</div>
                            <span class="badge badge-primary">${stocks.length}개 종목</span>
                        </div>
                        <div class="table-container">
                            <table class="table">
                                <thead>
                                    <tr>
                                        <th>선택</th>
                                        <th>티커</th>
                                        <th>종목명</th>
                                        <th>업종</th>
                                        <th>PER</th>
                                        <th>PBR</th>
                                        <th>ROE</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${stocks.map(stock => `
                                        <tr>
                                            <td>
                                                <button class="btn btn-outline btn-sm"
                                                    onclick="selectAsset('${stock.ticker}')"
                                                    ${selectedAssets.some(a => a.ticker === stock.ticker) ? 'disabled' : ''}>
                                                    ${selectedAssets.some(a => a.ticker === stock.ticker) ? '✓ 선택됨' : '선택'}
                                                </button>
                                            </td>
                                            <td><span class="stock-symbol">${stock.ticker}</span></td>
                                            <td>${stock.stockName}</td>
                                            <td>${stock.industry || '-'}</td>
                                            <td>${stock.per ? stock.per.toFixed(2) : '-'}</td>
                                            <td>${stock.pbr ? stock.pbr.toFixed(2) : '-'}</td>
                                            <td>${stock.roe ? stock.roe.toFixed(2) : '-'}%</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    </div>
                `;

    } catch (error) {
        resultDiv.innerHTML = `
                    <div class="alert alert-error" style="margin-top: 1rem;">
                        <span>❌</span>
                        <div>검색 실패: ${error.message}</div>
                    </div>
                `;
    }
}

// ========================================
// Asset Selection
// ========================================
async function selectAsset(ticker) {
    if (selectedAssets.length >= 10) {
        alert('최대 10개까지만 선택할 수 있습니다.');
        return;
    }

    try {
        const response = await apiCall('/api/stocks/select', {
            method: 'POST',
            body: JSON.stringify({ ticker: ticker })
        });

        if (response.success) {
            await loadSelectedAssets();
            searchStocks(); // Refresh search results
        }

    } catch (error) {
        alert('자산 선택 실패: ' + error.message);
    }
}

async function loadSelectedAssets() {
    try {
        const response = await apiCall('/api/stocks/selected');
        selectedAssets = response.data || [];

        const tbody = document.getElementById('selected-assets-body');
        const countBadge = document.getElementById('selected-assets-count');

        countBadge.textContent = `${selectedAssets.length}개 선택`;
        document.getElementById('selected-count').textContent = selectedAssets.length;

        if (selectedAssets.length === 0) {
            tbody.innerHTML = `
                        <tr>
                            <td colspan="8" style="text-align: center; color: var(--text-gray); padding: 2rem;">
                                선택된 종목이 없습니다. 위에서 종목을 검색하여 선택해주세요.
                            </td>
                        </tr>
                    `;
        } else {
            tbody.innerHTML = selectedAssets.map((asset, index) => `
                        <tr>
                            <td><strong>${index + 1}</strong></td>
                            <td><span class="stock-symbol">${asset.ticker}</span></td>
                            <td>${asset.stockName}</td>
                            <td>${asset.industry || '-'}</td>
                            <td>${asset.per ? asset.per.toFixed(2) : '-'}</td>
                            <td>${asset.pbr ? asset.pbr.toFixed(2) : '-'}</td>
                            <td>${asset.roe ? asset.roe.toFixed(2) : '-'}%</td>
                            <td>
                                <button class="btn btn-outline btn-sm" onclick="deselectAsset('${asset.ticker}')">
                                    삭제
                                </button>
                            </td>
                        </tr>
                    `).join('');
        }

    } catch (error) {
        console.error('Failed to load selected assets:', error);
    }
}

async function deselectAsset(ticker) {
    try {
        await apiCall(`/api/stocks/deselect/${ticker}`, {
            method: 'DELETE'
        });
        await loadSelectedAssets();

    } catch (error) {
        alert('자산 선택 해제 실패: ' + error.message);
    }
}

async function clearSelectedAssets() {
    if (!confirm('선택한 모든 종목을 초기화하시겠습니까?')) {
        return;
    }

    try {
        await apiCall('/api/stocks/clear', {
            method: 'DELETE'
        });
        await loadSelectedAssets();

    } catch (error) {
        alert('초기화 실패: ' + error.message);
    }
}

// ========================================
// ========================================
// ========================================
// Correlation Analysis (최종 수정)
// ========================================

/**
 * 1단계: 상관관계 분석 실행
 */
async function performCorrelationAnalysis() {
    const resultDiv = document.getElementById('heatmap-result');

    // 선택된 자산 다시 로드
    try {
        const selectedResponse = await apiCall('/api/stocks/selected');
        selectedAssets = selectedResponse.data || [];
    } catch (error) {
        console.error('Failed to load selected assets:', error);
    }

    if (selectedAssets.length < 2) {
        resultDiv.innerHTML = `
        <div class="alert alert-warning">
          <span>⚠️</span>
          <div>상관관계 분석을 위해서는 최소 2개의 종목이 필요합니다. 현재: ${selectedAssets.length}개</div>
        </div>
      `;
        return;
    }

    resultDiv.innerHTML = '<div class="spinner"></div>';

    try {
        const tickers = selectedAssets.map(a => a.ticker);

        console.log('상관관계 분석 시작 - 티커:', tickers);

        // 1단계: 상관관계 분석 실행
        const analysisResponse = await apiCall('/api/correlation/analyze', {
            method: 'POST',
            body: JSON.stringify({
                tickers: tickers,
                period: 'ALL',
                highCorrelationThreshold: 0.7
            })
        });

        console.log('분석 완료:', analysisResponse);

        resultDiv.innerHTML = `
        <div class="alert alert-success">
          <span>✅</span>
          <div>
            <strong>분석 완료!</strong><br>
            ${selectedAssets.length}개 종목 상관관계 분석됨
          </div>
        </div>
      `;

        // 2단계: 히트맵 자동 로드
        await loadHeatmap();

    } catch (error) {
        console.error('상관관계 분석 에러:', error);
        resultDiv.innerHTML = `
        <div class="alert alert-error">
          <span>❌</span>
          <div>
            <strong>분석 실패</strong><br>
            ${error.message}
          </div>
        </div>
      `;
    }
}

/**
 * 2단계: 히트맵 로드
 */
async function loadHeatmap() {
    const displayCard = document.getElementById('heatmap-card');
    const displayDiv = document.getElementById('heatmap-display');

    try {
        console.log('히트맵 로드 시작');

        // 티커 없이 호출 (분석된 데이터 사용)
        const response = await apiCall('/api/correlation/heatmap');

        console.log('히트맵 응답:', response);

        if (!response.success || !response.data) {
            throw new Error('히트맵 데이터를 받지 못했습니다.');
        }

        // 히트맵 표시
        displayHeatmap(response.data);

    } catch (error) {
        console.error('히트맵 로드 에러:', error);
        displayCard.style.display = 'none';
    }
}

function displayHeatmap(data) {
    const card = document.getElementById('heatmap-card');
    const displayDiv = document.getElementById('heatmap-display');

    console.log('히트맵 데이터:', data);

    // 백엔드 실제 응답 구조에 맞게 수정
    if (!data || !data.labels || !data.periodData) {
        console.error('히트맵 데이터가 유효하지 않습니다:', data);
        card.style.display = 'none';
        return;
    }

    card.style.display = 'block';

    const tickers = data.labels; // labels가 티커 목록

    // periodData에서 1년 데이터 찾기 (또는 첫 번째 데이터 사용)
    const periodItem = data.periodData.find(p => p.period === '1Y') || data.periodData[0];
    const matrix = periodItem ? periodItem.matrix : [];

    if (tickers.length === 0 || matrix.length === 0) {
        displayDiv.innerHTML = '<p style="text-align: center; color: var(--text-gray); padding: 2rem;">상관계수 데이터가 없습니다.</p>';
        return;
    }

    let html = `
      <div style="overflow-x: auto;">
        <table class="table" style="min-width: 600px;">
          <thead>
            <tr>
              <th></th>
              ${tickers.map(t => `<th>${t}</th>`).join('')}
            </tr>
          </thead>
          <tbody>
    `;

    tickers.forEach((ticker1, i) => {
        html += `<tr><td><strong>${ticker1}</strong></td>`;
        tickers.forEach((ticker2, j) => {
            const value = matrix[i] && matrix[i][j] !== undefined && matrix[i][j] !== null ? matrix[i][j] : null;
            const color = getCorrelationColor(value);
            html += `<td style="background-color: ${color}; text-align: center;">
          ${value !== null ? value.toFixed(3) : '-'}
        </td>`;
        });
        html += '</tr>';
    });

    html += `
          </tbody>
        </table>
      </div>
      <div style="margin-top: 1rem; padding: 1rem; background-color: var(--bg-beige); border-radius: 8px;">
        <div style="font-size: 0.9rem; color: var(--text-gray);">
          <strong>범례:</strong>
          <span style="display: inline-block; width: 20px; height: 20px; background: rgba(231, 76, 60, 0.3); margin-left: 1rem; border-radius: 4px;"></span> 높은 상관관계 (0.7~1.0)
          <span style="display: inline-block; width: 20px; height: 20px; background: rgba(243, 156, 18, 0.3); margin-left: 0.5rem; border-radius: 4px;"></span> 중간 상관관계 (0.3~0.7)
          <span style="display: inline-block; width: 20px; height: 20px; background: rgba(39, 174, 96, 0.3); margin-left: 0.5rem; border-radius: 4px;"></span> 낮은 상관관계 (0~0.3)
          <br><br>
          <small style="color: var(--text-gray);">💡 상관계수가 0.7 이상이면 두 종목이 함께 움직이므로 분산 효과가 낮습니다.</small>
        </div>
      </div>
    `;

    displayDiv.innerHTML = html;
}

// ========================================
// Diversification Optimization (최종 수정)
// ========================================
async function optimizeDiversification() {
    const resultDiv = document.getElementById('optimization-result');

    // 선택된 자산 확인
    try {
        const selectedResponse = await apiCall('/api/stocks/selected');
        selectedAssets = selectedResponse.data || [];
    } catch (error) {
        console.error('Failed to load selected assets:', error);
    }

    if (selectedAssets.length < 2) {
        resultDiv.innerHTML = `
        <div class="alert alert-warning">
          <span>⚠️</span>
          <div>최적화를 위해서는 최소 2개의 종목이 필요합니다. 현재: ${selectedAssets.length}개</div>
        </div>
      `;
        return;
    }

    resultDiv.innerHTML = '<div class="spinner"></div>';

    try {
        const tickers = selectedAssets.map(a => a.ticker);

        console.log('최적화 요청 시작 - 티커:', tickers);

        const requestBody = {
            tickers: tickers,
            highCorrelationThreshold: 0.7
        };

        console.log('Request Body:', JSON.stringify(requestBody));

        // Request Body 명시적으로 전송
        const response = await fetch(`${API_BASE_URL}/api/correlation/diversification/optimize`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(requestBody)
        });

        console.log('Response status:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Response error:', errorText);
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log('최적화 응답:', data);

        resultDiv.innerHTML = `
        <div class="alert alert-success">
          <span>✅</span>
          <div>
            <strong>최적화 완료!</strong><br>
            분산 최적화 포트폴리오가 생성되었습니다.
          </div>
        </div>
      `;

        // 결과 표시
        displayOptimizationResult(data);

    } catch (error) {
        console.error('최적화 에러:', error);
        resultDiv.innerHTML = `
        <div class="alert alert-error">
          <span>❌</span>
          <div>
            <strong>최적화 실패</strong><br>
            ${error.message}
          </div>
        </div>
      `;
    }
}

function displayOptimizationResult(data) {
    const card = document.getElementById('optimization-display-card');
    const displayDiv = document.getElementById('optimization-display');

    console.log('최적화 결과 데이터:', data);

    if (!data) {
        card.style.display = 'none';
        return;
    }

    card.style.display = 'block';

    const selectedStocks = data.selectedStocks || [];
    const excludedStocks = data.excludedStocks || [];
    const score = data.portfolioDiversificationScore || 0;
    const avgCorr = data.averageCorrelation || 0;

    let html = '<div class="stats-grid">';

    // 포트폴리오 지표
    html += `
      <div class="stat-card">
        <div class="stat-label">분산점수</div>
        <div class="stat-value">${score.toFixed(1)}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">평균 상관계수</div>
        <div class="stat-value">${avgCorr.toFixed(3)}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">선택된 종목</div>
        <div class="stat-value">${selectedStocks.length}개</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">제외된 종목</div>
        <div class="stat-value">${excludedStocks.length}개</div>
      </div>
    `;

    html += '</div>';

    // 선택된 종목
    if (selectedStocks.length > 0) {
        html += `
        <div style="margin-top: 2rem;">
          <h3 style="margin-bottom: 1rem;">✅ 선택된 종목 (분산 효과 우수)</h3>
          <table class="table">
            <thead>
              <tr>
                <th>종목 코드</th>
                <th>종목명</th>
                <th>평균 상관계수</th>
              </tr>
            </thead>
            <tbody>
      `;

        selectedStocks.forEach(stock => {
            html += `
          <tr>
            <td><strong>${stock.ticker}</strong></td>
            <td>${stock.stockName || '-'}</td>
            <td>${stock.averageCorrelation ? stock.averageCorrelation.toFixed(3) : '-'}</td>
          </tr>
        `;
        });

        html += `
            </tbody>
          </table>
        </div>
      `;
    }

    // 제외된 종목
    if (excludedStocks.length > 0) {
        html += `
        <div style="margin-top: 2rem;">
          <h3 style="margin-bottom: 1rem;">❌ 제외된 종목 (높은 상관관계)</h3>
          <table class="table">
            <thead>
              <tr>
                <th>종목 코드</th>
                <th>종목명</th>
                <th>제외 사유</th>
              </tr>
            </thead>
            <tbody>
      `;

        excludedStocks.forEach(stock => {
            html += `
          <tr>
            <td><strong>${stock.ticker}</strong></td>
            <td>${stock.stockName || '-'}</td>
            <td>${stock.exclusionReason || '높은 상관관계'}</td>
          </tr>
        `;
        });

        html += `
            </tbody>
          </table>
        </div>
      `;
    }

    displayDiv.innerHTML = html;
}

function getCorrelationColor(value) {
    if (value === null || value === undefined) {
        return 'rgba(200, 200, 200, 0.1)';
    }

    const absValue = Math.abs(value);

    if (absValue >= 0.7) {
        return 'rgba(231, 76, 60, 0.3)'; // Red
    } else if (absValue >= 0.3) {
        return 'rgba(243, 156, 18, 0.3)'; // Orange
    } else {
        return 'rgba(39, 174, 96, 0.3)'; // Green
    }
}
// ========================================
// Utility Functions
// ========================================
function showAlert(elementId, type, message) {
    const element = document.getElementById(elementId);
    const icons = {
        success: '✅',
        error: '❌',
        warning: '⚠️',
        info: 'ℹ️'
    };

    element.innerHTML = `
                <div class="alert alert-${type}">
                    <span>${icons[type] || 'ℹ️'}</span>
                    <div>${message}</div>
                </div>
            `;
}

// ========================================
// Initialize
// ========================================
window.addEventListener('DOMContentLoaded', () => {
    console.log('Portfolio Optimizer - Frontend Loaded');
    updateWeights();
});