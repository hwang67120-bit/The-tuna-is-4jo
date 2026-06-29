const state = {
  token: localStorage.getItem('saverToken') || '',
  role: localStorage.getItem('saverRole') || '',
  memberId: localStorage.getItem('saverMemberId') || '',
  memberName: localStorage.getItem('saverMemberName') || '',
  products: [],
  portOneConfig: null,
  pendingOrder: null,
  orderPreviewed: false,
  supportChatRoomId: localStorage.getItem('saverSupportChatRoomId') || '',
  supportSocket: null,
  supportConnectedRoomId: '',
  supportStompConnected: false,
  supportReconnectTimer: null,
  supportLastMessageIds: JSON.parse(localStorage.getItem('saverSupportLastMessageIds') || '{}')
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

function clearSignupForm() {
  const form = $('#signupForm');
  const passwordInput = $('#signupPassword');
  const passwordToggle = $('[data-password-toggle="signupPassword"]');

  if (!form) return;

  form.reset();
  if (passwordInput) passwordInput.type = 'password';
  if (passwordToggle) {
    passwordToggle.textContent = '보기';
    passwordToggle.setAttribute('aria-label', '비밀번호 보기');
  }
}
function bindPasswordToggle() {
  $$('[data-password-toggle]').forEach((button) => {
    button.addEventListener('click', () => {
      const input = document.getElementById(button.dataset.passwordToggle);
      if (!input) return;

      const isHidden = input.type === 'password';
      input.type = isHidden ? 'text' : 'password';
      button.textContent = isHidden ? '숨김' : '보기';
      button.setAttribute('aria-label', isHidden ? '비밀번호 숨기기' : '비밀번호 보기');
    });
  });
}

function openAuthModal(mode = 'login') {
  const isSignup = mode === 'signup';
  $('#authModal').classList.remove('hidden');
  $('#loginForm').classList.toggle('hidden', isSignup);
  $('#keepLoginLabel').classList.toggle('hidden', isSignup);
  $('#authLinks').classList.toggle('hidden', isSignup);
  $('#signupForm').classList.toggle('hidden', !isSignup);
  if (!isSignup) clearSignupForm();
}

function closeAuthModal() {
  clearSignupForm();
  $('#loginForm').classList.remove('hidden');
  $('#keepLoginLabel').classList.remove('hidden');
  $('#authLinks').classList.remove('hidden');
  $('#signupForm').classList.add('hidden');
  $('#authModal').classList.add('hidden');
}

function formatPrice(value) {
  return Number(value || 0).toLocaleString('ko-KR') + '원';
}

function showToast(message) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.remove('hidden');
  setTimeout(() => toast.classList.add('hidden'), 2800);
}


function escapeHtml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

function formatDateTime(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}
function setSession(data) {
  state.token = data.accessToken;
  state.role = data.role;
  state.memberId = String(data.memberId);
  state.memberName = data.name || state.memberName || '';
  localStorage.setItem('saverToken', state.token);
  localStorage.setItem('saverRole', state.role);
  localStorage.setItem('saverMemberId', state.memberId);
  if (state.memberName) localStorage.setItem('saverMemberName', state.memberName);
  renderSession();
}

function setSessionMemberName(name) {
  state.memberName = name || '';
  if (state.memberName) {
    localStorage.setItem('saverMemberName', state.memberName);
  } else {
    localStorage.removeItem('saverMemberName');
  }
  renderSession();
}

function clearSession() {
  state.token = '';
  state.role = '';
  state.memberId = '';
  state.memberName = '';
  localStorage.removeItem('saverToken');
  localStorage.removeItem('saverRole');
  localStorage.removeItem('saverMemberId');
  localStorage.removeItem('saverMemberName');
  renderSession();
}

function renderSession() {
  const loggedIn = Boolean(state.token);
  const labelName = state.memberName || `회원 ${state.memberId}`;
  $('#sessionLabel').textContent = loggedIn ? `${labelName} · ${state.role}` : '';
  $('#sessionLabel').classList.toggle('hidden', !loggedIn);
  $('#loginOpenButton').classList.toggle('hidden', loggedIn);
  $('#signupOpenButton').classList.toggle('hidden', loggedIn);
  $('#logoutButton').classList.toggle('hidden', !loggedIn);
  $$('[data-auth-only]').forEach((el) => el.classList.toggle('hidden', !loggedIn));
  $$('[data-admin-only]').forEach((el) => el.classList.toggle('hidden', state.role !== 'ADMIN'));
  if (!loggedIn && !$('#profileView').classList.contains('hidden')) {
    showView('home');
  }
  renderSupportState();
}

async function api(path, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = headers['Content-Type'] || 'application/json';
  }
  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }
  const response = await fetch(path, { ...options, headers });
  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(payload?.message || `HTTP ${response.status}`);
  }
  return payload;
}

function showView(name) {
  if ((name === 'cart' || name === 'profile') && !state.token) {
    showToast('로그인이 필요합니다.');
    openAuthModal('login');
    return;
  }
  if (name === 'admin' && state.role !== 'ADMIN') {
    showToast('관리자만 접근할 수 있습니다.');
    return;
  }
  $$('.view').forEach((view) => view.classList.add('hidden'));
  $(`#${name}View`).classList.remove('hidden');
  if (name === 'home') {
    loadProducts();
    loadPopularSearches();
  }
  if (name === 'cart') loadCart();
  if (name === 'profile') loadProfile();
}

async function loadProducts(categoryId = 'all') {
  try {
    const url = categoryId === 'all' ? '/api/products?page=0&size=20' : `/api/products/categories/${categoryId}`;
    const payload = await api(url, { method: 'GET' });
    const data = payload.data;
    const products = Array.isArray(data) ? data : (Array.isArray(data?.content) ? data.content : (data?.products || []));
    state.products = products;
    renderProducts(products);
  } catch (error) {
    showToast(error.message);
  }
}

async function searchProducts(keyword) {
  try {
    const payload = await api(`/api/products/search?keyword=${encodeURIComponent(keyword)}&page=0&size=20`, { method: 'GET' });
    renderProducts(payload.data?.products || []);
  } catch (error) {
    showToast(error.message);
  }
}

async function loadPopularSearches() {
  try {
    const payload = await api('/api/products/popular-searches', { method: 'GET' });
    const data = payload.data || {};
    const searches = data.popularSearches || data.keywords || data.searches || data.content || [];
    const panel = $('#popularSearchPanel');
    if (!searches.length) {
      panel.innerHTML = '<span class="muted-text">인기검색어가 없습니다.</span>';
      return;
    }
    panel.innerHTML = searches.map((item, index) => {
      const keyword = item.keyword || item.searchKeyword || item.word || item.name || String(item);
      const count = item.count || item.searchCount || item.score || '';
      return `<button class="search-chip" type="button" data-popular-keyword="${escapeHtml(keyword)}">${index + 1}. ${escapeHtml(keyword)}${count ? ` · ${escapeHtml(count)}` : ''}</button>`;
    }).join('');
  } catch (error) {
    $('#popularSearchPanel').innerHTML = '<span class="muted-text">인기검색어를 불러오지 못했습니다.</span>';
  }
}

function renderProducts(products) {
  const grid = $('#productGrid');
  if (!products.length) {
    grid.innerHTML = '<div class="table-panel">상품이 없습니다.</div>';
    return;
  }
  grid.innerHTML = products.map((product) => {
    const id = product.id ?? product.productId;
    const name = product.name ?? product.productName;
    return `<article class="product-card">
      <div class="product-thumb">S</div>
      <div class="product-card-body">
        <span class="badge">${product.status || 'ON_SALE'}</span>
        <h3>${name}</h3>
        <div class="price">${formatPrice(product.price)}</div>
        <button class="outline-button" type="button" data-detail-id="${id}">상세 보기</button>
      </div>
    </article>`;
  }).join('');
}

async function loadProductDetail(productId) {
  try {
    const payload = await api(`/api/products/${productId}`, { method: 'GET' });
    renderProductDetail(payload.data);
    showView('detail');
  } catch (error) {
    showToast(error.message);
  }
}

