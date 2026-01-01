const API_BASE = '/api/v1';
const ACCESS_TOKEN_KEY = 'accessToken';

const el = (id) => document.getElementById(id);

const baseUrlEl = el('baseUrl');
const tokenValueEl = el('tokenValue');
const logOutputEl = el('logOutput');
const meOutputEl = el('meOutput');
const gymmatesOutputEl = el('gymmatesOutput');
const sentOutputEl = el('sentOutput');
const receivedOutputEl = el('receivedOutput');

const emailInput = el('emailInput');
const passwordInput = el('passwordInput');
const nicknameInput = el('nicknameInput');
const targetUserIdInput = el('targetUserIdInput');
const messageInput = el('messageInput');

const loginBtn = el('loginBtn');
const signupBtn = el('signupBtn');
const meBtn = el('meBtn');
const gymmatesBtn = el('gymmatesBtn');
const sentBtn = el('sentBtn');
const receivedBtn = el('receivedBtn');
const createMatchingBtn = el('createMatchingBtn');
const logoutBtn = el('logoutBtn');

function getToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

function setToken(token) {
  if (token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  }
  renderToken();
}

function renderToken() {
  const token = getToken();
  tokenValueEl.textContent = token ? `${token.slice(0, 8)}...` : '(none)';
}

function logMessage(message) {
  const timestamp = new Date().toISOString();
  const entry = `[${timestamp}] ${message}`;
  logOutputEl.textContent = `${entry}\n${logOutputEl.textContent}`.trim();
}

async function requestJson(path, options = {}) {
  const token = getToken();
  const headers = Object.assign(
    { 'Content-Type': 'application/json' },
    options.headers || {},
    token ? { Authorization: `Bearer ${token}` } : {}
  );

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  let body = null;
  try {
    body = await res.json();
  } catch {
    body = null;
  }

  if (!res.ok) {
    const error = body?.error;
    const code = error?.code || 'UNKNOWN';
    const message = error?.message || res.statusText;
    logMessage(`ERROR ${res.status} ${code} ${message}`);
    throw new Error(message);
  }

  if (body?.success === false) {
    const error = body?.error;
    const code = error?.code || 'UNKNOWN';
    const message = error?.message || 'Unknown error';
    logMessage(`ERROR ${code} ${message}`);
    throw new Error(message);
  }

  return body;
}

function renderJson(target, data) {
  target.textContent = data ? JSON.stringify(data, null, 2) : '(empty)';
}

function renderGymmates(items) {
  if (!items || items.length === 0) {
    gymmatesOutputEl.textContent = 'No gymmates found.';
    return;
  }

  gymmatesOutputEl.innerHTML = '';
  items.forEach((user) => {
    const card = document.createElement('div');
    card.className = 'card';
    const info = document.createElement('div');
    info.innerHTML = `
      <strong>${user.nickname || 'unknown'}</strong>
      <div>User ID: ${user.userId}</div>
      <div>City: ${user.city || '-'} / ${user.district || '-'}</div>
      <div>Fitness: ${user.fitnessLevel || '-'}</div>
    `;

    const actions = document.createElement('div');
    actions.className = 'actions';
    const btn = document.createElement('button');
    btn.className = 'btn';
    btn.textContent = 'Request Matching';
    btn.addEventListener('click', async () => {
      targetUserIdInput.value = user.userId;
      await createMatching();
    });
    actions.appendChild(btn);

    card.appendChild(info);
    card.appendChild(actions);
    gymmatesOutputEl.appendChild(card);
  });
}

function renderMatchings(target, items, isReceived) {
  if (!items || items.length === 0) {
    target.textContent = 'No matchings.';
    return;
  }
  target.innerHTML = '';
  items.forEach((match) => {
    const card = document.createElement('div');
    card.className = 'card';
    const row = document.createElement('div');
    row.className = 'card-row';
    const role =
      currentUserId === match.requesterUserId
        ? 'REQUESTER'
        : currentUserId === match.targetUserId
          ? 'TARGET'
          : 'UNKNOWN';
    row.innerHTML = `
      <div>
        <strong>Matching #${match.matchingId}</strong>
        <div>Status: ${match.status}</div>
        <div>Requester: ${match.requesterUserId}</div>
        <div>Target: ${match.targetUserId}</div>
        <div>Role: <span class="badge">${role}</span></div>
      </div>
    `;

    if (match.status === 'REQUESTED' && role === 'TARGET') {
      const actions = document.createElement('div');
      actions.className = 'actions';

      const acceptBtn = document.createElement('button');
      acceptBtn.className = 'btn';
      acceptBtn.textContent = 'Accept';
      acceptBtn.addEventListener('click', async () => {
        await updateStatus(match.matchingId, 'ACCEPTED');
        await loadReceived();
      });

      const rejectBtn = document.createElement('button');
      rejectBtn.className = 'btn btn-secondary';
      rejectBtn.textContent = 'Reject';
      rejectBtn.addEventListener('click', async () => {
        await updateStatus(match.matchingId, 'REJECTED');
        await loadReceived();
        await loadSent();
      });

      actions.appendChild(acceptBtn);
      actions.appendChild(rejectBtn);
      row.appendChild(actions);
    }

    if (match.status === 'REQUESTED' && role === 'REQUESTER') {
      const actions = document.createElement('div');
      actions.className = 'actions';
      const cancelBtn = document.createElement('button');
      cancelBtn.className = 'btn btn-secondary';
      cancelBtn.textContent = 'Cancel';
      cancelBtn.addEventListener('click', async () => {
        await updateStatus(match.matchingId, 'CANCELLED');
        await loadSent();
        await loadReceived();
      });
      actions.appendChild(cancelBtn);
      row.appendChild(actions);
    }

    if (match.status === 'ACCEPTED' && role !== 'UNKNOWN') {
      const actions = document.createElement('div');
      actions.className = 'actions';
      const endBtn = document.createElement('button');
      endBtn.className = 'btn';
      endBtn.textContent = 'End';
      endBtn.addEventListener('click', async () => {
        await updateStatus(match.matchingId, 'ENDED');
        await loadSent();
        await loadReceived();
      });
      actions.appendChild(endBtn);
      row.appendChild(actions);
    }

    card.appendChild(row);
    target.appendChild(card);
  });
}

