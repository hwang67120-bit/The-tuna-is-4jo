const state = {
  token: localStorage.getItem('saverToken') || '',
  role: localStorage.getItem('saverRole') || '',
  memberId: localStorage.getItem('saverMemberId') || '',
  products: []
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

function formatPrice(value) {
  return Number(value || 0).toLocaleString('ko-KR') + '원';
}

function showToast(message) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.remove('hidden');
  setTimeout(() => toast.classList.add('hidden'), 2800);
}

function setSession(data) {
  state.token = data.accessToken;
  state.role = data.role;
  state.memberId = String(data.memberId);
  localStorage.setItem('saverToken', state.token);
  localStorage.setItem('saverRole', state.role);
  localStorage.setItem('saverMemberId', state.memberId);
  renderSession();
}

function clearSession() {
  state.token = '';
  state.role = '';
  state.memberId = '';
  localStorage.removeItem('saverToken');
  localStorage.removeItem('saverRole');
  localStorage.removeItem('saverMemberId');
  renderSession();
}

function renderSession() {
  const loggedIn = Boolean(state.token);
  $('#sessionLabel').textContent = loggedIn ? `회원 ${state.memberId} · ${state.role}` : '로그인이 필요합니다';
  $('#loginOpenButton').classList.toggle('hidden', loggedIn);
  $('#logoutButton').classList.toggle('hidden', !loggedIn);
  $$('[data-admin-only]').forEach((el) => el.classList.toggle('hidden', state.role !== 'ADMIN'));
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
  $$('.view').forEach((view) => view.classList.add('hidden'));
  $(`#${name}View`).classList.remove('hidden');
  if (name === 'home') loadProducts();
  if (name === 'cart') loadCart();
  if (name === 'profile') loadProfile();
}

async function loadProducts(categoryId = 'all') {
  try {
    const url = categoryId === 'all' ? '/api/products?page=0&size=20' : `/api/products/categories/${categoryId}`;
    const payload = await api(url, { method: 'GET' });
    const data = payload.data;
    const products = Array.isArray(data?.content) ? data.content : (data?.products || []);
    state.products = products;
    renderProducts(products);
  } catch (error) {
    showToast(error.message);
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
      <span class="badge">${product.status}</span>
      <h1>${product.name}</h1>
      <p>${product.categoryName || ''}</p>
      <p>${product.description || ''}</p>
      <div class="price">${formatPrice(product.price)}</div>
      <div class="option-list">
        ${options.map((option) => `<div class="option-row">
          <div><strong>${option.optionName}</strong><br><small>재고 ${option.optionStock} · ${option.status}</small></div>
          <input type="number" min="1" value="1" data-quantity-for="${option.optionId}">
          <button class="primary-button small" type="button" data-cart-option-id="${option.optionId}" ${option.status !== 'ON_SALE' ? 'disabled' : ''}>담기</button>
        </div>`).join('')}
      </div>
    </div>`;
}

async function loadCart() {
  if (!state.token) {
    $('#cartPanel').innerHTML = '로그인이 필요합니다.';
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
    $('#cartPanel').innerHTML = items.map((item) => `<div class="cart-row">
      <strong>${item.productName}<br><small>${item.optionName}</small></strong>
      <span>${formatPrice(item.unitPrice)}</span>
      <input type="number" min="1" value="${item.quantity}" data-cart-quantity="${item.cartItemId}">
      <strong>${formatPrice(item.totalPrice)}</strong>
    </div>`).join('') + `<div class="cart-total">총 ${formatPrice(data.totalPrice)}</div>`;
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
  } catch (error) {
    showToast(error.message);
  }
}

async function addCartItem(optionId) {
  if (!state.token) {
    showToast('로그인이 필요합니다.');
    $('#authModal').classList.remove('hidden');
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

function formToObject(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function bindEvents() {
  $$('[data-view]').forEach((button) => button.addEventListener('click', () => showView(button.dataset.view)));
  $$('.pill').forEach((button) => button.addEventListener('click', () => {
    $$('.pill').forEach((item) => item.classList.remove('active'));
    button.classList.add('active');
    loadProducts(button.dataset.category);
  }));
  $('#loginOpenButton').addEventListener('click', () => $('#authModal').classList.remove('hidden'));
  $('#authCloseButton').addEventListener('click', () => $('#authModal').classList.add('hidden'));
  $('#showSignupButton').addEventListener('click', () => $('#signupForm').classList.toggle('hidden'));
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
      $('#authModal').classList.add('hidden');
      showToast('로그인 성공');
    } catch (error) { showToast(error.message); }
  });
  $('#signupForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
      await api('/api/members/signup', { method: 'POST', body: JSON.stringify(formToObject(event.currentTarget)) });
      showToast('회원가입 성공');
      event.currentTarget.reset();
      event.currentTarget.classList.add('hidden');
    } catch (error) { showToast(error.message); }
  });
  $('#productGrid').addEventListener('click', (event) => {
    const button = event.target.closest('[data-detail-id]');
    if (button) loadProductDetail(button.dataset.detailId);
  });
  $('#productDetail').addEventListener('click', (event) => {
    const button = event.target.closest('[data-cart-option-id]');
    if (button) addCartItem(button.dataset.cartOptionId);
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
  $('#productCreateForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formToObject(event.currentTarget);
    body.categoryId = Number(body.categoryId);
    body.price = Number(body.price);
    try {
      const payload = await api('/api/products', { method: 'POST', body: JSON.stringify(body) });
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
}

renderSession();
bindEvents();
loadProducts();