function renderProductDetail(product) {
  const options = product.options || [];
  $('#productDetail').innerHTML = `<div class="detail-visual">S</div>
    <div class="detail-info">
      <span class="badge">${escapeHtml(product.status)}</span>
      <h1>${escapeHtml(product.name)}</h1>
      <p>${escapeHtml(product.categoryName || '')}</p>
      <p>${escapeHtml(product.description || '')}</p>
      <div class="price">${formatPrice(product.price)}</div>
      <div class="option-list">
        ${options.map((option) => {
          const disabled = option.status !== 'ON_SALE' ? 'disabled' : '';
          const unitPrice = Number(product.price || 0) + Number(option.additionalPrice || 0);
          return `<div class="option-row">
            <div><strong>${escapeHtml(option.optionName)}</strong><br><small>재고 ${escapeHtml(option.optionStock)} · ${escapeHtml(option.status)}</small></div>
            <input type="number" min="1" value="1" data-quantity-for="${option.optionId}">
            <button class="primary-button small" type="button" data-cart-option-id="${option.optionId}" ${disabled}>담기</button>
            <button class="outline-button" type="button" data-direct-option-id="${option.optionId}" data-product-name="${escapeHtml(product.name)}" data-option-name="${escapeHtml(option.optionName)}" data-unit-price="${unitPrice}" ${disabled}>바로 구매</button>
          </div>`;
        }).join('')}
      </div>
    </div>`;
}

async function loadCart() {
  if (!state.token) {
    $('#cartPanel').innerHTML = '로그인이 필요합니다.';
    $('#orderResult')?.classList.add('hidden');
    return;
  }
  try {
    const payload = await api('/api/carts', { method: 'GET' });
    const data = payload.data;
    const items = data.items || [];
    if (!items.length) {
      $('#cartPanel').innerHTML = '장바구니가 비어 있습니다.';
      return;
    }
    $('#cartPanel').innerHTML = items.map((item) => `<div class="cart-row" data-cart-row="${item.cartItemId}" data-product-name="${escapeHtml(item.productName)}" data-option-name="${escapeHtml(item.optionName)}" data-unit-price="${item.unitPrice}" data-quantity="${item.quantity}" data-total-price="${item.totalPrice}">
      <input type="checkbox" checked data-cart-select="${item.cartItemId}" aria-label="주문 선택">
      <strong>${escapeHtml(item.productName)}<br><small>${escapeHtml(item.optionName)}</small></strong>
      <span>${formatPrice(item.unitPrice)}</span>
      <input type="number" min="1" value="${item.quantity}" data-cart-quantity="${item.cartItemId}">
      <strong>${formatPrice(item.totalPrice)}</strong>
      <button class="outline-button" type="button" data-cart-delete="${item.cartItemId}">삭제</button>
    </div>`).join('') + `<div class="cart-total">총 ${formatPrice(data.totalPrice)}</div>
      <div class="cart-actions">
        <button class="outline-button" type="button" data-clear-cart>전체 비우기</button>
        <button class="primary-button" type="button" data-cart-order-selected>선택 상품 구매</button>
      </div>`;
  } catch (error) {
    showToast(error.message);
  }
}

async function loadProfile() {
  if (!state.token) {
    showToast('로그인이 필요합니다.');
    return;
  }
  try {
    const payload = await api('/api/members/info', { method: 'GET' });
    const member = payload.data;
    $('#profileMemberId').textContent = member.memberId;
    $('#profileEmail').textContent = member.email;
    $('#profileRole').textContent = member.role;
    $('#profileName').value = member.name;
    $('#profilePhone').value = member.phoneNumber;
    setSessionMemberName(member.name);
    await loadMyCoupons();
    await loadAddresses('#profileAddressPanel', false);
  } catch (error) {
    showToast(error.message);
  }
}

async function syncSessionMemberName() {
  if (!state.token) return;
  try {
    const payload = await api('/api/members/info', { method: 'GET' });
    setSessionMemberName(payload.data?.name);
  } catch (error) {
    // 세션 라벨 보강용 호출이라 실패해도 로그인 흐름은 유지합니다.
  }
}


async function addCartItem(optionId) {
  if (!state.token) {
    showToast('로그인이 필요합니다.');
    openAuthModal('login');
    return;
  }
  const quantity = Number($(`[data-quantity-for="${optionId}"]`).value || 1);
  try {
    await api('/api/carts/items', {
      method: 'POST',
      body: JSON.stringify({ productOptionId: Number(optionId), quantity })
    });
    showToast('장바구니에 담았습니다.');
  } catch (error) {
    showToast(error.message);
  }
}


function requireLogin() {
  if (state.token) return true;

  showToast('로그인이 필요합니다.');
  openAuthModal('login');
  return false;
}

function getOptionQuantity(optionId) {
  return Number($(`[data-quantity-for="${optionId}"]`)?.value || 1);
}

function getSelectedCartItemIds() {
  return $$('[data-cart-select]:checked').map((checkbox) => Number(checkbox.dataset.cartSelect));
}

function getSelectedCartItems() {
  return $$('[data-cart-select]:checked').map((checkbox) => {
    const row = checkbox.closest('[data-cart-row]');
    return {
      cartItemId: Number(checkbox.dataset.cartSelect),
      productName: row?.dataset.productName || '상품',
      optionName: row?.dataset.optionName || '',
      unitPrice: Number(row?.dataset.unitPrice || 0),
      quantity: Number(row?.querySelector('[data-cart-quantity]')?.value || row?.dataset.quantity || 1),
      totalPrice: Number(row?.dataset.totalPrice || 0)
    };
  });
}

async function renderOrderForm() {
  const pendingOrder = state.pendingOrder;
  if (!pendingOrder) return;

  state.orderPreviewed = false;
  showView('order');
  renderPendingOrderItems(pendingOrder.items || []);
  $('#orderSummaryPrice').textContent = '주문 생성 후 확인';
  $('#orderSummaryDiscount').textContent = '-0원';
  $('#orderSummaryDelivery').textContent = '주문 생성 후 확인';
  $('#orderSummaryTotal').textContent = '주문 생성 후 확인';
  $('#submitOrderButton').textContent = '결제하기';
  $('#selectedAddressId').value = '';
  $('#selectedAddressPanel').classList.add('hidden');
  $('#selectedAddressPanel').innerHTML = '';
  await loadCouponsForOrder();
  await loadAddresses('#addressPanel', true);
  if (pendingOrder.type === 'CART') {
    await previewPendingOrder({ silent: true });
  }
}

function renderPendingOrderItems(items) {
  if (!items.length) {
    $('#orderItemsPanel').innerHTML = '<div class="cart-row order-row"><strong>상품 정보</strong><span>선택된 상품을 확인 중입니다.</span></div>';
    return;
  }
  $('#orderItemsPanel').innerHTML = items.map((item) => `<div class="cart-row order-row">
    <strong>${escapeHtml(item.productName || '상품')}<br><small>${escapeHtml(item.optionName || '')}</small></strong>
    <span>${formatPrice(item.unitPrice)} × ${escapeHtml(item.quantity)} = ${formatPrice(item.totalPrice || item.unitPrice * item.quantity)}</span>
  </div>`).join('');
}

function prepareDirectOrder(optionId, item = {}) {
  if (!requireLogin()) return;

  const quantity = getOptionQuantity(optionId);
  const unitPrice = Number(item.unitPrice || 0);
  state.pendingOrder = {
    type: 'DIRECT',
    productOptionId: Number(optionId),
    quantity,
    items: [{
      productName: item.productName || '바로 구매 상품',
      optionName: item.optionName || `옵션 #${optionId}`,
      unitPrice,
      quantity,
      totalPrice: unitPrice * quantity
    }]
  };
  renderOrderForm();
}

function prepareCartOrder(cartItemIds) {
  if (!requireLogin()) return;
  if (!cartItemIds.length) {
    showToast('구매할 상품을 선택해 주세요.');
    return;
  }

  state.pendingOrder = {
    type: 'CART',
    cartItemIds,
    items: getSelectedCartItems()
  };
  renderOrderForm();
}

function getAddressRequest(form) {
  const request = formToObject(form);
  request.defaultAddress = form.elements.defaultAddress.checked;
  return request;
}

async function createProfileAddress(form) {
  if (!form.reportValidity()) {
    throw new Error('배송지 정보를 입력해 주세요.');
  }
  const payload = await api('/api/addresses', {
    method: 'POST',
    body: JSON.stringify(getAddressRequest(form))
  });
  showToast('배송지가 저장되었습니다.');
  closeProfileAddressForm();
  await loadAddresses('#profileAddressPanel', false);
  return payload.data;
}