async function login() {
  const email = emailInput.value.trim();
  const password = passwordInput.value.trim();
  if (!email || !password) {
    alert('Email and password are required.');
    return;
  }
  const body = await requestJson('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
  const token = body?.data?.accessToken || body?.accessToken;
  if (!token) {
    alert('No accessToken returned.');
    return;
  }
  setToken(token);
  await loadMe();
  logMessage('Login success');
}

async function signup() {
  const email = emailInput.value.trim();
  const password = passwordInput.value.trim();
  const nickname = nicknameInput.value.trim();
  if (!email || !password || !nickname) {
    alert('Email, password, nickname are required.');
    return;
  }
  const body = await requestJson('/auth/signup', {
    method: 'POST',
    body: JSON.stringify({ email, password, nickname }),
  });
  const token = body?.data?.accessToken || body?.accessToken;
  if (token) {
    setToken(token);
    await loadMe();
  }
  logMessage('Signup success');
}

async function loadMe() {
  const body = await requestJson('/me', { method: 'GET' });
  currentUserId = body?.data?.userId ?? body?.userId ?? null;
  renderJson(meOutputEl, body?.data || body);
  logMessage('Loaded /me');
}

async function loadGymmates() {
  const body = await requestJson('/gymmates?page=0&size=20', { method: 'GET' });
  const items = body?.data?.items || body?.items || [];
  renderGymmates(items);
  logMessage('Loaded /gymmates');
}

async function createMatching() {
  const targetUserId = Number(targetUserIdInput.value);
  if (!targetUserId) {
    alert('Target user ID is required.');
    return;
  }
  const message = messageInput.value.trim();
  const body = await requestJson('/matchings', {
    method: 'POST',
    body: JSON.stringify({ targetUserId, message }),
  });
  logMessage(`Created matching: ${JSON.stringify(body?.data || body)}`);
  await loadSent();
}

async function loadSent() {
  const body = await requestJson('/matchings/sent?page=0&size=20', { method: 'GET' });
  const items = body?.data?.items || body?.items || [];
  renderMatchings(sentOutputEl, items, false);
  logMessage('Loaded /matchings/sent');
}

async function loadReceived() {
  const body = await requestJson('/matchings/received?page=0&size=20', { method: 'GET' });
  const items = body?.data?.items || body?.items || [];
  renderMatchings(receivedOutputEl, items, true);
  logMessage('Loaded /matchings/received');
}

async function updateStatus(matchingId, status) {
  await requestJson(`/matchings/${matchingId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  });
  logMessage(`Updated matching ${matchingId} -> ${status}`);
}

function logout() {
  setToken('');
  currentUserId = null;
  renderJson(meOutputEl, null);
  gymmatesOutputEl.textContent = '';
  sentOutputEl.textContent = '';
  receivedOutputEl.textContent = '';
  logMessage('Logged out');
}

function init() {
  baseUrlEl.textContent = window.location.origin;
  renderToken();
  loginBtn.addEventListener('click', () => login().catch((err) => logMessage(err.message)));
  signupBtn.addEventListener('click', () => signup().catch((err) => logMessage(err.message)));
  meBtn.addEventListener('click', () => loadMe().catch((err) => logMessage(err.message)));
  gymmatesBtn.addEventListener('click', () => loadGymmates().catch((err) => logMessage(err.message)));
  sentBtn.addEventListener('click', () => loadSent().catch((err) => logMessage(err.message)));
  receivedBtn.addEventListener('click', () => loadReceived().catch((err) => logMessage(err.message)));
  createMatchingBtn.addEventListener('click', () => createMatching().catch((err) => logMessage(err.message)));
  logoutBtn.addEventListener('click', logout);
  if (getToken()) {
    loadMe().catch(() => {});
  }
}

init();
let currentUserId = null;
