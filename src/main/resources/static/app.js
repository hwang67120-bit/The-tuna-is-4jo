const state = {
  token: localStorage.getItem('saverToken') || '',
  role: localStorage.getItem('saverRole') || '',
  memberId: localStorage.getItem('saverMemberId') || '',
  products: [],
  supportChatRoomId: localStorage.getItem('saverSupportChatRoomId') || '',
  supportSocket: null,
  supportConnectedRoomId: '',
  supportStompConnected: false
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

function connectSupportRoom(chatRoomId) {
  const roomId = String(chatRoomId);
  if (state.supportSocket && state.supportSocket.readyState === WebSocket.OPEN && state.supportConnectedRoomId === roomId) {
    return;
  }
  if (state.supportSocket) {
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
  };
}

function renderSupportMessages(chatRoomId, messages) {
  const roomId = String(chatRoomId);
  if (!messages.length) {
    return `<div class="support-messages" data-support-messages="${roomId}"><div class="support-empty compact">메시지가 없습니다.</div></div>`;
  }
  return `<div class="support-messages" data-support-messages="${roomId}">${messages.map(renderSupportMessage).join('')}</div>`;
}

function renderSupportMessage(message) {
  const type = message.messageType || (String(message.senderId) === state.memberId ? state.role : 'MESSAGE');
  const createdAt = message.createdAt || message.sentAt;
  return `<div class="support-message"><span>#${escapeHtml(message.senderId)} · ${escapeHtml(type)} · ${formatDateTime(createdAt)}</span><p>${escapeHtml(message.content)}</p></div>`;
}

function appendSupportMessage(message) {
  const roomId = String(message.chatRoomId);
  const containers = $$(`[data-support-messages="${roomId}"]`);
  containers.forEach((container) => {
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
    $('#authModal').classList.remove('hidden');
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
    const firstMessage = { chatRoomId: chatRoom.chatRoomId, senderId: state.memberId, content: body.content, messageType: 'USER', createdAt: new Date().toISOString() };
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
    $('#supportChatDetail').innerHTML = `<div class="support-detail-head"><strong>${escapeHtml(room.title)}</strong><span>${escapeHtml(room.status)}</span></div>${renderSupportMessages(room.chatRoomId, messages)}${renderSupportReplyForm(room.chatRoomId)}`;
    connectSupportRoom(room.chatRoomId);
    loadSupportChatList();
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