function getSelectedCouponId() {
  const value = $('#orderCouponSelect')?.value;
  return value ? Number(value) : null;
}

function getSelectedAddressId() {
  const value = $('#selectedAddressId')?.value;
  return value ? Number(value) : null;
}

async function resolveOrderAddressId() {
  const selectedAddressId = getSelectedAddressId();
  if (selectedAddressId) return selectedAddressId;
  throw new Error('내 정보에서 저장한 배송지를 선택해 주세요.');
}

function renderOrderPreview(preview) {
  const items = preview.items || [];
  if (items.length) {
    $('#orderItemsPanel').innerHTML = items.map((item) => `<div class="cart-row order-row">
      <strong>${escapeHtml(item.productName || '상품')}<br><small>${escapeHtml(item.optionName || '')}</small></strong>
      <span>${formatPrice(item.unitPrice)} × ${escapeHtml(item.quantity)} = ${formatPrice(item.totalPrice)}</span>
    </div>`).join('');
  }
  $('#orderSummaryPrice').textContent = formatPrice(preview.orderPrice);
  $('#orderSummaryDiscount').textContent = `-${formatPrice(preview.discountPrice)}`;
  $('#orderSummaryDelivery').textContent = formatPrice(preview.deliveryPrice);
  $('#orderSummaryTotal').textContent = formatPrice(preview.totalAmount);
  state.orderPreviewed = true;
  $('#submitOrderButton').textContent = '결제하기';
}

async function previewPendingOrder(options = {}) {
  if (!requireLogin()) return;
  if (!state.pendingOrder) {
    if (!options.silent) showToast('주문할 상품을 먼저 선택해 주세요.');
    return;
  }
  if (state.pendingOrder.type !== 'CART') {
    if (!options.silent) showToast('바로 구매는 결제 시 금액을 확인할 수 있습니다.');
    return;
  }
  try {
    const params = new URLSearchParams();
    state.pendingOrder.cartItemIds.forEach((id) => params.append('cartItemIds', id));
    const memberCouponId = getSelectedCouponId();
    if (memberCouponId) params.append('memberCouponId', memberCouponId);
    const payload = await api(`/api/orders/preview?${params.toString()}`, { method: 'GET' });
    renderOrderPreview(payload.data);
    if (!options.silent) showToast('주문 금액이 갱신되었습니다.');
  } catch (error) {
    showToast(error.message);
  }
}

async function submitPendingOrder() {
  if (!requireLogin()) return;
  if (!state.pendingOrder) {
    showToast('주문할 상품을 먼저 선택해 주세요.');
    return;
  }

  try {
    const memberAddressId = await resolveOrderAddressId();
    let paid = false;
    if (state.pendingOrder.type === 'DIRECT') {
      paid = await createDirectOrder(state.pendingOrder.productOptionId, state.pendingOrder.quantity, memberAddressId);
    } else {
      paid = await createCartOrder(state.pendingOrder.cartItemIds, memberAddressId);
    }
    if (paid) {
      state.pendingOrder = null;
      state.orderPreviewed = false;
    }
  } catch (error) {
    showToast(error.message);
  }
}

function renderOrderResult(order) {
  const panel = $('#orderResultPanel');
  if (!panel) return;

  showView('orderResult');
  panel.innerHTML = `<h3>결제 영수증</h3>
    <div class="order-result-row"><strong>주문 ID</strong><span>${escapeHtml(order.orderId)}</span></div>
    <div class="order-result-row"><strong>주문 번호</strong><span>${escapeHtml(order.orderNumber)}</span></div>
    <div class="order-result-row"><strong>결제 ID</strong><span>${escapeHtml(order.paymentId)}</span></div>
    <div class="order-result-row"><strong>PortOne 결제 ID</strong><span>${escapeHtml(order.portonePaymentId)}</span></div>
    <div class="order-result-row"><strong>주문 상태</strong><span>${escapeHtml(order.orderStatus)}</span></div>
    <div class="order-result-row"><strong>결제 상태</strong><span>${escapeHtml(order.paymentStatus)}</span></div>
    <div class="order-result-row"><strong>상품 금액</strong><span>${formatPrice(order.orderPrice)}</span></div>
    <div class="order-result-row"><strong>할인 금액</strong><span>-${formatPrice(order.discountPrice)}</span></div>
    <div class="order-result-row"><strong>배송비</strong><span>${formatPrice(order.deliveryPrice)}</span></div>
    <div class="order-result-row"><strong>총 결제 금액</strong><span>${formatPrice(order.totalAmount)}</span></div>
    <div class="order-result-actions">
      <button class="primary-button" type="button" data-payment-confirm data-order-id="${order.orderId}" data-payment-id="${order.paymentId}" data-portone-payment-id="${escapeHtml(order.portonePaymentId)}" data-order-name="${escapeHtml(getOrderName(order))}" data-total-amount="${order.totalAmount}">결제하기</button>
    </div>
    <div id="paymentGuidePanel" class="payment-guide">결제하기를 누르면 PortOne 결제창이 열리고, 결제 성공 후 서버 결제 확정까지 진행됩니다.</div>`;
}

function getOrderName(order) {
  const items = order.items || [];
  if (!items.length) return `주문 ${order.orderNumber}`;

  const firstItem = items[0];
  const firstName = firstItem.productName || firstItem.optionName || '상품';
  return items.length === 1 ? firstName : `${firstName} 외 ${items.length - 1}건`;
}

async function loadPortOneConfig() {
  if (state.portOneConfig) return state.portOneConfig;

  const payload = await api('/api/config/portone', { method: 'GET' });
  state.portOneConfig = payload.data;
  if (!state.portOneConfig?.storeId || !state.portOneConfig?.channelKey) {
    throw new Error('PortOne storeId 또는 channelKey 설정이 비어 있습니다.');
  }
  return state.portOneConfig;
}

async function requestPortOnePayment(orderId, paymentId, portonePaymentId, orderName, totalAmount) {
  if (!window.PortOne) {
    throw new Error('PortOne 결제 SDK를 불러오지 못했습니다.');
  }
  if (!portonePaymentId) {
    throw new Error('PortOne 결제 ID가 없습니다. 주문 생성 응답을 확인해 주세요.');
  }
  if (!Number(totalAmount)) {
    throw new Error('결제 금액이 올바르지 않습니다.');
  }

  const config = await loadPortOneConfig();
  $('#paymentGuidePanel').textContent = 'PortOne 결제창을 여는 중입니다.';
  const response = await PortOne.requestPayment({
    storeId: config.storeId,
    channelKey: config.channelKey,
    paymentId: portonePaymentId,
    orderName,
    totalAmount: Number(totalAmount),
    currency: 'KRW',
    payMethod: 'CARD',
    customer: {
      fullName: `회원 ${state.memberId}`
    }
  });

  if (response.code) {
    throw new Error(response.message || '결제가 실패했습니다.');
  }

  $('#paymentGuidePanel').textContent = '결제 성공을 확인했습니다. 서버에 결제 확정을 요청합니다.';
  return confirmPayment(orderId, paymentId, portonePaymentId);
}

async function startPaymentForOrder(order) {
  renderOrderResult(order);
  return requestPortOnePayment(
    order.orderId,
    order.paymentId,
    order.portonePaymentId,
    getOrderName(order),
    order.totalAmount
  );
}

async function createDirectOrder(optionId, quantity = getOptionQuantity(optionId), memberAddressId = null) {
  if (!requireLogin()) return;
  try {
    const payload = await api('/api/orders/direct', {
      method: 'POST',
      body: JSON.stringify({ productOptionId: Number(optionId), quantity, memberCouponId: getSelectedCouponId(), memberAddressId })
    });
    showToast('주문이 생성되었습니다. 결제를 시작합니다.');
    await startPaymentForOrder(payload.data);
    return true;
  } catch (error) {
    showToast(error.message);
    return false;
  }
}

async function createCartOrder(cartItemIds, memberAddressId = null) {
  if (!requireLogin()) return;
  if (!cartItemIds.length) {
    showToast('구매할 상품을 선택해 주세요.');
    return;
  }

  try {
    const payload = await api('/api/orders/cart', {
      method: 'POST',
      body: JSON.stringify({ cartItemIds, memberCouponId: getSelectedCouponId(), memberAddressId })
    });
    showToast('주문이 생성되었습니다. 결제를 시작합니다.');
    await loadCart();
    await startPaymentForOrder(payload.data);
    return true;
  } catch (error) {
    showToast(error.message);
    return false;
  }
}

async function confirmPayment(orderId, paymentId, portonePaymentId) {
  if (!requireLogin()) return;

  try {
    const payload = await api('/api/payments/confirm', {
      method: 'POST',
      body: JSON.stringify({ orderId: Number(orderId), paymentId: Number(paymentId), portonePaymentId })
    });
    showToast(payload.data?.message || '결제가 확정되었습니다.');
    await loadCart();
    showView('profile');
    await loadOrderHistory();
  } catch (error) {
    showToast(error.message);
  }
}

async function updateCartItemQuantity(cartItemId, quantity) {
  if (!requireLogin()) return;

  try {
    await api(`/api/carts/${cartItemId}`, {
      method: 'PATCH',
      body: JSON.stringify({ quantity: Number(quantity) })
    });
    showToast('수량이 변경되었습니다.');
    loadCart();
  } catch (error) {
    showToast(error.message);
    loadCart();
  }
}

async function deleteCartItem(cartItemId) {
  if (!requireLogin()) return;

  try {
    await api(`/api/carts/${cartItemId}`, { method: 'DELETE' });
    showToast('장바구니 상품을 삭제했습니다.');
    loadCart();
  } catch (error) {
    showToast(error.message);
  }
}

async function clearCart() {
  if (!requireLogin()) return;

  try {
    await api('/api/carts/items', { method: 'DELETE' });
    showToast('장바구니를 비웠습니다.');
    loadCart();
  } catch (error) {
    showToast(error.message);
  }
}
async function loadOrderHistory() {
  if (!requireLogin()) return;

  try {
    const payload = await api('/api/orders', { method: 'GET' });
    const orders = payload.data || [];
    const panel = $('#orderHistoryPanel');
    $('#orderDetailPanel')?.classList.add('hidden');
    if ($('#orderDetailPanel')) $('#orderDetailPanel').innerHTML = '';

    if (!orders.length) {
      panel.innerHTML = '주문내역이 없습니다.';
      return;
    }

    panel.innerHTML = orders.map((order) => `<div class="cart-row order-history-row">
      <strong>${escapeHtml(order.orderNumber)}<br><small>${formatDateTime(order.orderedAt)}</small></strong>
      <span>${escapeHtml(order.orderStatus)}</span>
      <strong>${formatPrice(order.totalAmount)}</strong>
      <button class="outline-button" type="button" data-order-detail-id="${order.orderId}">상세</button>
      <button class="outline-button" type="button" data-order-cancel-id="${order.orderId}">취소</button>
    </div>`).join('');
  } catch (error) {
    showToast(error.message);
  }
}
async function loadOrderDetail(orderId) {
  if (!requireLogin()) return;

  try {
    const payload = await api(`/api/orders/${orderId}`, { method: 'GET' });
    renderOrderDetail(payload.data);
  } catch (error) {
    showToast(error.message);
  }
}

function renderOrderDetail(order) {
  const panel = $('#orderDetailPanel');
  const deliveryAddress = order.deliveryAddress;
  const items = order.items || [];
  const canRefund = order.paymentStatus === 'PAID';
  const addressText = deliveryAddress
    ? `${deliveryAddress.receiverName} / ${deliveryAddress.receiverPhone}<br>${deliveryAddress.zipcode} ${deliveryAddress.address} ${deliveryAddress.detailAddress}`
    : '배송지 정보가 없습니다.';

  panel.classList.remove('hidden');
  panel.innerHTML = `<div class="panel-title-row">
    <h3>주문 상세</h3>
    <button class="outline-button small-button" type="button" data-close-order-detail>닫기</button>
  </div>
    <div class="order-result-row"><strong>주문 번호</strong><span>${escapeHtml(order.orderNumber)}</span></div>
    <div class="order-result-row"><strong>주문 상태</strong><span>${escapeHtml(order.orderStatus)}</span></div>
    <div class="order-result-row"><strong>주문 일시</strong><span>${formatDateTime(order.orderedAt)}</span></div>
    <div class="order-result-row"><strong>배송지</strong><span>${addressText}</span></div>
    <h3>상품</h3>
    ${items.map((item) => `<div class="cart-row order-row">
      <strong>${escapeHtml(item.productName)}<br><small>${escapeHtml(item.optionName)}</small></strong>
      <span>${formatPrice(item.unitPrice)} × ${escapeHtml(item.quantity)} = ${formatPrice(item.totalPrice)}</span>
    </div>`).join('')}
    <h3>결제 금액</h3>
    <div class="order-result-row"><strong>상품 금액</strong><span>${formatPrice(order.orderPrice)}</span></div>
    <div class="order-result-row"><strong>할인 금액</strong><span>-${formatPrice(order.discountPrice)}</span></div>
    <div class="order-result-row"><strong>배송비</strong><span>${formatPrice(order.deliveryPrice)}</span></div>
    <div class="order-result-row"><strong>총 결제 금액</strong><span>${formatPrice(order.totalAmount)}</span></div>
    <h3>결제 내역</h3>
    <div class="order-result-row"><strong>결제 ID</strong><span>${escapeHtml(order.paymentId || '-')}</span></div>
    <div class="order-result-row"><strong>PortOne 결제 ID</strong><span>${escapeHtml(order.portonePaymentId || '-')}</span></div>
    <div class="order-result-row"><strong>결제 상태</strong><span>${escapeHtml(order.paymentStatus || '-')}</span></div>
    <div class="order-result-actions">
      <button class="outline-button" type="button" data-order-cancel-id="${order.orderId}">주문 취소</button>
    </div>
    ${canRefund ? `<div class="section-heading compact"><h2>환불 요청</h2></div>
      <form class="console-form" data-refund-request-form>
        <input name="paymentId" type="hidden" value="${escapeHtml(order.paymentId)}">
        <label><span>환불 금액</span><input name="refundAmount" type="number" min="1" max="${escapeHtml(order.totalAmount)}" value="${escapeHtml(order.totalAmount)}" required></label>
        <label><span>사유</span><textarea name="reason" rows="3" required></textarea></label>
        <button class="outline-button" type="submit">환불 요청</button>
      </form>
      <div class="compact-panel" data-refund-result-panel></div>` : '<p class="muted-text">결제 완료 상태에서만 환불 요청이 가능합니다.</p>'}`;
  panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function cancelOrder(orderId) {
  if (!requireLogin()) return;
  if (!window.confirm('주문을 취소할까요?')) return;
  try {
    await api(`/api/orders/${orderId}/cancel`, { method: 'PATCH' });
    showToast('주문이 취소되었습니다.');
    await loadOrderHistory();
  } catch (error) {
    showToast(error.message);
  }
}

function renderCoupons(coupons, targetSelector = '#couponPanel') {
  const panel = $(targetSelector);
  if (!panel) return;
  if (!coupons.length) {
    panel.innerHTML = '보유 쿠폰이 없습니다.';
    return;
  }
  panel.innerHTML = `<select class="readonly-select" size="${Math.min(coupons.length, 5)}" aria-label="내 쿠폰 목록">
    ${coupons.map((coupon) => {
      const name = coupon.name || coupon.couponName || `쿠폰 #${coupon.memberCouponId || coupon.couponId}`;
      const status = coupon.couponStatus || coupon.status || '';
      return `<option>${escapeHtml(name)} | ${formatPrice(coupon.discountPrice)} 할인 | 최소 ${formatPrice(coupon.minOrderPrice)} | ${escapeHtml(status)} | ${escapeHtml(formatDateTime(coupon.expirationAt))}</option>`;
    }).join('')}
  </select>`;
}

async function loadMyCoupons(targetSelector = '#couponPanel') {
  if (!requireLogin()) return [];
  try {
    const payload = await api('/api/coupons', { method: 'GET' });
    const coupons = payload.data || [];
    renderCoupons(coupons, targetSelector);
    return coupons;
  } catch (error) {
    showToast(error.message);
    return [];
  }
}

async function loadCouponsForOrder() {
  const coupons = await loadMyCoupons('#couponPanel');
  const select = $('#orderCouponSelect');
  if (!select) return;
  select.innerHTML = '<option value="">쿠폰을 사용하지 않음</option>' + coupons.map((coupon) => {
    const memberCouponId = coupon.memberCouponId || coupon.id;
    const name = coupon.name || coupon.couponName || `쿠폰 #${memberCouponId}`;
    return `<option value="${memberCouponId}">${escapeHtml(name)} · ${formatPrice(coupon.discountPrice)}</option>`;
  }).join('');
}

function renderAddresses(addresses, targetSelector = '#profileAddressPanel', selectable = false) {
  const panel = $(targetSelector);
  if (!panel) return;
  if (!addresses.length) {
    if (!selectable) {
      closeProfileAddressForm();
      panel.innerHTML = `<div class="empty-action-panel">
        <span>등록된 배송지가 없습니다.</span>
        <button class="primary-button small" type="button" data-open-address-form>배송지 등록</button>
      </div>`;
      return;
    }
    if (selectable) {
      $('#selectedAddressId').value = '';
      $('#selectedAddressPanel').classList.add('hidden');
      $('#selectedAddressPanel').innerHTML = '';
    }
    panel.innerHTML = '내 정보에서 배송지를 먼저 저장해 주세요.';
    return;
  }
  if (selectable) {
    renderOrderAddressSelection(addresses, panel);
    return;
  }
  if (!selectable) closeProfileAddressForm();
  panel.innerHTML = addresses.map((address) => {
    const addressId = address.addressId || address.id;
    const text = `${address.receiverName} / ${address.receiverPhone} / ${address.zipcode} ${address.address} ${address.detailAddress}`;
    return `<div class="simple-row" data-address-row-id="${addressId}" data-address-text="${escapeHtml(text)}">
      <strong>${escapeHtml(address.defaultAddress ? '기본 배송지' : `배송지 #${addressId}`)}</strong>
      <span>${escapeHtml(text)}</span>
      <div class="row-actions">
        ${selectable ? `<button class="outline-button" type="button" data-select-address-id="${addressId}">선택</button>` : ''}
        <button class="outline-button" type="button" data-default-address-id="${addressId}">기본 설정</button>
        ${selectable ? '' : `<button class="outline-button danger-button" type="button" data-delete-address-id="${addressId}">삭제</button>`}
      </div>
    </div>`;
  }).join('');
}

function renderOrderAddressSelection(addresses, panel) {
  const currentAddressId = getSelectedAddressId();
  const defaultAddress = addresses.find((address) => address.defaultAddress) || addresses[0];
  const selectedAddress = addresses.find((address) => Number(address.addressId || address.id) === currentAddressId) || defaultAddress;
  const selectedAddressId = selectedAddress.addressId || selectedAddress.id;
  const selectedAddressText = getAddressText(selectedAddress);

  $('#selectedAddressId').value = selectedAddressId;
  $('#selectedAddressPanel').classList.add('hidden');
  $('#selectedAddressPanel').innerHTML = `<div class="address-scroll-list">
    ${addresses.map((address) => {
      const addressId = address.addressId || address.id;
      const isSelected = Number(addressId) === Number(selectedAddressId);
      return `<div class="simple-row ${isSelected ? 'selected-row' : ''}" data-address-row-id="${addressId}" data-address-text="${escapeHtml(getAddressText(address))}">
        <strong>${escapeHtml(address.defaultAddress ? '기본 배송지' : `배송지 #${addressId}`)}</strong>
        <span>${escapeHtml(getAddressText(address))}</span>
        <div class="row-actions">
          <button class="outline-button" type="button" data-select-address-id="${addressId}">${isSelected ? '선택됨' : '선택'}</button>
          <button class="outline-button" type="button" data-default-address-id="${addressId}">기본 설정</button>
        </div>
      </div>`;
    }).join('')}
  </div>`;

  panel.innerHTML = `<div class="selected-address-box order-address-box">
    <div class="panel-title-row">
      <strong>${escapeHtml(selectedAddress.defaultAddress ? '기본 배송지' : '선택한 배송지')}</strong>
      <button class="outline-button small-button" type="button" data-toggle-address-list>변경</button>
    </div>
    <span>${escapeHtml(selectedAddressText)}</span>
  </div>`;
}

function getAddressText(address) {
  return `${address.receiverName} / ${address.receiverPhone} / ${address.zipcode} ${address.address} ${address.detailAddress}`;
}

function openProfileAddressForm() {
  const form = $('#profileAddressForm');
  if (!form) return;
  form.classList.remove('hidden');
  form.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function closeProfileAddressForm() {
  const form = $('#profileAddressForm');
  if (!form) return;
  form.reset();
  form.classList.add('hidden');
}

function renderSelectedAddress(addressId) {
  const row = $(`[data-address-row-id="${addressId}"]`);
  const panel = $('#selectedAddressPanel');
  if (!panel) return;
  if (!row) {
    panel.innerHTML = `선택된 배송지 #${escapeHtml(addressId)}`;
    return;
  }
  panel.innerHTML = `<div class="selected-address-box">
    <strong>선택한 배송지</strong>
    <span>${row.dataset.addressText}</span>
  </div>`;
}

async function loadAddresses(targetSelector = '#profileAddressPanel', selectable = false) {
  if (!requireLogin()) return [];
  try {
    const payload = await api('/api/addresses', { method: 'GET' });
    const addresses = payload.data || [];
    renderAddresses(addresses, targetSelector, selectable);
    return addresses;
  } catch (error) {
    showToast(error.message);
    return [];
  }
}

async function changeDefaultAddress(addressId, targetSelector = '#profileAddressPanel', selectable = false) {
  if (!requireLogin()) return;
  try {
    await api(`/api/addresses/${addressId}/default`, { method: 'PATCH' });
    showToast('기본 배송지가 변경되었습니다.');
    await loadAddresses(targetSelector, selectable);
  } catch (error) {
    showToast(error.message);
  }
}

async function deleteAddress(addressId) {
  if (!requireLogin()) return;
  if (!window.confirm('배송지를 삭제할까요?')) return;
  try {
    await api(`/api/addresses/${addressId}`, { method: 'DELETE' });
    showToast('배송지가 삭제되었습니다.');
    await loadAddresses('#profileAddressPanel', false);
  } catch (error) {
    showToast(error.message);
  }
}

async function requestRefund(form) {
  if (!requireLogin()) return;
  const body = formToObject(form);
  const resultPanel = form.closest('#orderDetailPanel')?.querySelector('[data-refund-result-panel]');
  try {
    const payload = await api('/api/refunds', {
      method: 'POST',
      body: JSON.stringify({
        paymentId: Number(body.paymentId),
        refundAmount: Number(body.refundAmount),
        reason: body.reason
      })
    });
    if (resultPanel) resultPanel.innerHTML = renderRefund(payload.data || payload);
    form.reset();
    showToast('환불 요청이 접수되었습니다.');
  } catch (error) {
    showToast(error.message);
  }
}

function renderRefund(refund) {
  return `<div class="simple-row">
    <strong>환불 #${escapeHtml(refund.refundId)}</strong>
    <span>결제 ${escapeHtml(refund.paymentId)} / ${formatPrice(refund.refundAmount)} / ${escapeHtml(refund.status)}</span>
    <small>${escapeHtml(refund.reason || '')}</small>
  </div>`;
}
function showAdminPanel(panelId) {
  $$('.admin-panel').forEach((panel) => panel.classList.toggle('hidden', panel.id !== panelId));
  $$('[data-admin-panel]').forEach((button) => button.classList.toggle('active', button.dataset.adminPanel === panelId));
}

function formToObject(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function requireAdmin() {
  if (state.role === 'ADMIN') return true;
  showToast('관리자만 접근할 수 있습니다.');
  return false;
}

async function updateProduct(form) {
  if (!requireAdmin()) return;
  const body = formToObject(form);
  try {
    await api(`/api/products/${body.productId}`, {
      method: 'PUT',
      body: JSON.stringify({
        categoryId: Number(body.categoryId),
        name: body.name,
        price: Number(body.price),
        description: body.description
      })
    });
    showToast('상품이 수정되었습니다.');
    await loadProducts();
  } catch (error) {
    showToast(error.message);
  }
}

async function deleteProduct(form) {
  if (!requireAdmin()) return;
  const body = formToObject(form);
  if (!window.confirm(`상품 #${body.productId}을 삭제할까요?`)) return;
  try {
    await api(`/api/products/${body.productId}`, { method: 'DELETE' });
    showToast('상품이 삭제되었습니다.');
    form.reset();
    await loadProducts();
  } catch (error) {
    showToast(error.message);
  }
}

async function createCategory(form) {
  if (!requireAdmin()) return;
  try {
    const payload = await api('/api/categories', {
      method: 'POST',
      body: JSON.stringify(formToObject(form))
    });
    showToast(`카테고리가 생성되었습니다. ID ${payload.data}`);
    form.reset();
  } catch (error) {
    showToast(error.message);
  }
}

async function createCoupon(form) {
  if (!requireAdmin()) return;
  const body = formToObject(form);
  try {
    const payload = await api('/api/admin/coupons', {
      method: 'POST',
      body: JSON.stringify({
        name: body.name,
        discountPrice: Number(body.discountPrice),
        minOrderPrice: Number(body.minOrderPrice),
        totalQuantity: Number(body.totalQuantity),
        expirationAt: body.expirationAt
      })
    });
    showToast(`쿠폰이 생성되었습니다. ID ${payload.data?.couponId || ''}`);
    form.reset();
    await loadAdminCoupons();
  } catch (error) {
    showToast(error.message);
  }
}

async function loadAdminCoupons() {
  if (!requireAdmin()) return;
  try {
    const payload = await api('/api/admin/coupons', { method: 'GET' });
    const coupons = payload.data || [];
    const panel = $('#adminCouponPanel');
    if (!coupons.length) {
      panel.innerHTML = '쿠폰 현황이 없습니다.';
      return;
    }
    panel.innerHTML = coupons.map((coupon) => `<div class="simple-row">
      <strong>${escapeHtml(coupon.name || `쿠폰 #${coupon.couponId}`)}</strong>
      <span>총 ${escapeHtml(coupon.totalQuantity)} / 잔여 ${escapeHtml(coupon.remainingQuantity)} / 발급 ${escapeHtml(coupon.issuedQuantity)} / 사용 ${escapeHtml(coupon.usedQuantity)}</span>
      <small>${formatDateTime(coupon.expirationAt)}</small>
    </div>`).join('');
  } catch (error) {
    showToast(error.message);
  }
}

async function approveRefund(form) {
  if (!requireAdmin()) return;
  const body = formToObject(form);
  try {
    const payload = await api(`/api/admin/refunds/${body.refundId}/approve`, { method: 'POST' });
    $('#adminRefundResultPanel').innerHTML = renderRefund(payload.data || payload);
    showToast('환불이 승인되었습니다.');
  } catch (error) {
    showToast(error.message);
  }
}

async function rejectRefund(form) {
  if (!requireAdmin()) return;
  const body = formToObject(form);
  try {
    const payload = await api(`/api/admin/refunds/${body.refundId}/reject`, {
      method: 'POST',
      body: JSON.stringify({ rejectionReason: body.rejectionReason })
    });
    $('#adminRefundResultPanel').innerHTML = renderRefund(payload.data || payload);
    showToast('환불이 거절되었습니다.');
  } catch (error) {
    showToast(error.message);
  }
}


function setSupportTab(name) {
  const panels = { home: '#supportHomePanel', talk: '#supportTalkPanel', settings: '#supportSettingsPanel' };
  Object.values(panels).forEach((selector) => $(selector).classList.add('hidden'));
  $(panels[name]).classList.remove('hidden');
  $$('[data-support-tab]').forEach((button) => button.classList.toggle('active', button.dataset.supportTab === name));
}

function renderSupportState() {
  const adminPanel = $('#supportAdminPanel');
  if (!adminPanel) return;
  adminPanel.classList.toggle('hidden', state.role !== 'ADMIN');
  if (state.role === 'ADMIN' && !$('#supportWidget').classList.contains('hidden')) {
    loadSupportChatList();
  }
}

function supportFrame(command, headers = {}, body = '') {
  const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`).join('\n');
  return `${command}\n${headerLines}\n\n${body}\0`;
}

function supportSendFrame(command, headers = {}, body = '') {
  if (!state.supportSocket || state.supportSocket.readyState !== WebSocket.OPEN) {
    return false;
  }
  state.supportSocket.send(supportFrame(command, headers, body));
  return true;
}

function supportParseBody(frame) {
  const cleanFrame = frame.replace(/\0/g, '');
  const bodyStart = cleanFrame.indexOf('\n\n');
  return bodyStart >= 0 ? cleanFrame.slice(bodyStart + 2) : '';
}

function getSupportMessageId(message) {
  return Number(message.messageId || message.id || 0);
}

function saveSupportLastMessageIds() {
  localStorage.setItem('saverSupportLastMessageIds', JSON.stringify(state.supportLastMessageIds));
}

function rememberSupportMessage(message, fallbackRoomId = '') {
  const roomId = String(message.chatRoomId || fallbackRoomId || '');
  const messageId = getSupportMessageId(message);
  if (!roomId || !messageId) {
    return messageId;
  }

  const savedMessageId = Number(state.supportLastMessageIds[roomId] || 0);
  if (messageId > savedMessageId) {
    state.supportLastMessageIds[roomId] = messageId;
    saveSupportLastMessageIds();
  }

  return messageId;
}

function rememberSupportMessages(chatRoomId, messages) {
  messages.forEach((message) => rememberSupportMessage(message, chatRoomId));
}

function scheduleSupportReconnect(chatRoomId) {
  clearTimeout(state.supportReconnectTimer);
  state.supportReconnectTimer = window.setTimeout(() => {
    connectSupportRoom(chatRoomId);
  }, 5000);
}

async function loadMissingSupportMessages(chatRoomId) {
  const roomId = String(chatRoomId);
  const afterMessageId = Number(state.supportLastMessageIds[roomId] || 0);
  try {
    const payload = await api(`/api/chats/${roomId}/messages?afterMessageId=${afterMessageId}`, { method: 'GET' });
    const messages = payload.data || [];
    messages.forEach((message) => appendSupportMessage({ ...message, chatRoomId: Number(roomId) }));
  } catch (error) {
    showToast(error.message);
  }
}

function connectSupportRoom(chatRoomId) {
  const roomId = String(chatRoomId);
  if (state.supportSocket && state.supportConnectedRoomId === roomId && [WebSocket.OPEN, WebSocket.CONNECTING].includes(state.supportSocket.readyState)) {
    return;
  }
  clearTimeout(state.supportReconnectTimer);
  if (state.supportSocket) {
    state.supportSocket.onclose = null;
    state.supportSocket.close();
  }
  state.supportStompConnected = false;

  const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
  state.supportSocket = new WebSocket(`${protocol}://${location.host}/ws/chat`);
  state.supportConnectedRoomId = roomId;
  state.supportSocket.onopen = () => {
    supportSendFrame('CONNECT', { 'accept-version': '1.2', host: location.host, Authorization: `Bearer ${state.token}` });
  };
  state.supportSocket.onmessage = (event) => {
    const frame = String(event.data);
    if (frame.startsWith('CONNECTED')) {
      state.supportStompConnected = true;
      supportSendFrame('SUBSCRIBE', { id: `support-${roomId}`, destination: `/topic/chat/rooms/${roomId}` });
      loadMissingSupportMessages(roomId);
      return;
    }
    if (frame.startsWith('MESSAGE')) {
      const body = supportParseBody(frame);
      if (!body) return;
      appendSupportMessage(JSON.parse(body));
    }
  };
  state.supportSocket.onerror = () => showToast('채팅 연결을 확인해 주세요.');
  state.supportSocket.onclose = () => {
    state.supportStompConnected = false;
    if (state.token && state.supportConnectedRoomId === roomId) {
      scheduleSupportReconnect(roomId);
    }
  };
}

function renderSupportMessages(chatRoomId, messages) {
  const roomId = String(chatRoomId);
  rememberSupportMessages(roomId, messages);
  if (!messages.length) {
    return `<div class="support-messages" data-support-messages="${roomId}"><div class="support-empty compact">메시지가 없습니다.</div></div>`;
  }
  return `<div class="support-messages" data-support-messages="${roomId}">${messages.map(renderSupportMessage).join('')}</div>`;
}

function renderSupportMessage(message) {
  const type = message.messageType || 'MESSAGE';
  const createdAt = message.createdAt || message.sentAt;
  const messageId = getSupportMessageId(message);
  const messageIdAttribute = messageId ? ` data-support-message-id="${messageId}"` : '';
  return `<div class="support-message"${messageIdAttribute}><span>${escapeHtml(type)} · ${formatDateTime(createdAt)}</span><p>${escapeHtml(message.content)}</p></div>`;
}

function appendSupportMessage(message) {
  const roomId = String(message.chatRoomId || state.supportConnectedRoomId);
  const messageId = rememberSupportMessage(message, roomId);
  const containers = $$(`[data-support-messages="${roomId}"]`);
  containers.forEach((container) => {
    if (messageId && container.querySelector(`[data-support-message-id="${messageId}"]`)) {
      return;
    }
    container.querySelector('.support-empty')?.remove();
    container.insertAdjacentHTML('beforeend', renderSupportMessage(message));
    container.scrollTop = container.scrollHeight;
  });
}

function renderSupportReplyForm(chatRoomId) {
  return `<form class="support-reply-form" data-support-send-room-id="${chatRoomId}"><input name="content" placeholder="메시지를 입력해 주세요" autocomplete="off" required><button type="submit">전송</button></form>`;
}

async function createSupportChat(form) {
  if (!state.token) {
    showToast('로그인이 필요합니다.');
    openAuthModal('login');
    return;
  }
  if (state.role !== 'USER') {
    showToast('사용자만 문의를 생성할 수 있습니다.');
    return;
  }
  const body = formToObject(form);
  try {
    const payload = await api('/api/chats', { method: 'POST', body: JSON.stringify(body) });
    const chatRoom = payload.data;
    state.supportChatRoomId = String(chatRoom.chatRoomId);
    localStorage.setItem('saverSupportChatRoomId', state.supportChatRoomId);
    const firstMessage = { chatRoomId: chatRoom.chatRoomId, content: body.content, messageType: 'USER', createdAt: new Date().toISOString() };
    $('#supportTalkContent').innerHTML = `<div class="support-created"><strong>문의가 접수되었습니다.</strong><span>채팅방 #${escapeHtml(chatRoom.chatRoomId)} · ${escapeHtml(chatRoom.status)}</span></div>${renderSupportMessages(chatRoom.chatRoomId, [firstMessage])}${renderSupportReplyForm(chatRoom.chatRoomId)}`;
    connectSupportRoom(chatRoom.chatRoomId);
    form.reset();
    setSupportTab('talk');
    showToast('문의가 접수되었습니다.');
  } catch (error) {
    showToast(error.message);
  }
}

function supportSendWhenReady(payload, attempt = 0) {
  if (state.supportStompConnected) {
    supportSendFrame('SEND', { destination: '/app/chat/message', 'content-type': 'application/json' }, payload);
    return;
  }
  if (attempt >= 20) {
    showToast('채팅 연결을 확인해 주세요.');
    return;
  }
  window.setTimeout(() => supportSendWhenReady(payload, attempt + 1), 100);
}

async function sendSupportMessage(chatRoomId, content) {
  if (!state.token || !state.memberId) {
    showToast('로그인이 필요합니다.');
    return;
  }
  connectSupportRoom(chatRoomId);
  const payload = JSON.stringify({ chatRoomId: Number(chatRoomId), content });
  supportSendWhenReady(payload);
}

async function loadSupportChatList() {
  if (state.role !== 'ADMIN') return;
  try {
    const payload = await api('/api/chats', { method: 'GET' });
    const chatRooms = payload.data?.chatRooms || [];
    const list = $('#supportChatList');
    if (!chatRooms.length) {
      list.innerHTML = '<div class="support-empty compact">문의가 없습니다.</div>';
      $('#supportChatDetail').innerHTML = '';
      return;
    }
    list.innerHTML = chatRooms.map((room) => `<button type="button" data-support-room-id="${room.chatRoomId}"><strong>${escapeHtml(room.title)}</strong><span>#${room.chatRoomId} · ${escapeHtml(room.status)} · ${formatDateTime(room.createdAt)}</span></button>`).join('');
  } catch (error) {
    showToast(error.message);
  }
}

async function loadSupportChatDetail(chatRoomId) {
  try {
    await api(`/api/chats/${chatRoomId}/join`, { method: 'POST', body: JSON.stringify({}) });
    const payload = await api(`/api/chats/${chatRoomId}`, { method: 'GET' });
    const room = payload.data;
    const messages = room.messages || [];
    $('#supportChatDetail').innerHTML = `<div class="support-detail-head">
      <strong>${escapeHtml(room.title)}</strong>
      <span>${escapeHtml(room.status)}</span>
      <button class="outline-button small-button" type="button" data-support-close-id="${room.chatRoomId}">닫기</button>
    </div>${renderSupportMessages(room.chatRoomId, messages)}${renderSupportReplyForm(room.chatRoomId)}`;
    connectSupportRoom(room.chatRoomId);
    loadSupportChatList();
  } catch (error) {
    showToast(error.message);
  }
}

async function closeSupportChat(chatRoomId) {
  if (!requireAdmin()) return;
  try {
    await api(`/api/chats/${chatRoomId}/close`, { method: 'PATCH' });
    showToast('채팅방을 닫았습니다.');
    await loadSupportChatDetail(chatRoomId);
    await loadSupportChatList();
  } catch (error) {
    showToast(error.message);
  }
}
function bindEvents() {
  $('#supportToggleButton').addEventListener('click', () => {
    $('#supportWidget').classList.toggle('hidden');
    renderSupportState();
  });
  $$('[data-support-tab]').forEach((button) => button.addEventListener('click', () => setSupportTab(button.dataset.supportTab)));
  $('#supportCreateForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    await createSupportChat(event.currentTarget);
  });
  $('#supportRefreshButton').addEventListener('click', loadSupportChatList);
  $('#supportWidget').addEventListener('submit', async (event) => {
    const form = event.target.closest('.support-reply-form');
    if (!form) {
      return;
    }
    event.preventDefault();
    const content = form.elements.content.value.trim();
    if (!content) {
      return;
    }
    await sendSupportMessage(form.dataset.supportSendRoomId, content);
    form.reset();
  });
  $('#supportChatList').addEventListener('click', (event) => {
    const button = event.target.closest('[data-support-room-id]');
    if (button) loadSupportChatDetail(button.dataset.supportRoomId);
  });
  $('#supportChatDetail').addEventListener('click', (event) => {
    const button = event.target.closest('[data-support-close-id]');
    if (button) closeSupportChat(button.dataset.supportCloseId);
  });
  $$('[data-view]').forEach((button) => button.addEventListener('click', () => showView(button.dataset.view)));
  $$('.pill').forEach((button) => button.addEventListener('click', () => {
    $$('.pill').forEach((item) => item.classList.remove('active'));
    button.classList.add('active');
    loadProducts(button.dataset.category);
  }));
  $('#loginOpenButton').addEventListener('click', () => openAuthModal('login'));
  $('#signupOpenButton').addEventListener('click', () => openAuthModal('signup'));
  $('#authCloseButton').addEventListener('click', closeAuthModal);
  bindPasswordToggle();
  window.addEventListener('pagehide', clearSignupForm);
  $('#showSignupButton').addEventListener('click', () => {
    openAuthModal('signup');
  });
  $('#showLoginButton').addEventListener('click', () => openAuthModal('login'));
  $('#logoutButton').addEventListener('click', async () => {
    try { await api('/api/members/logout', { method: 'POST' }); } catch (error) { }
    clearSession();
    showToast('로그인이 필요합니다.');
    showView('home');
  });
  $('#loginForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
      const payload = await api('/api/members/login', { method: 'POST', body: JSON.stringify(formToObject(event.currentTarget)) });
      setSession(payload.data);
      await syncSessionMemberName();
      closeAuthModal();
      showToast('로그인 성공');
    } catch (error) { showToast(error.message); }
  });
  $('#signupForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
      await api('/api/members/signup', { method: 'POST', body: JSON.stringify(formToObject(event.currentTarget)) });
      showToast('회원가입 성공');
      clearSignupForm();
      event.currentTarget.classList.add('hidden');
    } catch (error) { showToast(error.message); }
  });
  $("#productSearchForm").addEventListener("submit", (event) => {
    event.preventDefault();
    searchProducts(formToObject(event.currentTarget).keyword);
  });
  $('#popularSearchPanel').addEventListener('click', (event) => {
    const button = event.target.closest('[data-popular-keyword]');
    if (!button) return;
    $('#productSearchForm').elements.keyword.value = button.dataset.popularKeyword;
    searchProducts(button.dataset.popularKeyword);
  });
  $('#refreshPopularSearchButton').addEventListener('click', loadPopularSearches);
  $("#clearProductSearchButton").addEventListener("click", () => {
    $("#productSearchForm").reset();
    loadProducts('all');
  });
  $('#productGrid').addEventListener('click', (event) => {
    const button = event.target.closest('[data-detail-id]');
    if (button) loadProductDetail(button.dataset.detailId);
  });
  $('#productDetail').addEventListener('click', (event) => {
    const cartButton = event.target.closest('[data-cart-option-id]');
    if (cartButton) addCartItem(cartButton.dataset.cartOptionId);

    const directButton = event.target.closest('[data-direct-option-id]');
    if (directButton) prepareDirectOrder(directButton.dataset.directOptionId, {
      productName: directButton.dataset.productName,
      optionName: directButton.dataset.optionName,
      unitPrice: directButton.dataset.unitPrice
    });
  });
  $('#cartPanel').addEventListener('click', (event) => {
    const deleteButton = event.target.closest('[data-cart-delete]');
    if (deleteButton) deleteCartItem(deleteButton.dataset.cartDelete);

    const clearButton = event.target.closest('[data-clear-cart]');
    if (clearButton) clearCart();

    const orderButton = event.target.closest('[data-cart-order-selected]');
    if (orderButton) prepareCartOrder(getSelectedCartItemIds());
  });
  $('#cartPanel').addEventListener('change', (event) => {
    const quantityInput = event.target.closest('[data-cart-quantity]');
    if (quantityInput) updateCartItemQuantity(quantityInput.dataset.cartQuantity, quantityInput.value);
  });
  $('#refreshCouponButton').addEventListener('click', loadCouponsForOrder);
  $('#orderCouponSelect').addEventListener('change', async () => {
    state.orderPreviewed = false;
    $('#submitOrderButton').textContent = '결제하기';
    if (state.pendingOrder?.type === 'CART') {
      await previewPendingOrder({ silent: true });
    }
  });
  $('#addressPanel').addEventListener('click', (event) => {
    const toggleButton = event.target.closest('[data-toggle-address-list]');
    if (toggleButton) {
      $('#selectedAddressPanel').classList.toggle('hidden');
      if (!$('#selectedAddressPanel').classList.contains('hidden')) {
        $('#selectedAddressPanel').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    }
  });
  $('#selectedAddressPanel').addEventListener('click', async (event) => {
    const selectButton = event.target.closest('[data-select-address-id]');
    if (selectButton) {
      $('#selectedAddressId').value = selectButton.dataset.selectAddressId;
      $('#selectedAddressPanel').classList.add('hidden');
      await loadAddresses('#addressPanel', true);
      showToast(`배송지 #${selectButton.dataset.selectAddressId} 선택`);
    }
    const defaultButton = event.target.closest('[data-default-address-id]');
    if (defaultButton) changeDefaultAddress(defaultButton.dataset.defaultAddressId, '#addressPanel', true);
  });
  $("#submitOrderButton").addEventListener("click", submitPendingOrder);
  $('#orderResultPanel').addEventListener('click', (event) => {
    const button = event.target.closest('[data-payment-confirm]');
    if (button) requestPortOnePayment(
      button.dataset.orderId,
      button.dataset.paymentId,
      button.dataset.portonePaymentId,
      button.dataset.orderName,
      button.dataset.totalAmount
    ).catch((error) => showToast(error.message));
  });
  $('#goToOrderHistoryButton').addEventListener('click', async () => {
    showView('profile');
    await loadOrderHistory();
  });
  $('#loadOrderHistoryButton').addEventListener('click', loadOrderHistory);
  $('#orderHistoryPanel').addEventListener('click', (event) => {
    const button = event.target.closest('[data-order-detail-id]');
    if (button) loadOrderDetail(button.dataset.orderDetailId);
    const cancelButton = event.target.closest('[data-order-cancel-id]');
    if (cancelButton) cancelOrder(cancelButton.dataset.orderCancelId);
  });
  $('#orderDetailPanel').addEventListener('click', (event) => {
    const closeButton = event.target.closest('[data-close-order-detail]');
    if (closeButton) {
      $('#orderDetailPanel').classList.add('hidden');
      $('#orderDetailPanel').innerHTML = '';
    }
    const cancelButton = event.target.closest('[data-order-cancel-id]');
    if (cancelButton) cancelOrder(cancelButton.dataset.orderCancelId);
  });
  $('#orderDetailPanel').addEventListener('submit', async (event) => {
    const form = event.target.closest('[data-refund-request-form]');
    if (!form) return;
    event.preventDefault();
    await requestRefund(form);
  });
  $('#profileAddressForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    await createProfileAddress(event.currentTarget);
  });
  $('#cancelAddressFormButton').addEventListener('click', closeProfileAddressForm);
  $('#profileAddressPanel').addEventListener('click', (event) => {
    const openButton = event.target.closest('[data-open-address-form]');
    if (openButton) openProfileAddressForm();
    const defaultButton = event.target.closest('[data-default-address-id]');
    if (defaultButton) changeDefaultAddress(defaultButton.dataset.defaultAddressId, '#profileAddressPanel', false);
    const deleteButton = event.target.closest('[data-delete-address-id]');
    if (deleteButton) deleteAddress(deleteButton.dataset.deleteAddressId);
  });
  $('#refreshCartButton').addEventListener('click', loadCart);
  $('#loadProfileButton').addEventListener('click', loadProfile);
  $('#profileForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
      await api('/api/members/info', { method: 'PUT', body: JSON.stringify(formToObject(event.currentTarget)) });
      showToast('회원정보가 수정되었습니다.');
      loadProfile();
    } catch (error) { showToast(error.message); }
  });
  $$('[data-admin-panel]').forEach((button) => button.addEventListener('click', () => showAdminPanel(button.dataset.adminPanel)));
  $('#productCreateForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formToObject(event.currentTarget);
    const request = {
      categoryId: Number(body.categoryId),
      name: body.name,
      price: Number(body.price),
      description: body.description,
      options: [{
        optionName: body.optionName,
        optionStock: Number(body.optionStock),
        additionalPrice: Number(body.additionalPrice)
      }]
    };
    try {
      const payload = await api('/api/products', { method: 'POST', body: JSON.stringify(request) });
      showToast(`상품이 등록되었습니다. ID ${payload.data.productId}`);
      event.currentTarget.reset();
      loadProducts();
    } catch (error) { showToast(error.message); }
  });
  $('#stockForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formToObject(event.currentTarget);
    try {
      await api(`/api/products/${body.productId}/stock`, { method: 'PUT', body: JSON.stringify({ stockQuantity: Number(body.stockQuantity) }) });
      showToast('재고가 수정되었습니다.');
    } catch (error) { showToast(error.message); }
  });
  $('#optionForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formToObject(event.currentTarget);
    const option = { optionId: Number(body.optionId), optionStock: Number(body.optionStock), additionalPrice: Number(body.additionalPrice), status: body.status };
    try {
      await api(`/api/products/${body.productId}/options`, { method: 'PUT', body: JSON.stringify([option]) });
      showToast('옵션이 수정되었습니다.');
    } catch (error) { showToast(error.message); }
  });
  $('#productUpdateForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    await updateProduct(event.currentTarget);
  });
  $('#productDeleteForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    await deleteProduct(event.currentTarget);
  });
  $('#categoryCreateForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    await createCategory(event.currentTarget);
  });
  $('#couponCreateForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    await createCoupon(event.currentTarget);
  });
  $('#loadAdminCouponsButton').addEventListener('click', loadAdminCoupons);
  $('#refundApproveForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    await approveRefund(event.currentTarget);
  });
  $('#refundRejectForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    await rejectRefund(event.currentTarget);
  });
}

renderSession();
bindEvents();
loadProducts();
loadPopularSearches();
